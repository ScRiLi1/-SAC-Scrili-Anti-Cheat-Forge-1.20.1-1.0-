package com.scrili.sac.forge.antiesp;

import com.scrili.sac.forge.adapter.ForgeConfig;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Method;
import java.util.*;

public class AntiESP {

    private static final int CHECK_INTERVAL = 20;
    private static final double MAX_DIST = 128.0;
    private static final double MIN_DIST = 5.0;

    private int tick = 0;
    private final Map<UUID, Set<UUID>> hiddenPlayers = new HashMap<>();

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!ForgeConfig.isAntiEspEnabled()) return;
        if (tick++ % CHECK_INTERVAL != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            List<ServerPlayer> players = level.players();
            if (players.size() < 2) continue;

            for (ServerPlayer viewer : players) {
                if (viewer.isSpectator()) continue;
                Set<UUID> hidden = hiddenPlayers.computeIfAbsent(viewer.getUUID(), k -> new HashSet<>());

                for (ServerPlayer target : players) {
                    if (viewer == target) continue;
                    double distSq = viewer.distanceToSqr(target);
                    boolean isHidden = hidden.contains(target.getUUID());

                    if (distSq > MAX_DIST * MAX_DIST) {
                        if (isHidden) { hidden.remove(target.getUUID()); forceUpdateTracking(level, viewer, target); }
                        continue;
                    }
                    if (distSq < MIN_DIST * MIN_DIST) {
                        if (isHidden) { hidden.remove(target.getUUID()); forceUpdateTracking(level, viewer, target); }
                        continue;
                    }

                    boolean canSee = hasLineOfSight(level, viewer, target);

                    if (!canSee && !isHidden) {
                        viewer.connection.send(new ClientboundRemoveEntitiesPacket(target.getId()));
                        hidden.add(target.getUUID());
                    } else if (canSee && isHidden) {
                        hidden.remove(target.getUUID());
                        forceUpdateTracking(level, viewer, target);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        hiddenPlayers.remove(uuid);
        hiddenPlayers.values().forEach(set -> set.remove(uuid));
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        hiddenPlayers.remove(event.getEntity().getUUID());
    }

    private boolean forceUpdateTracking(ServerLevel level, ServerPlayer viewer, ServerPlayer target) {
        try {
            var chunkMap = level.getChunkSource().chunkMap;
            Object trackedEntity = getTrackedEntity(chunkMap, target);
            if (trackedEntity == null) return false;

            Method removePlayer = null, updatePlayer = null;
            for (Method m : trackedEntity.getClass().getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == ServerPlayer.class) {
                    String name = m.getName();
                    if (removePlayer == null || name.compareTo(removePlayer.getName()) < 0) {
                        updatePlayer = removePlayer;
                        removePlayer = m;
                    } else if (updatePlayer == null || name.compareTo(updatePlayer.getName()) < 0) {
                        updatePlayer = m;
                    }
                }
            }
            if (removePlayer == null || updatePlayer == null) return false;

            removePlayer.setAccessible(true);
            removePlayer.invoke(trackedEntity, viewer);
            updatePlayer.setAccessible(true);
            updatePlayer.invoke(trackedEntity, viewer);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Object getTrackedEntity(net.minecraft.server.level.ChunkMap chunkMap, ServerPlayer target) throws Exception {
        for (java.lang.reflect.Field f : chunkMap.getClass().getDeclaredFields()) {
            if (!Map.class.isAssignableFrom(f.getType())) continue;
            f.setAccessible(true);
            Object val = f.get(chunkMap);
            if (!(val instanceof Map<?,?> map)) continue;
            Object tracked = null;
            try { tracked = map.get(target.getId()); }
            catch (ClassCastException e) {
                try { tracked = map.get((long) target.getId()); } catch (Exception ignored) {}
            }
            if (tracked != null && tracked.getClass().getSimpleName().contains("Tracked")) return tracked;
        }
        return null;
    }

    private boolean hasLineOfSight(ServerLevel level, ServerPlayer viewer, ServerPlayer target) {
        Vec3 from = viewer.getEyePosition();
        double tx = target.getX(), ty = target.getY(), tz = target.getZ();
        Vec3[] toPoints = { target.getEyePosition(), new Vec3(tx, ty+1.0, tz), new Vec3(tx, ty+0.1, tz) };
        for (Vec3 to : toPoints) {
            HitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer));
            if (hit.getType() == HitResult.Type.MISS) return true;
        }
        return false;
    }
}

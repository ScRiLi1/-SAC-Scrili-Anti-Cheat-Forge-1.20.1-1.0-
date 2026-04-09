package com.scrili.sac.bukkit.antiesp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;

import java.util.*;

/**
 * AntiESP для Bukkit — использует встроенный hidePlayer/showPlayer API.
 * Скрывает игроков за стенами через player.hidePlayer(), показывает через showPlayer().
 */
public class AntiESP {

    private static final int HIDE_BUFFER = 3;
    private static final int SHOW_BUFFER = 2;
    private static final double MAX_DIST  = 128.0;
    private static final double MIN_DIST  = 5.0;

    private final JavaPlugin plugin;

    // viewer -> target -> буфер (>0 = скрытие, <0 = показ)
    private final Map<UUID, Map<UUID, Integer>> buffer = new HashMap<>();
    // viewer -> set of hidden targets
    private final Map<UUID, Set<UUID>> hidden = new HashMap<>();

    public AntiESP(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void tick(Collection<? extends Player> players) {
        for (Player viewer : players) {
            if (viewer.isInsideVehicle()) continue;
            for (Player target : players) {
                if (viewer == target) continue;
                processVisibility(viewer, target);
            }
        }
    }

    public void onPlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        // Показываем этого игрока всем кто его скрыл, перед выходом
        for (Map.Entry<UUID, Set<UUID>> entry : hidden.entrySet()) {
            if (entry.getValue().contains(uuid)) {
                Player viewer = plugin.getServer().getPlayer(entry.getKey());
                if (viewer != null) viewer.showPlayer(plugin, player);
            }
        }
        buffer.remove(uuid);
        hidden.remove(uuid);
        buffer.values().forEach(m -> m.remove(uuid));
        hidden.values().forEach(s -> s.remove(uuid));
    }

    // -------------------------------------------------------------------------

    private void processVisibility(Player viewer, Player target) {
        if (!viewer.getWorld().equals(target.getWorld())) {
            if (isHidden(viewer, target)) showTarget(viewer, target);
            clearBuffer(viewer, target);
            return;
        }

        double distSq = viewer.getLocation().distanceSquared(target.getLocation());
        boolean currentlyHidden = isHidden(viewer, target);

        if (distSq > MAX_DIST * MAX_DIST || distSq < MIN_DIST * MIN_DIST) {
            if (currentlyHidden) showTarget(viewer, target);
            clearBuffer(viewer, target);
            return;
        }

        boolean canSee = hasLineOfSight(viewer, target);

        if (canSee) {
            int buf = getBuffer(viewer, target);
            if (buf > 0) {
                clearBuffer(viewer, target);
            } else if (currentlyHidden) {
                int showCount = (-buf) + 1;
                if (showCount >= SHOW_BUFFER) {
                    showTarget(viewer, target);
                    clearBuffer(viewer, target);
                } else {
                    setBuffer(viewer, target, -showCount);
                }
            }
        } else {
            if (currentlyHidden) return;
            int buf = getBuffer(viewer, target);
            if (buf < 0) clearBuffer(viewer, target);
            int hideCount = Math.max(buf, 0) + 1;
            if (hideCount >= HIDE_BUFFER) {
                hideTarget(viewer, target);
                clearBuffer(viewer, target);
            } else {
                setBuffer(viewer, target, hideCount);
            }
        }
    }

    private void hideTarget(Player viewer, Player target) {
        viewer.hidePlayer(plugin, target);
        hidden.computeIfAbsent(viewer.getUniqueId(), k -> new HashSet<>()).add(target.getUniqueId());
    }

    private void showTarget(Player viewer, Player target) {
        viewer.showPlayer(plugin, target);
        Set<UUID> set = hidden.get(viewer.getUniqueId());
        if (set != null) set.remove(target.getUniqueId());
    }

    // -------------------------------------------------------------------------

    private boolean hasLineOfSight(Player viewer, Player target) {
        World world = viewer.getWorld();
        Location from = viewer.getEyeLocation();
        double tx = target.getX();
        double ty = target.getY();
        double tz = target.getZ();

        Location[] toPoints = {
            target.getEyeLocation(),
            new Location(world, tx, ty + 1.0, tz),
            new Location(world, tx, ty + 0.1, tz),
        };

        for (Location to : toPoints) {
            org.bukkit.util.Vector dir = to.toVector().subtract(from.toVector());
            double len = dir.length();
            if (len < 0.01) return true;
            RayTraceResult result = world.rayTraceBlocks(from, dir.normalize(), len);
            if (result == null) return true; // луч не попал в блок — виден
        }
        return false;
    }

    // -------------------------------------------------------------------------

    private boolean isHidden(Player viewer, Player target) {
        Set<UUID> set = hidden.get(viewer.getUniqueId());
        return set != null && set.contains(target.getUniqueId());
    }

    private int getBuffer(Player viewer, Player target) {
        return buffer
            .getOrDefault(viewer.getUniqueId(), Collections.emptyMap())
            .getOrDefault(target.getUniqueId(), 0);
    }

    private void setBuffer(Player viewer, Player target, int value) {
        buffer.computeIfAbsent(viewer.getUniqueId(), k -> new HashMap<>())
            .put(target.getUniqueId(), value);
    }

    private void clearBuffer(Player viewer, Player target) {
        Map<UUID, Integer> map = buffer.get(viewer.getUniqueId());
        if (map != null) map.remove(target.getUniqueId());
    }
}

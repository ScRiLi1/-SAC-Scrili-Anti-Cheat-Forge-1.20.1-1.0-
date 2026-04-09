package com.scrili.sac.forge.adapter;

import com.scrili.sac.core.api.ISACPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.UserBanListEntry;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

import net.minecraft.world.effect.MobEffects;

public class ForgePlayer implements ISACPlayer {

    private final ServerPlayer player;

    public ForgePlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override public UUID getUUID() { return player.getUUID(); }
    @Override public String getName() { return player.getName().getString(); }
    @Override public double getX() { return player.getX(); }
    @Override public double getY() { return player.getY(); }
    @Override public double getZ() { return player.getZ(); }
    @Override public boolean isOnGround() { return player.onGround(); }
    @Override public boolean isFlightAllowed() { return player.getAbilities().flying; }
    @Override public boolean isCreativeOrSpec() { return player.isCreative() || player.isSpectator(); }
    @Override public boolean isInWater() { return player.isInWater(); }
    @Override public boolean isInLava() { return player.isInLava(); }
    @Override public boolean isOnClimbable() { return player.onClimbable(); }
    @Override public boolean isGliding() { return player.isFallFlying(); }
    @Override public boolean isRiding() { return player.isPassenger(); }
    @Override public float getYaw() { return player.getYRot(); }
    @Override public float getPitch() { return player.getXRot(); }

    @Override
    public boolean hasLineOfSightTo(double x, double y, double z) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 from = player.getEyePosition();
        // y — это ноги цели, глаза примерно на +1.62
        // Проверяем глаза и грудь цели
        Vec3[] toPoints = {
            new Vec3(x, y + 1.62, z), // глаза
            new Vec3(x, y + 1.0,  z), // грудь
        };
        for (Vec3 to : toPoints) {
            net.minecraft.world.phys.HitResult hit = level.clip(
                new net.minecraft.world.level.ClipContext(
                    from, to,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    player
                )
            );
            if (hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS) return true;
        }
        return false;
    }

    @Override
    public int getSpeedAmplifier() {
        var effect = player.getEffect(MobEffects.MOVEMENT_SPEED);
        return effect != null ? effect.getAmplifier() + 1 : 0;
    }

    @Override
    public void sendMessage(String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    @Override
    public void kick(String reason) {
        player.connection.disconnect(Component.literal(reason));
    }

    @Override
    public void ban(String reason) {
        player.getServer().getPlayerList().getBans().add(
            new UserBanListEntry(player.getGameProfile(), null, "[SAC]", null, reason)
        );
    }
}

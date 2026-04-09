package com.scrili.sac.bukkit.adapter;

import com.scrili.sac.core.api.ISACPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class BukkitPlayer implements ISACPlayer {

    private final Player player;

    public BukkitPlayer(Player player) {
        this.player = player;
    }

    @Override public UUID getUUID() { return player.getUniqueId(); }
    @Override public String getName() { return player.getName(); }
    @Override public double getX() { return player.getLocation().getX(); }
    @Override public double getY() { return player.getLocation().getY(); }
    @Override public double getZ() { return player.getLocation().getZ(); }
    @Override public boolean isOnGround() { return player.isOnGround(); }
    @Override public boolean isFlightAllowed() { return player.getAllowFlight() && player.isFlying(); }
    @Override public boolean isCreativeOrSpec() {
        return switch (player.getGameMode()) {
            case CREATIVE, SPECTATOR -> true;
            default -> false;
        };
    }
    @Override public boolean isInWater() { return player.isInWater(); }
    @Override public boolean isInLava() { return player.getLocation().getBlock().isLiquid(); }
    @Override public boolean isOnClimbable() { return player.isClimbing(); }
    @Override public boolean isGliding() { return player.isGliding(); }
    @Override public boolean isRiding() { return player.isInsideVehicle(); }
    @Override public float getYaw() { return player.getLocation().getYaw(); }
    @Override public float getPitch() { return player.getLocation().getPitch(); }

    @Override
    public boolean hasLineOfSightTo(double x, double y, double z) {
        org.bukkit.World world = player.getWorld();
        org.bukkit.Location from = player.getEyeLocation();
        double[] yOffsets = { 1.62, 1.0, 0.1 };
        for (double dy : yOffsets) {
            org.bukkit.util.Vector dir = new org.bukkit.util.Vector(x - from.getX(), (y + dy) - from.getY(), z - from.getZ());
            double len = dir.length();
            if (len < 0.01) return true;
            org.bukkit.util.RayTraceResult result = world.rayTraceBlocks(from, dir.normalize(), len);
            if (result == null) return true;
        }
        return false;
    }

    @Override
    public int getSpeedAmplifier() {
        var effect = player.getPotionEffect(PotionEffectType.SPEED);
        return effect != null ? effect.getAmplifier() + 1 : 0;
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public void kick(String reason) {
        player.kickPlayer(reason);
    }

    @Override
    public void ban(String reason) {
        player.banPlayer(reason, "[SAC]");
    }
}

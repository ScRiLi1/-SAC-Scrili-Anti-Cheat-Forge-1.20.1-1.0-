package com.scrili.sac.core.api;

import java.util.UUID;

/**
 * Абстракция игрока — не зависит от Forge или Bukkit.
 * Forge и Bukkit реализуют этот интерфейс через свои обёртки.
 */
public interface ISACPlayer {

    UUID getUUID();
    String getName();

    double getX();
    double getY();
    double getZ();

    boolean isOnGround();
    boolean isFlightAllowed();   // разрешён ли полёт (gamemode, abilities)
    boolean isCreativeOrSpec();  // creative / spectator
    boolean isInWater();
    boolean isInLava();
    boolean isOnClimbable();
    boolean isGliding();         // элитры
    boolean isRiding();          // в транспорте

    // амплификатор зелья скорости (0 = нет зелья, 1 = Speed I, 2 = Speed II и т.д.)
    int getSpeedAmplifier();

    float getYaw();
    float getPitch();

    /**
     * Проверяет есть ли прямая видимость между этим игроком и указанными координатами.
     */
    boolean hasLineOfSightTo(double x, double y, double z);

    void sendMessage(String message);
    void kick(String reason);
    void ban(String reason);
}

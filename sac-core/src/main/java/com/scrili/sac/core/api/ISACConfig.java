package com.scrili.sac.core.api;

import java.util.List;

/**
 * Абстракция конфига — Forge читает из .toml, Bukkit из config.yml.
 */
public interface ISACConfig {

    // --- Общие ---
    int getMaxWarnings();
    String getPunishAction();

    // --- SpeedCheck ---
    double getMaxSpeed();
    boolean isSpeedCheckEnabled();

    // --- FlyCheck ---
    double getMaxFlyTicks();
    boolean isFlyCheckEnabled();

    // --- ReachCheck ---
    double getMaxReach();
    boolean isReachCheckEnabled();

    // --- AutoClicker ---
    int getMaxCps();
    boolean isAutoClickerCheckEnabled();

    // --- Scaffold ---
    int getMaxBlocksPerSecond();
    boolean isScaffoldCheckEnabled();

    // --- FastAttack ---
    long getMinAttackIntervalMs();
    boolean isFastAttackCheckEnabled();

    // --- KillAura ---
    boolean isKillAuraCheckEnabled();
    int getKillAuraAngleViolationThreshold();
    float getKillAuraMaxAngle();

    // --- FreecamCheck ---
    boolean isFreecamCheckEnabled();
    double getMaxInteractDistance();

    // --- ModBlacklist ---
    List<String> getModBlacklist();
    boolean isModBlacklistEnabled();

    // --- Уведомления ---
    boolean isAdminAlertsEnabled();
}

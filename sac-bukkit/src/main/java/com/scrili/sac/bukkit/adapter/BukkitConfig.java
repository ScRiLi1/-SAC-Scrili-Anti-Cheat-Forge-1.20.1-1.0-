package com.scrili.sac.bukkit.adapter;

import com.scrili.sac.core.api.ISACConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class BukkitConfig implements ISACConfig {

    private final FileConfiguration config;

    public BukkitConfig(FileConfiguration config) {
        this.config = config;
    }

    @Override public int getMaxWarnings()          { return config.getInt("general.maxWarnings", 3); }
    @Override public String getPunishAction()       { return config.getString("general.punishAction", "KICK"); }
    @Override public boolean isAdminAlertsEnabled() { return config.getBoolean("general.adminAlertsEnabled", true); }

    @Override public boolean isSpeedCheckEnabled()  { return config.getBoolean("speed.enabled", true); }
    @Override public double getMaxSpeed()           { return config.getDouble("speed.maxSpeed", 0.9); }

    @Override public boolean isFlyCheckEnabled()    { return config.getBoolean("fly.enabled", true); }
    @Override public double getMaxFlyTicks()        { return config.getDouble("fly.maxFlyTicks", 60.0); }

    @Override public boolean isReachCheckEnabled()  { return config.getBoolean("reach.enabled", true); }
    @Override public double getMaxReach()           { return config.getDouble("reach.maxReach", 4.0); }

    @Override public boolean isAutoClickerCheckEnabled() { return config.getBoolean("autoclicker.enabled", true); }
    @Override public int getMaxCps()               { return config.getInt("autoclicker.maxCps", 16); }

    @Override public boolean isScaffoldCheckEnabled() { return config.getBoolean("scaffold.enabled", true); }
    @Override public int getMaxBlocksPerSecond()   { return config.getInt("scaffold.maxBlocksPerSecond", 6); }

    @Override public boolean isFastAttackCheckEnabled() { return config.getBoolean("fastattack.enabled", true); }
    @Override public long getMinAttackIntervalMs() { return config.getLong("fastattack.minAttackIntervalMs", 100L); }

    @Override public boolean isKillAuraCheckEnabled() { return config.getBoolean("killaura.enabled", true); }
    @Override public int getKillAuraAngleViolationThreshold() { return config.getInt("killaura.angleViolationThreshold", 3); }
    @Override public float getKillAuraMaxAngle()   { return (float) config.getDouble("killaura.maxAngle", 90.0); }

    @Override public boolean isTimerCheckEnabled() { return config.getBoolean("timer.enabled", true); }
    @Override public double getMaxTimerSpeed()     { return config.getDouble("timer.maxTimerSpeed", 1.1); }

    @Override public boolean isFreecamCheckEnabled() { return config.getBoolean("freecam.enabled", true); }
    @Override public double getMaxInteractDistance() { return config.getDouble("freecam.maxInteractDistance", 6.0); }

    @Override public boolean isModBlacklistEnabled() { return config.getBoolean("modblacklist.enabled", true); }
    @Override public List<String> getModBlacklist() { return config.getStringList("modblacklist.mods"); }
}

package com.scrili.sac.forge.adapter;

import com.scrili.sac.core.api.ISACConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.Arrays;
import java.util.List;

public class ForgeConfig implements ISACConfig {

    private static ForgeConfigSpec SPEC;

    // --- Общие ---
    private static ForgeConfigSpec.IntValue MAX_WARNINGS;
    private static ForgeConfigSpec.ConfigValue<String> PUNISH_ACTION;

    // --- SpeedCheck ---
    private static ForgeConfigSpec.BooleanValue SPEED_ENABLED;
    private static ForgeConfigSpec.DoubleValue MAX_SPEED;

    // --- FlyCheck ---
    private static ForgeConfigSpec.BooleanValue FLY_ENABLED;
    private static ForgeConfigSpec.DoubleValue MAX_FLY_TICKS;

    // --- ReachCheck ---
    private static ForgeConfigSpec.BooleanValue REACH_ENABLED;
    private static ForgeConfigSpec.DoubleValue MAX_REACH;

    // --- AutoClicker ---
    private static ForgeConfigSpec.BooleanValue AUTOCLICKER_ENABLED;
    private static ForgeConfigSpec.IntValue MAX_CPS;

    // --- Scaffold ---
    private static ForgeConfigSpec.BooleanValue SCAFFOLD_ENABLED;
    private static ForgeConfigSpec.IntValue MAX_BLOCKS_PER_SECOND;

    // --- FastAttack ---
    private static ForgeConfigSpec.BooleanValue FAST_ATTACK_ENABLED;
    private static ForgeConfigSpec.LongValue MIN_ATTACK_INTERVAL_MS;

    // --- KillAura ---
    private static ForgeConfigSpec.BooleanValue KILLAURA_ENABLED;
    private static ForgeConfigSpec.IntValue KILLAURA_ANGLE_VIOLATIONS;
    private static ForgeConfigSpec.DoubleValue KILLAURA_MAX_ANGLE;

    // --- Velocity ---
    private static ForgeConfigSpec.BooleanValue VELOCITY_ENABLED;
    private static ForgeConfigSpec.DoubleValue MIN_VELOCITY_FACTOR;

    // --- Timer ---
    private static ForgeConfigSpec.BooleanValue TIMER_ENABLED;
    private static ForgeConfigSpec.DoubleValue MAX_TIMER_SPEED;

    // --- Freecam ---
    private static ForgeConfigSpec.BooleanValue FREECAM_ENABLED;
    private static ForgeConfigSpec.DoubleValue MAX_INTERACT_DISTANCE;

    // --- ModBlacklist ---
    private static ForgeConfigSpec.BooleanValue MOD_BLACKLIST_ENABLED;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> MOD_BLACKLIST;

    // --- AntiESP ---
    private static ForgeConfigSpec.BooleanValue ANTI_ESP_ENABLED;
    private static ForgeConfigSpec.IntValue ANTI_ESP_INTERVAL;

    // --- Уведомления ---
    private static ForgeConfigSpec.BooleanValue ADMIN_ALERTS_ENABLED;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        // ---- general ----
        b.push("general");
        MAX_WARNINGS = b.comment("Количество предупреждений до наказания")
            .defineInRange("maxWarnings", 3, 1, 20);
        PUNISH_ACTION = b.comment("Действие при достижении лимита: KICK, BAN, NONE")
            .define("punishAction", "KICK");
        ADMIN_ALERTS_ENABLED = b.comment("Отправлять уведомления о нарушениях онлайн-операторам")
            .define("adminAlertsEnabled", true);
        b.pop();

        // ---- speed ----
        b.push("speed");
        SPEED_ENABLED = b.comment("Включить проверку скорости")
            .define("enabled", true);
        MAX_SPEED = b.comment("Макс. горизонтальная скорость (блоков/тик)")
            .defineInRange("maxSpeed", 0.9, 0.1, 5.0);
        b.pop();

        // ---- fly ----
        b.push("fly");
        FLY_ENABLED = b.comment("Включить проверку полёта")
            .define("enabled", true);
        MAX_FLY_TICKS = b.comment("Макс. тиков в воздухе без разрешения")
            .defineInRange("maxFlyTicks", 60.0, 5.0, 200.0);
        b.pop();

        // ---- reach ----
        b.push("reach");
        REACH_ENABLED = b.comment("Включить проверку дистанции удара")
            .define("enabled", true);
        MAX_REACH = b.comment("Макс. дистанция удара (блоков)")
            .defineInRange("maxReach", 4.0, 2.0, 8.0);
        b.pop();

        // ---- autoclicker ----
        b.push("autoclicker");
        AUTOCLICKER_ENABLED = b.comment("Включить проверку AutoClicker")
            .define("enabled", true);
        MAX_CPS = b.comment("Макс. кликов в секунду")
            .defineInRange("maxCps", 16, 5, 50);
        b.pop();

        // ---- scaffold ----
        b.push("scaffold");
        SCAFFOLD_ENABLED = b.comment("Включить проверку Scaffold")
            .define("enabled", true);
        MAX_BLOCKS_PER_SECOND = b.comment("Макс. блоков в секунду")
            .defineInRange("maxBlocksPerSecond", 6, 1, 20);
        b.pop();

        // ---- fastattack ----
        b.push("fastattack");
        FAST_ATTACK_ENABLED = b.comment("Включить проверку FastAttack")
            .define("enabled", true);
        MIN_ATTACK_INTERVAL_MS = b.comment("Мин. интервал между ударами (мс)")
            .defineInRange("minAttackIntervalMs", 100L, 50L, 1000L);
        b.pop();

        // ---- killaura ----
        b.push("killaura");
        KILLAURA_ENABLED = b.comment("Включить проверку KillAura")
            .define("enabled", true);
        KILLAURA_ANGLE_VIOLATIONS = b.comment("Кол-во подряд нарушений угла перед варном")
            .defineInRange("angleViolationThreshold", 2, 1, 10);
        KILLAURA_MAX_ANGLE = b.comment("Макс. угол между взглядом и целью при ударе (градусов)")
            .defineInRange("maxAngle", 75.0, 45.0, 180.0);
        b.pop();

        // ---- velocity ----
        b.push("velocity");
        VELOCITY_ENABLED = b.comment("Включить проверку игнорирования knockback")
            .define("enabled", true);
        MIN_VELOCITY_FACTOR = b.comment("Минимальная доля knockback (0.3 = игрок должен получить хотя бы 30%)")
            .defineInRange("minVelocityFactor", 0.3, 0.1, 1.0);
        b.pop();

        // ---- timer ----
        b.push("timer");
        TIMER_ENABLED = b.comment("Включить проверку Timer (ускорение тикрейта)")
            .define("enabled", true);
        MAX_TIMER_SPEED = b.comment("Макс. скорость тикрейта (1.0 = норма, 1.1 = +10%)")
            .defineInRange("maxTimerSpeed", 1.1, 1.0, 2.0);
        b.pop();

        // ---- freecam ----
        b.push("freecam");
        FREECAM_ENABLED = b.comment("Включить проверку Freecam")
            .define("enabled", true);
        MAX_INTERACT_DISTANCE = b.comment("Макс. дистанция взаимодействия с блоком (блоков)")
            .defineInRange("maxInteractDistance", 6.0, 3.0, 10.0);
        b.pop();

        // ---- modblacklist ----
        b.push("modblacklist");
        MOD_BLACKLIST_ENABLED = b.comment("Включить проверку запрещённых модов")
            .define("enabled", true);
        MOD_BLACKLIST = b.comment("Список запрещённых модов (modid)")
            .defineListAllowEmpty(
                List.of("mods"),
                () -> Arrays.asList("wurst", "meteor-client", "baritone", "xray"),
                o -> o instanceof String
            );
        b.pop();

        // ---- antiesp ----
        b.push("antiesp");
        ANTI_ESP_ENABLED = b.comment("Включить Anti-ESP")
            .define("enabled", true);
        ANTI_ESP_INTERVAL = b.comment("Интервал проверки видимости в тиках (20 = 1 сек)")
            .defineInRange("checkInterval", 20, 1, 100);
        b.pop();

        SPEC = b.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "sac-server.toml");
    }

    // --- ISACConfig ---
    @Override public int getMaxWarnings()               { return MAX_WARNINGS.get(); }
    @Override public String getPunishAction()           { return PUNISH_ACTION.get(); }
    @Override public boolean isAdminAlertsEnabled()     { return ADMIN_ALERTS_ENABLED.get(); }

    @Override public boolean isSpeedCheckEnabled()      { return SPEED_ENABLED.get(); }
    @Override public double getMaxSpeed()               { return MAX_SPEED.get(); }

    @Override public boolean isFlyCheckEnabled()        { return FLY_ENABLED.get(); }
    @Override public double getMaxFlyTicks()            { return MAX_FLY_TICKS.get(); }

    @Override public boolean isReachCheckEnabled()      { return REACH_ENABLED.get(); }
    @Override public double getMaxReach()               { return MAX_REACH.get(); }

    @Override public boolean isAutoClickerCheckEnabled(){ return AUTOCLICKER_ENABLED.get(); }
    @Override public int getMaxCps()                    { return MAX_CPS.get(); }

    @Override public boolean isScaffoldCheckEnabled()   { return SCAFFOLD_ENABLED.get(); }
    @Override public int getMaxBlocksPerSecond()        { return MAX_BLOCKS_PER_SECOND.get(); }

    @Override public boolean isFastAttackCheckEnabled() { return FAST_ATTACK_ENABLED.get(); }
    @Override public long getMinAttackIntervalMs()      { return MIN_ATTACK_INTERVAL_MS.get(); }

    @Override public boolean isKillAuraCheckEnabled()   { return KILLAURA_ENABLED.get(); }
    @Override public int getKillAuraAngleViolationThreshold() { return KILLAURA_ANGLE_VIOLATIONS.get(); }
    @Override public float getKillAuraMaxAngle()        { return KILLAURA_MAX_ANGLE.get().floatValue(); }

    @Override public boolean isFreecamCheckEnabled()    { return FREECAM_ENABLED.get(); }
    @Override public double getMaxInteractDistance()    { return MAX_INTERACT_DISTANCE.get(); }

    @Override public boolean isModBlacklistEnabled()    { return MOD_BLACKLIST_ENABLED.get(); }
    @Override public List<String> getModBlacklist()     { return (List<String>) MOD_BLACKLIST.get(); }

    public static boolean isAntiEspEnabled()            { return ANTI_ESP_ENABLED.get(); }
    public static int getAntiEspInterval()              { return ANTI_ESP_INTERVAL.get(); }
}

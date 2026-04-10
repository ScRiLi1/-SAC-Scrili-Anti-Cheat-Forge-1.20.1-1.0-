package com.scrili.sac.core.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private static final Map<UUID, PlayerData> DATA = new HashMap<>();

    public int warnings = 0;
    public int airTicks = 0;
    public double lastX = 0, lastY = 0, lastZ = 0;
    public boolean posInitialized = false;

    // FastAttack / KillAura
    public long lastAttackTime = -1;
    public float lastAttackYaw = -999f;

    // AimAssist
    public float lastYaw = -999f;
    public float lastPitch = -999f;
    public float[] yawDeltas = new float[20];
    public float[] pitchDeltas = new float[20];
    public int aimSampleIndex = 0;
    public float[] attackAngles = new float[10];
    public int attackAngleIndex = 0;

    // KillAura violation buffer
    public int killAuraAngleViolations = 0;

    // Mouse aim statistics (для будущего анализа)
    public float mouseYawVariance   = 0;
    public float mousePitchVariance = 0;

    // VelocityCheck
    public double expectedVelocityX = 0;
    public double expectedVelocityZ = 0;
    public boolean pendingKnockback = false;
    public long knockbackTime = -1;

    // TimerCheck
    public long lastPacketTime = -1;
    public long[] packetIntervals = new long[20];
    public int packetIntervalIndex = 0;

    // Speed sampling
    public double[] speedSamples = new double[10];
    public int speedSampleIndex = 0;

    // Scaffold
    public long blockPlaceWindowStart = 0;
    public int blocksPlacedInWindow = 0;

    // AutoClicker
    public int lastReportedCps = 0;
    public long serverCpsWindowStart = 0;
    public int serverCpsCount = 0;

    // История варнов (последние 20)
    public final List<String> warnHistory = new ArrayList<>();
    public static final int MAX_WARN_HISTORY = 20;

    public void addWarnHistory(String reason) {
        if (warnHistory.size() >= MAX_WARN_HISTORY) {
            warnHistory.remove(0);
        }
        warnHistory.add(reason);
    }

    public List<String> getWarnHistory() {
        return Collections.unmodifiableList(warnHistory);
    }

    public static PlayerData get(UUID uuid) {
        return DATA.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public static void remove(UUID uuid) {
        DATA.remove(uuid);
    }

    public static Map<UUID, PlayerData> getAll() {
        return Collections.unmodifiableMap(DATA);
    }
}

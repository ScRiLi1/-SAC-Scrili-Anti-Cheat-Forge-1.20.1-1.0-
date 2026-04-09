package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

public class MouseAimCheck {

    private static final int MIN_ACTIVE_TICKS = 5;
    private static final float MAX_SILENT_RATIO = 0.4f;
    private static final float MIN_ANGLE_DELTA = 0.5f;
    private static final float MIN_MOUSE_PIXELS = 0.5f;
    private static final float MIN_SENSITIVITY_VARIANCE = 0.01f;
    private static final int MIN_SENSITIVITY_SAMPLES = 8;

    private final PunishmentManager punishment;

    public MouseAimCheck(PunishmentManager punishment) {
        this.punishment = punishment;
    }

    public void analyze(ISACPlayer player, float[] rawDX, float[] rawDY,
                        float[] deltaYaw, float[] deltaPitch) {
        if (player.isCreativeOrSpec()) return;
        if (deltaYaw.length < MIN_ACTIVE_TICKS) return;

        // Silent aim: угол меняется без движения мыши
        checkSilentAim(player, rawDX, rawDY, deltaYaw, deltaPitch);

        // FakeMouse отключён — даёт ложные срабатывания
        // checkSensitivityConsistency(player, rawDX, deltaYaw);
    }

    private void checkSilentAim(ISACPlayer player,
                                 float[] rawDX, float[] rawDY,
                                 float[] deltaYaw, float[] deltaPitch) {
        int activeTicks = 0;
        int silentTicks = 0;

        for (int i = 0; i < deltaYaw.length; i++) {
            float angleMoved = Math.abs(deltaYaw[i]) + Math.abs(deltaPitch[i]);
            if (angleMoved < MIN_ANGLE_DELTA) continue;
            activeTicks++;
            float mouseMoved = Math.abs(rawDX[i]) + Math.abs(rawDY[i]);
            if (mouseMoved < MIN_MOUSE_PIXELS) silentTicks++;
        }

        if (activeTicks < MIN_ACTIVE_TICKS) return;

        float silentRatio = (float) silentTicks / activeTicks;
        PlayerData data = PlayerData.get(player.getUUID());
        data.mouseYawVariance = silentRatio;

        if (silentRatio > MAX_SILENT_RATIO) {
            punishment.warn(player,
                "AimBot/Silent (камера без мыши: "
                + String.format("%.0f", silentRatio * 100) + "% из " + activeTicks + " тиков)"
            );
        }
    }

    private void checkSensitivityConsistency(ISACPlayer player,
                                              float[] rawDX, float[] deltaYaw) {
        float[] ratios = new float[deltaYaw.length];
        int count = 0;

        for (int i = 0; i < deltaYaw.length; i++) {
            if (Math.abs(deltaYaw[i]) < MIN_ANGLE_DELTA) continue;
            if (Math.abs(rawDX[i]) < MIN_MOUSE_PIXELS) continue;
            ratios[count++] = rawDX[i] / deltaYaw[i];
        }

        if (count < MIN_SENSITIVITY_SAMPLES) return;

        float[] trimmed = java.util.Arrays.copyOf(ratios, count);
        float variance = variance(trimmed);

        if (variance < MIN_SENSITIVITY_VARIANCE) {
            punishment.warn(player,
                "AimBot/FakeMouse (var=" + String.format("%.5f", variance) + ")"
            );
        }
    }

    private float variance(float[] values) {
        float sum = 0;
        for (float v : values) sum += v;
        float mean = sum / values.length;
        float var = 0;
        for (float v : values) var += (v - mean) * (v - mean);
        return var / values.length;
    }
}

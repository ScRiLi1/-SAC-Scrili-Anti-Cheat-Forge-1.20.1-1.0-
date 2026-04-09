package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

/**
 * AimAssistCheck — детектирует аим-боты по двум признакам:
 *
 * 1. Snap — мгновенный поворот камеры на большой угол за один тик.
 *    Человек физически не может повернуть мышь на 60°+ за 50мс.
 *
 * 2. LowDispersion — слишком точное прицеливание при атаке.
 *    Аим-бот всегда бьёт с минимальным отклонением от цели.
 *    Считаем дисперсию угла между взглядом и целью за последние N атак.
 */
public class AimAssistCheck {

    // Минимальная дисперсия угла атаки (градусов²) — ниже этого подозрительно
    private static final float MIN_ANGLE_VARIANCE = 0.5f;
    // Минимум атак для проверки дисперсии
    private static final int MIN_SAMPLES_FOR_VARIANCE = 10;

    private final PunishmentManager punishment;

    public AimAssistCheck(PunishmentManager punishment) {
        this.punishment = punishment;
    }

    /**
     * Вызывается каждый тик — больше не проверяем Snap, только собираем историю.
     */
    public void onTick(ISACPlayer player) {
        if (player.isCreativeOrSpec()) return;

        PlayerData data = PlayerData.get(player.getUUID());
        data.lastYaw   = player.getYaw();
        data.lastPitch = player.getPitch();
    }

    /**
     * Вызывается при каждой атаке.
     * LowDispersion чек удалён — ненадёжен серверно, даёт ложные срабатывания
     * на обычных игроков которые нормально целятся.
     */
    public void onAttack(ISACPlayer attacker, double targetX, double targetY, double targetZ) {
        // Зарезервировано для будущих чеков
    }

    // -------------------------------------------------------------------------

    /**
     * Угол между взглядом игрока и направлением на цель (в градусах).
     */
    private float getAngleToTarget(ISACPlayer player, double tx, double ty, double tz) {
        float yaw   = (float) Math.toRadians(player.getYaw());
        float pitch = (float) Math.toRadians(player.getPitch());

        double lookX = -Math.sin(yaw) * Math.cos(pitch);
        double lookY = -Math.sin(pitch);
        double lookZ =  Math.cos(yaw) * Math.cos(pitch);

        double dx = tx - player.getX();
        double dy = (ty + 1.0) - (player.getY() + 1.62);
        double dz = tz - player.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001) return 0f;

        dx /= len; dy /= len; dz /= len;

        double dot = Math.max(-1.0, Math.min(1.0, lookX * dx + lookY * dy + lookZ * dz));
        return (float) Math.toDegrees(Math.acos(dot));
    }

    /**
     * Минимальная разница между двумя углами с учётом перехода через 360°.
     */
    private float angleDelta(float a, float b) {
        float d = Math.abs(a - b) % 360f;
        return d > 180f ? 360f - d : d;
    }

    /**
     * Дисперсия массива значений.
     */
    private float variance(float[] values) {
        float sum = 0;
        for (float v : values) sum += v;
        float mean = sum / values.length;
        float var = 0;
        for (float v : values) var += (v - mean) * (v - mean);
        return var / values.length;
    }
}

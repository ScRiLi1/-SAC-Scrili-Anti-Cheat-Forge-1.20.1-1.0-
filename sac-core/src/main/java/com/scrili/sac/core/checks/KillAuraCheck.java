package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

public class KillAuraCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public KillAuraCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    public void onAttack(ISACPlayer attacker, double targetX, double targetY, double targetZ) {
        if (attacker.isCreativeOrSpec()) return;

        // 1. Атака сквозь стену
        if (!attacker.hasLineOfSightTo(targetX, targetY, targetZ)) {
            punishment.warn(attacker, "KillAura/Wall (атака сквозь стену)");
            return;
        }

        // 2. Угол взгляда
        float angle = getAngleToTarget(attacker, targetX, targetY, targetZ);
        PlayerData data = PlayerData.get(attacker.getUUID());
        float maxAngle = config.getKillAuraMaxAngle();
        int threshold  = config.getKillAuraAngleViolationThreshold();

        if (angle > maxAngle) {
            data.killAuraAngleViolations++;
            if (data.killAuraAngleViolations >= threshold) {
                punishment.warn(attacker,
                    "KillAura/Angle (угол: " + String.format("%.1f", angle) + "°)"
                );
                data.killAuraAngleViolations = 0;
            }
        } else {
            data.killAuraAngleViolations = Math.max(0, data.killAuraAngleViolations - 1);
        }
    }

    private float getAngleToTarget(ISACPlayer attacker, double tx, double ty, double tz) {
        float yaw   = (float) Math.toRadians(attacker.getYaw());
        float pitch = (float) Math.toRadians(attacker.getPitch());

        double lookX = -Math.sin(yaw) * Math.cos(pitch);
        double lookY = -Math.sin(pitch);
        double lookZ =  Math.cos(yaw) * Math.cos(pitch);

        double dx = tx - attacker.getX();
        double dy = (ty + 1.0) - (attacker.getY() + 1.62);
        double dz = tz - attacker.getZ();
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 0.001) return 0f;

        dx /= len; dy /= len; dz /= len;

        double dot = Math.max(-1.0, Math.min(1.0, lookX * dx + lookY * dy + lookZ * dz));
        return (float) Math.toDegrees(Math.acos(dot));
    }
}

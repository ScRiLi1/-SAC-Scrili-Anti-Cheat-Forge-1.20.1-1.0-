package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.punishment.PunishmentManager;

public class ReachCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public ReachCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    /**
     * @param attacker атакующий игрок
     * @param targetX/Y/Z позиция цели
     */
    public void check(ISACPlayer attacker, double targetX, double targetY, double targetZ) {
        if (attacker.isCreativeOrSpec()) return;

        double dx = attacker.getX() - targetX;
        double dy = attacker.getY() - targetY;
        double dz = attacker.getZ() - targetZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double maxReach = config.getMaxReach();
        if (dist > maxReach) {
            punishment.warn(attacker,
                "Reach (дистанция: " + String.format("%.2f", dist) + " > " + maxReach + ")"
            );
        }
    }
}

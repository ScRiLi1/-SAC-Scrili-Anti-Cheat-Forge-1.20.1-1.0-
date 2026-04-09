package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

public class FastAttackCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public FastAttackCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    public void onAttack(ISACPlayer player) {
        if (player.isCreativeOrSpec()) return;

        PlayerData data = PlayerData.get(player.getUUID());
        long now = System.currentTimeMillis();

        long minInterval = config.getMinAttackIntervalMs();
        if (data.lastAttackTime > 0) {
            long delta = now - data.lastAttackTime;
            if (delta < minInterval) {
                punishment.warn(player,
                    "FastAttack (интервал: " + delta + "ms < " + minInterval + "ms)"
                );
            }
        }

        if (data.lastAttackYaw >= -360f) {
            float yawDelta = Math.abs(player.getYaw() - data.lastAttackYaw);
            if (yawDelta > 180.0f) {
                punishment.warn(player,
                    "KillAura (поворот между ударами: " + String.format("%.1f", yawDelta) + "°)"
                );
            }
        }

        data.lastAttackTime = now;
        data.lastAttackYaw = player.getYaw();
    }
}

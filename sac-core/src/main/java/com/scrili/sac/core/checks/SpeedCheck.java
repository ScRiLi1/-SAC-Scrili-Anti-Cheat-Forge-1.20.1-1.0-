package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

public class SpeedCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public SpeedCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    public void check(ISACPlayer player) {
        if (player.isRiding() || player.isCreativeOrSpec()) return;

        PlayerData data = PlayerData.get(player.getUUID());

        if (data.posInitialized) {
            double dx = player.getX() - data.lastX;
            double dz = player.getZ() - data.lastZ;
            double speed = Math.sqrt(dx * dx + dz * dz);

            // Если позиция изменилась слишком резко — это телепорт, сбрасываем
            if (speed > 10.0) {
                data.posInitialized = false;
                data.speedSampleIndex = 0;
                data.lastX = player.getX();
                data.lastY = player.getY();
                data.lastZ = player.getZ();
                return;
            }

            // накапливаем скорость за последние 10 тиков
            data.speedSamples[data.speedSampleIndex % 10] = speed;
            data.speedSampleIndex++;

            // проверяем только когда игрок на земле и набрали 10 сэмплов
            if (player.isOnGround() && data.speedSampleIndex >= 10) {
                double avg = 0;
                for (double s : data.speedSamples) avg += s;
                avg /= 10.0;

                double limit = config.getMaxSpeed()
                    + player.getSpeedAmplifier() * 0.12;

                if (avg > limit) {
                    punishment.warn(player,
                        "SpeedHack (скорость: " + String.format("%.2f", avg) + " > " + String.format("%.2f", limit) + ")"
                    );
                }
            }
        }

        data.lastX = player.getX();
        data.lastY = player.getY();
        data.lastZ = player.getZ();
        data.posInitialized = true;
    }
}

package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

public class JesusCheck {

    // если игрок движется по воде быстрее этого — подозрительно
    private static final double MAX_WATER_SPEED = 0.22;

    private final PunishmentManager punishment;

    public JesusCheck(PunishmentManager punishment) {
        this.punishment = punishment;
    }

    public void check(ISACPlayer player) {
        if (player.isCreativeOrSpec()) return;
        if (!player.isInWater()) return;

        PlayerData data = PlayerData.get(player.getUUID());
        if (!data.posInitialized) return;

        double dx = player.getX() - data.lastX;
        double dz = player.getZ() - data.lastZ;
        double speed = Math.sqrt(dx * dx + dz * dz);

        // идёт по поверхности воды (Y почти не меняется) с высокой скоростью
        double dy = Math.abs(player.getY() - data.lastY);
        if (speed > MAX_WATER_SPEED && dy < 0.05) {
            punishment.warn(player,
                "Jesus (скорость по воде: " + String.format("%.2f", speed) + ")"
            );
        }
    }
}

package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

public class FlyCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public FlyCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    public void check(ISACPlayer player) {
        if (player.isFlightAllowed()) return;
        if (player.isCreativeOrSpec()) return;
        if (player.isInWater() || player.isInLava()) return;
        if (player.isOnClimbable()) return;
        if (player.isGliding()) return;

        PlayerData data = PlayerData.get(player.getUUID());

        if (!player.isOnGround()) {
            data.airTicks++;
        } else {
            data.airTicks = 0;
        }

        int maxTicks = (int) config.getMaxFlyTicks();
        if (data.airTicks > maxTicks) {
            punishment.warn(player, "FlyHack (в воздухе " + data.airTicks + " тиков)");
            data.airTicks = 0;
        }
    }
}

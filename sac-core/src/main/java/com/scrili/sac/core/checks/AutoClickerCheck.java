package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

public class AutoClickerCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public AutoClickerCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    public void check(ISACPlayer player, int cps) {
        if (player.isCreativeOrSpec()) return;

        int max = config.getMaxCps();
        if (cps > max) {
            punishment.warn(player, "AutoClicker (CPS: " + cps + " > " + max + ")");
        }

        PlayerData.get(player.getUUID()).lastReportedCps = cps;
    }
}

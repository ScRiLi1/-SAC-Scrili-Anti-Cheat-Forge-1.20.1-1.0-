package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

public class ScaffoldCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public ScaffoldCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    public void onBlockPlace(ISACPlayer player) {
        if (player.isCreativeOrSpec()) return;

        PlayerData data = PlayerData.get(player.getUUID());
        long now = System.currentTimeMillis();

        if (now - data.blockPlaceWindowStart > 1000) {
            data.blockPlaceWindowStart = now;
            data.blocksPlacedInWindow = 0;
        }

        data.blocksPlacedInWindow++;
        int max = config.getMaxBlocksPerSecond();

        if (data.blocksPlacedInWindow > max) {
            punishment.warn(player,
                "Scaffold (блоков/сек: " + data.blocksPlacedInWindow + " > " + max + ")"
            );
        }
    }
}

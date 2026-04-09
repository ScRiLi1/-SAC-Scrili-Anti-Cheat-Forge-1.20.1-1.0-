package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.log.LogManager;
import com.scrili.sac.core.punishment.PunishmentManager;

import java.util.List;

public class ModBlacklistCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public ModBlacklistCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    public void check(ISACPlayer player, List<String> clientMods) {
        List<String> blacklist = config.getModBlacklist();

        LogManager.log(player.getName(), player.getUUID().toString(),
            "MOD_LIST", String.join(", ", clientMods));

        for (String modId : clientMods) {
            if (blacklist.contains(modId.toLowerCase())) {
                LogManager.log(player.getName(), player.getUUID().toString(),
                    "BLACKLISTED_MOD", "Обнаружен мод: " + modId);
                punishment.warn(player, "Запрещённый мод: " + modId);
                return;
            }
        }
    }
}

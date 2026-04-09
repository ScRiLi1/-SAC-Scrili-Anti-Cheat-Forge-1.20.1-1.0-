package com.scrili.sac.core.punishment;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.log.LogManager;

public class PunishmentManager {

    private final ISACConfig config;

    public PunishmentManager(ISACConfig config) {
        this.config = config;
    }

    public void warn(ISACPlayer player, String reason) {
        PlayerData data = PlayerData.get(player.getUUID());
        int maxWarns = config.getMaxWarnings();

        data.warnings++;

        // Сохраняем в историю
        String entry = "[" + java.time.LocalTime.now().withNano(0) + "] " + reason;
        data.addWarnHistory(entry);

        String name = player.getName();
        String uuid = player.getUUID().toString();

        if (data.warnings < maxWarns) {
            player.sendMessage("§e[SAC] Предупреждение " + data.warnings + "/" + maxWarns + ": " + reason);
            LogManager.log(name, uuid, "WARN (" + data.warnings + "/" + maxWarns + ")", reason);

        } else if (data.warnings == maxWarns) {
            LogManager.log(name, uuid, "KICK", reason);
            player.kick("§c[SAC] Вы были кикнуты.\nПричина: " + reason);

        } else {
            LogManager.log(name, uuid, "BAN", reason);
            player.ban("Автобан SAC: " + reason);
            player.kick("§4[SAC] Вы забанены.\nПричина: " + reason);
        }
    }
}

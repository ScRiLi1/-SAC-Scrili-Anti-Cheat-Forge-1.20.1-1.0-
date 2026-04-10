package com.scrili.sac.core.punishment;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.log.LogManager;

public class PunishmentManager {

    private final ISACConfig config;
    // Callback для отправки алертов администраторам — реализуется платформой
    private AdminAlertSender alertSender;

    public interface AdminAlertSender {
        void sendAlert(String message);
    }

    public PunishmentManager(ISACConfig config) {
        this.config = config;
    }

    public void setAlertSender(AdminAlertSender sender) {
        this.alertSender = sender;
    }

    public AdminAlertSender getAlertSender() {
        return alertSender;
    }

    public void warn(ISACPlayer player, String reason) {
        PlayerData data = PlayerData.get(player.getUUID());
        int maxWarns = config.getMaxWarnings();

        data.warnings++;

        String entry = "[" + java.time.LocalTime.now().withNano(0) + "] " + reason;
        data.addWarnHistory(entry);

        String name = player.getName();
        String uuid = player.getUUID().toString();

        if (data.warnings < maxWarns) {
            player.sendMessage("§e[SAC] Предупреждение " + data.warnings + "/" + maxWarns + ": " + reason);
            LogManager.log(name, uuid, "WARN (" + data.warnings + "/" + maxWarns + ")", reason);
            sendAdminAlert("§e[SAC] §f" + name + " §7— варн " + data.warnings + "/" + maxWarns + ": §e" + reason);

        } else if (data.warnings == maxWarns) {
            LogManager.log(name, uuid, "KICK", reason);
            player.kick("§c[SAC] Вы были кикнуты.\nПричина: " + reason);
            sendAdminAlert("§c[SAC] §f" + name + " §7кикнут: §c" + reason);

        } else {
            LogManager.log(name, uuid, "BAN", reason);
            player.ban("Автобан SAC: " + reason);
            player.kick("§4[SAC] Вы забанены.\nПричина: " + reason);
            sendAdminAlert("§4[SAC] §f" + name + " §7забанен: §4" + reason);
        }
    }

    private void sendAdminAlert(String message) {
        if (alertSender != null && config.isAdminAlertsEnabled()) {
            alertSender.sendAlert(message);
        }
    }
}

package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.data.PlayerData;
import com.scrili.sac.core.punishment.PunishmentManager;

/**
 * AutoClickerCheck — два метода детекта:
 * 1. Клиентский CPS пакет (легко обойти, но ловит простые автокликеры)
 * 2. Серверный подсчёт атак в секунду через LivingAttackEvent (надёжнее)
 */
public class AutoClickerCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public AutoClickerCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    /** Вызывается с данными от клиентского CPS трекера */
    public void check(ISACPlayer player, int cps) {
        if (player.isCreativeOrSpec()) return;
        int max = config.getMaxCps();
        if (cps > max) {
            punishment.warn(player, "AutoClicker (CPS: " + cps + " > " + max + ")");
        }
        PlayerData.get(player.getUUID()).lastReportedCps = cps;
    }

    /** Вызывается при каждой атаке — серверный подсчёт */
    public void onAttack(ISACPlayer player) {
        if (player.isCreativeOrSpec()) return;
        PlayerData data = PlayerData.get(player.getUUID());
        long now = System.currentTimeMillis();

        // Инициализация окна
        if (data.serverCpsWindowStart == 0) {
            data.serverCpsWindowStart = now;
            data.serverCpsCount = 0;
        }

        data.serverCpsCount++;
        long elapsed = now - data.serverCpsWindowStart;

        // Проверяем каждую секунду
        if (elapsed >= 1000) {
            double cps = data.serverCpsCount * 1000.0 / elapsed;
            int max = config.getMaxCps();
            if (cps > max) {
                punishment.warn(player,
                    "AutoClicker/Server (CPS: " + String.format("%.1f", cps) + " > " + max + ")"
                );
            }
            // Сброс окна
            data.serverCpsWindowStart = now;
            data.serverCpsCount = 0;
        }
    }
}

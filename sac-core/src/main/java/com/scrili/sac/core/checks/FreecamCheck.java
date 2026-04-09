package com.scrili.sac.core.checks;

import com.scrili.sac.core.api.ISACConfig;
import com.scrili.sac.core.api.ISACPlayer;
import com.scrili.sac.core.punishment.PunishmentManager;

/**
 * FreecamCheck — детектирует взаимодействие с блоками/сущностями
 * на расстоянии больше допустимого.
 *
 * Freecam позволяет камере летать отдельно от тела, и игрок может
 * кликать на блоки/сундуки/NPC далеко от своего реального положения.
 */
public class FreecamCheck {

    private final ISACConfig config;
    private final PunishmentManager punishment;

    public FreecamCheck(ISACConfig config, PunishmentManager punishment) {
        this.config = config;
        this.punishment = punishment;
    }

    /**
     * Вызывается при взаимодействии игрока с блоком.
     * @param blockX/Y/Z координаты блока
     */
    public void onBlockInteract(ISACPlayer player, double blockX, double blockY, double blockZ) {
        if (player.isCreativeOrSpec()) return;

        double dx = player.getX() - blockX;
        double dy = player.getY() - blockY;
        double dz = player.getZ() - blockZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        double maxDist = config.getMaxInteractDistance();
        if (dist > maxDist) {
            punishment.warn(player,
                "Freecam/Interact (дистанция: " + String.format("%.1f", dist)
                + " > " + maxDist + " блоков)"
            );
        }
    }

    /**
     * Вызывается при размещении блока.
     */
    public void onBlockPlace(ISACPlayer player, double blockX, double blockY, double blockZ) {
        if (player.isCreativeOrSpec()) return;

        double dx = player.getX() - blockX;
        double dy = player.getY() - blockY;
        double dz = player.getZ() - blockZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Для размещения блока допуск чуть меньше
        double maxDist = config.getMaxInteractDistance();
        if (dist > maxDist) {
            punishment.warn(player,
                "Freecam/Place (дистанция: " + String.format("%.1f", dist)
                + " > " + maxDist + " блоков)"
            );
        }
    }
}

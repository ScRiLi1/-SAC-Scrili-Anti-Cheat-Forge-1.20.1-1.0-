package com.scrili.sac.bukkit;

import com.scrili.sac.bukkit.adapter.BukkitConfig;
import com.scrili.sac.bukkit.adapter.BukkitPlayer;
import com.scrili.sac.bukkit.antiesp.AntiESP;
import com.scrili.sac.core.SACCore;
import com.scrili.sac.core.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class SACBukkit extends JavaPlugin implements Listener {

    private SACCore core;
    private AntiESP antiESP;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        core = new SACCore(new BukkitConfig(getConfig()));
        antiESP = new AntiESP(this);
        getServer().getPluginManager().registerEvents(this, this);

        // тик-проверки через Bukkit scheduler (каждый тик)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : getServer().getOnlinePlayers()) {
                    BukkitPlayer bp = new BukkitPlayer(player);
                    core.getSpeedCheck().check(bp);
                    core.getFlyCheck().check(bp);
                }
                // AntiESP каждые 20 тиков
                if (getServer().getCurrentTick() % 20 == 0) {
                    antiESP.tick(getServer().getOnlinePlayers());
                }
            }
        }.runTaskTimer(this, 0L, 1L);

        getLogger().info("SAC (Bukkit) включён.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SAC (Bukkit) выключен.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        antiESP.onPlayerQuit(event.getPlayer());
        PlayerData.remove(event.getPlayer().getUniqueId());
    }
}

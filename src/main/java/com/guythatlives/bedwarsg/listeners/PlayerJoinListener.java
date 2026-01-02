package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final BedwarsG plugin;

    public PlayerJoinListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player stats
        plugin.getStatsManager().getStats(event.getPlayer());
    }
}

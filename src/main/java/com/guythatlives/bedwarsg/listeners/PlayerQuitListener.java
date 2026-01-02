package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final BedwarsG plugin;

    public PlayerQuitListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove from arena if in one
        Arena arena = plugin.getArenaManager().getPlayerArena(player);
        if (arena != null) {
            arena.removePlayer(player);
        }

        // Save and unload stats
        plugin.getStatsManager().unloadStats(player);
    }
}

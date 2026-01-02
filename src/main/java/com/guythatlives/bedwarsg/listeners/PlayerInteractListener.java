package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractListener implements Listener {

    private final BedwarsG plugin;

    public PlayerInteractListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        if (arena == null || arena.getState() != ArenaState.RUNNING) {
            return;
        }

        // Open shop when clicking villager
        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            event.setCancelled(true);
            plugin.getShopManager().openMainShop(player);
        }
    }
}

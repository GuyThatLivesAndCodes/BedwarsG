package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class ShopInteractListener implements Listener {

    private final BedwarsG plugin;

    public ShopInteractListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.VILLAGER) {
            return;
        }

        Player player = event.getPlayer();
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        if (arena == null || arena.getState() != ArenaState.RUNNING) {
            return;
        }

        Villager villager = (Villager) event.getRightClicked();
        String name = villager.getCustomName();

        if (name == null) {
            return;
        }

        event.setCancelled(true); // Prevent default villager GUI

        // Open appropriate shop based on villager name
        if (name.contains("ITEM SHOP")) {
            plugin.getShopManager().openShop(player, "items");
        } else if (name.contains("UPGRADE SHOP")) {
            plugin.getShopManager().openShop(player, "upgrades");
        }
    }
}

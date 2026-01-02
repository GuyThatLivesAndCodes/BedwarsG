package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private final BedwarsG plugin;

    public InventoryClickListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getInventory().getTitle();

        // Handle shop clicks
        if (title.contains("Shop")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) {
                return;
            }

            // Check if it's a category or item
            String itemName = event.getCurrentItem().getItemMeta().getDisplayName();

            if (title.equals("Item Shop")) {
                // Category selection
                if (itemName.contains("Blocks")) {
                    plugin.getShopManager().openShop(player, "blocks");
                } else if (itemName.contains("Weapons")) {
                    plugin.getShopManager().openShop(player, "weapons");
                } else if (itemName.contains("Armor")) {
                    plugin.getShopManager().openShop(player, "armor");
                } else if (itemName.contains("Tools")) {
                    plugin.getShopManager().openShop(player, "tools");
                } else if (itemName.contains("Food")) {
                    plugin.getShopManager().openShop(player, "food");
                } else if (itemName.contains("Special")) {
                    plugin.getShopManager().openShop(player, "special");
                }
            }
        }
    }
}

package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.GameMode;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIClickListener implements Listener {

    private final BedwarsG plugin;

    public GUIClickListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (title == null) return;

        // Check if it's a Bedwars GUI
        if (!title.contains("Bedwars") && !title.contains("Admin") && !title.contains("§")) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String itemName = clicked.getItemMeta().getDisplayName();
        if (itemName == null) return;

        event.setCancelled(true); // Cancel all GUI clicks

        // Player GUI handling
        if (title.contains("§8§lBedwars Menu")) {
            handlePlayerMainMenu(player, itemName);
        } else if (title.contains("Your Statistics")) {
            handleStatsMenu(player, itemName);
        } else if (title.contains("Games")) {
            handleGameModeMenu(player, itemName, title);
        }

        // Admin GUI handling
        else if (title.contains("§c§lBedwarsG Admin Panel")) {
            handleAdminMainMenu(player, itemName);
        } else if (title.contains("§e§lMap Management")) {
            handleMapManagementMenu(player, itemName, event);
        } else if (title.contains("§e§lEdit:")) {
            handleMapEditMenu(player, itemName, title);
        } else if (title.contains("§a§lRunning Games")) {
            handleRunningGamesMenu(player, itemName, event);
        }
    }

    private void handlePlayerMainMenu(Player player, String itemName) {
        if (itemName.contains("Solo Mode")) {
            plugin.getPlayerGUI().openGameModeMenu(player, GameMode.SOLO);
        } else if (itemName.contains("Doubles Mode")) {
            plugin.getPlayerGUI().openGameModeMenu(player, GameMode.DOUBLES);
        } else if (itemName.contains("3v3 Mode")) {
            plugin.getPlayerGUI().openGameModeMenu(player, GameMode.TRIO);
        } else if (itemName.contains("4v4 Mode")) {
            plugin.getPlayerGUI().openGameModeMenu(player, GameMode.QUAD);
        } else if (itemName.contains("1v1 Duels")) {
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§eUse /bw1v1 <player> to challenge someone!");
        } else if (itemName.contains("Your Stats")) {
            plugin.getPlayerGUI().openStatsMenu(player);
        }
    }

    private void handleStatsMenu(Player player, String itemName) {
        if (itemName.contains("Back")) {
            plugin.getPlayerGUI().openMainMenu(player);
        }
    }

    private void handleGameModeMenu(Player player, String itemName, String title) {
        if (itemName.contains("Back")) {
            plugin.getPlayerGUI().openMainMenu(player);
            return;
        }

        // Check if player is already in a game
        if (plugin.getArenaManager().isInArena(player)) {
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getMessage("game.already-in-game"));
            return;
        }

        // Try to find and join the arena
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (itemName.contains(arena.getName())) {
                player.closeInventory();
                if (!arena.isFull()) {
                    arena.addPlayer(player);
                    player.sendMessage(plugin.getConfigManager().getMessage("game.joined"));
                    if (arena.canStart()) {
                        plugin.getGameManager().startGame(arena);
                    }
                } else {
                    player.sendMessage(plugin.getConfigManager().getMessage("game.full"));
                }
                return;
            }
        }
    }

    private void handleAdminMainMenu(Player player, String itemName) {
        if (itemName.contains("Map Management")) {
            plugin.getAdminGUI().openMapManagementMenu(player);
        } else if (itemName.contains("Running Games")) {
            plugin.getAdminGUI().openRunningGamesMenu(player);
        } else if (itemName.contains("Arena Management")) {
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§eUse /bwadmin createarena <name> <map> <mode>");
        } else if (itemName.contains("Settings") || itemName.contains("Quick Actions")) {
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§eUse /bwadmin for command list");
        }
    }

    private void handleMapManagementMenu(Player player, String itemName, InventoryClickEvent event) {
        if (itemName.contains("Back")) {
            plugin.getAdminGUI().openMainMenu(player);
            return;
        }

        if (itemName.contains("Create New Map")) {
            player.closeInventory();
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§eUse /bwadmin createmap <name>");
            return;
        }

        // Find and edit the map
        for (String mapName : plugin.getMapManager().getMaps().keySet()) {
            BedwarsMap map = plugin.getMapManager().getMap(mapName);
            if (itemName.contains(map.getDisplayName()) || itemName.contains(mapName)) {
                if (event.isLeftClick()) {
                    plugin.getAdminGUI().openMapEditMenu(player, mapName);
                } else if (event.isRightClick()) {
                    map.setEnabled(!map.isEnabled());
                    plugin.getMapManager().saveMap(map);
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§aMap " +
                        (map.isEnabled() ? "enabled" : "disabled") + ": §e" + mapName);
                    plugin.getAdminGUI().openMapManagementMenu(player); // Refresh
                }
                return;
            }
        }
    }

    private void handleMapEditMenu(Player player, String itemName, String title) {
        // Extract map name from title "§e§lEdit: mapname"
        String mapName = title.replace("§e§lEdit: ", "").trim();

        if (itemName.contains("Back")) {
            plugin.getAdminGUI().openMapManagementMenu(player);
            return;
        }

        player.closeInventory();

        if (itemName.contains("Visual Editor")) {
            if (plugin.getMapVisualizer().isInEditMode(player)) {
                plugin.getMapVisualizer().disableEditMode(player);
            } else {
                plugin.getMapVisualizer().enableEditMode(player, mapName);
            }
        } else if (itemName.contains("Enable Map") || itemName.contains("Disable Map")) {
            BedwarsMap map = plugin.getMapManager().getMap(mapName);
            if (map != null) {
                map.setEnabled(!map.isEnabled());
                plugin.getMapManager().saveMap(map);
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§aMap " +
                    (map.isEnabled() ? "enabled" : "disabled"));
            }
        } else if (itemName.contains("Spawns")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§e/bwadmin setspawn " + mapName + " <team>");
        } else if (itemName.contains("Beds")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§e/bwadmin setbed " + mapName + " <team>");
        } else if (itemName.contains("Generators")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§e/bwadmin addgen " + mapName + " <type>");
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§e/bwadmin listgen " + mapName);
        } else if (itemName.contains("Shops")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§e/bwadmin setshop " + mapName + " <team>");
        } else if (itemName.contains("Save Map World")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§e/bwadmin savemapworld " + mapName);
        } else if (itemName.contains("Delete Map")) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§e/bwadmin deletemap " + mapName);
        }
    }

    private void handleRunningGamesMenu(Player player, String itemName, InventoryClickEvent event) {
        if (itemName.contains("Back")) {
            plugin.getAdminGUI().openMainMenu(player);
            return;
        }

        if (itemName.contains("No games running")) {
            return;
        }

        // Find the arena
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (itemName.contains(arena.getName())) {
                player.closeInventory();

                if (event.isLeftClick()) {
                    // Teleport to game
                    String worldName = arena.getGameWorldName();
                    if (worldName != null) {
                        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
                        if (world != null) {
                            player.teleport(world.getSpawnLocation());
                            player.sendMessage(plugin.getConfigManager().getPrefix() + "§aTeleported to: §e" + arena.getName());
                        }
                    }
                } else if (event.isRightClick()) {
                    // Force end game
                    plugin.getGameManager().endGame(arena, null);
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§cForce ended: §e" + arena.getName());
                }
                return;
            }
        }
    }
}

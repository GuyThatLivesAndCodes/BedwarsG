package com.guythatlives.bedwarsg.gui;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminGUI {

    private final BedwarsG plugin;

    public AdminGUI(BedwarsG plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§c§lBedwarsG Admin Panel");

        // Map Management
        inv.setItem(10, createItem(Material.FILLED_MAP, "§e§lMap Management",
            "§7Create, edit, and configure maps",
            "§e",
            "§eClick to manage maps!"));

        // Arena Management
        inv.setItem(11, createItem(Material.BEACON, "§b§lArena Management",
            "§7Create and manage arenas",
            "§e",
            "§eClick to manage arenas!"));

        // Running Games
        inv.setItem(12, createItem(Material.COMPASS, "§a§lRunning Games",
            "§7View and manage active games",
            "§e",
            "§eClick to view games!"));

        // Plugin Settings
        inv.setItem(13, createItem(Material.REDSTONE_TORCH, "§c§lSettings",
            "§7Configure plugin settings",
            "§e",
            "§eClick to configure!"));

        // Quick Actions
        inv.setItem(16, createItem(Material.COMMAND_BLOCK, "§6§lQuick Actions",
            "§7Fast access to common commands",
            "§e",
            "§eClick for quick actions!"));

        player.openInventory(inv);
    }

    public void openMapManagementMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§e§lMap Management");

        int slot = 0;
        for (Map.Entry<String, BedwarsMap> entry : plugin.getMapManager().getMaps().entrySet()) {
            if (slot >= 45) break;

            BedwarsMap map = entry.getValue();
            List<String> lore = new ArrayList<>();
            lore.add("§7World: §e" + map.getWorld());
            lore.add("§7Players: §e" + map.getMinPlayers() + "-" + map.getMaxPlayers());
            lore.add("§7Spawns: §e" + map.getSpawns().size());
            lore.add("§7Beds: §e" + map.getBeds().size());
            lore.add("§7Generators: §e" + map.getGenerators().size());
            lore.add("§7Shops: §e" + map.getShops().size());
            lore.add("§7Status: " + (map.isEnabled() ? "§a§lENABLED" : "§c§lDISABLED"));
            lore.add("§e");
            lore.add("§eLeft click: Edit map");
            lore.add("§eRight click: Toggle enable/disable");

            Material icon = map.isEnabled() ? Material.LIME_WOOL : Material.RED_WOOL;
            inv.setItem(slot++, createItem(icon, "§a" + map.getDisplayName(), lore));
        }

        // Create New Map
        inv.setItem(49, createItem(Material.WRITABLE_BOOK, "§a§lCreate New Map",
            "§7Click to create a new map",
            "§7(Use command: /bwadmin createmap <name>)"));

        // Back button
        inv.setItem(45, createItem(Material.ARROW, "§c§lBack", "§7Return to main menu"));

        player.openInventory(inv);
    }

    public void openMapEditMenu(Player player, String mapName) {
        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) return;

        Inventory inv = Bukkit.createInventory(null, 54, "§e§lEdit: " + mapName);

        // Spawns
        inv.setItem(10, createItem(Material.ENDER_PEARL, "§a§lSpawns (" + map.getSpawns().size() + ")",
            "§7Set team spawn points",
            "§e",
            "§e/bwadmin setspawn " + mapName + " <team>"));

        // Beds
        inv.setItem(11, createItem(Material.RED_BED, "§c§lBeds (" + map.getBeds().size() + ")",
            "§7Set team bed locations",
            "§e",
            "§e/bwadmin setbed " + mapName + " <team>"));

        // Generators
        inv.setItem(12, createItem(Material.IRON_INGOT, "§6§lGenerators (" + map.getGenerators().size() + ")",
            "§7Add/remove resource generators",
            "§e",
            "§e/bwadmin addgen " + mapName + " <type>",
            "§e/bwadmin listgen " + mapName,
            "§e/bwadmin delgen " + mapName + " <id>"));

        // Shops
        inv.setItem(13, createItem(Material.EMERALD, "§b§lShops (" + map.getShops().size() + ")",
            "§7Set team shop locations",
            "§e",
            "§e/bwadmin setshop " + mapName + " <team>"));

        // Visual Editor
        inv.setItem(14, createItem(Material.ENDER_EYE, "§d§lVisual Editor",
            "§7Show particles for all elements",
            "§e",
            "§eClick to toggle visual editor!"));

        // Save World
        inv.setItem(16, createItem(Material.CHEST, "§e§lSave Map World",
            "§7Save current world as this map",
            "§e",
            "§e/bwadmin savemapworld " + mapName));

        // Enable/Disable
        Material toggleIcon = map.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE;
        String toggleText = map.isEnabled() ? "§c§lDisable Map" : "§a§lEnable Map";
        inv.setItem(22, createItem(toggleIcon, toggleText,
            "§7Current: " + (map.isEnabled() ? "§aEnabled" : "§cDisabled"),
            "§e",
            "§eClick to toggle!"));

        // Delete Map
        inv.setItem(26, createItem(Material.BARRIER, "§c§l§nDelete Map",
            "§7§lWARNING: This cannot be undone!",
            "§e",
            "§e/bwadmin deletemap " + mapName));

        // Back button
        inv.setItem(45, createItem(Material.ARROW, "§c§lBack", "§7Return to map list"));

        player.openInventory(inv);
    }

    public void openRunningGamesMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§a§lRunning Games");

        int slot = 0;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getState() == ArenaState.RUNNING && slot < 45) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Map: §e" + arena.getMap().getDisplayName());
                lore.add("§7Mode: §e" + arena.getGameMode().getDisplayName());
                lore.add("§7Players: §e" + arena.getPlayers().size());
                lore.add("§7World: §e" + arena.getGameWorldName());
                lore.add("§7Time: §e" + formatTime(arena.getGameTimer()));
                lore.add("§e");
                lore.add("§eLeft click: Teleport to game");
                lore.add("§eRight click: Force end game");

                inv.setItem(slot++, createItem(Material.GRASS_BLOCK, "§a" + arena.getName(), lore));
            }
        }

        if (slot == 0) {
            inv.setItem(22, createItem(Material.BARRIER, "§7§lNo games running",
                "§7There are currently no active games"));
        }

        // Back button
        inv.setItem(49, createItem(Material.ARROW, "§c§lBack", "§7Return to main menu"));

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }
}

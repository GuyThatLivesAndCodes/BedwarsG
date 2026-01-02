package com.guythatlives.bedwarsg.gui;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.GameMode;
import com.guythatlives.bedwarsg.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PlayerGUI {

    private final BedwarsG plugin;

    public PlayerGUI(BedwarsG plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lBedwars Menu");

        // Solo (1v1v1v1)
        inv.setItem(10, createItem(Material.RED_BED, "§c§lSolo Mode",
            "§71v1v1v1 - One player per team",
            "§e",
            "§eClick to find a game!"));

        // Doubles (2v2v2v2)
        inv.setItem(11, createItem(Material.ORANGE_BED, "§6§lDoubles Mode",
            "§72v2v2v2 - Two players per team",
            "§e",
            "§eClick to find a game!"));

        // 3v3v3v3
        inv.setItem(12, createItem(Material.YELLOW_BED, "§e§l3v3 Mode",
            "§73v3v3v3 - Three players per team",
            "§e",
            "§eClick to find a game!"));

        // 4v4v4v4
        inv.setItem(13, createItem(Material.GREEN_BED, "§a§l4v4 Mode",
            "§74v4v4v4 - Four players per team",
            "§e",
            "§eClick to find a game!"));

        // 1v1 Duels
        inv.setItem(14, createItem(Material.DIAMOND_SWORD, "§b§l1v1 Duels",
            "§7Challenge another player",
            "§e",
            "§eClick to view duel menu!"));

        // Party Info
        inv.setItem(19, createItem(Material.PLAYER_HEAD, "§d§lParty",
            "§7Manage your Bedwars party",
            "§e",
            "§eClick to manage!"));

        // Stats
        inv.setItem(22, createItem(Material.PAPER, "§e§lYour Stats",
            "§7View your statistics",
            "§e",
            "§eClick to view!"));

        // Running Games
        inv.setItem(25, createItem(Material.COMPASS, "§a§lRunning Games",
            "§7Spectate ongoing matches",
            "§e",
            "§eClick to browse!"));

        player.openInventory(inv);
    }

    public void openGameModeMenu(Player player, GameMode mode) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§l" + mode.getDisplayName() + " Games");

        int slot = 0;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getGameMode() == mode && slot < 54) {
                List<String> lore = new ArrayList<>();
                lore.add("§7Map: §e" + arena.getMap().getDisplayName());
                lore.add("§7Players: §e" + arena.getPlayers().size() + "/" + arena.getMap().getMaxPlayers());
                lore.add("§7Status: " + getStatusColor(arena) + arena.getState().name());
                lore.add("§e");
                lore.add("§eClick to join!");

                inv.setItem(slot++, createItem(Material.MAP, "§a" + arena.getName(), lore));
            }
        }

        // Back button
        inv.setItem(49, createItem(Material.ARROW, "§c§lBack", "§7Return to main menu"));

        player.openInventory(inv);
    }

    public void openStatsMenu(Player player) {
        PlayerStats stats = plugin.getStatsManager().getStats(player);
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lYour Statistics");

        // Wins
        inv.setItem(10, createItem(Material.EMERALD, "§a§lWins",
            "§7Total: §e" + stats.getWins()));

        // Losses
        inv.setItem(11, createItem(Material.REDSTONE, "§c§lLosses",
            "§7Total: §e" + stats.getLosses()));

        // Kills
        inv.setItem(12, createItem(Material.DIAMOND_SWORD, "§e§lKills",
            "§7Total: §e" + stats.getKills()));

        // Deaths
        inv.setItem(13, createItem(Material.SKELETON_SKULL, "§7§lDeaths",
            "§7Total: §e" + stats.getDeaths()));

        // Final Kills
        inv.setItem(14, createItem(Material.NETHER_STAR, "§6§lFinal Kills",
            "§7Total: §e" + stats.getFinalKills()));

        // Beds Broken
        inv.setItem(15, createItem(Material.RED_BED, "§c§lBeds Broken",
            "§7Total: §e" + stats.getBedsDestroyed()));

        // Games Played
        inv.setItem(16, createItem(Material.BOOK, "§b§lGames Played",
            "§7Total: §e" + (stats.getWins() + stats.getLosses())));

        // Back button
        inv.setItem(22, createItem(Material.ARROW, "§c§lBack", "§7Return to main menu"));

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

    private String getStatusColor(Arena arena) {
        switch (arena.getState()) {
            case WAITING:
                return "§a";
            case STARTING:
                return "§e";
            case RUNNING:
                return "§c";
            case ENDING:
                return "§7";
            default:
                return "§7";
        }
    }
}

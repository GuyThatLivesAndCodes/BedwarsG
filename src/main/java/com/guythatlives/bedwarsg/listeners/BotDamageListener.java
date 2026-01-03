package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import com.guythatlives.bedwarsg.bot.BotPlayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class BotDamageListener implements Listener {

    private final BedwarsG plugin;

    public BotDamageListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBotDamage(EntityDamageByEntityEvent event) {
        Entity damaged = event.getEntity();

        // Check if an armor stand was damaged
        if (!(damaged instanceof ArmorStand)) {
            return;
        }

        ArmorStand armorStand = (ArmorStand) damaged;

        // Check if damager is a player
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        Player attacker = (Player) event.getDamager();
        Arena arena = plugin.getArenaManager().getPlayerArena(attacker);

        // Must be in running game
        if (arena == null || arena.getState() != ArenaState.RUNNING) {
            return;
        }

        // Find the bot this armor stand belongs to
        BotPlayer bot = null;
        for (BotPlayer b : plugin.getBotManager().getActiveBots()) {
            if (b.getArmorStand() != null && b.getArmorStand().equals(armorStand)) {
                bot = b;
                break;
            }
        }

        if (bot == null) {
            return; // Not a bot armor stand
        }

        // Kill the bot
        plugin.getLogger().info("Bot " + bot.getName() + " was killed by " + attacker.getName());

        // Drop bot's inventory items at death location
        dropBotInventory(bot, armorStand.getLocation());

        // Broadcast death message to arena players
        String deathMessage = plugin.getConfigManager().getPrefix() +
                             "§c" + bot.getName() + " §7was killed by §c" + attacker.getName();
        for (Player p : arena.getPlayers()) {
            p.sendMessage(deathMessage);
        }

        // Remove the bot
        plugin.getBotManager().removeBot(bot.getUUID());

        // Give kill credit to attacker
        if (plugin.getGameManager().getGame(arena) != null) {
            plugin.getGameManager().getGame(arena).addKill(attacker);
        }

        // Cancel the event to prevent default armor stand damage behavior
        event.setCancelled(true);
    }

    /**
     * Drop all items from the bot's inventory at the death location
     */
    private void dropBotInventory(BotPlayer bot, Location deathLocation) {
        Map<Material, Integer> inventory = bot.getInventory();

        if (inventory.isEmpty()) {
            return;
        }

        // Drop each item type
        for (Map.Entry<Material, Integer> entry : inventory.entrySet()) {
            Material material = entry.getKey();
            int amount = entry.getValue();

            if (amount <= 0) {
                continue;
            }

            // Create item stacks (split into max stack sizes if necessary)
            int maxStackSize = material.getMaxStackSize();
            while (amount > 0) {
                int stackAmount = Math.min(amount, maxStackSize);
                ItemStack itemStack = new ItemStack(material, stackAmount);

                // Drop the item at the death location
                if (deathLocation.getWorld() != null) {
                    deathLocation.getWorld().dropItemNaturally(deathLocation, itemStack);
                }

                amount -= stackAmount;
            }
        }

        plugin.getLogger().info("Dropped " + inventory.size() + " item types from bot " + bot.getName());
    }
}

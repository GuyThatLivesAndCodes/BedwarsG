package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import com.guythatlives.bedwarsg.bot.BotPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

        // Kill the bot (remove armor stand)
        plugin.getLogger().info("Bot " + bot.getName() + " was killed by " + attacker.getName());

        // Remove the bot
        plugin.getBotManager().removeBot(bot.getUUID());

        // Give kill credit to attacker
        if (plugin.getGameManager().getGame(arena) != null) {
            plugin.getGameManager().getGame(arena).addKill(attacker);
        }

        // Cancel the event to prevent default armor stand damage behavior
        event.setCancelled(true);
    }
}

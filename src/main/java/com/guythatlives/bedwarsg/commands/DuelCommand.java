package com.guythatlives.bedwarsg.commands;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.GameMode;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuelCommand implements CommandExecutor {

    private final BedwarsG plugin;
    private Map<UUID, Map<UUID, Long>> duelRequests;

    public DuelCommand(BedwarsG plugin) {
        this.plugin = plugin;
        this.duelRequests = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwarsg.play.1v1")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bw1v1 <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("duel.cannot-duel-self"));
            return true;
        }

        if (plugin.getArenaManager().isInArena(player) || plugin.getArenaManager().isInArena(target)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cOne of you is already in a game!");
            return true;
        }

        // Check if target has sent a request to player (accepting)
        if (hasRequest(player, target)) {
            acceptDuel(player, target);
            return true;
        }

        // Send new request
        if (hasRequest(target, player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("duel.already-sent"));
            return true;
        }

        sendRequest(player, target);
        return true;
    }

    private void sendRequest(Player from, Player to) {
        duelRequests.computeIfAbsent(to.getUniqueId(), k -> new HashMap<>())
                .put(from.getUniqueId(), System.currentTimeMillis());

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", to.getName());
        from.sendMessage(plugin.getConfigManager().getMessage("duel.sent", placeholders));

        placeholders.put("player", from.getName());
        to.sendMessage(plugin.getConfigManager().getMessage("duel.received", placeholders));
    }

    private boolean hasRequest(Player to, Player from) {
        Map<UUID, Long> requests = duelRequests.get(to.getUniqueId());
        if (requests == null) {
            return false;
        }

        Long requestTime = requests.get(from.getUniqueId());
        if (requestTime == null) {
            return false;
        }

        // Requests expire after 60 seconds
        if (System.currentTimeMillis() - requestTime > 60000) {
            requests.remove(from.getUniqueId());
            return false;
        }

        return true;
    }

    private void acceptDuel(Player player1, Player player2) {
        // Remove requests
        Map<UUID, Long> requests = duelRequests.get(player1.getUniqueId());
        if (requests != null) {
            requests.remove(player2.getUniqueId());
        }

        player1.sendMessage(plugin.getConfigManager().getMessage("duel.accepted"));
        player2.sendMessage(plugin.getConfigManager().getMessage("duel.accepted"));

        // Find 1v1 arena
        Arena arena = plugin.getArenaManager().findAvailableArena(GameMode.DUEL);
        if (arena == null) {
            player1.sendMessage(plugin.getConfigManager().getPrefix() + "§cNo 1v1 arenas available!");
            player2.sendMessage(plugin.getConfigManager().getPrefix() + "§cNo 1v1 arenas available!");
            return;
        }

        // Add players to arena
        arena.addPlayer(player1);
        arena.addPlayer(player2);

        // Start game
        plugin.getGameManager().startGame(arena);
    }
}

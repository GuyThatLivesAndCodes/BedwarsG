package com.guythatlives.bedwarsg.commands;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BedwarsCommand implements CommandExecutor {

    private final BedwarsG plugin;

    public BedwarsCommand(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            // Open GUI menu
            plugin.getPlayerGUI().openMainMenu(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "list":
                handleList(player);
                break;
            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void handleJoin(Player player, String[] args) {
        if (plugin.getArenaManager().isInArena(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("game.already-in-game"));
            return;
        }

        GameMode gameMode = GameMode.SOLO;
        if (args.length >= 2) {
            gameMode = GameMode.fromString(args[1]);
            if (gameMode == null) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cInvalid game mode!");
                return;
            }
        }

        if (!player.hasPermission(gameMode.getPermission())) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return;
        }

        Arena arena = plugin.getArenaManager().findAvailableArena(gameMode);
        if (arena == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cNo available arenas!");
            return;
        }

        if (arena.isFull()) {
            player.sendMessage(plugin.getConfigManager().getMessage("game.full"));
            return;
        }

        arena.addPlayer(player);
        player.sendMessage(plugin.getConfigManager().getMessage("game.joined"));

        if (arena.canStart() && !arena.getPlayers().isEmpty()) {
            plugin.getGameManager().startGame(arena);
        }
    }

    private void handleLeave(Player player) {
        Arena arena = plugin.getArenaManager().getPlayerArena(player);
        if (arena == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("game.not-in-game"));
            return;
        }

        arena.removePlayer(player);
        player.sendMessage(plugin.getConfigManager().getMessage("game.left"));
    }

    private void handleList(Player player) {
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aAvailable Arenas:");
        int count = 0;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            player.sendMessage("  §7- §e" + arena.getName() + " §7(" + arena.getPlayers().size() +
                             "/" + arena.getMap().getMaxPlayers() + ") §7- §a" + arena.getState());
            count++;
        }

        if (count == 0) {
            player.sendMessage("  §7No arenas available");
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m----------§r §cBedwarsG §8§m----------");
        player.sendMessage("§e/bw join [mode] §7- Join a game");
        player.sendMessage("§e/bw leave §7- Leave current game");
        player.sendMessage("§e/bw list §7- List available arenas");
        player.sendMessage("§e/bp §7- Party commands");
        player.sendMessage("§e/bw1v1 <player> §7- Challenge to 1v1");
        player.sendMessage("§e/bwstats [player] §7- View stats");
    }
}

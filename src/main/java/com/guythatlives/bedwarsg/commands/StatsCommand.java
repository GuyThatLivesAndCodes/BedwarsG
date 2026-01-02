package com.guythatlives.bedwarsg.commands;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.stats.PlayerStats;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class StatsCommand implements CommandExecutor {

    private final BedwarsG plugin;
    private final DecimalFormat df = new DecimalFormat("#.##");

    public StatsCommand(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwarsg.stats")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        Player target = player;
        if (args.length > 0) {
            if (!player.hasPermission("bedwarsg.stats.others")) {
                player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                return true;
            }

            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                return true;
            }
        }

        displayStats(player, target);
        return true;
    }

    private void displayStats(Player viewer, Player target) {
        PlayerStats stats = plugin.getStatsManager().getStats(target);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", stats.getPlayerName());
        placeholders.put("kills", String.valueOf(stats.getKills()));
        placeholders.put("deaths", String.valueOf(stats.getDeaths()));
        placeholders.put("kdr", df.format(stats.getKDR()));
        placeholders.put("wins", String.valueOf(stats.getWins()));
        placeholders.put("losses", String.valueOf(stats.getLosses()));
        placeholders.put("wlr", df.format(stats.getWLR()));
        placeholders.put("beds", String.valueOf(stats.getBedsDestroyed()));
        placeholders.put("final-kills", String.valueOf(stats.getFinalKills()));
        placeholders.put("games", String.valueOf(stats.getGamesPlayed()));

        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.header"));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.player", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.kills", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.deaths", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.kdr", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.wins", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.losses", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.wlr", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.beds-broken", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.final-kills", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.games-played", placeholders));
        viewer.sendMessage(plugin.getConfigManager().getMessage("stats.footer"));
    }
}

package com.guythatlives.bedwarsg.commands;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PartyCommand implements CommandExecutor {

    private final BedwarsG plugin;

    public PartyCommand(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwarsg.party")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "create":
                handleCreate(player);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "join":
                handleJoin(player, args);
                break;
            case "leave":
                handleLeave(player);
                break;
            case "kick":
                handleKick(player, args);
                break;
            case "disband":
                handleDisband(player);
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

    private void handleCreate(Player player) {
        if (plugin.getPartyManager().isInParty(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.already-in-party"));
            return;
        }

        plugin.getPartyManager().createParty(player);
        player.sendMessage(plugin.getConfigManager().getMessage("party.created"));
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bp invite <player>");
            return;
        }

        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.not-in-party"));
            return;
        }

        if (!party.isLeader(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.not-leader"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.cannot-invite-self"));
            return;
        }

        if (party.isFull()) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.party-full"));
            return;
        }

        plugin.getPartyManager().sendInvite(player, target);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        player.sendMessage(plugin.getConfigManager().getMessage("party.invited", placeholders));

        placeholders.put("player", player.getName());
        target.sendMessage(plugin.getConfigManager().getMessage("party.invite-received", placeholders));
    }

    private void handleJoin(Player player, String[] args) {
        if (plugin.getPartyManager().isInParty(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.already-in-party"));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bp join <player>");
            return;
        }

        Player leader = Bukkit.getPlayer(args[1]);
        if (leader == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        if (!plugin.getPartyManager().hasInvite(player, leader)) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.no-pending-invite"));
            return;
        }

        Party party = plugin.getPartyManager().getParty(leader);
        if (party == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.no-pending-invite"));
            return;
        }

        if (party.isFull()) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.party-full"));
            return;
        }

        plugin.getPartyManager().addToParty(party, player);
        plugin.getPartyManager().removeInvite(player, leader);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        party.broadcast(plugin.getConfigManager().getMessage("party.joined", placeholders));
    }

    private void handleLeave(Player player) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.not-in-party"));
            return;
        }

        plugin.getPartyManager().removeFromParty(party, player);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        party.broadcast(plugin.getConfigManager().getMessage("party.left", placeholders));

        player.sendMessage(plugin.getConfigManager().getMessage("party.left", placeholders));
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bp kick <player>");
            return;
        }

        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.not-in-party"));
            return;
        }

        if (!party.isLeader(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.not-leader"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        if (!party.isMember(target)) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cThat player is not in your party!");
            return;
        }

        plugin.getPartyManager().removeFromParty(party, target);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", target.getName());
        party.broadcast(plugin.getConfigManager().getMessage("party.player-kicked", placeholders));

        target.sendMessage(plugin.getConfigManager().getMessage("party.kicked"));
    }

    private void handleDisband(Player player) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.not-in-party"));
            return;
        }

        if (!party.isLeader(player)) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.not-leader"));
            return;
        }

        party.broadcast(plugin.getConfigManager().getMessage("party.disbanded"));
        plugin.getPartyManager().disbandParty(party);
    }

    private void handleList(Player player) {
        Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("party.not-in-party"));
            return;
        }

        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aParty Members:");
        player.sendMessage("  §eLeader: §a" + party.getLeader().getName());
        player.sendMessage("  §eMembers:");
        for (Player member : party.getOnlineMembers()) {
            if (!party.isLeader(member)) {
                player.sendMessage("    §7- §a" + member.getName());
            }
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m----------§r §cBedwars Party §8§m----------");
        player.sendMessage("§e/bp create §7- Create a party");
        player.sendMessage("§e/bp invite <player> §7- Invite a player");
        player.sendMessage("§e/bp join <player> §7- Join a party");
        player.sendMessage("§e/bp leave §7- Leave party");
        player.sendMessage("§e/bp kick <player> §7- Kick a player");
        player.sendMessage("§e/bp disband §7- Disband party");
        player.sendMessage("§e/bp list §7- List party members");
    }
}

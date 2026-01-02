package com.guythatlives.bedwarsg.commands;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.GameMode;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class AdminCommand implements CommandExecutor {

    private final BedwarsG plugin;

    public AdminCommand(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("bedwarsg.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "createmap":
                handleCreateMap(player, args);
                break;
            case "deletemap":
                handleDeleteMap(player, args);
                break;
            case "setspawn":
                handleSetSpawn(player, args);
                break;
            case "setbed":
                handleSetBed(player, args);
                break;
            case "addgen":
                handleAddGenerator(player, args);
                break;
            case "enable":
                handleEnableMap(player, args);
                break;
            case "disable":
                handleDisableMap(player, args);
                break;
            case "createarena":
                handleCreateArena(player, args);
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

    private void handleCreateMap(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin createmap <name>");
            return;
        }

        String mapName = args[1];
        String worldName = player.getWorld().getName();

        BedwarsMap map = plugin.getMapManager().createMap(mapName, worldName);
        plugin.getMapManager().saveMap(map);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("arena", mapName);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.arena-created", placeholders));
    }

    private void handleDeleteMap(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin deletemap <name>");
            return;
        }

        String mapName = args[1];
        plugin.getMapManager().deleteMap(mapName);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("arena", mapName);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.arena-deleted", placeholders));
    }

    private void handleSetSpawn(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin setspawn <map> <team>");
            return;
        }

        String mapName = args[1];
        String team = args[2].toUpperCase();

        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        map.addSpawn(team, player.getLocation());
        plugin.getMapManager().saveMap(map);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("team", team);
        placeholders.put("arena", mapName);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.spawn-added", placeholders));
    }

    private void handleSetBed(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin setbed <map> <team>");
            return;
        }

        String mapName = args[1];
        String team = args[2].toUpperCase();

        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        map.addBed(team, player.getLocation());
        plugin.getMapManager().saveMap(map);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("team", team);
        placeholders.put("arena", mapName);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.bed-set", placeholders));
    }

    private void handleAddGenerator(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin addgen <map> <type>");
            return;
        }

        String mapName = args[1];
        String type = args[2].toUpperCase();

        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        map.addGenerator(type, player.getLocation());
        plugin.getMapManager().saveMap(map);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("arena", mapName);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.generator-added", placeholders));
    }

    private void handleEnableMap(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin enable <map>");
            return;
        }

        String mapName = args[1];
        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        map.setEnabled(true);
        plugin.getMapManager().saveMap(map);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("arena", mapName);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.arena-enabled", placeholders));
    }

    private void handleDisableMap(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin disable <map>");
            return;
        }

        String mapName = args[1];
        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        map.setEnabled(false);
        plugin.getMapManager().saveMap(map);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("arena", mapName);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.arena-disabled", placeholders));
    }

    private void handleCreateArena(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin createarena <name> <map> <gamemode>");
            return;
        }

        String arenaName = args[1];
        String mapName = args[2];
        GameMode gameMode = args.length >= 4 ? GameMode.fromString(args[3]) : GameMode.SOLO;

        if (gameMode == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cInvalid game mode!");
            return;
        }

        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        Arena arena = plugin.getArenaManager().createArena(arenaName, map, gameMode);
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aArena created: " + arenaName);
    }

    private void handleList(Player player) {
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aMaps:");
        for (String mapName : plugin.getMapManager().getMaps().keySet()) {
            BedwarsMap map = plugin.getMapManager().getMap(mapName);
            String status = map.isEnabled() ? "§aEnabled" : "§cDisabled";
            player.sendMessage("  §7- §e" + mapName + " " + status);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m----------§r §cBedwarsG Admin §8§m----------");
        player.sendMessage("§e/bwadmin createmap <name> §7- Create a new map");
        player.sendMessage("§e/bwadmin deletemap <name> §7- Delete a map");
        player.sendMessage("§e/bwadmin setspawn <map> <team> §7- Set team spawn");
        player.sendMessage("§e/bwadmin setbed <map> <team> §7- Set team bed");
        player.sendMessage("§e/bwadmin addgen <map> <type> §7- Add generator");
        player.sendMessage("§e/bwadmin enable <map> §7- Enable a map");
        player.sendMessage("§e/bwadmin disable <map> §7- Disable a map");
        player.sendMessage("§e/bwadmin createarena <name> <map> <mode> §7- Create arena");
        player.sendMessage("§e/bwadmin list §7- List all maps");
    }
}

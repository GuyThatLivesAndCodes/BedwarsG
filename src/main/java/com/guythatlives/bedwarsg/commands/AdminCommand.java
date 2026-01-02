package com.guythatlives.bedwarsg.commands;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
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
            // Open Admin GUI
            plugin.getAdminGUI().openMainMenu(player);
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
            case "savemapworld":
                handleSaveMapWorld(player, args);
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
            case "delgen":
                handleDeleteGenerator(player, args);
                break;
            case "listgen":
                handleListGenerators(player, args);
                break;
            case "genspeed":
                handleGeneratorSpeed(player, args);
                break;
            case "setshop":
                handleSetShop(player, args);
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
            case "games":
                handleGames(player);
                break;
            case "teleport":
            case "tp":
                handleTeleport(player, args);
                break;
            case "forceend":
                handleForceEnd(player, args);
                break;
            case "visualize":
            case "vis":
                handleVisualize(player, args);
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

        // Add generator with unique ID (type + current count)
        int count = 0;
        for (String key : map.getGenerators().keySet()) {
            if (key.startsWith(type)) {
                count++;
            }
        }
        String genId = type + "-" + count;
        map.addGenerator(genId, player.getLocation());
        plugin.getMapManager().saveMap(map);

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("arena", mapName);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.generator-added", placeholders));
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aGenerator ID: §e" + genId);
    }

    private void handleDeleteGenerator(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin delgen <map> <id>");
            return;
        }

        String mapName = args[1];
        String genId = args[2];

        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        if (map.getGenerators().containsKey(genId)) {
            map.getGenerators().remove(genId);
            plugin.getMapManager().saveMap(map);
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§aGenerator deleted: §e" + genId);
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cGenerator not found: §e" + genId);
        }
    }

    private void handleListGenerators(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin listgen <map>");
            return;
        }

        String mapName = args[1];
        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        player.sendMessage("§8§m----------§r §eGenerators: " + mapName + " §8§m----------");
        if (map.getGenerators().isEmpty()) {
            player.sendMessage("§7No generators configured");
        } else {
            for (Map.Entry<String, org.bukkit.Location> entry : map.getGenerators().entrySet()) {
                org.bukkit.Location loc = entry.getValue();
                player.sendMessage("§e" + entry.getKey() + " §7@ §f" +
                    (int)loc.getX() + ", " + (int)loc.getY() + ", " + (int)loc.getZ());
            }
        }
    }

    private void handleGeneratorSpeed(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin genspeed <map> [type] [ticks]");
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§7Examples:");
            player.sendMessage("§e  /bwadmin genspeed mapname §7- List all speeds");
            player.sendMessage("§e  /bwadmin genspeed mapname IRON §7- Get IRON speed");
            player.sendMessage("§e  /bwadmin genspeed mapname GOLD 40 §7- Set GOLD to 40 ticks (2 seconds)");
            return;
        }

        String mapName = args[1];
        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        // List all speeds
        if (args.length == 2) {
            player.sendMessage("§8§m----------§r §eGenerator Speeds: " + mapName + " §8§m----------");
            player.sendMessage("§7(20 ticks = 1 second)");
            for (Map.Entry<String, Integer> entry : map.getGeneratorSpeeds().entrySet()) {
                int ticks = entry.getValue();
                double seconds = ticks / 20.0;
                player.sendMessage("§e" + entry.getKey() + ": §f" + ticks + " ticks §7(" + seconds + "s)");
            }
            return;
        }

        String type = args[2].toUpperCase();

        // Get speed for specific type
        if (args.length == 3) {
            if (!map.getGeneratorSpeeds().containsKey(type)) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUnknown generator type: " + type);
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§7Valid types: IRON, GOLD, DIAMOND, EMERALD");
                return;
            }
            int ticks = map.getGeneratorSpeed(type);
            double seconds = ticks / 20.0;
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§e" + type + " §7speed: §f" + ticks + " ticks §7(" + seconds + "s)");
            return;
        }

        // Set speed for specific type
        if (args.length >= 4) {
            try {
                int ticks = Integer.parseInt(args[3]);
                if (ticks < 1) {
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§cSpeed must be at least 1 tick!");
                    return;
                }

                map.setGeneratorSpeed(type, ticks);
                plugin.getMapManager().saveMap(map);

                double seconds = ticks / 20.0;
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§aSet " + type + " generator speed to " + ticks + " ticks (" + seconds + "s)");
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cInvalid number: " + args[3]);
            }
        }
    }

    private void handleSetShop(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin setshop <map> <team>");
            return;
        }

        String mapName = args[1];
        String team = args[2].toUpperCase();

        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        map.addShop(team, player.getLocation());
        plugin.getMapManager().saveMap(map);

        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aShop set for team: §e" + team);
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

    private void handleSaveMapWorld(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin savemapworld <mapname>");
            return;
        }

        String mapName = args[1];
        String worldName = player.getWorld().getName();

        boolean success = plugin.getWorldManager().saveMapWorld(mapName, worldName);
        if (success) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§aMap world saved: " + mapName);
        } else {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cFailed to save map world!");
        }
    }

    private void handleGames(Player player) {
        player.sendMessage("§8§m----------§r §cRunning Games §8§m----------");

        int count = 0;
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getState() == ArenaState.RUNNING) {
                count++;
                player.sendMessage("§e" + arena.getName() + " §7- §a" + arena.getGameMode().getDisplayName());
                player.sendMessage("  §7Players: §e" + arena.getPlayers().size());
                player.sendMessage("  §7World: §e" + arena.getGameWorldName());
                player.sendMessage("  §7Time: §e" + formatTime(arena.getGameTimer()));
            }
        }

        if (count == 0) {
            player.sendMessage("§7No games currently running");
        }
    }

    private void handleTeleport(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin tp <arena>");
            return;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cArena not found!");
            return;
        }

        if (arena.getState() != ArenaState.RUNNING) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cThat arena is not running!");
            return;
        }

        String worldName = arena.getGameWorldName();
        if (worldName == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cGame world not found!");
            return;
        }

        org.bukkit.World world = org.bukkit.Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cGame world not loaded!");
            return;
        }

        org.bukkit.Location spawnLoc = world.getSpawnLocation();
        player.teleport(spawnLoc);
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aTeleported to arena: " + arenaName);
    }

    private void handleForceEnd(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin forceend <arena>");
            return;
        }

        String arenaName = args[1];
        Arena arena = plugin.getArenaManager().getArena(arenaName);

        if (arena == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cArena not found!");
            return;
        }

        if (arena.getState() != ArenaState.RUNNING) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cThat arena is not running!");
            return;
        }

        plugin.getGameManager().endGame(arena, null);
        player.sendMessage(plugin.getConfigManager().getMessage("admin.game-force-stopped"));
    }

    private void handleVisualize(Player player, String[] args) {
        if (args.length < 2) {
            // Toggle off if already enabled
            if (plugin.getMapVisualizer().isInEditMode(player)) {
                plugin.getMapVisualizer().disableEditMode(player);
            } else {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cUsage: /bwadmin vis <map>");
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§7Or use /bwadmin vis to disable");
            }
            return;
        }

        String mapName = args[1];
        BedwarsMap map = plugin.getMapManager().getMap(mapName);
        if (map == null) {
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cMap not found!");
            return;
        }

        plugin.getMapVisualizer().enableEditMode(player, mapName);
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    private void sendHelp(Player player) {
        player.sendMessage("§8§m----------§r §cBedwarsG Admin §8§m----------");
        player.sendMessage("§6Map Management:");
        player.sendMessage("§e/bwadmin createmap <name> §7- Create a new map");
        player.sendMessage("§e/bwadmin deletemap <name> §7- Delete a map");
        player.sendMessage("§e/bwadmin savemapworld <name> §7- Save current world as map");
        player.sendMessage("§e/bwadmin setspawn <map> <team> §7- Set team spawn");
        player.sendMessage("§e/bwadmin setbed <map> <team> §7- Set team bed");
        player.sendMessage("§e/bwadmin setshop <map> <team> §7- Set team shop");
        player.sendMessage("§e/bwadmin addgen <map> <type> §7- Add generator");
        player.sendMessage("§e/bwadmin delgen <map> <id> §7- Delete generator");
        player.sendMessage("§e/bwadmin listgen <map> §7- List generators");
        player.sendMessage("§e/bwadmin genspeed <map> [type] [ticks] §7- Get/Set generator speeds");
        player.sendMessage("§e/bwadmin vis <map> §7- Visual editor (particles)");
        player.sendMessage("§e/bwadmin enable <map> §7- Enable a map");
        player.sendMessage("§e/bwadmin disable <map> §7- Disable a map");
        player.sendMessage("§e/bwadmin list §7- List all maps");
        player.sendMessage("");
        player.sendMessage("§6Arena & Game Management:");
        player.sendMessage("§e/bwadmin createarena <name> <map> <mode> §7- Create arena");
        player.sendMessage("§e/bwadmin games §7- View running games");
        player.sendMessage("§e/bwadmin tp <arena> §7- Teleport to game");
        player.sendMessage("§e/bwadmin forceend <arena> §7- Force end a game");
    }
}

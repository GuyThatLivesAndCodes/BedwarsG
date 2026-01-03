package com.guythatlives.bedwarsg.game;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import com.guythatlives.bedwarsg.arena.GameMode;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class GameManager {

    private final BedwarsG plugin;
    private Map<String, Game> activeGames;

    public GameManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.activeGames = new HashMap<>();
    }

    public Game createGame(Arena arena) {
        Game game = new Game(arena, plugin);
        activeGames.put(arena.getName(), game);
        return game;
    }

    public void startGame(Arena arena) {
        if (arena.getState() != ArenaState.WAITING) {
            return;
        }

        arena.setState(ArenaState.STARTING);
        int countdownTime = plugin.getConfigManager().getInt("settings.countdown-time");
        arena.setCountdown(countdownTime);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (arena.getState() != ArenaState.STARTING) {
                    cancel();
                    return;
                }

                int countdown = arena.getCountdown();
                if (countdown <= 0) {
                    beginGame(arena);
                    cancel();
                    return;
                }

                if (countdown <= 10 || countdown % 10 == 0) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("time", String.valueOf(countdown));
                    String message = plugin.getConfigManager().getMessage("game.countdown", placeholders);

                    for (Player player : arena.getPlayers()) {
                        player.sendMessage(message);
                    }
                }

                arena.setCountdown(countdown - 1);
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void beginGame(Arena arena) {
        // Create temporary game world
        String worldName = plugin.getWorldManager().createGameWorld(arena.getMap().getName());
        if (worldName == null) {
            plugin.getLogger().severe("Failed to create game world for arena: " + arena.getName());
            for (Player player : arena.getPlayers()) {
                player.sendMessage(plugin.getConfigManager().getPrefix() + "§cFailed to start game! Contact an administrator.");
            }
            resetArena(arena);
            return;
        }

        arena.setGameWorldName(worldName);
        arena.setState(ArenaState.RUNNING);

        org.bukkit.World gameWorld = Bukkit.getWorld(worldName);

        // Create game instance (but don't start the game loop yet)
        Game game = createGame(arena);

        // Start generators, spawn shops, and spawn bots in the game world
        if (gameWorld != null) {
            plugin.getGeneratorManager().startGenerators(arena, gameWorld);
            plugin.getShopNPCManager().spawnShops(arena, gameWorld);

            // Spawn bot armor stands now that game world exists
            if (plugin.getBotManager() != null) {
                plugin.getBotManager().spawnBotsInGame(arena);
            }
        }

        String message = plugin.getConfigManager().getMessage("game.started");

        for (Player player : arena.getPlayers()) {
            player.sendMessage(message);
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);

            // Teleport to team spawn in game world
            BedwarsTeam team = arena.getPlayerTeam(player);
            if (team != null && gameWorld != null) {
                org.bukkit.Location originalSpawn = arena.getMap().getSpawn(team.getColor());
                if (originalSpawn != null) {
                    org.bukkit.Location gameSpawn = new org.bukkit.Location(
                        gameWorld,
                        originalSpawn.getX(),
                        originalSpawn.getY(),
                        originalSpawn.getZ(),
                        originalSpawn.getYaw(),
                        originalSpawn.getPitch()
                    );
                    player.teleport(gameSpawn);

                    // Grant respawn protection to prevent void death loop
                    if (plugin.getVoidDeathTask() != null) {
                        plugin.getVoidDeathTask().grantRespawnProtection(player);
                    }
                }
            }
        }

        // Start the game loop AFTER everything is set up (players, bots, generators)
        game.start();
    }

    public void endGame(Arena arena, BedwarsTeam winner) {
        Game game = activeGames.get(arena.getName());
        if (game != null) {
            game.end(winner);
            activeGames.remove(arena.getName());
        }

        arena.setState(ArenaState.ENDING);

        String message;
        if (winner != null) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("team", winner.getDisplayName());
            message = plugin.getConfigManager().getMessage("game.victory", placeholders);
        } else {
            message = plugin.getConfigManager().getMessage("game.ended");
        }

        for (Player player : arena.getPlayers()) {
            player.sendMessage(message);
        }

        // Schedule arena reset
        new BukkitRunnable() {
            @Override
            public void run() {
                resetArena(arena);
            }
        }.runTaskLater(plugin, 100L);
    }

    public void endAllGames() {
        for (Game game : new ArrayList<>(activeGames.values())) {
            endGame(game.getArena(), null);
        }
    }

    private void resetArena(Arena arena) {
        // Stop generators and remove shops
        plugin.getGeneratorManager().stopGenerators(arena);
        plugin.getShopNPCManager().removeShops(arena);

        // Remove all bots from the arena
        if (plugin.getBotManager() != null) {
            plugin.getBotManager().removeBotsFromArena(arena);
        }

        // Clear player-placed blocks tracking
        arena.getPlayerPlacedBlocks().clear();

        // Teleport players back to lobby
        for (Player player : new ArrayList<>(arena.getPlayers())) {
            arena.removePlayer(player);
            // Teleport to lobby spawn
            org.bukkit.Location lobbySpawn = getLobbySpawn();
            if (lobbySpawn != null) {
                player.teleport(lobbySpawn);
            }
            player.setGameMode(org.bukkit.GameMode.ADVENTURE);
        }

        // Delete game world
        String worldName = arena.getGameWorldName();
        if (worldName != null) {
            plugin.getWorldManager().deleteGameWorld(worldName);
            arena.setGameWorldName(null);
        }

        arena.setState(ArenaState.WAITING);
        arena.setCountdown(0);
    }

    private org.bukkit.Location getLobbySpawn() {
        String world = plugin.getConfigManager().getString("settings.lobby-spawn.world");
        double x = plugin.getConfigManager().getDouble("settings.lobby-spawn.x");
        double y = plugin.getConfigManager().getDouble("settings.lobby-spawn.y");
        double z = plugin.getConfigManager().getDouble("settings.lobby-spawn.z");
        float yaw = (float) plugin.getConfigManager().getDouble("settings.lobby-spawn.yaw");
        float pitch = (float) plugin.getConfigManager().getDouble("settings.lobby-spawn.pitch");

        org.bukkit.World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            return null;
        }

        return new org.bukkit.Location(bukkitWorld, x, y, z, yaw, pitch);
    }

    public Game getGame(Arena arena) {
        return activeGames.get(arena.getName());
    }

    public Collection<Game> getActiveGames() {
        return activeGames.values();
    }
}

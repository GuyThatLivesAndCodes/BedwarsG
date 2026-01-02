package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import com.guythatlives.bedwarsg.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class PlayerDeathListener implements Listener {

    private final BedwarsG plugin;

    public PlayerDeathListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        if (arena == null || arena.getState() != ArenaState.RUNNING) {
            return;
        }

        Player killer = player.getKiller();
        BedwarsTeam team = arena.getPlayerTeam(player);
        Game game = plugin.getGameManager().getGame(arena);

        if (game != null) {
            game.addDeath(player);
            if (killer != null) {
                game.addKill(killer);
            }
        }

        // Check if bed is alive
        boolean canRespawn = team != null && team.isBedAlive();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("victim", player.getName());
        String message;

        if (killer != null && killer != player) {
            placeholders.put("killer", killer.getName());
            if (canRespawn) {
                message = plugin.getConfigManager().getMessage("game.player-killed", placeholders);
            } else {
                message = plugin.getConfigManager().getMessage("game.final-kill", placeholders);
                if (game != null) {
                    game.addFinalKill(killer);
                }
            }
        } else {
            placeholders.put("killer", "Unknown");
            message = plugin.getConfigManager().getMessage("game.player-killed", placeholders);
        }

        for (Player p : arena.getPlayers()) {
            p.sendMessage(message);
        }

        // Clear drops
        event.getDrops().clear();
        event.setDroppedExp(0);

        // Handle respawn or elimination
        if (canRespawn) {
            handleRespawn(player, arena, team);
        } else {
            handleElimination(player, arena, team);
        }
    }

    private void handleRespawn(Player player, Arena arena, BedwarsTeam team) {
        int respawnTime = plugin.getConfigManager().getInt("respawn.time");

        new BukkitRunnable() {
            int timeLeft = respawnTime;

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    // Respawn player in game world
                    org.bukkit.Location originalSpawn = arena.getMap().getSpawn(team.getColor());
                    if (originalSpawn != null && arena.getGameWorldName() != null) {
                        org.bukkit.World gameWorld = org.bukkit.Bukkit.getWorld(arena.getGameWorldName());
                        if (gameWorld != null) {
                            org.bukkit.Location gameSpawn = new org.bukkit.Location(
                                gameWorld,
                                originalSpawn.getX(),
                                originalSpawn.getY(),
                                originalSpawn.getZ(),
                                originalSpawn.getYaw(),
                                originalSpawn.getPitch()
                            );
                            player.spigot().respawn();
                            player.teleport(gameSpawn);
                            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
                        }
                    }
                    cancel();
                    return;
                }

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("time", String.valueOf(timeLeft));
                player.sendMessage(plugin.getConfigManager().getMessage("game.respawn-in", placeholders));
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void handleElimination(Player player, Arena arena, BedwarsTeam team) {
        // Set player to spectator mode in the game world
        player.setGameMode(org.bukkit.GameMode.SPECTATOR);
        player.setAllowFlight(true);
        player.setFlying(true);

        // Teleport to center of map if possible
        if (arena.getGameWorldName() != null) {
            org.bukkit.World gameWorld = org.bukkit.Bukkit.getWorld(arena.getGameWorldName());
            if (gameWorld != null) {
                player.teleport(gameWorld.getSpawnLocation().add(0, 20, 0));
            }
        }

        player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou have been eliminated! You are now spectating.");

        // Check if team is eliminated
        boolean hasAlivePlayers = false;
        for (java.util.UUID uuid : team.getPlayers()) {
            Player p = plugin.getServer().getPlayer(uuid);
            if (p != null && p.isOnline() && !p.equals(player)) {
                hasAlivePlayers = true;
                break;
            }
        }

        if (!hasAlivePlayers) {
            team.setEliminated(true);

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("team", team.getDisplayName());
            String message = plugin.getConfigManager().getMessage("game.elimination", placeholders);

            for (Player p : arena.getPlayers()) {
                p.sendMessage(message);
            }
        }
    }
}

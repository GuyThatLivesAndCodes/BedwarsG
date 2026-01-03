package com.guythatlives.bedwarsg.game;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Game {

    private final Arena arena;
    private final BedwarsG plugin;
    private final Map<UUID, PlayerData> playerData;
    private BukkitRunnable gameTask;

    public Game(Arena arena, BedwarsG plugin) {
        this.arena = arena;
        this.plugin = plugin;
        this.playerData = new HashMap<>();
    }

    public void start() {
        // Initialize player data
        for (Player player : arena.getPlayers()) {
            playerData.put(player.getUniqueId(), new PlayerData(player));
        }

        // Start game loop
        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        };
        gameTask.runTaskTimer(plugin, 0L, 20L);
    }

    private void tick() {
        // Check win conditions
        BedwarsTeam winner = checkWinCondition();
        if (winner != null) {
            plugin.getGameManager().endGame(arena, winner);
            if (gameTask != null) {
                gameTask.cancel();
            }
        }

        // Update game timer
        arena.setGameTimer(arena.getGameTimer() + 1);
    }

    private BedwarsTeam checkWinCondition() {
        List<BedwarsTeam> aliveTeams = new ArrayList<>();

        for (BedwarsTeam team : arena.getTeams().values()) {
            if (!team.isEliminated()) {
                boolean hasAlivePlayers = false;
                int realPlayersChecked = 0;
                int botsChecked = 0;

                for (UUID uuid : team.getPlayers()) {
                    // Check if it's a real player
                    Player player = plugin.getServer().getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        hasAlivePlayers = true;
                        realPlayersChecked++;
                        break;
                    }

                    // Check if it's a bot
                    if (plugin.getBotManager() != null && plugin.getBotManager().isBot(uuid)) {
                        com.guythatlives.bedwarsg.bot.BotPlayer bot = plugin.getBotManager().getBot(uuid);
                        botsChecked++;
                        if (bot != null && bot.getArmorStand() != null && bot.getArmorStand().isValid()) {
                            hasAlivePlayers = true;
                            plugin.getLogger().info("[Win Check] Team " + team.getColor() + " has alive bot: " + bot.getName() +
                                                   " in world: " + bot.getArmorStand().getWorld().getName());
                            break;
                        } else {
                            plugin.getLogger().warning("[Win Check] Team " + team.getColor() + " bot check failed - " +
                                                      "bot=" + (bot != null) + ", stand=" + (bot != null && bot.getArmorStand() != null) +
                                                      ", valid=" + (bot != null && bot.getArmorStand() != null && bot.getArmorStand().isValid()));
                        }
                    }
                }

                plugin.getLogger().info("[Win Check] Team " + team.getColor() + " - Players: " + realPlayersChecked +
                                       ", Bots checked: " + botsChecked + ", Has alive: " + hasAlivePlayers);

                if (hasAlivePlayers) {
                    aliveTeams.add(team);
                } else {
                    team.setEliminated(true);
                    plugin.getLogger().info("[Win Check] Team " + team.getColor() + " eliminated!");
                }
            }
        }

        plugin.getLogger().info("[Win Check] Alive teams: " + aliveTeams.size());
        return aliveTeams.size() == 1 ? aliveTeams.get(0) : null;
    }

    public void end(BedwarsTeam winner) {
        if (gameTask != null) {
            gameTask.cancel();
        }

        // Save stats
        for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null) {
                PlayerData data = entry.getValue();
                BedwarsTeam team = arena.getPlayerTeam(player);

                boolean won = team != null && team.equals(winner);
                plugin.getStatsManager().updateStats(player, data, won);
            }
        }
    }

    public Arena getArena() {
        return arena;
    }

    public PlayerData getPlayerData(Player player) {
        return playerData.get(player.getUniqueId());
    }

    public void addKill(Player player) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data != null) {
            data.addKill();
        }
    }

    public void addDeath(Player player) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data != null) {
            data.addDeath();
        }
    }

    public void addFinalKill(Player player) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data != null) {
            data.addFinalKill();
        }
    }

    public void addBedDestroyed(Player player) {
        PlayerData data = playerData.get(player.getUniqueId());
        if (data != null) {
            data.addBedDestroyed();
        }
    }
}

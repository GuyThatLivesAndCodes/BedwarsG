package com.guythatlives.bedwarsg.arena;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Arena {

    private final String name;
    private final BedwarsMap map;
    private final GameMode gameMode;
    private final BedwarsG plugin;

    private ArenaState state;
    private Map<UUID, Player> players;
    private Map<String, BedwarsTeam> teams;
    private Map<UUID, BedwarsTeam> playerTeams;
    private Set<UUID> bots; // Track bot UUIDs

    private int countdown;
    private int gameTimer;
    private String gameWorldName;
    private UUID generatorTaskId;
    private Set<org.bukkit.Location> playerPlacedBlocks;

    public Arena(String name, BedwarsMap map, GameMode gameMode, BedwarsG plugin) {
        this.name = name;
        this.map = map;
        this.gameMode = gameMode;
        this.plugin = plugin;
        this.state = ArenaState.WAITING;
        this.players = new HashMap<>();
        this.teams = new HashMap<>();
        this.playerTeams = new HashMap<>();
        this.bots = ConcurrentHashMap.newKeySet();
        this.playerPlacedBlocks = ConcurrentHashMap.newKeySet();

        initializeTeams();
    }

    private void initializeTeams() {
        List<String> teamColors = plugin.getConfigManager().getConfig().getStringList("teams");
        int teamsNeeded = getTeamsCount();

        for (int i = 0; i < Math.min(teamsNeeded, teamColors.size()); i++) {
            String color = teamColors.get(i);
            BedwarsTeam team = new BedwarsTeam(color, getPlayersPerTeam());
            teams.put(color, team);
        }
    }

    public void addPlayer(Player player) {
        players.put(player.getUniqueId(), player);
        assignToTeam(player);
        plugin.getArenaManager().setPlayerArena(player, this);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        BedwarsTeam team = playerTeams.remove(player.getUniqueId());
        if (team != null) {
            team.removePlayer(player);
        }
        plugin.getArenaManager().setPlayerArena(player, null);
    }

    private void assignToTeam(Player player) {
        BedwarsTeam smallestTeam = null;
        int minSize = Integer.MAX_VALUE;

        for (BedwarsTeam team : teams.values()) {
            if (team.getPlayers().size() < team.getMaxSize() && team.getPlayers().size() < minSize) {
                smallestTeam = team;
                minSize = team.getPlayers().size();
            }
        }

        if (smallestTeam != null) {
            smallestTeam.addPlayer(player);
            playerTeams.put(player.getUniqueId(), smallestTeam);
        }
    }

    public boolean canStart() {
        int totalPlayers = players.size() + bots.size();
        return totalPlayers >= map.getMinPlayers();
    }

    public boolean isFull() {
        int totalPlayers = players.size() + bots.size();
        return totalPlayers >= map.getMaxPlayers();
    }

    /**
     * Add a bot to this arena
     */
    public void addBot(UUID botUUID, BedwarsTeam team) {
        bots.add(botUUID);
        if (team != null) {
            playerTeams.put(botUUID, team);
        }
    }

    /**
     * Remove a bot from this arena
     */
    public void removeBot(UUID botUUID) {
        bots.remove(botUUID);
        playerTeams.remove(botUUID);
    }

    /**
     * Get total player count (real players + bots)
     */
    public int getTotalPlayerCount() {
        return players.size() + bots.size();
    }

    /**
     * Get bot count
     */
    public int getBotCount() {
        return bots.size();
    }

    private int getTeamsCount() {
        switch (gameMode) {
            case SOLO:
            case DOUBLES:
                return 8;
            case TRIO:
            case QUAD:
                return 4;
            case DUEL:
                return 2;
            default:
                return 4;
        }
    }

    private int getPlayersPerTeam() {
        switch (gameMode) {
            case SOLO:
            case DUEL:
                return 1;
            case DOUBLES:
                return 2;
            case TRIO:
                return 3;
            case QUAD:
                return 4;
            default:
                return 1;
        }
    }

    // Getters
    public String getName() {
        return name;
    }

    public BedwarsMap getMap() {
        return map;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public ArenaState getState() {
        return state;
    }

    public void setState(ArenaState state) {
        this.state = state;
    }

    public Collection<Player> getPlayers() {
        return players.values();
    }

    public Map<String, BedwarsTeam> getTeams() {
        return teams;
    }

    public BedwarsTeam getPlayerTeam(Player player) {
        return playerTeams.get(player.getUniqueId());
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown) {
        this.countdown = countdown;
    }

    public int getGameTimer() {
        return gameTimer;
    }

    public void setGameTimer(int gameTimer) {
        this.gameTimer = gameTimer;
    }

    public String getGameWorldName() {
        return gameWorldName;
    }

    public void setGameWorldName(String gameWorldName) {
        this.gameWorldName = gameWorldName;
    }

    public UUID getGeneratorTaskId() {
        return generatorTaskId;
    }

    public void setGeneratorTaskId(UUID generatorTaskId) {
        this.generatorTaskId = generatorTaskId;
    }

    public Set<org.bukkit.Location> getPlayerPlacedBlocks() {
        return playerPlacedBlocks;
    }
}

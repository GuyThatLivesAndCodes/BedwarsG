package com.guythatlives.bedwarsg.arena;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.entity.Player;

import java.util.*;

public class Arena {

    private final String name;
    private final BedwarsMap map;
    private final GameMode gameMode;
    private final BedwarsG plugin;

    private ArenaState state;
    private Map<UUID, Player> players;
    private Map<String, BedwarsTeam> teams;
    private Map<UUID, BedwarsTeam> playerTeams;

    private int countdown;
    private int gameTimer;
    private String gameWorldName;
    private UUID generatorTaskId;

    public Arena(String name, BedwarsMap map, GameMode gameMode, BedwarsG plugin) {
        this.name = name;
        this.map = map;
        this.gameMode = gameMode;
        this.plugin = plugin;
        this.state = ArenaState.WAITING;
        this.players = new HashMap<>();
        this.teams = new HashMap<>();
        this.playerTeams = new HashMap<>();

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
        return players.size() >= map.getMinPlayers();
    }

    public boolean isFull() {
        return players.size() >= map.getMaxPlayers();
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
}

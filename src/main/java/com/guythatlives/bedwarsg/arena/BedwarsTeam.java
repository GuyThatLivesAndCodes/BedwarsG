package com.guythatlives.bedwarsg.arena;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;

import java.util.*;

public class BedwarsTeam {

    private final String color;
    private final int maxSize;
    private final Set<UUID> players;
    private boolean bedAlive;
    private boolean eliminated;

    public BedwarsTeam(String color, int maxSize) {
        this.color = color;
        this.maxSize = maxSize;
        this.players = new HashSet<>();
        this.bedAlive = true;
        this.eliminated = false;
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
    }

    public void addPlayer(UUID playerUUID) {
        players.add(playerUUID);
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public void removePlayer(UUID playerUUID) {
        players.remove(playerUUID);
    }

    public String getColor() {
        return color;
    }

    public ChatColor getChatColor() {
        try {
            return ChatColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            return ChatColor.WHITE;
        }
    }

    public DyeColor getDyeColor() {
        try {
            return DyeColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            return DyeColor.WHITE;
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public int getSize() {
        return players.size();
    }

    public boolean isBedAlive() {
        return bedAlive;
    }

    public void setBedAlive(boolean bedAlive) {
        this.bedAlive = bedAlive;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }

    public String getDisplayName() {
        return getChatColor() + color + " Team";
    }
}

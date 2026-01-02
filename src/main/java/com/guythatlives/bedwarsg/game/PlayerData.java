package com.guythatlives.bedwarsg.game;

import org.bukkit.entity.Player;

public class PlayerData {

    private final String playerName;
    private int kills;
    private int deaths;
    private int finalKills;
    private int bedsDestroyed;

    public PlayerData(Player player) {
        this.playerName = player.getName();
        this.kills = 0;
        this.deaths = 0;
        this.finalKills = 0;
        this.bedsDestroyed = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void addDeath() {
        deaths++;
    }

    public int getFinalKills() {
        return finalKills;
    }

    public void addFinalKill() {
        finalKills++;
    }

    public int getBedsDestroyed() {
        return bedsDestroyed;
    }

    public void addBedDestroyed() {
        bedsDestroyed++;
    }
}

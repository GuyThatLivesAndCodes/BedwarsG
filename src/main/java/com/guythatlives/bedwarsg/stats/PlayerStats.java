package com.guythatlives.bedwarsg.stats;

public class PlayerStats {

    private String playerName;
    private int kills;
    private int deaths;
    private int wins;
    private int losses;
    private int finalKills;
    private int bedsDestroyed;
    private int gamesPlayed;

    public PlayerStats(String playerName) {
        this.playerName = playerName;
        this.kills = 0;
        this.deaths = 0;
        this.wins = 0;
        this.losses = 0;
        this.finalKills = 0;
        this.bedsDestroyed = 0;
        this.gamesPlayed = 0;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void addKills(int kills) {
        this.kills += kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void addDeaths(int deaths) {
        this.deaths += deaths;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void addWins(int wins) {
        this.wins += wins;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void addLosses(int losses) {
        this.losses += losses;
    }

    public int getFinalKills() {
        return finalKills;
    }

    public void setFinalKills(int finalKills) {
        this.finalKills = finalKills;
    }

    public void addFinalKills(int finalKills) {
        this.finalKills += finalKills;
    }

    public int getBedsDestroyed() {
        return bedsDestroyed;
    }

    public void setBedsDestroyed(int bedsDestroyed) {
        this.bedsDestroyed = bedsDestroyed;
    }

    public void addBedsDestroyed(int bedsDestroyed) {
        this.bedsDestroyed += bedsDestroyed;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public void addGamesPlayed(int gamesPlayed) {
        this.gamesPlayed += gamesPlayed;
    }

    public double getKDR() {
        if (deaths == 0) {
            return kills;
        }
        return (double) kills / deaths;
    }

    public double getWLR() {
        if (losses == 0) {
            return wins;
        }
        return (double) wins / losses;
    }
}

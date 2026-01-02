package com.guythatlives.bedwarsg.stats;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.game.PlayerData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StatsManager {

    private final BedwarsG plugin;
    private Map<UUID, PlayerStats> statsCache;

    public StatsManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.statsCache = new HashMap<>();
    }

    public PlayerStats getStats(Player player) {
        return getStats(player.getUniqueId(), player.getName());
    }

    public PlayerStats getStats(UUID uuid, String name) {
        if (statsCache.containsKey(uuid)) {
            return statsCache.get(uuid);
        }

        PlayerStats stats = loadStats(uuid, name);
        statsCache.put(uuid, stats);
        return stats;
    }

    private PlayerStats loadStats(UUID uuid, String name) {
        File file = new File(plugin.getDataFolder(), "stats/" + uuid.toString() + ".yml");
        if (!file.exists()) {
            return new PlayerStats(name);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerStats stats = new PlayerStats(name);

        stats.setKills(config.getInt("kills", 0));
        stats.setDeaths(config.getInt("deaths", 0));
        stats.setWins(config.getInt("wins", 0));
        stats.setLosses(config.getInt("losses", 0));
        stats.setFinalKills(config.getInt("final-kills", 0));
        stats.setBedsDestroyed(config.getInt("beds-destroyed", 0));
        stats.setGamesPlayed(config.getInt("games-played", 0));

        return stats;
    }

    public void saveStats(Player player) {
        saveStats(player.getUniqueId(), getStats(player));
    }

    public void saveStats(UUID uuid, PlayerStats stats) {
        File file = new File(plugin.getDataFolder(), "stats/" + uuid.toString() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("name", stats.getPlayerName());
        config.set("kills", stats.getKills());
        config.set("deaths", stats.getDeaths());
        config.set("wins", stats.getWins());
        config.set("losses", stats.getLosses());
        config.set("final-kills", stats.getFinalKills());
        config.set("beds-destroyed", stats.getBedsDestroyed());
        config.set("games-played", stats.getGamesPlayed());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats for " + uuid);
            e.printStackTrace();
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerStats> entry : statsCache.entrySet()) {
            saveStats(entry.getKey(), entry.getValue());
        }
    }

    public void updateStats(Player player, PlayerData gameData, boolean won) {
        PlayerStats stats = getStats(player);

        stats.addKills(gameData.getKills());
        stats.addDeaths(gameData.getDeaths());
        stats.addFinalKills(gameData.getFinalKills());
        stats.addBedsDestroyed(gameData.getBedsDestroyed());
        stats.addGamesPlayed(1);

        if (won) {
            stats.addWins(1);
        } else {
            stats.addLosses(1);
        }

        saveStats(player);
    }

    public void unloadStats(Player player) {
        saveStats(player);
        statsCache.remove(player.getUniqueId());
    }
}

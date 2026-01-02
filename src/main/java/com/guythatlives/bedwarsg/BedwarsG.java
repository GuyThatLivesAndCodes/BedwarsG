package com.guythatlives.bedwarsg;

import com.guythatlives.bedwarsg.arena.ArenaManager;
import com.guythatlives.bedwarsg.commands.*;
import com.guythatlives.bedwarsg.game.GameManager;
import com.guythatlives.bedwarsg.listeners.*;
import com.guythatlives.bedwarsg.map.MapManager;
import com.guythatlives.bedwarsg.party.PartyManager;
import com.guythatlives.bedwarsg.shop.ShopManager;
import com.guythatlives.bedwarsg.stats.StatsManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BedwarsG extends JavaPlugin {

    private static BedwarsG instance;

    private ArenaManager arenaManager;
    private GameManager gameManager;
    private MapManager mapManager;
    private PartyManager partyManager;
    private ShopManager shopManager;
    private StatsManager statsManager;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Initializing BedwarsG...");

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Initialize managers
        mapManager = new MapManager(this);
        arenaManager = new ArenaManager(this);
        partyManager = new PartyManager(this);
        gameManager = new GameManager(this);
        shopManager = new ShopManager(this);
        statsManager = new StatsManager(this);

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        getLogger().info("BedwarsG has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        // Save all data
        if (statsManager != null) {
            statsManager.saveAll();
        }

        if (gameManager != null) {
            gameManager.endAllGames();
        }

        if (mapManager != null) {
            mapManager.saveAll();
        }

        getLogger().info("BedwarsG has been disabled!");
    }

    private void registerCommands() {
        getCommand("bedwars").setExecutor(new BedwarsCommand(this));
        getCommand("bw").setExecutor(new BedwarsCommand(this));
        getCommand("bparty").setExecutor(new PartyCommand(this));
        getCommand("bp").setExecutor(new PartyCommand(this));
        getCommand("bwadmin").setExecutor(new AdminCommand(this));
        getCommand("bw1v1").setExecutor(new DuelCommand(this));
        getCommand("bwstats").setExecutor(new StatsCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
    }

    public static BedwarsG getInstance() {
        return instance;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public MapManager getMapManager() {
        return mapManager;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

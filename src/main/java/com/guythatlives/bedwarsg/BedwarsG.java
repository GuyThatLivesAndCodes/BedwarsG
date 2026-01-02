package com.guythatlives.bedwarsg;

import com.guythatlives.bedwarsg.arena.ArenaManager;
import com.guythatlives.bedwarsg.commands.*;
import com.guythatlives.bedwarsg.game.GameManager;
import com.guythatlives.bedwarsg.game.GeneratorManager;
import com.guythatlives.bedwarsg.game.ShopNPCManager;
import com.guythatlives.bedwarsg.gui.AdminGUI;
import com.guythatlives.bedwarsg.gui.PlayerGUI;
import com.guythatlives.bedwarsg.listeners.*;
import com.guythatlives.bedwarsg.map.MapManager;
import com.guythatlives.bedwarsg.party.PartyManager;
import com.guythatlives.bedwarsg.shop.ShopManager;
import com.guythatlives.bedwarsg.stats.StatsManager;
import com.guythatlives.bedwarsg.visual.MapVisualizer;
import com.guythatlives.bedwarsg.world.WorldManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BedwarsG extends JavaPlugin {

    private static BedwarsG instance;

    private ArenaManager arenaManager;
    private GameManager gameManager;
    private GeneratorManager generatorManager;
    private ShopNPCManager shopNPCManager;
    private MapManager mapManager;
    private PartyManager partyManager;
    private ShopManager shopManager;
    private StatsManager statsManager;
    private ConfigManager configManager;
    private WorldManager worldManager;
    private MapVisualizer mapVisualizer;
    private PlayerGUI playerGUI;
    private AdminGUI adminGUI;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Initializing BedwarsG...");

        // Initialize configuration
        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        // Initialize managers
        worldManager = new WorldManager(this);
        mapManager = new MapManager(this);
        arenaManager = new ArenaManager(this);
        partyManager = new PartyManager(this);
        generatorManager = new GeneratorManager(this);
        shopNPCManager = new ShopNPCManager(this);
        gameManager = new GameManager(this);
        shopManager = new ShopManager(this);
        statsManager = new StatsManager(this);
        mapVisualizer = new MapVisualizer(this);
        playerGUI = new PlayerGUI(this);
        adminGUI = new AdminGUI(this);

        // Register commands
        registerCommands();

        // Register listeners
        registerListeners();

        // Start visualizer
        mapVisualizer.startVisualization();

        getLogger().info("BedwarsG has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Shutting down BedwarsG...");

        // Stop visualizer
        if (mapVisualizer != null) {
            mapVisualizer.stopVisualization();
        }

        // Remove all shop NPCs
        if (shopNPCManager != null) {
            shopNPCManager.removeAllShops();
        }

        // End all games gracefully
        if (gameManager != null) {
            gameManager.endAllGames();
        }

        // Save all data
        if (statsManager != null) {
            statsManager.saveAll();
        }

        if (mapManager != null) {
            mapManager.saveAll();
        }

        // Clean up all game worlds
        if (worldManager != null) {
            worldManager.deleteAllGameWorlds();
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
        getServer().getPluginManager().registerEvents(new GUIClickListener(this), this);
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

    public GeneratorManager getGeneratorManager() {
        return generatorManager;
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

    public WorldManager getWorldManager() {
        return worldManager;
    }

    public MapVisualizer getMapVisualizer() {
        return mapVisualizer;
    }

    public PlayerGUI getPlayerGUI() {
        return playerGUI;
    }

    public AdminGUI getAdminGUI() {
        return adminGUI;
    }

    public ShopNPCManager getShopNPCManager() {
        return shopNPCManager;
    }
}

package com.guythatlives.bedwarsg.bot;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all bot players in the game
 */
public class BotManager {

    private final BedwarsG plugin;
    private final Map<UUID, BotPlayer> activeBots;
    private final Map<String, Long> arenaAutoFillTimers; // Arena name -> time when waiting started
    private final List<String> availableNames;
    private final Random random;

    // Configuration
    private boolean enabled;
    private int autoFillDelay;
    private int maxBotsPerGame;
    private BotDifficulty defaultDifficulty;
    private Map<BotDifficulty, BotSkills> skillsByDifficulty;

    public BotManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.activeBots = new ConcurrentHashMap<>();
        this.arenaAutoFillTimers = new ConcurrentHashMap<>();
        this.availableNames = new ArrayList<>();
        this.random = new Random();

        loadConfiguration();
    }

    /**
     * Load bot configuration from config.yml
     */
    private void loadConfiguration() {
        this.enabled = plugin.getConfig().getBoolean("bots.enabled", true);
        this.autoFillDelay = plugin.getConfigManager().getInt("bots.auto-fill-delay");
        this.maxBotsPerGame = plugin.getConfigManager().getInt("bots.max-bots-per-game");

        String difficultyStr = plugin.getConfig().getString("bots.difficulty", "MEDIUM");
        this.defaultDifficulty = BotDifficulty.fromString(difficultyStr);

        // Load skill levels for each difficulty
        this.skillsByDifficulty = new HashMap<>();
        for (BotDifficulty difficulty : BotDifficulty.values()) {
            String path = "bots.skills." + difficulty.name();
            double accuracy = plugin.getConfig().getDouble(path + ".accuracy");
            double blockPlacing = plugin.getConfig().getDouble(path + ".block-placing-speed");
            double pvp = plugin.getConfig().getDouble(path + ".pvp-skill");
            double decision = plugin.getConfig().getDouble(path + ".decision-speed");
            double teamwork = plugin.getConfig().getDouble(path + ".teamwork");

            skillsByDifficulty.put(difficulty, new BotSkills(accuracy, blockPlacing, pvp, decision, teamwork));
        }

        // Load bot names
        List<String> configNames = plugin.getConfig().getStringList("bots.names");
        if (configNames != null && !configNames.isEmpty()) {
            availableNames.addAll(configNames);
        } else {
            // Default names if config is empty
            availableNames.addAll(Arrays.asList(
                "Bot_Alpha", "Bot_Beta", "Bot_Gamma", "Bot_Delta",
                "Bot_Epsilon", "Bot_Zeta", "Bot_Eta", "Bot_Theta"
            ));
        }
    }

    /**
     * Check if an arena needs bots and add them if necessary
     */
    public void checkAndFillArena(Arena arena) {
        if (!enabled) {
            return;
        }

        // Don't add bots to already running games
        if (arena.getState() != com.guythatlives.bedwarsg.arena.ArenaState.WAITING) {
            arenaAutoFillTimers.remove(arena.getName());
            return;
        }

        // Check if arena is full
        int currentPlayers = arena.getPlayers().size();
        int maxPlayers = arena.getMap().getMaxPlayers();

        if (currentPlayers >= maxPlayers) {
            arenaAutoFillTimers.remove(arena.getName());
            return;
        }

        // Start timer if not started
        if (!arenaAutoFillTimers.containsKey(arena.getName())) {
            if (currentPlayers > 0) { // Only start timer if there's at least one real player
                arenaAutoFillTimers.put(arena.getName(), System.currentTimeMillis());
            }
            return;
        }

        // Check if enough time has passed
        long waitTime = System.currentTimeMillis() - arenaAutoFillTimers.get(arena.getName());
        if (waitTime < autoFillDelay * 1000L) {
            return;
        }

        // Add bots to fill the arena
        int botsToAdd = Math.min(maxPlayers - currentPlayers, maxBotsPerGame);
        addBotsToArena(arena, botsToAdd);

        // Remove timer
        arenaAutoFillTimers.remove(arena.getName());
    }

    /**
     * Add specified number of bots to an arena
     */
    public void addBotsToArena(Arena arena, int count) {
        if (!enabled || count <= 0) {
            return;
        }

        for (int i = 0; i < count; i++) {
            BotPlayer bot = createBot(arena);
            if (bot != null) {
                spawnBot(bot, arena);
            }
        }

        plugin.getLogger().info("Added " + count + " bots to arena: " + arena.getName());
    }

    /**
     * Create a new bot player
     */
    private BotPlayer createBot(Arena arena) {
        String name = getUniqueBotName();
        BotSkills skills = skillsByDifficulty.get(defaultDifficulty);

        if (skills == null) {
            skills = skillsByDifficulty.get(BotDifficulty.MEDIUM);
        }

        return new BotPlayer(plugin, name, defaultDifficulty, skills, arena);
    }

    /**
     * Spawn a bot in the arena as an armor stand
     */
    private void spawnBot(BotPlayer bot, Arena arena) {
        // Get spawn location for bot's team
        BedwarsTeam team = assignBotToTeam(arena);
        if (team == null) {
            plugin.getLogger().warning("Could not assign bot to team in arena: " + arena.getName());
            return;
        }

        Location spawnLoc = arena.getMap().getSpawn(team.getColor());
        if (spawnLoc == null) {
            plugin.getLogger().warning("No spawn location for team: " + team.getColor());
            return;
        }

        // Get the game world
        World world;
        if (arena.getGameWorldName() != null) {
            world = Bukkit.getWorld(arena.getGameWorldName());
            if (world != null) {
                // Convert spawn location to game world
                spawnLoc = new Location(
                    world,
                    spawnLoc.getX(),
                    spawnLoc.getY(),
                    spawnLoc.getZ(),
                    spawnLoc.getYaw(),
                    spawnLoc.getPitch()
                );
            }
        } else {
            world = spawnLoc.getWorld();
        }

        if (world == null) {
            plugin.getLogger().warning("Could not find world for bot spawn");
            return;
        }

        // Spawn armor stand
        ArmorStand armorStand = (ArmorStand) world.spawnEntity(spawnLoc, EntityType.ARMOR_STAND);

        // Configure armor stand to look like a player
        armorStand.setCustomName(ChatColor.YELLOW + bot.getName());
        armorStand.setCustomNameVisible(true);
        armorStand.setGravity(true);
        armorStand.setVisible(true);
        armorStand.setBasePlate(false);
        armorStand.setArms(true);
        armorStand.setSmall(false);
        armorStand.setMarker(false); // Not a marker, can be interacted with
        armorStand.setInvulnerable(false); // Can be damaged

        // Add equipment to make it look like a player
        equipBot(armorStand, team);

        // Link armor stand to bot
        bot.setArmorStand(armorStand);

        // Initialize the bot AI
        bot.initialize();

        // Add to active bots
        activeBots.put(bot.getUUID(), bot);

        plugin.getLogger().info("Bot " + bot.getName() + " spawned as armor stand in team " + team.getColor());
    }

    /**
     * Equip bot armor stand with items
     */
    private void equipBot(ArmorStand armorStand, BedwarsTeam team) {
        // Give basic armor based on team color
        Color armorColor = getArmorColor(team.getColor());

        if (armorColor != null) {
            // Leather armor in team color
            ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);
            org.bukkit.inventory.meta.LeatherArmorMeta helmetMeta =
                (org.bukkit.inventory.meta.LeatherArmorMeta) helmet.getItemMeta();
            if (helmetMeta != null) {
                helmetMeta.setColor(armorColor);
                helmet.setItemMeta(helmetMeta);
            }

            ItemStack chestplate = new ItemStack(Material.LEATHER_CHESTPLATE);
            org.bukkit.inventory.meta.LeatherArmorMeta chestMeta =
                (org.bukkit.inventory.meta.LeatherArmorMeta) chestplate.getItemMeta();
            if (chestMeta != null) {
                chestMeta.setColor(armorColor);
                chestplate.setItemMeta(chestMeta);
            }

            ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);
            org.bukkit.inventory.meta.LeatherArmorMeta legMeta =
                (org.bukkit.inventory.meta.LeatherArmorMeta) leggings.getItemMeta();
            if (legMeta != null) {
                legMeta.setColor(armorColor);
                leggings.setItemMeta(legMeta);
            }

            ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
            org.bukkit.inventory.meta.LeatherArmorMeta bootMeta =
                (org.bukkit.inventory.meta.LeatherArmorMeta) boots.getItemMeta();
            if (bootMeta != null) {
                bootMeta.setColor(armorColor);
                boots.setItemMeta(bootMeta);
            }

            armorStand.getEquipment().setHelmet(helmet);
            armorStand.getEquipment().setChestplate(chestplate);
            armorStand.getEquipment().setLeggings(leggings);
            armorStand.getEquipment().setBoots(boots);
        }

        // Give a wooden sword in hand
        armorStand.getEquipment().setItemInMainHand(new ItemStack(Material.WOODEN_SWORD));
    }

    /**
     * Get armor color for team
     */
    private Color getArmorColor(String teamColor) {
        switch (teamColor.toUpperCase()) {
            case "RED":
                return Color.RED;
            case "BLUE":
                return Color.BLUE;
            case "GREEN":
                return Color.GREEN;
            case "YELLOW":
                return Color.YELLOW;
            case "AQUA":
                return Color.AQUA;
            case "WHITE":
                return Color.WHITE;
            case "PINK":
                return Color.FUCHSIA;
            case "GRAY":
                return Color.GRAY;
            default:
                return Color.WHITE;
        }
    }

    /**
     * Assign a bot to a team (prefers teams with fewer players)
     * Only assigns to teams that have spawn points configured
     */
    private BedwarsTeam assignBotToTeam(Arena arena) {
        BedwarsTeam smallestTeam = null;
        int smallestSize = Integer.MAX_VALUE;

        for (BedwarsTeam team : arena.getTeams().values()) {
            // Only consider teams that have spawn points configured
            Location spawnLoc = arena.getMap().getSpawn(team.getColor());
            if (spawnLoc == null) {
                continue; // Skip teams without spawn points
            }

            int teamSize = team.getPlayers().size();
            if (teamSize < smallestSize) {
                smallestSize = teamSize;
                smallestTeam = team;
            }
        }

        return smallestTeam;
    }

    /**
     * Get a unique bot name that's not currently in use
     */
    private String getUniqueBotName() {
        List<String> usedNames = new ArrayList<>();
        for (BotPlayer bot : activeBots.values()) {
            usedNames.add(bot.getName());
        }

        // Find an unused name
        for (String name : availableNames) {
            if (!usedNames.contains(name)) {
                return name;
            }
        }

        // If all names are used, append a number
        return "Bot_" + (activeBots.size() + 1);
    }

    /**
     * Remove a bot from the game
     */
    public void removeBot(UUID botUUID) {
        BotPlayer bot = activeBots.get(botUUID);
        if (bot != null) {
            bot.cleanup();
            activeBots.remove(botUUID);

            // TODO: Remove the actual NPC/player entity

            plugin.getLogger().info("Removed bot: " + bot.getName());
        }
    }

    /**
     * Remove all bots from an arena
     */
    public void removeBotsFromArena(Arena arena) {
        List<UUID> botsToRemove = new ArrayList<>();

        for (BotPlayer bot : activeBots.values()) {
            if (bot.getArena().equals(arena)) {
                botsToRemove.add(bot.getUUID());
            }
        }

        for (UUID botUUID : botsToRemove) {
            removeBot(botUUID);
        }
    }

    /**
     * Remove all active bots
     */
    public void removeAllBots() {
        for (UUID botUUID : new ArrayList<>(activeBots.keySet())) {
            removeBot(botUUID);
        }
        arenaAutoFillTimers.clear();
    }

    /**
     * Check if a player is a bot
     */
    public boolean isBot(Player player) {
        return isBot(player.getUniqueId());
    }

    /**
     * Check if a UUID belongs to a bot
     */
    public boolean isBot(UUID uuid) {
        return activeBots.containsKey(uuid);
    }

    /**
     * Get a bot by UUID
     */
    public BotPlayer getBot(UUID uuid) {
        return activeBots.get(uuid);
    }

    /**
     * Get all active bots
     */
    public Collection<BotPlayer> getActiveBots() {
        return activeBots.values();
    }

    /**
     * Check if bot system is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Reload bot configuration
     */
    public void reload() {
        loadConfiguration();
    }
}

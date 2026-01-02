package com.guythatlives.bedwarsg.bot;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Represents a bot player in the game
 * Uses an ArmorStand entity to represent the bot visually
 */
public class BotPlayer {

    private final BedwarsG plugin;
    private final UUID uuid;
    private final String name;
    private final BotDifficulty difficulty;
    private final BotSkills skills;
    private final Arena arena;

    private BotBehaviorMode behaviorMode;
    private BotAI ai;
    private BukkitTask updateTask;
    private long lastModeSwitch;
    private int ticksAlive;
    private ArmorStand armorStand; // The visual representation of the bot

    // State tracking
    private boolean isGathering;
    private boolean isInCombat;
    private Location targetLocation;
    private Player targetEnemy;

    public BotPlayer(BedwarsG plugin, String name, BotDifficulty difficulty,
                     BotSkills skills, Arena arena) {
        this.plugin = plugin;
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.difficulty = difficulty;
        this.skills = skills;
        this.arena = arena;
        this.behaviorMode = BotBehaviorMode.PASSIVE;
        this.lastModeSwitch = System.currentTimeMillis();
        this.ticksAlive = 0;
        this.isGathering = true;
        this.isInCombat = false;
    }

    /**
     * Initialize the bot's AI and start its update task
     */
    public void initialize() {
        this.ai = new BotAI(this, plugin);

        int updateRate = plugin.getConfigManager().getInt("bots.update-rate");

        // Start the bot's update task
        this.updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                tick();
            }
        }.runTaskTimer(plugin, 0L, updateRate);
    }

    /**
     * Main update loop for the bot
     */
    private void tick() {
        // Check if armor stand is still valid
        if (armorStand == null || !armorStand.isValid() || armorStand.isDead()) {
            cleanup();
            return;
        }

        ticksAlive++;

        // Check if we should switch behavior mode
        checkModeSwitch();

        // Update AI
        if (ai != null) {
            ai.update();
        }
    }

    /**
     * Check if bot should switch between passive/aggressive modes
     */
    private void checkModeSwitch() {
        long modeSwitchInterval = plugin.getConfigManager().getInt("bots.behavior.mode-switch-interval") * 1000L;
        double modeSwitchChance = plugin.getConfig().getDouble("bots.behavior.mode-switch-chance");

        if (System.currentTimeMillis() - lastModeSwitch >= modeSwitchInterval) {
            if (Math.random() < modeSwitchChance) {
                // Switch between modes
                if (behaviorMode == BotBehaviorMode.PASSIVE) {
                    behaviorMode = Math.random() < 0.5 ? BotBehaviorMode.AGGRESSIVE : BotBehaviorMode.DEFENSIVE;
                } else {
                    behaviorMode = BotBehaviorMode.PASSIVE;
                }
                lastModeSwitch = System.currentTimeMillis();
            }
        }
    }

    /**
     * Get the armor stand representing this bot
     */
    public ArmorStand getArmorStand() {
        return armorStand;
    }

    /**
     * Set the armor stand for this bot
     */
    public void setArmorStand(ArmorStand armorStand) {
        this.armorStand = armorStand;
    }

    /**
     * Get the location of the bot
     */
    public Location getLocation() {
        return armorStand != null ? armorStand.getLocation() : null;
    }

    /**
     * Teleport the bot to a location
     */
    public void teleport(Location location) {
        if (armorStand != null && location != null) {
            armorStand.teleport(location);
        }
    }

    /**
     * Cleanup the bot and stop all tasks
     */
    public void cleanup() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
        }

        if (ai != null) {
            ai.cleanup();
        }

        // Remove armor stand
        if (armorStand != null) {
            armorStand.remove();
            armorStand = null;
        }
    }

    // Getters
    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public BotDifficulty getDifficulty() {
        return difficulty;
    }

    public BotSkills getSkills() {
        return skills;
    }

    public Arena getArena() {
        return arena;
    }

    public BotBehaviorMode getBehaviorMode() {
        return behaviorMode;
    }

    public void setBehaviorMode(BotBehaviorMode mode) {
        this.behaviorMode = mode;
    }

    public boolean isGathering() {
        return isGathering;
    }

    public void setGathering(boolean gathering) {
        isGathering = gathering;
    }

    public boolean isInCombat() {
        return isInCombat;
    }

    public void setInCombat(boolean inCombat) {
        isInCombat = inCombat;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location target) {
        this.targetLocation = target;
    }

    public Player getTargetEnemy() {
        return targetEnemy;
    }

    public void setTargetEnemy(Player enemy) {
        this.targetEnemy = enemy;
    }

    public int getTicksAlive() {
        return ticksAlive;
    }
}

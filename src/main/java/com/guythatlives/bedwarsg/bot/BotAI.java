package com.guythatlives.bedwarsg.bot;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI controller for bot players
 * Handles decision-making and actions based on behavior mode
 */
public class BotAI {

    private final BotPlayer bot;
    private final BedwarsG plugin;
    private final Random random;

    private long lastActionTime;
    private long gatherStartTime;
    private long lastAttackTime;

    public BotAI(BotPlayer bot, BedwarsG plugin) {
        this.bot = bot;
        this.plugin = plugin;
        this.random = new Random();
        this.lastActionTime = System.currentTimeMillis();
        this.gatherStartTime = System.currentTimeMillis();
        this.lastAttackTime = 0;
    }

    /**
     * Main update method called every tick
     */
    public void update() {
        Location botLoc = bot.getLocation();
        if (botLoc == null) {
            return;
        }

        // Always try to pick up nearby items
        tryPickupNearbyItems(botLoc);

        // Reduced reaction time for faster response
        int reactionTime = plugin.getConfigManager().getInt("bots.behavior.reaction-time");
        if (System.currentTimeMillis() - lastActionTime < reactionTime * 10) { // Changed from *50 to *10
            return;
        }

        // Check for threats first
        if (checkForThreats(botLoc)) {
            lastActionTime = System.currentTimeMillis();
            return;
        }

        // Execute behavior based on current mode
        switch (bot.getBehaviorMode()) {
            case PASSIVE:
                executePassiveBehavior(botLoc);
                break;
            case AGGRESSIVE:
                executeAggressiveBehavior(botLoc);
                break;
            case DEFENSIVE:
                executeDefensiveBehavior(botLoc);
                break;
        }

        lastActionTime = System.currentTimeMillis();
    }

    /**
     * Check for nearby threats and respond
     */
    private boolean checkForThreats(Location botLoc) {
        double combatRange = plugin.getConfig().getDouble("bots.behavior.combat-range");

        // Find nearest enemy
        Player nearestEnemy = findNearestEnemy(botLoc, combatRange);

        if (nearestEnemy != null) {
            bot.setInCombat(true);
            bot.setTargetEnemy(nearestEnemy);

            double distance = botLoc.distance(nearestEnemy.getLocation());

            // Attack if close enough
            if (distance <= 3.5) {
                attackPlayer(nearestEnemy);
            } else {
                // Move towards enemy
                moveTowards(botLoc, nearestEnemy.getLocation());
            }
            return true;
        }

        bot.setInCombat(false);
        bot.setTargetEnemy(null);
        return false;
    }

    /**
     * Passive behavior: gather resources and buy items
     */
    private void executePassiveBehavior(Location botLoc) {
        int gatherDuration = plugin.getConfigManager().getInt("bots.behavior.gather-duration") * 1000;

        // Check if we should gather or shop
        if (System.currentTimeMillis() - gatherStartTime < gatherDuration) {
            gatherResources(botLoc);
        } else {
            // Try to shop
            if (tryShop(botLoc)) {
                gatherStartTime = System.currentTimeMillis(); // Reset gather timer
            } else {
                gatherResources(botLoc);
            }
        }
    }

    /**
     * Aggressive behavior: attack enemies and break beds
     */
    private void executeAggressiveBehavior(Location botLoc) {
        double bedBreakPriority = plugin.getConfig().getDouble("bots.behavior.bed-break-priority");

        // Decide between attacking players or breaking beds
        if (random.nextDouble() < bedBreakPriority) {
            targetEnemyBed(botLoc);
        } else {
            huntPlayers(botLoc);
        }
    }

    /**
     * Defensive behavior: protect own bed and base
     */
    private void executeDefensiveBehavior(Location botLoc) {
        // For armor stand bots, just patrol around spawn
        Location spawnLoc = findBotSpawnLocation();
        if (spawnLoc == null) {
            gatherResources(botLoc);
            return;
        }

        // Stay near spawn
        if (botLoc.distance(spawnLoc) > 15) {
            moveTowards(botLoc, spawnLoc);
        } else {
            // Patrol around spawn
            patrolArea(botLoc, spawnLoc, 10);
        }
    }

    /**
     * Gather nearby resources
     */
    private void gatherResources(Location botLoc) {
        // Find nearest dropped item within range
        Item nearestItem = findNearestItem(botLoc, 10);

        if (nearestItem != null) {
            moveTowards(botLoc, nearestItem.getLocation());
        } else {
            // Move to nearest generator
            Location generatorLoc = findNearestGenerator();
            if (generatorLoc != null) {
                moveTowards(botLoc, generatorLoc);
            }
        }
    }

    /**
     * Try to pick up items within pickup range
     */
    private void tryPickupNearbyItems(Location botLoc) {
        if (botLoc.getWorld() == null) {
            return;
        }

        double pickupRange = 1.5; // Same as player pickup range

        for (Entity entity : botLoc.getWorld().getNearbyEntities(botLoc, pickupRange, pickupRange, pickupRange)) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                ItemStack itemStack = item.getItemStack();

                // Add to bot's inventory
                bot.addToInventory(itemStack.getType(), itemStack.getAmount());

                // Remove the item from the world
                item.remove();

                plugin.getLogger().info("Bot " + bot.getName() + " picked up " +
                                       itemStack.getAmount() + "x " + itemStack.getType());
            }
        }
    }

    /**
     * Try to use shop
     */
    private boolean tryShop(Location botLoc) {
        // Get shop location
        Location shopLoc = findShopLocation();
        if (shopLoc == null) {
            return false;
        }

        // Move to shop if not there
        if (botLoc.distance(shopLoc) > 3) {
            moveTowards(botLoc, shopLoc);
            return false;
        }

        // Armor stands can't actually shop, but they can hang around the shop area
        return true;
    }

    /**
     * Move towards enemy bed
     */
    private void targetEnemyBed(Location botLoc) {
        // Find nearest enemy bed that's still alive
        Location nearestBed = findNearestEnemyBed();

        if (nearestBed != null) {
            moveTowards(botLoc, nearestBed);
        }
    }

    /**
     * Hunt for enemy players
     */
    private void huntPlayers(Location botLoc) {
        double combatRange = plugin.getConfig().getDouble("bots.behavior.combat-range") * 2;
        Player target = findNearestEnemy(botLoc, combatRange);

        if (target != null) {
            double distance = botLoc.distance(target.getLocation());

            // Attack if close enough
            if (distance <= 3.5) {
                attackPlayer(target);
            } else {
                moveTowards(botLoc, target.getLocation());
            }
        } else {
            // Wander towards center
            if (bot.getArena().getGameWorldName() != null) {
                org.bukkit.World world = plugin.getServer().getWorld(bot.getArena().getGameWorldName());
                if (world != null) {
                    moveTowards(botLoc, world.getSpawnLocation());
                }
            }
        }
    }

    /**
     * Attack a player
     */
    private void attackPlayer(Player target) {
        // Check attack cooldown based on bot difficulty
        long attackCooldown;
        switch (bot.getDifficulty()) {
            case EASY:
                attackCooldown = 1000; // 1 second
                break;
            case MEDIUM:
                attackCooldown = 750; // 0.75 seconds
                break;
            case HARD:
                attackCooldown = 500; // 0.5 seconds
                break;
            default:
                attackCooldown = 750;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastAttackTime < attackCooldown) {
            return; // Still on cooldown
        }

        lastAttackTime = currentTime;

        // Calculate damage based on bot's inventory (better weapons = more damage)
        double damage = 2.0; // Base damage (1 heart)

        // Check if bot has better weapons
        if (bot.hasInInventory(Material.DIAMOND_SWORD, 1)) {
            damage = 7.0; // Diamond sword damage
        } else if (bot.hasInInventory(Material.IRON_SWORD, 1)) {
            damage = 6.0; // Iron sword damage
        } else if (bot.hasInInventory(Material.STONE_SWORD, 1)) {
            damage = 5.0; // Stone sword damage
        } else if (bot.hasInInventory(Material.WOODEN_SWORD, 1)) {
            damage = 4.0; // Wood sword damage
        }

        // Apply difficulty modifier
        damage *= bot.getSkills().getDecisionSpeed(); // Use decision speed as damage multiplier

        // Damage the player
        target.damage(damage);

        // Visual and sound effects
        Location targetLoc = target.getLocation();
        if (targetLoc.getWorld() != null) {
            // Play attack sound
            targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_PLAYER_ATTACK_STRONG, 1.0f, 1.0f);

            // Spawn particles
            targetLoc.getWorld().spawnParticle(Particle.CRIT, targetLoc.clone().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0.1);
        }

        plugin.getLogger().info("Bot " + bot.getName() + " attacked " + target.getName() + " for " + damage + " damage");
    }

    /**
     * Patrol around a location
     */
    private void patrolArea(Location botLoc, Location center, double radius) {
        // Simple patrol: pick random point in radius
        if (bot.getTargetLocation() == null ||
            botLoc.distance(bot.getTargetLocation()) < 2) {

            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * radius;

            Location target = center.clone().add(
                Math.cos(angle) * distance,
                0,
                Math.sin(angle) * distance
            );

            bot.setTargetLocation(target);
        }

        moveTowards(botLoc, bot.getTargetLocation());
    }

    /**
     * Move bot towards target location
     */
    private void moveTowards(Location from, Location target) {
        if (target == null || from == null) {
            return;
        }

        // Calculate direction
        Vector direction = target.toVector().subtract(from.toVector());
        double distance = direction.length();

        // Don't move if already very close
        if (distance < 0.5) {
            return;
        }

        direction.setY(0); // Keep on same Y level
        direction.normalize();

        // Faster movement speed - increased from 0.5 to 1.5 base speed
        double speed = 1.5 * bot.getSkills().getDecisionSpeed();
        Location newLoc = from.clone().add(direction.multiply(speed));

        // Make sure new location has same Y as target if close
        if (distance < 3) {
            newLoc.setY(target.getY());
        }

        // Teleport the bot
        bot.teleport(newLoc);
    }

    /**
     * Find nearest enemy player
     */
    private Player findNearestEnemy(Location botLoc, double maxRange) {
        Player nearest = null;
        double nearestDistance = maxRange;

        for (Player p : bot.getArena().getPlayers()) {
            double distance = botLoc.distance(p.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = p;
            }
        }

        return nearest;
    }

    /**
     * Find nearest dropped item
     */
    private Item findNearestItem(Location botLoc, double maxRange) {
        if (botLoc.getWorld() == null) {
            return null;
        }

        Item nearest = null;
        double nearestDistance = maxRange;

        for (Entity entity : botLoc.getWorld().getNearbyEntities(botLoc, maxRange, maxRange, maxRange)) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                double distance = botLoc.distance(item.getLocation());
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = item;
                }
            }
        }

        return nearest;
    }

    /**
     * Find nearest generator location
     */
    private Location findNearestGenerator() {
        if (bot.getArena().getMap() == null) {
            return null;
        }

        Location botLoc = bot.getLocation();
        if (botLoc == null) {
            return null;
        }

        Location nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Location genLoc : bot.getArena().getMap().getGenerators().values()) {
            double distance = botLoc.distance(genLoc);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = genLoc;
            }
        }

        return nearest;
    }

    /**
     * Find shop location
     */
    private Location findShopLocation() {
        if (bot.getArena().getMap() == null) {
            return null;
        }

        // Get first shop location (simplified)
        for (Location loc : bot.getArena().getMap().getShops().values()) {
            return loc;
        }
        return null;
    }

    /**
     * Find bot's spawn location
     */
    private Location findBotSpawnLocation() {
        if (bot.getArena().getMap() == null) {
            return null;
        }

        // Get first spawn location (simplified)
        for (Location loc : bot.getArena().getMap().getSpawns().values()) {
            return loc;
        }
        return null;
    }

    /**
     * Find nearest enemy bed
     */
    private Location findNearestEnemyBed() {
        if (bot.getArena().getMap() == null) {
            return null;
        }

        Location botLoc = bot.getLocation();
        if (botLoc == null) {
            return null;
        }

        Location nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BedwarsTeam team : bot.getArena().getTeams().values()) {
            if (!team.isBedAlive()) {
                continue;
            }

            Location bedLoc = bot.getArena().getMap().getBed(team.getColor());
            if (bedLoc != null) {
                double distance = botLoc.distance(bedLoc);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearest = bedLoc;
                }
            }
        }

        return nearest;
    }

    public void cleanup() {
        // Cleanup any resources
    }
}

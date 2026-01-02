package com.guythatlives.bedwarsg.bot;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import org.bukkit.Location;
import org.bukkit.Material;
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

    public BotAI(BotPlayer bot, BedwarsG plugin) {
        this.bot = bot;
        this.plugin = plugin;
        this.random = new Random();
        this.lastActionTime = System.currentTimeMillis();
        this.gatherStartTime = System.currentTimeMillis();
    }

    /**
     * Main update method called every tick
     */
    public void update() {
        Player player = bot.getPlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        // Apply reaction time delay
        int reactionTime = plugin.getConfigManager().getInt("bots.behavior.reaction-time");
        if (System.currentTimeMillis() - lastActionTime < reactionTime * 50) {
            return;
        }

        // Check for threats first
        if (checkForThreats(player)) {
            lastActionTime = System.currentTimeMillis();
            return;
        }

        // Execute behavior based on current mode
        switch (bot.getBehaviorMode()) {
            case PASSIVE:
                executePassiveBehavior(player);
                break;
            case AGGRESSIVE:
                executeAggressiveBehavior(player);
                break;
            case DEFENSIVE:
                executeDefensiveBehavior(player);
                break;
        }

        lastActionTime = System.currentTimeMillis();
    }

    /**
     * Check for nearby threats and respond
     */
    private boolean checkForThreats(Player player) {
        double combatRange = plugin.getConfig().getDouble("bots.behavior.combat-range");

        // Find nearest enemy
        Player nearestEnemy = findNearestEnemy(player, combatRange);

        if (nearestEnemy != null) {
            bot.setInCombat(true);
            bot.setTargetEnemy(nearestEnemy);
            engageCombat(player, nearestEnemy);
            return true;
        }

        bot.setInCombat(false);
        bot.setTargetEnemy(null);
        return false;
    }

    /**
     * Passive behavior: gather resources and buy items
     */
    private void executePassiveBehavior(Player player) {
        int gatherDuration = plugin.getConfigManager().getInt("bots.behavior.gather-duration") * 1000;

        // Check if we should gather or shop
        if (System.currentTimeMillis() - gatherStartTime < gatherDuration) {
            gatherResources(player);
        } else {
            // Try to shop
            if (tryShop(player)) {
                gatherStartTime = System.currentTimeMillis(); // Reset gather timer
            } else {
                gatherResources(player);
            }
        }
    }

    /**
     * Aggressive behavior: attack enemies and break beds
     */
    private void executeAggressiveBehavior(Player player) {
        double bedBreakPriority = plugin.getConfig().getDouble("bots.behavior.bed-break-priority");

        // Decide between attacking players or breaking beds
        if (random.nextDouble() < bedBreakPriority) {
            targetEnemyBed(player);
        } else {
            huntPlayers(player);
        }
    }

    /**
     * Defensive behavior: protect own bed and base
     */
    private void executeDefensiveBehavior(Player player) {
        BedwarsTeam team = bot.getArena().getPlayerTeam(player);
        if (team == null) {
            return;
        }

        // Get bed location
        Location bedLoc = bot.getArena().getMap().getBed(team.getColor());
        if (bedLoc == null) {
            // No bed to defend, switch to gathering
            gatherResources(player);
            return;
        }

        // Stay near bed
        if (player.getLocation().distance(bedLoc) > 15) {
            moveTowards(player, bedLoc);
        } else {
            // Patrol around bed
            patrolArea(player, bedLoc, 10);
        }
    }

    /**
     * Gather nearby resources
     */
    private void gatherResources(Player player) {
        // Find nearest dropped item within range
        Item nearestItem = findNearestItem(player, 10);

        if (nearestItem != null) {
            moveTowards(player, nearestItem.getLocation());
        } else {
            // Move to nearest generator
            Location generatorLoc = findNearestGenerator(player);
            if (generatorLoc != null) {
                moveTowards(player, generatorLoc);

                // Stay at generator for a bit
                if (player.getLocation().distance(generatorLoc) < 2) {
                    // Just wait here
                    return;
                }
            }
        }
    }

    /**
     * Try to use shop
     */
    private boolean tryShop(Player player) {
        BedwarsTeam team = bot.getArena().getPlayerTeam(player);
        if (team == null) {
            return false;
        }

        // Get shop location
        Location shopLoc = bot.getArena().getMap().getShop(team.getColor());
        if (shopLoc == null) {
            return false;
        }

        // Move to shop if not there
        if (player.getLocation().distance(shopLoc) > 3) {
            moveTowards(player, shopLoc);
            return false;
        }

        // TODO: Implement actual shop interaction when near shop
        // For now, just simulate buying items by giving the player items
        simulateShopPurchase(player);

        return true;
    }

    /**
     * Simulate shop purchase by giving player basic items
     */
    private void simulateShopPurchase(Player player) {
        // Give basic items based on resources
        int iron = countMaterial(player, Material.IRON_INGOT);
        int gold = countMaterial(player, Material.GOLD_INGOT);

        // Buy sword if don't have one
        if (!hasItem(player, Material.IRON_SWORD) && iron >= 10) {
            player.getInventory().addItem(new ItemStack(Material.IRON_SWORD));
            removeItems(player, Material.IRON_INGOT, 10);
        }

        // Buy armor
        if (!hasItem(player, Material.LEATHER_CHESTPLATE) && iron >= 5) {
            player.getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            removeItems(player, Material.IRON_INGOT, 5);
        }

        // Buy blocks
        if (countMaterial(player, Material.WHITE_WOOL) < 16 && iron >= 4) {
            player.getInventory().addItem(new ItemStack(Material.WHITE_WOOL, 16));
            removeItems(player, Material.IRON_INGOT, 4);
        }
    }

    /**
     * Move towards enemy bed
     */
    private void targetEnemyBed(Player player) {
        BedwarsTeam ownTeam = bot.getArena().getPlayerTeam(player);
        if (ownTeam == null) {
            return;
        }

        // Find nearest enemy bed that's still alive
        Location nearestBed = null;
        double nearestDistance = Double.MAX_VALUE;

        for (BedwarsTeam team : bot.getArena().getTeams().values()) {
            if (team.equals(ownTeam) || !team.isBedAlive()) {
                continue;
            }

            Location bedLoc = bot.getArena().getMap().getBed(team.getColor());
            if (bedLoc != null) {
                double distance = player.getLocation().distance(bedLoc);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestBed = bedLoc;
                }
            }
        }

        if (nearestBed != null) {
            moveTowards(player, nearestBed);

            // Try to break bed if close enough
            if (player.getLocation().distance(nearestBed) < 3) {
                // Bot should break the bed block
                // This will be handled by block break events
            }
        }
    }

    /**
     * Hunt for enemy players
     */
    private void huntPlayers(Player player) {
        double combatRange = plugin.getConfig().getDouble("bots.behavior.combat-range") * 2;
        Player target = findNearestEnemy(player, combatRange);

        if (target != null) {
            moveTowards(player, target.getLocation());
            engageCombat(player, target);
        } else {
            // Wander towards center
            if (bot.getArena().getGameWorldName() != null) {
                org.bukkit.World world = plugin.getServer().getWorld(bot.getArena().getGameWorldName());
                if (world != null) {
                    moveTowards(player, world.getSpawnLocation());
                }
            }
        }
    }

    /**
     * Engage in combat with enemy
     */
    private void engageCombat(Player player, Player enemy) {
        // Face the enemy
        player.teleport(player.getLocation().setDirection(
            enemy.getLocation().toVector().subtract(player.getLocation().toVector())
        ));

        // Attack if in range
        double distance = player.getLocation().distance(enemy.getLocation());
        if (distance < 3.5) {
            // Apply PvP skill to determine if attack hits
            if (random.nextDouble() < bot.getSkills().getPvpSkill()) {
                player.attack(enemy);
            }
        } else {
            // Move towards enemy
            moveTowards(player, enemy.getLocation());
        }
    }

    /**
     * Patrol around a location
     */
    private void patrolArea(Player player, Location center, double radius) {
        // Simple patrol: pick random point in radius
        if (bot.getTargetLocation() == null ||
            player.getLocation().distance(bot.getTargetLocation()) < 2) {

            double angle = random.nextDouble() * 2 * Math.PI;
            double distance = random.nextDouble() * radius;

            Location target = center.clone().add(
                Math.cos(angle) * distance,
                0,
                Math.sin(angle) * distance
            );

            bot.setTargetLocation(target);
        }

        moveTowards(player, bot.getTargetLocation());
    }

    /**
     * Move player towards target location
     */
    private void moveTowards(Player player, Location target) {
        if (target == null) {
            return;
        }

        // Simple movement: set velocity towards target
        Vector direction = target.toVector().subtract(player.getLocation().toVector()).normalize();

        // Apply decision speed to movement
        double speed = 0.2 * bot.getSkills().getDecisionSpeed();
        player.setVelocity(direction.multiply(speed));

        // Face the direction
        player.teleport(player.getLocation().setDirection(direction));
    }

    /**
     * Find nearest enemy player
     */
    private Player findNearestEnemy(Player player, double maxRange) {
        BedwarsTeam ownTeam = bot.getArena().getPlayerTeam(player);
        if (ownTeam == null) {
            return null;
        }

        Player nearest = null;
        double nearestDistance = maxRange;

        for (Player p : bot.getArena().getPlayers()) {
            if (p.equals(player)) {
                continue;
            }

            BedwarsTeam team = bot.getArena().getPlayerTeam(p);
            if (team != null && team.equals(ownTeam)) {
                continue; // Same team
            }

            double distance = player.getLocation().distance(p.getLocation());
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
    private Item findNearestItem(Player player, double maxRange) {
        Item nearest = null;
        double nearestDistance = maxRange;

        for (Entity entity : player.getNearbyEntities(maxRange, maxRange, maxRange)) {
            if (entity instanceof Item) {
                Item item = (Item) entity;
                double distance = player.getLocation().distance(item.getLocation());
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
    private Location findNearestGenerator(Player player) {
        BedwarsTeam team = bot.getArena().getPlayerTeam(player);
        if (team == null || bot.getArena().getMap() == null) {
            return null;
        }

        Location nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Location genLoc : bot.getArena().getMap().getGenerators().values()) {
            double distance = player.getLocation().distance(genLoc);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = genLoc;
            }
        }

        return nearest;
    }

    // Helper methods

    private int countMaterial(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private boolean hasItem(Player player, Material material) {
        return player.getInventory().contains(material);
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                if (item.getAmount() >= remaining) {
                    item.setAmount(item.getAmount() - remaining);
                    return;
                } else {
                    remaining -= item.getAmount();
                    item.setAmount(0);
                }
            }
        }
    }

    public void cleanup() {
        // Cleanup any resources
    }
}

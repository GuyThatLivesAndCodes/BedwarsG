package com.guythatlives.bedwarsg.game;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GeneratorManager {

    private final BedwarsG plugin;
    private final Map<UUID, BukkitRunnable> activeTasks;

    public GeneratorManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.activeTasks = new HashMap<>();
    }

    public void startGenerators(Arena arena, World gameWorld) {
        if (arena.getMap().getGenerators().isEmpty()) {
            return;
        }

        UUID arenaId = UUID.randomUUID();

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<String, Location> entry : arena.getMap().getGenerators().entrySet()) {
                    String type = entry.getKey().toUpperCase();
                    Location originalLoc = entry.getValue();

                    // Convert location to game world
                    Location gameLoc = new Location(
                        gameWorld,
                        originalLoc.getX(),
                        originalLoc.getY(),
                        originalLoc.getZ()
                    );

                    spawnResource(gameLoc, type);
                }
            }
        };

        // Start task - runs every 2 seconds initially (we'll make this configurable)
        task.runTaskTimer(plugin, 0L, 40L);
        activeTasks.put(arenaId, task);
        arena.setGeneratorTaskId(arenaId);
    }

    public void stopGenerators(Arena arena) {
        UUID taskId = arena.getGeneratorTaskId();
        if (taskId != null && activeTasks.containsKey(taskId)) {
            activeTasks.get(taskId).cancel();
            activeTasks.remove(taskId);
            arena.setGeneratorTaskId(null);
        }
    }

    private void spawnResource(Location location, String type) {
        Material material;

        switch (type) {
            case "IRON":
                material = Material.IRON_INGOT;
                break;
            case "GOLD":
                material = Material.GOLD_INGOT;
                break;
            case "DIAMOND":
                material = Material.DIAMOND;
                break;
            case "EMERALD":
                material = Material.EMERALD;
                break;
            default:
                plugin.getLogger().warning("Unknown generator type: " + type);
                return;
        }

        ItemStack item = new ItemStack(material, 1);
        Item droppedItem = location.getWorld().dropItem(location.clone().add(0.5, 0.5, 0.5), item);
        droppedItem.setVelocity(new Vector(0, 0, 0));
        droppedItem.setPickupDelay(0);

        // Prevent item from despawning
        droppedItem.setCustomName("§e" + type);
        droppedItem.setCustomNameVisible(false);
    }
}

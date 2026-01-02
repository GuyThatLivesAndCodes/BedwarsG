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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class GeneratorManager {

    private final BedwarsG plugin;
    private final Map<UUID, List<BukkitRunnable>> activeTasks;

    public GeneratorManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.activeTasks = new ConcurrentHashMap<>();
    }

    public void startGenerators(Arena arena, World gameWorld) {
        if (arena.getMap().getGenerators().isEmpty()) {
            return;
        }

        UUID arenaId = UUID.randomUUID();
        List<BukkitRunnable> tasks = new ArrayList<>();

        // Group generators by type
        Map<String, List<Location>> generatorsByType = new HashMap<>();
        for (Map.Entry<String, Location> entry : arena.getMap().getGenerators().entrySet()) {
            String generatorId = entry.getKey();
            String type = generatorId.contains("-") ? generatorId.split("-")[0].toUpperCase() : generatorId.toUpperCase();

            generatorsByType.computeIfAbsent(type, k -> new ArrayList<>()).add(entry.getValue());
        }

        // Create a task for each generator type with its own speed
        for (Map.Entry<String, List<Location>> entry : generatorsByType.entrySet()) {
            String type = entry.getKey();
            List<Location> locations = entry.getValue();
            int speed = arena.getMap().getGeneratorSpeed(type);

            BukkitRunnable task = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Location originalLoc : locations) {
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

            task.runTaskTimer(plugin, 0L, speed);
            tasks.add(task);
        }

        activeTasks.put(arenaId, tasks);
        arena.setGeneratorTaskId(arenaId);
    }

    public void stopGenerators(Arena arena) {
        UUID taskId = arena.getGeneratorTaskId();
        if (taskId != null && activeTasks.containsKey(taskId)) {
            List<BukkitRunnable> tasks = activeTasks.get(taskId);
            for (BukkitRunnable task : tasks) {
                task.cancel();
            }
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

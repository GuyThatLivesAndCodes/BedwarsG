package com.guythatlives.bedwarsg.world;

import com.guythatlives.bedwarsg.BedwarsG;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class WorldManager {

    private final BedwarsG plugin;
    private final File mapsFolder;
    private final Set<String> activeGameWorlds;

    public WorldManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.mapsFolder = new File(plugin.getDataFolder(), "map-worlds");
        this.activeGameWorlds = new HashSet<>();

        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
        }
    }

    public boolean saveMapWorld(String mapName, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World " + worldName + " not found!");
            return false;
        }

        // Save the world first
        world.save();

        File sourceWorld = world.getWorldFolder();
        File targetWorld = new File(mapsFolder, mapName);

        // Delete existing backup if it exists
        if (targetWorld.exists()) {
            deleteDirectory(targetWorld);
        }

        try {
            copyDirectory(sourceWorld, targetWorld);
            plugin.getLogger().info("Saved map world: " + mapName + " from " + worldName);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save map world: " + mapName);
            e.printStackTrace();
            return false;
        }
    }

    public String createGameWorld(String mapName) {
        File sourceWorld = new File(mapsFolder, mapName);
        if (!sourceWorld.exists()) {
            plugin.getLogger().warning("Map world not found: " + mapName);
            return null;
        }

        // Generate unique world name
        String worldName = "bedwars-" + UUID.randomUUID().toString().substring(0, 8);
        File targetWorld = new File(Bukkit.getWorldContainer(), worldName);

        try {
            // Copy world files
            copyDirectory(sourceWorld, targetWorld);

            // Remove uid.dat to prevent conflicts
            File uidFile = new File(targetWorld, "uid.dat");
            if (uidFile.exists()) {
                uidFile.delete();
            }

            // Load the world
            WorldCreator creator = new WorldCreator(worldName);
            World world = Bukkit.createWorld(creator);

            if (world == null) {
                plugin.getLogger().severe("Failed to create world: " + worldName);
                deleteDirectory(targetWorld);
                return null;
            }

            // Configure world settings
            world.setAutoSave(false);
            world.setKeepSpawnInMemory(false);

            activeGameWorlds.add(worldName);
            plugin.getLogger().info("Created game world: " + worldName + " from map " + mapName);
            return worldName;

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create game world from map: " + mapName);
            e.printStackTrace();
            return null;
        }
    }

    public void deleteGameWorld(String worldName) {
        if (!activeGameWorlds.contains(worldName)) {
            plugin.getLogger().warning("Attempted to delete non-game world: " + worldName);
            return;
        }

        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            // Teleport all players out of the world
            org.bukkit.Location lobbySpawn = getLobbySpawn();
            if (lobbySpawn != null) {
                for (org.bukkit.entity.Player player : world.getPlayers()) {
                    player.teleport(lobbySpawn);
                }
            }

            // Unload the world
            Bukkit.unloadWorld(world, false);
        }

        // Delete world folder
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists()) {
            deleteDirectory(worldFolder);
            plugin.getLogger().info("Deleted game world: " + worldName);
        }

        activeGameWorlds.remove(worldName);
    }

    public void deleteAllGameWorlds() {
        plugin.getLogger().info("Cleaning up all game worlds...");
        List<String> worldsToDelete = new ArrayList<>(activeGameWorlds);
        for (String worldName : worldsToDelete) {
            deleteGameWorld(worldName);
        }
    }

    private void copyDirectory(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }

        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File targetFile = new File(target, file.getName());

                // Skip session.lock and uid.dat
                if (file.getName().equals("session.lock") || file.getName().equals("uid.dat")) {
                    continue;
                }

                if (file.isDirectory()) {
                    copyDirectory(file, targetFile);
                } else {
                    Files.copy(file.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    private org.bukkit.Location getLobbySpawn() {
        String world = plugin.getConfigManager().getString("settings.lobby-spawn.world");
        double x = plugin.getConfigManager().getDouble("settings.lobby-spawn.x");
        double y = plugin.getConfigManager().getDouble("settings.lobby-spawn.y");
        double z = plugin.getConfigManager().getDouble("settings.lobby-spawn.z");
        float yaw = (float) plugin.getConfigManager().getDouble("settings.lobby-spawn.yaw");
        float pitch = (float) plugin.getConfigManager().getDouble("settings.lobby-spawn.pitch");

        org.bukkit.World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            return null;
        }

        return new org.bukkit.Location(bukkitWorld, x, y, z, yaw, pitch);
    }

    public Set<String> getActiveGameWorlds() {
        return new HashSet<>(activeGameWorlds);
    }

    public File getMapsFolder() {
        return mapsFolder;
    }
}

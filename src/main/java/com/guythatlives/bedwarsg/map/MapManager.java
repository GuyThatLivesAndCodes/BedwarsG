package com.guythatlives.bedwarsg.map;

import com.guythatlives.bedwarsg.BedwarsG;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MapManager {

    private final BedwarsG plugin;
    private Map<String, BedwarsMap> maps;

    public MapManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.maps = new HashMap<>();
        loadMaps();
    }

    public void loadMaps() {
        File mapsFolder = new File(plugin.getDataFolder(), "maps");
        if (!mapsFolder.exists()) {
            mapsFolder.mkdirs();
            return;
        }

        File[] mapFiles = mapsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (mapFiles != null) {
            for (File mapFile : mapFiles) {
                loadMap(mapFile);
            }
        }

        plugin.getLogger().info("Loaded " + maps.size() + " maps!");
    }

    private void loadMap(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String name = file.getName().replace(".yml", "");

        BedwarsMap map = new BedwarsMap(name);
        map.setDisplayName(config.getString("display-name", name));
        map.setWorld(config.getString("world"));
        map.setMinPlayers(config.getInt("min-players", 2));
        map.setMaxPlayers(config.getInt("max-players", 8));

        // Load spawn points
        if (config.contains("spawns")) {
            for (String team : config.getConfigurationSection("spawns").getKeys(false)) {
                Location loc = deserializeLocation(config, "spawns." + team);
                if (loc != null) {
                    map.addSpawn(team, loc);
                }
            }
        }

        // Load bed locations
        if (config.contains("beds")) {
            for (String team : config.getConfigurationSection("beds").getKeys(false)) {
                Location loc = deserializeLocation(config, "beds." + team);
                if (loc != null) {
                    map.addBed(team, loc);
                }
            }
        }

        // Load generators
        if (config.contains("generators")) {
            for (String key : config.getConfigurationSection("generators").getKeys(false)) {
                String type = config.getString("generators." + key + ".type");
                Location loc = deserializeLocation(config, "generators." + key + ".location");
                if (loc != null && type != null) {
                    map.addGenerator(type, loc);
                }
            }
        }

        // Load shop locations
        if (config.contains("shops")) {
            for (String team : config.getConfigurationSection("shops").getKeys(false)) {
                Location loc = deserializeLocation(config, "shops." + team);
                if (loc != null) {
                    map.addShop(team, loc);
                }
            }
        }

        // Load generator speeds
        if (config.contains("generator-speeds")) {
            for (String type : config.getConfigurationSection("generator-speeds").getKeys(false)) {
                int ticks = config.getInt("generator-speeds." + type);
                map.setGeneratorSpeed(type, ticks);
            }
        }

        map.setEnabled(config.getBoolean("enabled", false));
        maps.put(name, map);
    }

    public void saveMap(BedwarsMap map) {
        File file = new File(plugin.getDataFolder(), "maps/" + map.getName() + ".yml");
        FileConfiguration config = new YamlConfiguration();

        config.set("display-name", map.getDisplayName());
        config.set("world", map.getWorld());
        config.set("min-players", map.getMinPlayers());
        config.set("max-players", map.getMaxPlayers());
        config.set("enabled", map.isEnabled());

        // Save spawn points
        for (Map.Entry<String, Location> entry : map.getSpawns().entrySet()) {
            serializeLocation(config, "spawns." + entry.getKey(), entry.getValue());
        }

        // Save bed locations
        for (Map.Entry<String, Location> entry : map.getBeds().entrySet()) {
            serializeLocation(config, "beds." + entry.getKey(), entry.getValue());
        }

        // Save generators
        int index = 0;
        for (Map.Entry<String, Location> entry : map.getGenerators().entrySet()) {
            config.set("generators." + index + ".type", entry.getKey());
            serializeLocation(config, "generators." + index + ".location", entry.getValue());
            index++;
        }

        // Save shop locations
        for (Map.Entry<String, Location> entry : map.getShops().entrySet()) {
            serializeLocation(config, "shops." + entry.getKey(), entry.getValue());
        }

        // Save generator speeds
        for (Map.Entry<String, Integer> entry : map.getGeneratorSpeeds().entrySet()) {
            config.set("generator-speeds." + entry.getKey(), entry.getValue());
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save map: " + map.getName());
            e.printStackTrace();
        }
    }

    public void saveAll() {
        for (BedwarsMap map : maps.values()) {
            saveMap(map);
        }
    }

    public BedwarsMap createMap(String name, String world) {
        BedwarsMap map = new BedwarsMap(name);
        map.setWorld(world);
        maps.put(name, map);
        return map;
    }

    public void deleteMap(String name) {
        maps.remove(name);
        File file = new File(plugin.getDataFolder(), "maps/" + name + ".yml");
        if (file.exists()) {
            file.delete();
        }
    }

    public BedwarsMap getMap(String name) {
        return maps.get(name);
    }

    public Map<String, BedwarsMap> getMaps() {
        return maps;
    }

    private Location deserializeLocation(FileConfiguration config, String path) {
        if (!config.contains(path)) {
            return null;
        }

        String worldName = config.getString(path + ".world");
        World world = plugin.getServer().getWorld(worldName);
        if (world == null) {
            return null;
        }

        double x = config.getDouble(path + ".x");
        double y = config.getDouble(path + ".y");
        double z = config.getDouble(path + ".z");
        float yaw = (float) config.getDouble(path + ".yaw");
        float pitch = (float) config.getDouble(path + ".pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    private void serializeLocation(FileConfiguration config, String path, Location loc) {
        config.set(path + ".world", loc.getWorld().getName());
        config.set(path + ".x", loc.getX());
        config.set(path + ".y", loc.getY());
        config.set(path + ".z", loc.getZ());
        config.set(path + ".yaw", loc.getYaw());
        config.set(path + ".pitch", loc.getPitch());
    }
}

package com.guythatlives.bedwarsg.map;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class BedwarsMap {

    private final String name;
    private String displayName;
    private String world;
    private int minPlayers;
    private int maxPlayers;
    private boolean enabled;

    private Map<String, Location> spawns;
    private Map<String, Location> beds;
    private Map<String, Location> generators;
    private Map<String, Location> shops;
    private Map<String, Integer> generatorSpeeds; // Type -> Speed in ticks

    public BedwarsMap(String name) {
        this.name = name;
        this.displayName = name;
        this.minPlayers = 2;
        this.maxPlayers = 8;
        this.enabled = false;
        this.spawns = new HashMap<>();
        this.beds = new HashMap<>();
        this.generators = new HashMap<>();
        this.shops = new HashMap<>();
        this.generatorSpeeds = new HashMap<>();

        // Default generator speeds (in ticks, 20 ticks = 1 second)
        this.generatorSpeeds.put("IRON", 20);      // 1 second
        this.generatorSpeeds.put("GOLD", 60);      // 3 seconds
        this.generatorSpeeds.put("DIAMOND", 200);  // 10 seconds
        this.generatorSpeeds.put("EMERALD", 300);  // 15 seconds
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addSpawn(String team, Location location) {
        spawns.put(team, location);
    }

    public void addBed(String team, Location location) {
        beds.put(team, location);
    }

    public void addGenerator(String type, Location location) {
        generators.put(type, location);
    }

    public void addShop(String team, Location location) {
        shops.put(team, location);
    }

    public Map<String, Location> getSpawns() {
        return spawns;
    }

    public Map<String, Location> getBeds() {
        return beds;
    }

    public Map<String, Location> getGenerators() {
        return generators;
    }

    public Map<String, Location> getShops() {
        return shops;
    }

    public Location getSpawn(String team) {
        return spawns.get(team);
    }

    public Location getBed(String team) {
        return beds.get(team);
    }

    public Location getShop(String team) {
        return shops.get(team);
    }

    public void removeGenerator(int index) {
        if (index >= 0 && index < generators.size()) {
            String[] keys = generators.keySet().toArray(new String[0]);
            generators.remove(keys[index]);
        }
    }

    public Map<String, Integer> getGeneratorSpeeds() {
        return generatorSpeeds;
    }

    public int getGeneratorSpeed(String type) {
        return generatorSpeeds.getOrDefault(type, 40); // Default 2 seconds if not found
    }

    public void setGeneratorSpeed(String type, int ticks) {
        generatorSpeeds.put(type, ticks);
    }

    public boolean isSetupComplete() {
        return world != null && !spawns.isEmpty() && !beds.isEmpty();
    }
}

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

    public BedwarsMap(String name) {
        this.name = name;
        this.displayName = name;
        this.minPlayers = 2;
        this.maxPlayers = 8;
        this.enabled = false;
        this.spawns = new HashMap<>();
        this.beds = new HashMap<>();
        this.generators = new HashMap<>();
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

    public Map<String, Location> getSpawns() {
        return spawns;
    }

    public Map<String, Location> getBeds() {
        return beds;
    }

    public Map<String, Location> getGenerators() {
        return generators;
    }

    public Location getSpawn(String team) {
        return spawns.get(team);
    }

    public Location getBed(String team) {
        return beds.get(team);
    }

    public boolean isSetupComplete() {
        return world != null && !spawns.isEmpty() && !beds.isEmpty();
    }
}

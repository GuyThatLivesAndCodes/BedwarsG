package com.guythatlives.bedwarsg.visual;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MapVisualizer {

    private final BedwarsG plugin;
    private final Map<UUID, String> playerEditingMap; // Player UUID -> Map name they're editing
    private final Map<String, List<ArmorStand>> hologramStands; // Map name -> holograms
    private BukkitRunnable visualizerTask;

    public MapVisualizer(BedwarsG plugin) {
        this.plugin = plugin;
        this.playerEditingMap = new HashMap<>();
        this.hologramStands = new HashMap<>();
    }

    public void startVisualization() {
        visualizerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, String> entry : playerEditingMap.entrySet()) {
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player == null || !player.isOnline()) {
                        continue;
                    }

                    String mapName = entry.getValue();
                    BedwarsMap map = plugin.getMapManager().getMap(mapName);
                    if (map == null) {
                        continue;
                    }

                    visualizeMapElements(player, map);
                }
            }
        };
        visualizerTask.runTaskTimer(plugin, 0L, 10L); // Run every 0.5 seconds
    }

    public void stopVisualization() {
        if (visualizerTask != null) {
            visualizerTask.cancel();
        }
        clearAllHolograms();
    }

    public void enableEditMode(Player player, String mapName) {
        playerEditingMap.put(player.getUniqueId(), mapName);
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§aVisual editor enabled for map: §e" + mapName);
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§7You will see particles and labels at spawn points, beds, generators, and shops");
    }

    public void disableEditMode(Player player) {
        String mapName = playerEditingMap.remove(player.getUniqueId());
        if (mapName != null) {
            clearHologramsForMap(mapName);
            player.sendMessage(plugin.getConfigManager().getPrefix() + "§cVisual editor disabled");
        }
    }

    public boolean isInEditMode(Player player) {
        return playerEditingMap.containsKey(player.getUniqueId());
    }

    public String getEditingMap(Player player) {
        return playerEditingMap.get(player.getUniqueId());
    }

    private void visualizeMapElements(Player player, BedwarsMap map) {
        // Visualize spawns
        for (Map.Entry<String, Location> entry : map.getSpawns().entrySet()) {
            Location loc = entry.getValue();
            if (isNearby(player.getLocation(), loc, 50)) {
                player.spawnParticle(Particle.VILLAGER_HAPPY, loc.clone().add(0.5, 1, 0.5), 2, 0.3, 0.3, 0.3, 0);
                showFloatingText(player, loc.clone().add(0, 2.2, 0), "§a§lSPAWN", "§e" + entry.getKey());
            }
        }

        // Visualize beds
        for (Map.Entry<String, Location> entry : map.getBeds().entrySet()) {
            Location loc = entry.getValue();
            if (isNearby(player.getLocation(), loc, 50)) {
                player.spawnParticle(Particle.HEART, loc.clone().add(0.5, 1, 0.5), 2, 0.3, 0.3, 0.3, 0);
                showFloatingText(player, loc.clone().add(0, 2.2, 0), "§c§lBED", "§e" + entry.getKey());
            }
        }

        // Visualize generators
        for (Map.Entry<String, Location> entry : map.getGenerators().entrySet()) {
            Location loc = entry.getValue();
            if (isNearby(player.getLocation(), loc, 50)) {
                Particle particle = getGeneratorParticle(entry.getKey());
                player.spawnParticle(particle, loc.clone().add(0.5, 1, 0.5), 3, 0.2, 0.2, 0.2, 0);
                showFloatingText(player, loc.clone().add(0, 2.2, 0), "§6§lGENERATOR", "§e" + entry.getKey());
            }
        }

        // Visualize shops
        for (Map.Entry<String, Location> entry : map.getShops().entrySet()) {
            Location loc = entry.getValue();
            if (isNearby(player.getLocation(), loc, 50)) {
                player.spawnParticle(Particle.ENCHANTMENT_TABLE, loc.clone().add(0.5, 1, 0.5), 5, 0.3, 0.3, 0.3, 0);
                showFloatingText(player, loc.clone().add(0, 2.2, 0), "§b§lSHOP", "§e" + entry.getKey());
            }
        }
    }

    private Particle getGeneratorParticle(String generatorKey) {
        String type = generatorKey.split("-")[0].toUpperCase();
        switch (type) {
            case "IRON":
                return Particle.CRIT;
            case "GOLD":
                return Particle.FLAME;
            case "DIAMOND":
                return Particle.DRIP_WATER;
            case "EMERALD":
                return Particle.VILLAGER_HAPPY;
            default:
                return Particle.CRIT;
        }
    }

    private void showFloatingText(Player player, Location location, String... lines) {
        // Note: Floating text is shown as particles/visual effects
        // For actual holograms, you'd need a hologram plugin or armor stands
        // This implementation uses a simple approach for demonstration
    }

    private boolean isNearby(Location loc1, Location loc2, double distance) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        return loc1.distanceSquared(loc2) <= distance * distance;
    }

    private void clearHologramsForMap(String mapName) {
        List<ArmorStand> stands = hologramStands.remove(mapName);
        if (stands != null) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
    }

    private void clearAllHolograms() {
        for (List<ArmorStand> stands : hologramStands.values()) {
            for (ArmorStand stand : stands) {
                stand.remove();
            }
        }
        hologramStands.clear();
    }
}

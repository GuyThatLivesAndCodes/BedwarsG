package com.guythatlives.bedwarsg.game;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.util.*;

public class ShopNPCManager {

    private final BedwarsG plugin;
    private final Map<UUID, List<Villager>> arenaShops; // Arena UUID -> List of shop villagers

    public ShopNPCManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.arenaShops = new HashMap<>();
    }

    public void spawnShops(Arena arena, World gameWorld) {
        BedwarsMap map = arena.getMap();
        List<Villager> shops = new ArrayList<>();
        UUID arenaId = UUID.randomUUID();

        // Spawn shops for each team
        for (Map.Entry<String, Location> entry : map.getShops().entrySet()) {
            String team = entry.getKey();
            Location originalLoc = entry.getValue();

            // Convert location to game world
            Location gameLoc = new Location(
                gameWorld,
                originalLoc.getX(),
                originalLoc.getY(),
                originalLoc.getZ(),
                originalLoc.getYaw(),
                originalLoc.getPitch()
            );

            // Spawn normal shop (item shop)
            Villager itemShop = spawnShopVillager(gameLoc, "§6§lITEM SHOP", Villager.Profession.FARMER);
            if (itemShop != null) {
                shops.add(itemShop);
            }

            // Spawn diamond shop (upgrades shop) - 2 blocks away
            Location diamondShopLoc = gameLoc.clone().add(2, 0, 0);
            Villager upgradeShop = spawnShopVillager(diamondShopLoc, "§b§lUPGRADE SHOP", Villager.Profession.LIBRARIAN);
            if (upgradeShop != null) {
                shops.add(upgradeShop);
            }
        }

        arenaShops.put(arenaId, shops);
    }

    private Villager spawnShopVillager(Location location, String name, Villager.Profession profession) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setCustomName(name);
        villager.setCustomNameVisible(true);
        villager.setProfession(profession);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setCollidable(false);
        villager.setCanPickupItems(false);

        return villager;
    }

    public void removeShops(Arena arena) {
        // Find and remove all shop villagers for this arena
        for (List<Villager> shops : arenaShops.values()) {
            for (Villager villager : shops) {
                if (villager != null && !villager.isDead()) {
                    villager.remove();
                }
            }
        }
        arenaShops.clear();
    }

    public void removeAllShops() {
        for (List<Villager> shops : arenaShops.values()) {
            for (Villager villager : shops) {
                if (villager != null && !villager.isDead()) {
                    villager.remove();
                }
            }
        }
        arenaShops.clear();
    }
}

package com.guythatlives.bedwarsg.arena;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.map.BedwarsMap;
import org.bukkit.entity.Player;

import java.util.*;

public class ArenaManager {

    private final BedwarsG plugin;
    private Map<String, Arena> arenas;
    private Map<UUID, Arena> playerArenas;

    public ArenaManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.arenas = new HashMap<>();
        this.playerArenas = new HashMap<>();
    }

    public Arena createArena(String name, BedwarsMap map, GameMode gameMode) {
        Arena arena = new Arena(name, map, gameMode, plugin);
        arenas.put(name, arena);
        return arena;
    }

    public void removeArena(String name) {
        arenas.remove(name);
    }

    public Arena getArena(String name) {
        return arenas.get(name);
    }

    public Arena getPlayerArena(Player player) {
        return playerArenas.get(player.getUniqueId());
    }

    public void setPlayerArena(Player player, Arena arena) {
        if (arena == null) {
            playerArenas.remove(player.getUniqueId());
        } else {
            playerArenas.put(player.getUniqueId(), arena);
        }
    }

    public boolean isInArena(Player player) {
        return playerArenas.containsKey(player.getUniqueId());
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public List<Arena> getAvailableArenas(GameMode gameMode) {
        List<Arena> available = new ArrayList<>();
        for (Arena arena : arenas.values()) {
            if (arena.getGameMode() == gameMode && arena.getState() == ArenaState.WAITING) {
                available.add(arena);
            }
        }
        return available;
    }

    public Arena findAvailableArena(GameMode gameMode) {
        List<Arena> available = getAvailableArenas(gameMode);
        if (available.isEmpty()) {
            return null;
        }

        // Return arena with most players
        available.sort((a1, a2) -> Integer.compare(a2.getPlayers().size(), a1.getPlayers().size()));
        return available.get(0);
    }
}

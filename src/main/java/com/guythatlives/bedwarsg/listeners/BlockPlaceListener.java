package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final BedwarsG plugin;

    public BlockPlaceListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        if (arena == null) {
            return;
        }

        if (arena.getState() != ArenaState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        // Track player-placed blocks so they can be broken
        arena.getPlayerPlacedBlocks().add(event.getBlock().getLocation());
    }
}

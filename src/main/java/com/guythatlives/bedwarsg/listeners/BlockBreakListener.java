package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import com.guythatlives.bedwarsg.game.Game;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;

public class BlockBreakListener implements Listener {

    private final BedwarsG plugin;

    public BlockBreakListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        if (arena == null) {
            return;
        }

        if (arena.getState() != ArenaState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();

        // Check if it's a bed (any color)
        if (block.getType().name().contains("_BED")) {
            handleBedBreak(event, player, arena, block);
        }
    }

    private void handleBedBreak(BlockBreakEvent event, Player player, Arena arena, Block block) {
        BedwarsTeam breakerTeam = arena.getPlayerTeam(player);

        // Find which team's bed was broken
        for (BedwarsTeam team : arena.getTeams().values()) {
            org.bukkit.Location bedLoc = arena.getMap().getBed(team.getColor());
            if (bedLoc != null && bedLoc.getBlock().getLocation().equals(block.getLocation())) {
                // Can't break own bed
                if (team.equals(breakerTeam)) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou can't break your own bed!");
                    return;
                }

                // Break the bed
                team.setBedAlive(false);
                Game game = plugin.getGameManager().getGame(arena);
                if (game != null) {
                    game.addBedDestroyed(player);
                }

                // Announce
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("player", player.getName());
                placeholders.put("team", team.getColor());
                String message = plugin.getConfigManager().getMessage("game.enemy-bed-destroyed", placeholders);

                for (Player p : arena.getPlayers()) {
                    p.sendMessage(message);
                }

                // Send message to team whose bed was destroyed
                message = plugin.getConfigManager().getMessage("game.bed-destroyed");
                for (java.util.UUID uuid : team.getPlayers()) {
                    Player teamPlayer = plugin.getServer().getPlayer(uuid);
                    if (teamPlayer != null) {
                        teamPlayer.sendMessage(message);
                    }
                }
                return;
            }
        }
    }
}

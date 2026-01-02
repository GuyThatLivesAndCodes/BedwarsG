package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import com.guythatlives.bedwarsg.game.Game;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

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
        Location blockLoc = block.getLocation();

        // Check if it's a bed (any color)
        if (block.getType().name().contains("_BED")) {
            handleBedBreak(event, player, arena, block);
            return;
        }

        // Check if player placed this block
        if (arena.getPlayerPlacedBlocks().contains(blockLoc)) {
            // Allow breaking player-placed blocks
            arena.getPlayerPlacedBlocks().remove(blockLoc);
            return;
        }

        // Check if it's an ore
        if (isOre(block.getType())) {
            handleOreBreak(event, player, arena, block);
            return;
        }

        // Protect all other base island blocks
        event.setCancelled(true);
        player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou can only break player-placed blocks and ores!");
    }

    private void handleBedBreak(BlockBreakEvent event, Player player, Arena arena, Block block) {
        BedwarsTeam breakerTeam = arena.getPlayerTeam(player);

        // Find which team's bed was broken
        for (BedwarsTeam team : arena.getTeams().values()) {
            org.bukkit.Location bedLoc = arena.getMap().getBed(team.getColor());
            if (bedLoc != null) {
                Block bedBlock = bedLoc.getBlock();
                Block clickedBlock = block;

                // Check if the clicked block is the bed or its other half
                boolean isBedBlock = false;
                if (bedBlock.getLocation().equals(clickedBlock.getLocation())) {
                    isBedBlock = true;
                } else if (clickedBlock.getType().name().contains("_BED")) {
                    // Check if it's the other half of the bed
                    org.bukkit.block.data.BlockData blockData = clickedBlock.getBlockData();
                    if (blockData instanceof org.bukkit.block.data.type.Bed) {
                        org.bukkit.block.data.type.Bed bedData = (org.bukkit.block.data.type.Bed) blockData;
                        org.bukkit.block.BlockFace facing = bedData.getFacing();

                        // Get the other half of the bed
                        Block otherHalf;
                        if (bedData.getPart() == org.bukkit.block.data.type.Bed.Part.HEAD) {
                            otherHalf = clickedBlock.getRelative(facing.getOppositeFace());
                        } else {
                            otherHalf = clickedBlock.getRelative(facing);
                        }

                        if (bedBlock.getLocation().equals(otherHalf.getLocation())) {
                            isBedBlock = true;
                        }
                    }
                }

                if (isBedBlock) {
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

    private boolean isOre(Material material) {
        return material == Material.IRON_ORE || material == Material.GOLD_ORE ||
               material == Material.DIAMOND_ORE || material == Material.EMERALD_ORE ||
               material == Material.COAL_ORE || material == Material.REDSTONE_ORE ||
               material == Material.LAPIS_ORE || material == Material.NETHER_QUARTZ_ORE;
    }

    private void handleOreBreak(BlockBreakEvent event, Player player, Arena arena, Block block) {
        Material oreType = block.getType();
        Location blockLoc = block.getLocation().clone();

        // Drop extra XP
        event.setExpToDrop(event.getExpToDrop() + 3);

        // Schedule ore regeneration after 30 seconds if no player is overlapping
        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if any player is at this location
                boolean playerOverlapping = false;
                for (Player p : arena.getPlayers()) {
                    Location playerLoc = p.getLocation();
                    if (playerLoc.getWorld().equals(blockLoc.getWorld()) &&
                        playerLoc.distance(blockLoc) < 1.5) {
                        playerOverlapping = true;
                        break;
                    }
                }

                // Regenerate ore if no player is overlapping
                if (!playerOverlapping && arena.getState() == ArenaState.RUNNING) {
                    blockLoc.getBlock().setType(oreType);
                }
            }
        }.runTaskLater(plugin, 30 * 20L); // 30 seconds = 600 ticks
    }
}

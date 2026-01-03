package com.guythatlives.bedwarsg.game;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoidDeathTask extends BukkitRunnable {

    private final BedwarsG plugin;
    private final Map<UUID, Long> respawnProtection; // Track when players last respawned
    private static final long PROTECTION_TIME = 3000; // 3 seconds protection after respawn
    private final double voidDeathY; // Y level below which players instantly die

    public VoidDeathTask(BedwarsG plugin) {
        this.plugin = plugin;
        this.respawnProtection = new HashMap<>();
        this.voidDeathY = plugin.getConfigManager().getDouble("settings.void-death-y");
    }

    @Override
    public void run() {
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getState() != ArenaState.RUNNING) {
                continue;
            }

            for (Player player : arena.getPlayers()) {
                // Skip spectators
                if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) {
                    continue;
                }

                // Check respawn protection
                Long protectedUntil = respawnProtection.get(player.getUniqueId());
                if (protectedUntil != null && System.currentTimeMillis() < protectedUntil) {
                    continue; // Player has respawn protection
                }

                // Check if player is below void death Y level
                if (player.getLocation().getY() < voidDeathY) {
                    // Instant kill in void
                    player.setHealth(0);
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou fell into the void!");
                }
            }
        }
    }

    /**
     * Grant respawn protection to a player
     */
    public void grantRespawnProtection(Player player) {
        respawnProtection.put(player.getUniqueId(), System.currentTimeMillis() + PROTECTION_TIME);
    }

    /**
     * Remove respawn protection from a player
     */
    public void removeRespawnProtection(Player player) {
        respawnProtection.remove(player.getUniqueId());
    }

    /**
     * Clear all respawn protections (called when task is cancelled)
     */
    public void clearProtections() {
        respawnProtection.clear();
    }
}

package com.guythatlives.bedwarsg.game;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class VoidDeathTask extends BukkitRunnable {

    private final BedwarsG plugin;

    public VoidDeathTask(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Arena arena : plugin.getArenaManager().getArenas()) {
            if (arena.getState() != ArenaState.RUNNING) {
                continue;
            }

            for (Player player : arena.getPlayers()) {
                // Check if player is below Y=0 (void)
                if (player.getLocation().getY() < 0) {
                    // Instant kill in void
                    player.setHealth(0);
                    player.sendMessage(plugin.getConfigManager().getPrefix() + "§cYou fell into the void!");
                }
            }
        }
    }
}

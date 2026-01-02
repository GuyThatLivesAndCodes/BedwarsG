package com.guythatlives.bedwarsg.listeners;

import com.guythatlives.bedwarsg.BedwarsG;
import com.guythatlives.bedwarsg.arena.Arena;
import com.guythatlives.bedwarsg.arena.ArenaState;
import com.guythatlives.bedwarsg.arena.BedwarsTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final BedwarsG plugin;

    public EntityDamageListener(BedwarsG plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Arena arena = plugin.getArenaManager().getPlayerArena(player);

        if (arena == null) {
            return;
        }

        // Prevent damage when not running
        if (arena.getState() != ArenaState.RUNNING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        Arena arena = plugin.getArenaManager().getPlayerArena(victim);
        if (arena == null || arena.getState() != ArenaState.RUNNING) {
            return;
        }

        // Prevent team damage
        BedwarsTeam victimTeam = arena.getPlayerTeam(victim);
        BedwarsTeam damagerTeam = arena.getPlayerTeam(damager);

        if (victimTeam != null && victimTeam.equals(damagerTeam)) {
            event.setCancelled(true);
        }
    }
}

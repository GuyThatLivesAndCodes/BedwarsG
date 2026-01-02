package com.guythatlives.bedwarsg.party;

import org.bukkit.entity.Player;

import java.util.*;

public class Party {

    private final UUID leaderUUID;
    private final Set<UUID> members;
    private final int maxSize;

    public Party(Player leader) {
        this.leaderUUID = leader.getUniqueId();
        this.members = new HashSet<>();
        this.members.add(leader.getUniqueId());
        this.maxSize = 8;
    }

    public void addMember(Player player) {
        members.add(player.getUniqueId());
    }

    public void removeMember(Player player) {
        members.remove(player.getUniqueId());
    }

    public boolean isLeader(Player player) {
        return leaderUUID.equals(player.getUniqueId());
    }

    public Player getLeader() {
        return org.bukkit.Bukkit.getPlayer(leaderUUID);
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public List<Player> getOnlineMembers() {
        List<Player> online = new ArrayList<>();
        for (UUID uuid : members) {
            Player player = org.bukkit.Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                online.add(player);
            }
        }
        return online;
    }

    public int getSize() {
        return members.size();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public boolean isMember(Player player) {
        return members.contains(player.getUniqueId());
    }

    public void broadcast(String message) {
        for (Player player : getOnlineMembers()) {
            player.sendMessage(message);
        }
    }
}

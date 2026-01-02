package com.guythatlives.bedwarsg.party;

import com.guythatlives.bedwarsg.BedwarsG;
import org.bukkit.entity.Player;

import java.util.*;

public class PartyManager {

    private final BedwarsG plugin;
    private Map<UUID, Party> parties;
    private Map<UUID, Party> playerParties;
    private Map<UUID, Map<UUID, Long>> partyInvites;

    public PartyManager(BedwarsG plugin) {
        this.plugin = plugin;
        this.parties = new HashMap<>();
        this.playerParties = new HashMap<>();
        this.partyInvites = new HashMap<>();
    }

    public Party createParty(Player leader) {
        Party party = new Party(leader);
        parties.put(leader.getUniqueId(), party);
        playerParties.put(leader.getUniqueId(), party);
        return party;
    }

    public void disbandParty(Party party) {
        for (UUID memberUUID : party.getMembers()) {
            playerParties.remove(memberUUID);
        }
        parties.remove(party.getLeader().getUniqueId());
    }

    public void addToParty(Party party, Player player) {
        party.addMember(player);
        playerParties.put(player.getUniqueId(), party);
    }

    public void removeFromParty(Party party, Player player) {
        party.removeMember(player);
        playerParties.remove(player.getUniqueId());

        if (party.getMembers().size() <= 1) {
            disbandParty(party);
        }
    }

    public Party getParty(Player player) {
        return playerParties.get(player.getUniqueId());
    }

    public boolean isInParty(Player player) {
        return playerParties.containsKey(player.getUniqueId());
    }

    public boolean isPartyLeader(Player player) {
        Party party = getParty(player);
        return party != null && party.isLeader(player);
    }

    public void sendInvite(Player from, Player to) {
        partyInvites.computeIfAbsent(to.getUniqueId(), k -> new HashMap<>())
                .put(from.getUniqueId(), System.currentTimeMillis());
    }

    public boolean hasInvite(Player to, Player from) {
        Map<UUID, Long> invites = partyInvites.get(to.getUniqueId());
        if (invites == null) {
            return false;
        }

        Long inviteTime = invites.get(from.getUniqueId());
        if (inviteTime == null) {
            return false;
        }

        // Invites expire after 60 seconds
        if (System.currentTimeMillis() - inviteTime > 60000) {
            invites.remove(from.getUniqueId());
            return false;
        }

        return true;
    }

    public void removeInvite(Player to, Player from) {
        Map<UUID, Long> invites = partyInvites.get(to.getUniqueId());
        if (invites != null) {
            invites.remove(from.getUniqueId());
        }
    }

    public void clearInvites(Player player) {
        partyInvites.remove(player.getUniqueId());
    }
}

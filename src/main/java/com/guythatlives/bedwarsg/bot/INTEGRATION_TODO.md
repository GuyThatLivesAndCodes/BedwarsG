# Bot System Integration TODOs

This file outlines what needs to be done to complete the bot system integration with actual NPC spawning.

## Required: NPC Library Integration

The bot system is fully implemented with AI logic, behavior management, and configuration. However, it needs an NPC library to spawn actual player entities in the game.

### Primary Integration Point: `BotManager.spawnBot()`

**File**: `src/main/java/com/guythatlives/bedwarsg/bot/BotManager.java`
**Method**: `spawnBot(BotPlayer bot, Arena arena)`
**Line**: ~150

**Current Status**: Placeholder implementation
**What's Needed**: Replace TODO comment with actual NPC spawning code

### Integration Steps

#### 1. Choose NPC Library
- **Recommended**: Citizens API (most popular, well-documented)
- **Alternative**: ProtocolLib (more control, more complex)
- **Testing Only**: Virtual bots (no visual representation)

#### 2. Add Dependency (for Citizens)
Add to `pom.xml`:
```xml
<dependency>
    <groupId>net.citizensnpcs</groupId>
    <artifactId>citizens-main</artifactId>
    <version>2.0.30-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>

<repository>
    <id>everything</id>
    <url>https://repo.citizensnpcs.co/</url>
</repository>
```

Add to `plugin.yml`:
```yaml
depend: [Citizens]
# or
softdepend: [Citizens]  # if Citizens is optional
```

#### 3. Implement NPC Spawning

Replace the TODO section in `BotManager.spawnBot()` with:

```java
// Create Citizens NPC
NPCRegistry registry = CitizensAPI.getNPCRegistry();
NPC npc = registry.createNPC(EntityType.PLAYER, bot.getName());

// Configure NPC
npc.data().set(NPC.NAMEPLATE_VISIBLE_METADATA, false); // Hide name tag if desired
npc.setProtected(false); // Allow damage

// Spawn NPC at location
npc.spawn(spawnLoc);

// Store NPC reference (add field to BotPlayer class)
bot.setNPC(npc); // You'll need to add this method

// Add bot to arena's player list
Player npcPlayer = (Player) npc.getEntity();
arena.addPlayer(npcPlayer);

// Assign to team
BedwarsTeam team = assignBotToTeam(arena);
if (team != null) {
    team.addPlayer(npcPlayer.getUniqueId());
}
```

#### 4. Update BotPlayer Class

**File**: `src/main/java/com/guythatlives/bedwarsg/bot/BotPlayer.java`

Add NPC field and methods:
```java
private NPC npc; // Add this field

public void setNPC(NPC npc) {
    this.npc = npc;
}

public NPC getNPC() {
    return npc;
}

// Update getPlayer() method:
@Override
public Player getPlayer() {
    if (npc != null && npc.isSpawned()) {
        return (Player) npc.getEntity();
    }
    return null;
}

// Update cleanup() method:
@Override
public void cleanup() {
    if (updateTask != null && !updateTask.isCancelled()) {
        updateTask.cancel();
    }

    if (ai != null) {
        ai.cleanup();
    }

    // Remove NPC
    if (npc != null) {
        npc.despawn();
        npc.destroy();
        npc = null;
    }
}
```

#### 5. Handle Bot-Specific Events

You may need to add special handling for bots in event listeners to prevent conflicts or unwanted behavior.

**Example** - In `PlayerDeathListener.java`:
```java
@EventHandler
public void onPlayerDeath(PlayerDeathEvent event) {
    Player player = event.getEntity();

    // Check if player is a bot
    BotManager botManager = plugin.getBotManager();
    boolean isBot = botManager.isBot(player);

    // Handle bot deaths differently if needed
    if (isBot) {
        // Bot-specific death handling
    }

    // ... rest of death handling
}
```

### Testing the Integration

1. **Test Bot Spawning**:
   - Create an arena
   - Join alone
   - Wait for auto-fill-delay (default 60 seconds)
   - Bots should spawn and join your team

2. **Test Bot Behavior**:
   - Start a game with bots
   - Observe bots gathering resources
   - Watch bots switch between passive/aggressive modes
   - Verify bots engage in combat

3. **Test Cleanup**:
   - End a game
   - Verify all bots are removed
   - Check for memory leaks with `/timings`

### Alternative: Virtual Bots (For Testing)

If you want to test the system without Citizens, you can create "virtual" bots:

```java
private void spawnBot(BotPlayer bot, Arena arena) {
    bot.initialize();
    activeBots.put(bot.getUUID(), bot);

    // Create offline player (virtual bot)
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(bot.getUUID());

    // Add to arena's internal tracking
    // Note: This won't create a visible player, but the game logic will recognize them

    BedwarsTeam team = assignBotToTeam(arena);
    if (team != null) {
        team.addPlayer(bot.getUUID());

        plugin.getLogger().info("Virtual bot " + bot.getName() +
                               " assigned to team " + team.getColor());
    }
}
```

**Limitations of Virtual Bots**:
- No visual representation in game
- Can't actually break blocks or attack players
- Useful only for testing bot logic and configuration
- Not suitable for production use

## Additional Considerations

### Performance
- Monitor TPS with bots active
- Adjust `update-rate` if performance issues occur
- Consider limiting max bots based on server capacity

### Balance
- Test different difficulty levels
- Adjust skill parameters based on player feedback
- Fine-tune behavior settings for realistic gameplay

### Integration with Existing Systems
- Ensure bots work with stats tracking
- Verify shop interactions function correctly
- Test bed breaking by bots
- Confirm generator spawning for bot teams

## Questions?

If you encounter issues during integration:
1. Check Citizens API documentation: https://wiki.citizensnpcs.co/
2. Review example plugins using Citizens
3. Test with virtual bots first
4. Check server logs for errors

## Summary

The bot system is **95% complete**. The main remaining task is integrating an NPC library to spawn actual player entities. The recommended approach is using Citizens API, which should take approximately 1-2 hours to integrate and test.

All the complex AI logic, behavior management, and configuration is already implemented and ready to use once NPC spawning is added.

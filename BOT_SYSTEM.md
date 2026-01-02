# BedwarsG Bot System

## Overview

The BedwarsG bot system provides AI-controlled players that can fill empty slots in games when real players don't join in time. Bots behave like real players with configurable difficulty levels and realistic gameplay patterns.

## Features

- **Automatic Game Filling**: Bots automatically join games after a configurable delay if not enough real players join
- **Multiple Difficulty Levels**: EASY, MEDIUM, HARD, and EXPERT with customizable skill parameters
- **Intelligent Behavior**: Bots switch between PASSIVE, AGGRESSIVE, and DEFENSIVE modes
- **Resource Management**: Bots gather resources, visit shops, and purchase items
- **Combat AI**: Bots engage in combat with configurable accuracy and skill
- **Fully Configurable**: All bot behaviors, timings, and skills can be adjusted in config.yml

## Configuration

All bot settings are in `config.yml` under the `bots:` section:

```yaml
bots:
  enabled: true                    # Enable/disable bot system
  auto-fill-delay: 60              # Seconds to wait before adding bots
  max-bots-per-game: 7             # Maximum bots per game
  difficulty: MEDIUM               # Default difficulty
  update-rate: 15                  # Ticks between bot AI updates (affects performance)
```

### Difficulty Levels

Each difficulty has configurable skills (0.0 - 1.0):

- **accuracy**: How well bots aim in combat
- **block-placing-speed**: How fast bots build
- **pvp-skill**: Combat effectiveness
- **decision-speed**: How quickly bots make decisions
- **teamwork**: Coordination with teammates

## Bot Behavior Modes

### PASSIVE Mode
- Focuses on gathering resources near base
- Visits shops to buy items
- Avoids unnecessary combat
- Default starting mode

### AGGRESSIVE Mode
- Actively seeks enemy players
- Attempts to break enemy beds
- Pursues targets across the map
- Higher risk, higher reward

### DEFENSIVE Mode
- Protects own bed and base area
- Patrols around bed location
- Engages enemies that come close
- Responds to threats quickly

Bots automatically switch between modes based on config settings:
- `mode-switch-interval`: How often to check for mode changes (seconds)
- `mode-switch-chance`: Probability of switching modes (0.0 - 1.0)

## Current Implementation Status

### ✅ Completed
- Bot configuration system
- BotPlayer class for individual bot management
- BotAI class with decision-making logic
- BotManager for lifecycle management
- Auto-fill system integrated with arenas
- Difficulty and skill system
- Behavior mode switching
- Resource gathering logic
- Combat AI
- Shop interaction (simulated)

### ⚠️ Requires Additional Integration

The current implementation provides the complete bot AI framework, but **requires an NPC library** to spawn actual player entities. The system is designed to work with:

#### Option 1: Citizens API (Recommended)
[Citizens](https://github.com/CitizensDev/Citizens2) is the most popular NPC plugin for Spigot.

**Integration Steps:**
1. Add Citizens as a dependency in `pom.xml`:
```xml
<dependency>
    <groupId>net.citizensnpcs</groupId>
    <artifactId>citizens-main</artifactId>
    <version>2.0.30-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

2. Add to `plugin.yml`:
```yaml
depend: [Citizens]
```

3. Modify `BotManager.spawnBot()` method:
```java
private void spawnBot(BotPlayer bot, Arena arena) {
    bot.initialize();
    activeBots.put(bot.getUUID(), bot);

    // Get team and spawn location
    BedwarsTeam team = assignBotToTeam(arena);
    Location spawnLoc = arena.getMap().getSpawn(team.getColor());

    // Create Citizens NPC
    NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, bot.getName());
    npc.spawn(spawnLoc);

    // Store NPC reference in bot
    // ... additional setup
}
```

#### Option 2: ProtocolLib (Advanced)
Use [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) to spawn fake players.

**Pros**: No dependency on Citizens, full control
**Cons**: More complex, requires packet management

#### Option 3: Simplified Virtual Bots
For testing or simpler setups, you can use "virtual" bots that exist in game logic but don't have visual representation.

**Modify `BotManager.spawnBot()` to:**
1. Add bot UUID to arena player list
2. Create offline player profile
3. Handle bot as special case in event listeners

## Performance Optimization

### Bot Update Rate
The `update-rate` setting controls how often bots think and act:
- **Lower** (5-10 ticks): More responsive, higher CPU usage
- **Medium** (10-20 ticks): **Recommended** - good balance
- **Higher** (20-30 ticks): Less responsive, lower CPU usage

### Recommended Settings by Server Size

**Small Server (< 20 players)**:
```yaml
update-rate: 10
max-bots-per-game: 7
difficulty: HARD
```

**Medium Server (20-100 players)**:
```yaml
update-rate: 15  # Recommended
max-bots-per-game: 5
difficulty: MEDIUM
```

**Large Server (100+ players)**:
```yaml
update-rate: 20
max-bots-per-game: 3
difficulty: MEDIUM
```

## Bot Names

Customize bot names in config.yml:
```yaml
bots:
  names:
    - "Bot_Alpha"
    - "Bot_Beta"
    - "Steve_Bot"
    - "Alex_Bot"
    # Add custom names here
```

## API Usage

### Check if Player is Bot
```java
BotManager botManager = plugin.getBotManager();
if (botManager.isBot(player)) {
    // Player is a bot
}
```

### Manually Add Bots
```java
Arena arena = plugin.getArenaManager().getArena("arena1");
plugin.getBotManager().addBotsToArena(arena, 3); // Add 3 bots
```

### Remove All Bots from Arena
```java
Arena arena = plugin.getArenaManager().getArena("arena1");
plugin.getBotManager().removeBotsFromArena(arena);
```

## Troubleshooting

### Bots Not Spawning
1. Check `bots.enabled` is `true` in config.yml
2. Verify `auto-fill-delay` has passed
3. Ensure arena has at least one real player
4. Check logs for errors

### Performance Issues
1. Increase `update-rate` to 20-30 ticks
2. Reduce `max-bots-per-game`
3. Lower difficulty level (simpler AI)
4. Check server TPS with `/tps` command

### Bots Behaving Strangely
1. Adjust skill levels for current difficulty
2. Modify behavior settings (reaction-time, combat-range)
3. Check mode-switch settings
4. Review logs for AI errors

## Future Enhancements

Potential features for future development:
- [ ] Team coordination (bots work together)
- [ ] Learning system (bots adapt to player strategies)
- [ ] Custom bot skins
- [ ] Per-arena bot difficulty settings
- [ ] Bot voice chat simulation (messages in chat)
- [ ] Advanced building patterns
- [ ] Bridge building AI
- [ ] Potion usage
- [ ] Ender pearl navigation

## Credits

Bot system developed for BedwarsG plugin. Designed to be efficient, configurable, and easy to integrate.

For support or questions, please open an issue on the GitHub repository.

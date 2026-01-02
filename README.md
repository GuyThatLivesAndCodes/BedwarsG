# BedwarsG
The official GuyThatLives Bedwars Plugin

A comprehensive, feature-rich Bedwars plugin for Spigot 1.8.8+ with support for multiple game modes, parties, custom games, and extensive customization.

## Features

### Dynamic World Management
- **Automatic World Copying** - Each game creates a fresh copy of the map
- **Temporary Game Worlds** - Games run in isolated worlds (e.g., `bedwars-a8f4c2d1`)
- **Auto-Cleanup** - Worlds are automatically deleted when games end
- **Graceful Shutdown** - All games end cleanly when server stops
- **Map Templates** - Save any world as a reusable map template

### Core Game Modes
- **Solo** - 8 teams of 1 player each
- **Doubles** - 8 teams of 2 players each
- **3v3v3v3** - 4 teams of 3 players each
- **4v4v4v4** - 4 teams of 4 players each
- **1v1 Duels** - Challenge other players to 1v1 matches
- **Custom Games** - Create custom game configurations

### BParty System
- Create and manage Bedwars parties
- Invite players to your party
- Join games together as a party
- Party leader controls (kick, disband)
- Maximum party size: 8 players

### Shop System
- **Blocks** - Wool, Hardened Clay, End Stone, Obsidian
- **Weapons** - Swords, Knockback Sticks
- **Armor** - Chainmail, Iron, Diamond armor
- **Tools** - Pickaxes, Axes, Shears
- **Food** - Apples, Golden Apples
- **Special Items** - TNT, Ender Pearls, Water Buckets, Bows & Arrows

### Resource Generators
- **Iron** - Spawns every 1.5 seconds at team generators
- **Gold** - Spawns every 7 seconds at team generators
- **Diamond** - Spawns every 30 seconds at map generators
- **Emerald** - Spawns every 60 seconds at map generators

### Statistics Tracking
- Kills & Deaths
- Wins & Losses
- K/D Ratio & W/L Ratio
- Final Kills
- Beds Destroyed
- Games Played

### Admin Tools
- Map creation and management
- Arena setup and configuration
- Spawn point placement
- Bed location setup
- Generator placement
- Enable/disable maps

## Commands

### Player Commands
- `/bw join [mode]` - Join a game (modes: solo, doubles, 3v3v3v3, 4v4v4v4)
- `/bw leave` - Leave current game
- `/bw list` - List available arenas
- `/bwstats [player]` - View player statistics

### Party Commands
- `/bp create` - Create a new party
- `/bp invite <player>` - Invite a player to your party
- `/bp join <player>` - Join a player's party
- `/bp leave` - Leave your current party
- `/bp kick <player>` - Kick a player from your party (leader only)
- `/bp disband` - Disband your party (leader only)
- `/bp list` - List party members

### 1v1 Commands
- `/bw1v1 <player>` - Challenge a player to 1v1 or accept their challenge

### Admin Commands

**Map Management:**
- `/bwadmin createmap <name>` - Create a new map
- `/bwadmin deletemap <name>` - Delete a map
- `/bwadmin savemapworld <name>` - Save current world as a map template
- `/bwadmin setspawn <map> <team>` - Set team spawn point
- `/bwadmin setbed <map> <team>` - Set team bed location
- `/bwadmin addgen <map> <type>` - Add a generator (IRON, GOLD, DIAMOND, EMERALD)
- `/bwadmin enable <map>` - Enable a map
- `/bwadmin disable <map>` - Disable a map
- `/bwadmin list` - List all maps

**Arena & Game Management:**
- `/bwadmin createarena <name> <map> <mode>` - Create an arena
- `/bwadmin games` - View all running games
- `/bwadmin tp <arena>` - Teleport to a running game
- `/bwadmin forceend <arena>` - Force end a running game

## Permissions

### Player Permissions
- `bedwarsg.play` - Allows playing Bedwars (default: true)
- `bedwarsg.play.solo` - Allows playing solo mode (default: true)
- `bedwarsg.play.doubles` - Allows playing doubles mode (default: true)
- `bedwarsg.play.3v3v3v3` - Allows playing 3v3v3v3 mode (default: true)
- `bedwarsg.play.4v4v4v4` - Allows playing 4v4v4v4 mode (default: true)
- `bedwarsg.play.1v1` - Allows playing 1v1 mode (default: true)
- `bedwarsg.play.custom` - Allows creating custom games (default: op)

### Party Permissions
- `bedwarsg.party` - Allows using party features (default: true)
- `bedwarsg.party.create` - Allows creating parties (default: true)
- `bedwarsg.party.invite` - Allows inviting to parties (default: true)

### Stats Permissions
- `bedwarsg.stats` - Allows viewing statistics (default: true)
- `bedwarsg.stats.others` - Allows viewing other players' statistics (default: true)

### Admin Permissions
- `bedwarsg.admin` - Full admin access (default: op)
- `bedwarsg.admin.setup` - Allows setting up arenas (default: op)
- `bedwarsg.admin.delete` - Allows deleting arenas (default: op)
- `bedwarsg.admin.forcestart` - Allows force starting games (default: op)
- `bedwarsg.admin.forcestop` - Allows force stopping games (default: op)
- `bedwarsg.*` - All permissions

## CI/CD

This plugin includes GitHub Actions for automatic building:
- Builds automatically on every push and pull request
- Compiled artifacts are available in the Actions tab
- JAR files are retained for 30 days

## Setup Guide

### 1. Installation
1. Download the BedwarsG.jar file
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure the plugin files in `plugins/BedwarsG/`

### 2. Creating Your First Map

#### Method 1: Build in a Separate World (Recommended)

1. **Build your map** in a dedicated world (e.g., `bedwars_builder`)
   - Create spawn points for each team
   - Place beds for each team
   - Add generator locations
   - Build the actual map terrain and structures

2. **Go to your built world** and run:
   ```
   /bwadmin savemapworld <mapname>
   ```
   This saves the entire world as a map template in `plugins/BedwarsG/map-worlds/`

3. **Create the map configuration**
   ```
   /bwadmin createmap <mapname>
   ```

4. **Set team spawns** (stand at each spawn location)
   ```
   /bwadmin setspawn <mapname> RED
   /bwadmin setspawn <mapname> BLUE
   /bwadmin setspawn <mapname> GREEN
   /bwadmin setspawn <mapname> YELLOW
   ```

5. **Set team beds** (stand at each bed location)
   ```
   /bwadmin setbed <mapname> RED
   /bwadmin setbed <mapname> BLUE
   /bwadmin setbed <mapname> GREEN
   /bwadmin setbed <mapname> YELLOW
   ```

6. **Add generators** (stand at generator locations)
   ```
   /bwadmin addgen <mapname> DIAMOND
   /bwadmin addgen <mapname> EMERALD
   ```

7. **Enable the map**
   ```
   /bwadmin enable <mapname>
   ```

8. **Create an arena**
   ```
   /bwadmin createarena <arenaname> <mapname> solo
   ```

#### Method 2: Quick Setup

Follow steps 1-6 from above without saving the world. The plugin will use location data but won't copy the world.

### How Games Work

When a game starts:
1. The plugin creates a **temporary copy** of the map world (e.g., `bedwars-a8f4c2d1`)
2. Players are teleported to this temporary world
3. All game actions happen in this isolated world
4. When the game ends, the temporary world is **automatically deleted**
5. The original map template remains untouched for the next game

### 3. Configuration

Edit `config.yml` to customize:
- Minimum players to start
- Countdown time
- Respawn settings
- Generator spawn rates
- Team colors
- And more!

Edit `messages.yml` to customize all plugin messages.

## Building from Source

1. Clone the repository
   ```bash
   git clone https://github.com/GuyThatLivesAndCodes/BedwarsG.git
   cd BedwarsG
   ```

2. Build with Maven
   ```bash
   mvn clean package
   ```

3. The compiled JAR will be in `target/BedwarsG-1.0.0.jar`

## Requirements

- Spigot/Paper 1.8.8 or higher
- Java 8 or higher

## Support

For issues, bugs, or feature requests, please open an issue on GitHub.

## License

This project is proprietary software owned by GuyThatLives.

## Credits

Developed by GuyThatLives with assistance from Claude Code.

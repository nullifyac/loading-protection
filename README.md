# Loading Protection Mod

A Minecraft mod that protects players from damage while loading into a world or server. Available for multiple Minecraft versions on Forge, NeoForge, and Fabric.

## Features

- **Invulnerability**: Players cannot take damage for a configurable duration after joining
- **Mob Protection**: Mobs cannot target protected players
- **Fair Play**: Protected players cannot deal damage to others or mobs
- **Adaptive Timer**: Protection lasts up to the configured maximum but ends immediately once the player moves their view (crosshair)
- **Countdown Alerts**: Optional per-player chat messages show how many seconds of protection remain and warn when it ends
- **Configurable**: Adjust protection duration and messaging via config file

## Supported Versions

This repository contains multiple versions of the mod:

| Version | Folder | Mod Loader | Java Version |
|---------|--------|------------|--------------|
| 1.16.5 | [`forge-1.16.5/`](forge-1.16.5/) | Forge 36.2.0+ | Java 8+ |
| 1.16.5 | [`fabric-1.16.5/`](fabric-1.16.5/) | Fabric Loader 0.18.4 + Fabric API 0.42.0+ | Java 8+ |
| 1.18.2 | [`forge-1.18.2/`](forge-1.18.2/) | Forge 40.2.0+ | Java 17+ |
| 1.18.2 | [`fabric-1.18.2/`](fabric-1.18.2/) | Fabric Loader 0.18.4 + Fabric API 0.77.0+ | Java 17+ |
| 1.19.2 | [`forge-1.19.2/`](forge-1.19.2/) | Forge 43.3.0+ | Java 17+ |
| 1.19.2 | [`fabric-1.19.2/`](fabric-1.19.2/) | Fabric Loader 0.18.4 + Fabric API 0.77.0+ | Java 17+ |
| 1.20.1 | [`forge-1.20.1/`](forge-1.20.1/) | Forge 47.2.0+ | Java 17+ |
| 1.20.1 | [`fabric-1.20.1/`](fabric-1.20.1/) | Fabric Loader 0.18.4 + Fabric API 0.92.7+ | Java 17+ |
| 1.21.1 | [`neoforge-1.21.1/`](neoforge-1.21.1/) | NeoForge 21.1.0+ | Java 21+ |
| 1.21.1 | [`fabric-1.21.1/`](fabric-1.21.1/) | Fabric Loader 0.18.4 + Fabric API 0.116.7+ | Java 21+ |

## Configuration

The mod creates a configuration file at `config/loadingprotection-common.toml`:

```toml
["Loading Protection Configuration"]
    # Duration of protection in seconds after joining (default: 60)
    # Range: 1 ~ 300
    protectionDuration = 60

    # Show a countdown plus an expiration notice in chat (default: true)
    showMessages = true
```

Messages let protected players know exactly how long they have left before the timer expires (up to the configured max) and confirm when protection ends early because they moved their view. They are only sent to the affected player and appear once per 5 seconds at most to avoid chat spam.

Fabric versions use `config/loadingprotection.json` with the same `protectionDuration` and `showMessages` keys.

## Installation

1. Download the mod JAR file for your Minecraft version
2. Place it in your `mods` folder
3. Launch Minecraft with the appropriate mod loader (Forge, NeoForge, or Fabric). Fabric versions also require Fabric API.

## Building from Source

Navigate to the version folder you want to build and run:

Use JDK 17 to run the Forge and Fabric builds for Minecraft 1.16.5 through 1.20.1, and JDK 21 for Minecraft 1.21.1. The 1.16.5 builds also require a JDK 8 toolchain, as specified in their build files. Select the build JDK with `JAVA_HOME`; do not add a machine-specific JDK path to the repository.

```bash
cd forge-1.20.1  # or any other version folder
./gradlew build  # Use gradlew.bat on Windows
```

The built JAR will be in `build/libs/`

## How It Works

The mod uses a timer-based approach:
1. When a player joins, they are added to a protection list with the current timestamp
2. Until they move their view or the timer hits the configured maximum, the player:
   - Cannot take any damage
   - Cannot be targeted by hostile mobs
   - Cannot deal damage to other entities
3. As soon as the player moves their view (or the timer finally expires), normal gameplay resumes and they receive a chat notice if messaging is enabled

## License

All Rights Reserved

# AutoUtils - Advanced Fabric Client-Side Utility Mod (Minecraft 26.2)

**AutoUtils** is a complete, modular, client-side utility mod for Minecraft 26.2. Built on Fabric API, it introduces automation, combat aids, and emergency safety triggers while being 100% configurable in-game through Cloth Config and Mod Menu.

---

## Features

### 1. Auto-Eat
- Automatically selects and consumes the most nutritious food item from hotbar or inventory when hunger or saturation drops below configured limits.
- Evaluates hunger points and saturation multipliers to pick optimal food.
- Ignores harmful food (Rotten Flesh, Pufferfish, Spider Eye, Poisonous Potato, Chorus Fruit) unless explicitly enabled.
- Supports Offhand food consumption priority.
- Simulates vanilla `useKey` input to maintain smooth packet sync and prevent desync flags.

### 2. Auto-Attack (Cooldown-Aware)
- Automatically attacks crosshair targets or entities within reach range (default 3.8 blocks).
- Strictly respects Minecraft's 1.9+ combat cooldown meter (attacks ONLY when meter is 100% charged for maximum damage output).
- Detailed entity filters: Hostile mobs, Passive animals, Players (with whitelist), and Bosses (Ender Dragon, Wither, Warden, Elder Guardian).

### 3. Auto-Commands on Join
- Dispatches configured commands after joining singleplayer worlds or multiplayer servers.
- Configurable delay in ticks (e.g. 40 ticks = 2 seconds) to avoid early-connection disconnects.
- Per-Server Profile support: binds command lists to specific server IPs (e.g., `/login <pass>`, `/rtp`, `/home`).

### 4. Auto-Rejoin
- Detects server kicks or connection drops and initiates an automated reconnection countdown.
- Displays an on-screen visual countdown on the `DisconnectedScreen` (`Reconnecting in X seconds...`).
- Features a manual `Cancel Rejoin` button on the screen.
- Configurable retry attempt limit (default: 5 retries).

### 5. Safety Trigger: Proximity Kick
- Continuously scans for other players within a configurable block radius (default: 50 blocks).
- Honors a customizable username whitelist (friends, team members).
- Automatically disconnects the client before enemies can locate or attack your base/AFK spot.
- Triggers visual HUD alerts, audible warning chimes, and console logging.

### 6. Safety Trigger: Damage / Low Health Kick
- Detects sudden health reductions and disconnects if health falls below the threshold (default: 4 hearts / 8.0 HP).
- Configurable toggles to distinguish between Combat/Entity attacks and Environmental damage (fall, lava, fire, suffocation).

### 7. Auto-Totem / Offhand Saver
- Scans inventory and immediately swaps a new Totem of Undying into the offhand slot when the held totem pops or offhand is empty.
- Employs packet-safe single-tick slot swap (`SlotActionType.SWAP` button 40).

### 8. Anti-AFK Routine
- Emits subtle, non-intrusive actions (small yaw/pitch jitter, tiny ground jump, arm swing) at randomized intervals to defeat idle kick timers.

### 9. Visual & Sound Alerts
- In-game HUD overlay for active emergency alerts and killswitch status.
- Warning chime (`BLOCK_NOTE_BLOCK_BELL`) on safety triggers.

---

## Controls & Keybindings

| Keybinding | Default Key | Action |
|---|---|---|
| **Toggle Kill-Switch** | `Right Shift` | Instantly disables or re-enables all automated utilities. |
| **Open Config Menu** | `O` | Directly opens the Cloth Config in-game settings GUI. |

---

## Configuration Categories

Accessible in-game via **Mod Menu** -> **AutoUtils** -> **Configure** or pressing `O`:
- **[General / Utilities]**: Auto-Eat thresholds, harmful food toggles, Auto-Attack ranges, filters, cooldown limits.
- **[Automation]**: Auto-Command join delays, per-server IP lists, Auto-Rejoin timers, Anti-AFK jitter settings.
- **[Safety & Disconnects]**: Proximity radius, player whitelists, damage thresholds, Auto-Totem delays.

Configuration files are stored in `.minecraft/config/autoutils.json`.

---

## Building & Installation

### Requirements
- Java 25 JDK
- Fabric Loader 0.19.x
- Fabric API 0.160.0+26.2
- Cloth Config 26.2.155 (`cloth-config-fabric`)
- Mod Menu 20.0.x

### Compilation
```bash
# Clone or navigate to the project directory
cd /path/to/autoutils

# Build the production jar using Gradle
./gradlew build
```

The compiled mod jar will be generated in `build/libs/autoutils-1.0.0.jar`. Place this jar into your `.minecraft/mods` directory alongside Fabric API and Cloth Config.

---

## 📄 License
All Rights Reserved (With Permissions). Created by [S0raKasugano](https://github.com/S0raKasugano). See [LICENSE](LICENSE) for full details.

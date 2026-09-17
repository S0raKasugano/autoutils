# <img src="icon.png" width="48" height="48" alt="AutoUtils Icon" align="center" /> AutoUtils (Minecraft 26.2)

[![Author](https://img.shields.io/badge/Author-S0raKasugano-blue.svg)](https://github.com/S0raKasugano)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-brightgreen.svg)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-gold.svg)](https://fabricmc.net)
[![Cloth%20Config](https://img.shields.io/badge/Config-Cloth%20Config-informational.svg)](https://curseforge.com/minecraft/mc-mods/cloth-config)

**AutoUtils** is a high-performance, client-side utility mod for **Minecraft 26.2** built on Fabric Loader. It provides combat utilities, survival automation, safety triggers, and a neat, categorized in-game Cloth Config GUI.

Developed by **[S0raKasugano](https://github.com/S0raKasugano)**.

---

## 📦 Download & Installation

The production-ready mod jar is ready to run directly from this repository:

👉 **[autoutils-1.0.0.jar](./autoutils-1.0.0.jar)**

### Requirements
- **Minecraft**: `26.2`
- **Fabric Loader**: `>= 0.19.x`
- **Fabric API**: `0.160.0+26.2`
- **Cloth Config**: `26.2.155+` (`cloth-config-fabric`)
- **Mod Menu**: `20.0.x` (Recommended for GUI access)

### Quick Install
1. Download **`autoutils-1.0.0.jar`**.
2. Place it into your `.minecraft/mods` folder (or your Lunar Client / Prism Launcher mods folder).
3. Ensure **Fabric API** and **Cloth Config** are also present in your `mods` folder.
4. Launch Minecraft 26.2!

---

## ⚡ Key Features

### 1. Auto-Attack (Cooldown-Aware)
- Automatically attacks crosshair targets or entities within reach (standard default: **3.0 blocks**).
- **100% Cooldown Synchronized**: Strikes only when the weapon attack indicator reaches 100% for maximum damage and sharpness crits.
- **Smart Target Filters (PvE Only)**: Configurable for Hostile Mobs (Monsters, Slimes, Ghasts, Phantoms, Shulkers, Breeze, etc.), Passive Animals, and Bosses (Ender Dragon, Wither, Warden, Elder Guardian). Player targeting is strictly disabled for fair play and Modrinth compliance.
- Toggle keybind: Press **`V`** to toggle on/off with on-screen HUD alerts.

### 2. Auto-Eat
- Consumes the best food item from your hotbar or inventory whenever hunger or saturation falls below your configured threshold.
- Automatically avoids harmful food (Rotten Flesh, Pufferfish, Spider Eyes) unless permitted in settings.
- Supports offhand food priority and smooth packet synchronization.

### 3. Auto-Totem
- Automatically replenishes Totems of Undying to your offhand slot instantly upon popping.

### 4. Auto-Commands on Join
- Dispatches configured commands automatically upon joining worlds or servers after a configurable delay.
- **Global Join Commands**: Runs everywhere (e.g. `/gamma 100`, `/sit`).
- **Server-Specific Commands**: Configurable per server IP directly in the GUI (e.g. `hypixel.net -> /play bedwars`).

### 5. Auto-Rejoin
- Automatically attempts reconnection if disconnected from a server with an on-screen countdown timer and cancel button.

### 6. Proximity Safety Kick
- Automatically disconnects if unknown players enter your configured radius (default: 50 blocks). Supports custom username whitelisting.

### 7. Damage Safety Kick
- Automatically disconnects if your health drops below a specified threshold (default: 4 hearts / 8.0 HP) to prevent dying while AFK.

### 8. Anti-AFK
- Executes subtle, human-like movements (micro-look adjustments, slight jumps, arm swings) to prevent idle kicks.

---

## ⌨️ Controls & Keybindings

All keybindings appear under their own dedicated **AutoUtils** section in **Options > Controls > Key Binds**:

| Keybinding | Default Key | Action |
|---|---|---|
| **Toggle Auto-Attack** | `V` | Toggles Auto-Attack on/off with on-screen HUD feedback |
| **Open Config Menu** | `O` | Directly opens the Cloth Config in-game settings GUI |
| **Toggle Kill-Switch** | `Right Shift` | Emergency killswitch: suspends all automated features instantly |
| **Toggle Auto-Eat** | *Unbound* | Toggles Auto-Eat on/off |
| **Toggle Anti-AFK** | *Unbound* | Toggles Anti-AFK on/off |

---

## ⚙️ Organized In-Game Configuration

Press **`O`** in-game or open **Mod Menu > AutoUtils > Configure** to access the neatly organized Cloth Config GUI:

- ⚔️ **Combat**: Auto-Attack toggle, standard 3.0 reach slider, full cooldown requirement, collapsible **Target Entity Filters** (hostiles, passives, bosses), and **Auto-Totem** offhand replenishment.
- 🍖 **Survival**: Auto-Eat toggle, hunger threshold, offhand food priority, eating delay ticks, and **Advanced Food Options** (saturation threshold, harmful food whitelist).
- 🤖 **Automation**: Collapsible subcategories for **Join Commands** (global + server-specific rules), **Auto-Rejoin** countdown & retries, and human-like **Anti-AFK System**.
- 🛡️ **Safety & Alerts**: Prominent emergency Kill-Switch, HUD alert notifications, warning sound volume, **Proximity Disconnect** (radius + whitelist), and **Low Health Disconnect**.

*Config file location: `.minecraft/config/autoutils.json`*

---

## 📄 License
MIT License. Created by [S0raKasugano](https://github.com/S0raKasugano).

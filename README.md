# ☁️ SkyFactions2: Ecosystem Integration & Spatial Logic

[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)]()
[![Spigot API](https://img.shields.io/badge/Spigot_API-1.20+-orange?style=for-the-badge)]()
[![WorldEdit](https://img.shields.io/badge/WorldEdit_API-Integrated-blue?style=for-the-badge)]()
[![WorldGuard](https://img.shields.io/badge/WorldGuard_API-Integrated-red?style=for-the-badge)]()
[![Vault](https://img.shields.io/badge/Vault_API-Integrated-green?style=for-the-badge)]()

**SkyFactions2** is a custom Java plugin engineered for Spigot/Paper server environments. It merges the mechanics of grid-based Skyblock generation with the geopolitical, state-driven gameplay of Factions. 

This repository serves as a technical showcase of complex third-party API orchestration, spatial mathematics, custom data persistence, and event-driven state machines within a live multiplayer environment.

> **Status:** Archived (2024 Deployment). This plugin was successfully deployed to a live server environment supporting 200+ users. It is preserved here as a demonstration of backend Java architecture.

---

## 🏗️ Core Architecture & Technical Highlights

### 1. Spatial Logic & Grid Generation
Island generation is not randomized; it utilizes a deterministic, expanding grid algorithm to ensure sky-islands never overlap and maintain exact chunk alignments.
* **Grid Mathematics:** Calculates `row`, `col`, and target vectors using a fixed `distance = 120` block offset to dynamically assign coordinates to new factions.
* **WorldEdit Integration:** Utilizes the `ClipboardReader` and `EditSession` APIs to programmatically load and paste `.schem` files directly into the world memory space without blocking the main server thread.

### 2. Region Protection & Sub-Claiming
Land management hooks deeply into the **WorldGuard API** to dynamically create, modify, and assign permissions to 3D spatial boundaries.
* **Chunk-Based Math:** Converts standard coordinates into chunk-aligned block vectors (`minX = chunk.getX() << 4`) to generate precise `ProtectedCuboidRegion` objects.
* **Hierarchical Regions:** Utilizes WorldGuard's `setParent()` methodology to link newly claimed chunks to the faction's master region, allowing for inherited flags (PvP rules, greeting messages) across a continuously expanding territory.

### 3. Event-Driven Combat State Machine
PvP is entirely governed by a custom state machine that tracks diplomatic relations (Alliances, Wars, Neutrality) and intercepts damage events in real-time.
* **O(1) Combat Resolution:** Active wars are cached in a `HashMap<String, List<String>> warringPlayers`. When a damage event fires, the plugin performs an immediate O(1) lookup to determine if the interaction is legally permitted by the state machine.
* **Comprehensive Interception:** Listens to `EntityDamageByEntityEvent` to intercept and cancel unauthorized damage, specifically casting and calculating edge cases for `Arrow` and `ThrownPotion` to trace damage back to the original shooter.

### 4. Custom YAML Data Persistence
Avoids the overhead of a heavy SQL database by implementing a highly segmented, bespoke YAML File I/O system tailored for Minecraft's lifecycle.
* **Data Segmentation:** Separates internal application state (`factions.yml`) from user-specific metadata (`data.yml`) to prevent file lock contention and optimize read/write times during server saves.
* **Safe Reloads:** Implements custom `DataManager` and `FactionsManager` classes with fallback `InputStreamReader` logic to ensure data integrity during hot-reloads.

---

## ⚙️ Dependencies

To compile and run this plugin, the server environment must be running Spigot/Paper and have the following API plugins installed:
* **Vault** (Requires a compatible economy provider like EssentialsX)
* **WorldEdit** (For schematic clipboard handling)
* **WorldGuard** (For spatial region manipulation)

---

## 🛠️ Command Router Overview

The plugin routes all interactions through a centralized `CommandExecutor` (`IslandCommands.java`), ensuring permissions and state are validated before executing complex backend logic.

| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/sf create [name]` | All | Deducts Vault currency, executes grid math, pastes WE schematic, generates WG region. |
| `/sf claim` | Officer/Leader | Validates chunk availability and appends a new inherited WG region to the faction. |
| `/sf declarewar [enemy]` | Leader | Mutates the diplomatic state machine and updates the real-time PvP HashMap. |
| `/sf joinwar [ally] [enemy]` | Leader | Submits a relational war request to be resolved by the allied faction. |
| `/sf promote/demote` | Leader | Mutates the YAML hierarchy to grant/revoke Officer status. |

---

## 👨‍💻 Engineering Notes
*This project was developed by Christopher Kendrick.*

Developing SkyFactions2 provided extensive hands-on experience in cross-plugin communication and managing memory states across frequent server ticks. Translating abstract concepts like "Diplomatic Immunity" into raw `EntityDamageEvent` cancellations required rigid adherence to event-driven programming principles and defensive coding against null states.

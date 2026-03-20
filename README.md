# ☁️ SkyFactions2: Ecosystem Integration & Spatial Logic

[![Spigot API](https://img.shields.io/badge/Spigot_API-1.20+-orange?style=for-the-badge)]()
[![WorldEdit](https://img.shields.io/badge/WorldEdit_API-Integrated-blue?style=for-the-badge)]()
[![WorldGuard](https://img.shields.io/badge/WorldGuard_API-Integrated-red?style=for-the-badge)]()
[![Vault](https://img.shields.io/badge/Vault_API-Integrated-green?style=for-the-badge)]()

**SkyFactions2** is a custom Java plugin developed for Spigot/Paper server environments. It merges the mechanics of grid-based Skyblock generation with the geopolitical, state-driven gameplay of Factions. 

This repository serves as a technical showcase of complex third-party API orchestration, spatial mathematics, custom data persistence, and event-driven state machines within a live multiplayer environment.

---

## 📖 The Concept (What, Why, & How)

**What is it?**
SkyFactions2 is a competitive multiplayer game mode. It takes the isolated, resource-management gameplay of "Skyblock" (where players survive on floating islands in the void) and introduces the territorial warfare of "Factions" (where teams claim land, forge alliances, and declare war).

**Why build it?**
Standard Skyblock is purely co-operative, while standard Factions relies on vanilla terrain that gets easily destroyed. SkyFactions2 was developed to create a high-stakes environment where players must physically build their battlegrounds across the void, managing an economy while engaging in complex server-wide diplomacy.

**How does it work?**
1. **Foundation:** A player spends in-game currency to create a sky-faction. The server calculates a mathematically perfectly distanced coordinate in the void and pastes a starting island for them.
2. **Expansion:** As the faction gathers resources, they can claim adjacent 16x16 chunk "airspace," expanding their protected 3D building borders.
3. **Diplomacy & Combat:** Factions operate under strict Role-Based Access Control (Leader, Officer, Member). Leaders can manage diplomacy (Ally, Neutral, Enemy). If a war is declared, the plugin dynamically drops region protections, allowing rival factions to bridge across the void and engage in PvP combat.

---

## 🏗️ Core Architecture & Technical Highlights

### 1. Spatial Logic & Grid Generation
Island generation is not randomised; it utilises a deterministic, expanding grid algorithm to ensure sky-islands never overlap and maintain exact chunk alignments.
* **Grid Mathematics:** Calculates `row`, `col`, and target vectors using a fixed `distance = 120` block offset to dynamically assign coordinates to new factions.
* **WorldEdit Integration:** Utilises the `ClipboardReader` and `EditSession` APIs to programmatically load and paste `.schem` files directly into the world memory space without blocking the main server thread.

### 2. Region Protection & Sub-Claiming
Land management hooks deeply into the **WorldGuard API** to dynamically create, modify, and assign permissions to 3D spatial boundaries.
* **Chunk-Based Maths:** Converts standard coordinates into chunk-aligned block vectors (`minX = chunk.getX() << 4`) to generate precise `ProtectedCuboidRegion` objects.
* **Hierarchical Regions:** Utilises WorldGuard's `setParent()` methodology to link newly claimed chunks to the faction's master region, allowing for inherited flags (PvP rules, greeting messages) across a continuously expanding territory.

### 3. Role-Based Access Control (RBAC)
Internal faction management relies on a strict, custom-built hierarchy that dictates permission levels independent of the server's global permission nodes.
* **Role Tiers:** Programmed a three-tier system (Leader, Officer, Member) saved natively in the plugin's data structures.
* **Command Gateway:** Wrote validation checks for every faction command, restricting critical actions (disbanding, declaring war) to Leaders, while allowing Officers to manage day-to-day operations (claiming land, inviting players).

### 4. Event-Driven Combat State Machine
PvP is entirely governed by a custom state machine that tracks diplomatic relations (Alliances, Wars, Neutrality) and intercepts damage events in real-time.
* **O(1) Combat Resolution:** Active wars are cached in a `HashMap<String, List<String>> warringPlayers`. When a damage event fires, the plugin performs an immediate O(1) lookup to determine if the interaction is legally permitted by the state machine.
* **Comprehensive Interception:** Listens to `EntityDamageByEntityEvent` to intercept and cancel unauthorised damage, specifically casting and calculating edge cases for `Arrow` and `ThrownPotion` to trace damage back to the original shooter.

### 5. Custom YAML Data Persistence
Avoids the overhead of a heavy SQL database by implementing a highly segmented, bespoke YAML File I/O system tailored for Minecraft's lifecycle.
* **Data Segmentation:** Separates internal application state (`factions.yml`) from user-specific metadata (`data.yml`) to prevent file lock contention and optimise read/write times during server saves.
* **Safe Reloads:** Implements custom `DataManager` and `FactionsManager` classes with fallback `InputStreamReader` logic to ensure data integrity during hot-reloads.

---

## ⚙️ Dependencies

To compile and run this plugin, the server environment must be running Spigot/Paper and have the following API plugins installed:
* **Vault** (Requires a compatible economy provider like EssentialsX)
* **WorldEdit** (For schematic clipboard handling)
* **WorldGuard** (For spatial region manipulation)

---

## 🛠️ Command Router Overview

The plugin routes all interactions through a centralised `CommandExecutor`, ensuring custom RBAC permissions and gamestates are validated before executing backend logic.

### Core Foundation
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/sf create [name]` | All | Deducts Vault currency, executes grid maths, pastes WE schematic, generates WG region. |
| `/sf disband` | Leader | Deletes the faction, purges YAML data, and wipes the physical island using WE. |
| `/sf claim` | Officer/Leader | Validates chunk availability and appends a new inherited WG region to the faction. |
| `/sf unclaim` | Officer/Leader | Removes the chunk from the faction's territory and strips WG protections. |

### Member Management (RBAC)
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/sf invite [player]` | Officer/Leader | Sends a join request, temporarily cached in local memory. |
| `/sf join [name]` | All | Accepts an invite and mutates YAML data to append the player to the faction. |
| `/sf kick [player]` | Officer/Leader | Removes a player from the faction and teleports them to spawn. |
| `/sf leave` | All | Removes the executing player from their current faction. |
| `/sf promote/demote` | Leader | Mutates the YAML hierarchy to grant/revoke Officer status. |

### Diplomacy & Teleportation
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/sf declarewar [enemy]` | Leader | Mutates the diplomatic state machine and updates the real-time PvP HashMap. |
| `/sf joinwar [ally] [enemy]`| Leader | Submits a relational war request to be resolved by the allied faction. |
| `/sf ally [faction]` | Leader | Updates state machine to prevent friendly fire and allow shared chunk access. |
| `/sf neutral [faction]` | Leader | Resets diplomatic relations back to the default protected state. |
| `/sf sethome` | Officer/Leader | Saves the player's current X/Y/Z vector to the YAML file as the faction spawn. |
| `/sf home` | All | Teleports the player to the saved faction vector. |

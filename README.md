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
3. **Diplomacy & Combat:** Factions operate under strict Role-Based Access Control (Leader, Officer, Member). Leaders can manage diplomacy. If a war is declared, the plugin dynamically registers the state change, allowing rival factions to engage in PvP combat.

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
* **Command Gateway:** Wrote validation checks for every faction command, restricting critical actions (deleting the faction, declaring war, setting ranks) to Leaders, while allowing Officers to manage day-to-day operations (claiming land, inviting players).

### 4. Event-Driven Combat State Machine
PvP is entirely governed by a custom state machine that tracks diplomatic relations (Wars, Joint Attacks/Defences) and intercepts damage events in real-time.
* **O(1) Combat Resolution:** Active wars are cached in a `HashMap<String, List<String>> warringPlayers`. When a damage event fires, the plugin performs an immediate O(1) lookup to determine if the interaction is legally permitted by the state machine.
* **Comprehensive Interception:** Listens to `EntityDamageByEntityEvent` to intercept and cancel unauthorised damage, specifically casting and calculating edge cases for `Arrow` and `ThrownPotion` to trace damage back to the original shooter.

### 5. Custom YAML Data Persistence
Avoids the overhead of a heavy SQL database by implementing a highly segmented, bespoke YAML File I/O system tailored for Minecraft's lifecycle.
* **Data Segmentation:** Separates internal application state (`factions.yml`) from user-specific metadata (`data.yml`) to prevent file lock contention and optimise read/write times during server saves.
* **Safe Reloads:** Implements custom `DataManager` and `FactionsManager` classes with fallback `InputStreamReader` logic to ensure data integrity during hot-reloads.

---

## ⚙️ Dependencies

To compile and run this plugin, the server environment must be running Spigot/Paper and have the following API plugins installed:
* **Vault** (Requires a compatible economy provider)
* **WorldEdit** (For schematic clipboard handling)
* **WorldGuard** (For spatial region manipulation)

---

## 🛠️ Command Router Overview

The plugin routes all interactions through a centralised `CommandExecutor`, ensuring custom RBAC permissions, Vault balances, and gamestates are validated before executing backend logic.

### Core Foundation & Economy
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/sf help` | All | Displays the help page. |
| `/bal` | All | Displays the player's current economy balance. |
| `/sf create [name]` | All | Deducts $1000, executes grid maths, pastes WE schematic, generates WG region. |
| `/sf delete` | Leader | Initiates the deletion process for the faction. |
| `/sf deleteconfirm` | Leader | Confirms deletion, purges YAML data, and removes WorldGuard regions. |
| `/sf claim` | Officer/Leader | Validates chunk availability, deducts $100, and appends a new inherited WG region. |
| `/sf unclaim` | Officer/Leader | Removes the chunk from the faction's territory and strips WG protections. |
| `/sf info (name)` | All | Prints information about a faction, including the leader and members. |

### Member Management (RBAC)
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/sf invite [player]` | Officer/Leader | Sends a join request, temporarily cached in local memory for 30 seconds. |
| `/sf cancelinvite [player]`| Officer/Leader | Cancels an active invitation early. |
| `/sf join [name]` | All | Accepts an invite and mutates YAML data to append the player to the faction. |
| `/sf leave` | Officer/Member | Removes the executing player from their current faction (Leaders cannot leave). |
| `/sf setrank [user] [rank]`| Leader | Promotes or demotes a player to the rank of 'member' or 'officer'. |
| `/sf setleader [user]` | Leader | Initiates the transfer of faction leadership to another player. |
| `/sf confirmsetleader [user]`| Leader | Confirms the transfer of leadership, demoting the previous leader to officer. |

### Diplomacy & Teleportation
| Command | Permission Target | Function |
| :--- | :--- | :--- |
| `/sf sethome` | Leader | Saves the player's current X/Y/Z vector to the YAML file as the faction spawn. |
| `/sf home` | All | Deducts $10 and teleports the player to the saved faction vector. |
| `/sf declarewar [enemy]` | Leader | Mutates the diplomatic state machine, updates the PvP HashMap, and broadcasts the war. |
| `/sf joinwar [ally] [enemy]`| Leader | Submits a relational war request to be resolved by the allied faction. |
| `/sf acceptjoin [ally] [enemy]`| Leader | Accepts an allied faction's request to join a war. |
| `/sf cancelwarapp` | Leader | Cancels a pending application to join an ally's war. |
| `/sf endwar [enemy]` | Leader | Sues for peace, removing the enemy from the active war data and updating the PvP HashMap. |

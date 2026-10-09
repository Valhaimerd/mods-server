# Modpack Player Handbook Scaffold

Last audited: 2026-10-09<br>
Pack target: Minecraft 26.2 with NeoForge 26.2<br>
Current distribution: 277 synchronized JARs plus 4 optional client-store JARs

## About this scaffold

This document is the working content outline for a future player-facing PDF handbook. It is written for players who understand vanilla Minecraft but are new to this modpack. It prioritizes changed mechanics, controls, configuration, and relationships between systems rather than exhaustive lists of items, creatures, blocks, recipes, structures, or loot.

Libraries, APIs, compatibility bridges, and invisible server internals are omitted from the gameplay catalog. A support mod may still appear in the keybind reference when it exposes controls to players.

## How entries are organized

Each mod has one primary entry. Secondary topics link back to that entry instead of repeating it. Broad mods use an Integrated expansion type when they combine multiple substantial systems.

**Entry types:** Core system; Integrated expansion; Focused content; Utility; Interface feature; Optional client feature.

**Integrated-expansion subtypes:** Integrated world expansion; Integrated dimension expansion; Integrated adventure expansion; Integrated character-progression suite; Integrated survival or profession expansion; Integrated building expansion.

**Cross-reference tags:** Combat; Character; Skills; Magic; Equipment; Bosses; Events; World Generation; Structures; Creatures; Companions; Farming; Food; Building; Storage; Travel; Multiplayer; Controls; HUD.

## Part I — Start Here

### Essential keybinds and conflict resolution

Configure controls from the top of Minecraft's Controls screen to the bottom. The complete category-ordered tables in this chapter keep actual defaults, pack recommendations, and conflicts separate.

The tables follow the Minecraft Controls screen from top to bottom so players can configure one category at a time. Screenshot values are not treated as official defaults. A **Default key** is shown only when official documentation states it; otherwise the cell remains blank. **Recommended key** values are pack choices and remain separate from defaults. A blank recommendation means **leave the action unbound** unless you personally need it.

The recommended profile keeps frequently used RPG systems on simple keys and groups related tools by letter: `M` for the world map, `N` for coordinates, `R` for pets, and `G` for progression and equipment. A key may be reused only when the actions operate in clearly different contexts, such as a JEI inventory shortcut and an in-world action.

#### LibTooltips

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Expand Tooltip |  | `Left Shift` | Tooltip-only action; sharing Sprint is safe. |

#### Starcatcher keybinds

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Minigame Hit |  | `Space` | Used only inside the fishing minigame; intentional context share with Jump. |
| Open Guide |  | `Ctrl + Space` |  |
| Toggle Tournament Overlay |  | `Tab` | Tournament-only action; intentional context share with Inventory. |

#### Movement

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Jump | `Space` | `Space` |  |
| Roll | `R` | `E` | Moved from `R` to keep the pet-control group together. |
| Sneak | `Left Shift` | `C` | PatPat uses the currently configured Sneak key plus right-click. |
| Sprint | `Left Ctrl` | `Left Shift` |  |
| Strafe Left | `A` | `A` |  |
| Strafe Right | `D` | `D` |  |
| Walk Backward | `S` | `S` |  |
| Walk Forward | `W` | `W` |  |

#### Miscellaneous

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Advancements |  | `L` | Frees `B` for Better Party. |
| Alex's Caves Special Ability |  | `X` | Dedicated ability key that stays clear of Drop Item. |
| Quick Actions |  |  |  |
| Take Screenshot | `F2` |  | `F2` is reserved for Show Advanced Tooltips in this profile. |
| Toggle Cinematic Camera |  |  |  |
| Toggle Fullscreen |  | `Equals (=)` |  |
| Toggle GUI | `F1` | `Minus (-)` |  |
| Toggle Perspective | `F5` | `0` |  |
| Toggle Spectator Shader Effects |  |  |  |

#### Multiplayer keybinds

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Friends Screen |  |  |  |
| List Players | `Tab` | `;` |  |
| Open Chat | `T` | `Enter` |  |
| Open Command | `/` | `/` |  |
| Social Interactions Screen |  | `'` |  |

#### Gameplay

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Attack/Destroy | `Left Mouse Button` | `Left Mouse Button` |  |
| Chain Mining Key (Hold) | `Grave Accent` | `Grave Accent` | Replaces the conflicting screenshot assignment on `C`. |
| Open Config Menu |  | `Ctrl + Grave Accent` |  |
| Pick Block |  | `Middle Mouse Button` |  |
| Use Item/Place Block | `Right Mouse Button` | `Right Mouse Button` |  |
| [Tom's Simple Storage] Open Terminal |  |  |  |

#### Inventory

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Drop Selected Item | `Q` | `Q` | Uses the familiar vanilla binding. |
| Hotbar Slot 1 | `1` | `1` |  |
| Hotbar Slot 2 | `2` | `2` |  |
| Hotbar Slot 3 | `3` | `3` |  |
| Hotbar Slot 4 | `4` | `4` |  |
| Hotbar Slot 5 | `5` | `5` |  |
| Hotbar Slot 6 | `6` | `6` |  |
| Hotbar Slot 7 | `7` | `7` |  |
| Hotbar Slot 8 | `8` | `8` |  |
| Hotbar Slot 9 | `9` | `9` |  |
| Open Pet Status | `U` | `Shift + R` | Part of the pet-control group. |
| Open/Close Inventory | `E` | `Tab` |  |
| Swap Item With Off Hand |  | `F` |  |

#### Creative Mode

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Load Hotbar Activator |  |  |  |
| Save Hotbar Activator |  |  |  |

#### Spectator

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Highlight Players |  |  |  |
| Select On Hotbar |  |  |  |

#### Debug

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Toggle Overlay |  | `F1` |  |
| Debug Modifier Key |  |  |  |
| Clear Chat |  |  |  |
| Copy Data |  |  |  |
| Copy Location |  |  |  |
| Cycle Spectator |  |  |  |
| Debug Crash |  |  |  |
| Debug Options |  | `F12` |  |
| Dump Dynamic Textures |  |  |  |
| Dump Version Info |  |  |  |
| Game Mode Switcher |  |  |  |
| Reload Chunks |  |  |  |
| Reload Resource Packs |  |  |  |
| Show Advanced Tooltips |  | `F2` |  |
| Show Chunk Boundaries |  | `F3` |  |
| Show Hitboxes |  | `F4` |  |
| Start/Stop Profiling |  |  |  |
| Toggle Lost Focus Pause |  | `F5` |  |
| Profiling Chart |  | `F6` |  |
| FPS Charts |  | `F7` |  |
| Network Charts |  | `F8` |  |
| Lightmap Texture |  | `F9` |  |

#### Puffish Skills

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open Skill Tree | `K` | `T` |  |

#### PatPat

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Pat Entity | `Shift + Right Mouse Button` | `Right Mouse Button` | Hold the recommended Sneak key (`C`) while clicking. |

#### Jade

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Narrate Target | `Numpad 5` |  |  |
| Open Config | `Numpad 0` |  | Use the Mods screen if the keyboard has no numpad. |
| Show Details |  |  |  |
| Show Overlay | `Numpad 1` |  |  |
| Show Recipes | `Numpad 3` |  | JEI already provides recipe lookup. |
| Show Uses | `Numpad 4` |  | JEI already provides usage lookup. |
| Toggle Fluid | `Numpad 2` |  |  |
| Use Profile #0 |  |  |  |
| Use Profile #1 |  |  |  |
| Use Profile #2 |  |  |  |
| Use Profile #3 |  |  |  |

#### Iris

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Reload Shaders | `R` | `Alt + R` | Separates it from the pet-control group. |
| Shaderpack Selection Screen | `O` |  | Open from Video Settings instead. |
| Toggle Shaders | `K` |  | Open from Video Settings instead. |
| Wireframe (SP only) |  |  |  |

#### Coordinates Display

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Change HUD Position |  | `Alt + N` | Part of the coordinates group. |
| Copy Current Position as /tp Command |  |  |  |
| Copy Current Position to Clipboard |  |  |  |
| Cycle Display Mode (hold Shift to go back) |  | `Shift + N` | Replaces the conflicting screenshot assignment on `Ctrl + M`. |
| Mark a Position |  |  |  |
| Open Coordinates GUI |  | `Ctrl + N` |  |
| Send Current Position in Chat |  | `Y` |  |
| Toggle 3D Compass Rendering |  | `H` |  |
| Toggle HUD |  | `N` |  |

#### Xaero's World Map

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open Server Settings |  | `Shift + M` | Server owners only. |
| Open Settings |  | `Ctrl + M` |  |
| Open World Map | `M` | `M` |  |
| Quick Manual Confirmation |  |  |  |
| Toggle Chunk Claims |  |  |  |
| Toggle Dimension |  |  |  |
| Toggle Tracked Players |  |  |  |
| Zoom In (alternative) |  |  |  |
| Zoom Out (alternative) |  |  |  |

#### Effortless Building

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open Modifier Settings | `Numpad +` |  | Also available from the radial menu. |
| Open Radial Menu | `Left Alt` | `Left Alt` |  |
| Redo | `Ctrl + Y` | `Ctrl + Y` |  |
| Undo | `Ctrl + Z` | `Ctrl + Z` |  |

#### Spell Engine

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Bypass Spell Hotbar |  | `Left Ctrl` | Separates it from Effortless Building's `Left Alt` radial menu. |
| Spell Hotbar Slot 1 |  |  |  |
| Spell Hotbar Slot 2 |  |  |  |
| Spell Hotbar Slot 3 |  |  |  |
| Spell Hotbar Slot 4 |  |  |  |
| Spell Hotbar Slot 5 |  |  |  |
| Spell Hotbar Slot 6 |  |  |  |
| Spell Hotbar Slot 7 |  |  |  |
| Spell Hotbar Slot 8 |  |  |  |
| Spell Hotbar Slot 9 |  |  |  |
| Tooltip Spell Details |  |  |  |

#### Better Combat

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Feint |  |  |  |
| Toggle Mine with Weapons |  |  |  |

#### Better Party

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open Party Menu | `Z` | `B` | Mnemonic pack binding for the party system. |

#### Curios

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open/Close Curios Inventory | `G` | `Ctrl + G` | Keeps plain `G` available for Skill Perks. |

#### Hovering Hotbar

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Move Hotbar Down |  | `Down Arrow` |  |
| Move Hotbar Up |  | `Up Arrow` |  |

#### Iourus Races

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open Race Menu |  | `P` |  |

#### Item Interactions

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Toggle Item Held By Cursor Tooltip |  |  |  |
| Toggle Item Storage Tooltip |  |  |  |

#### JEI — Cheat Mode

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Cheat 1 Item | `Right Mouse Button` | `Right Mouse Button` | Mouse binding while Cheat Mode is active. |
| Cheat 1 Item |  |  | Alternate binding left unbound. |
| Cheat 1 Stack | `Left Mouse Button` | `Left Mouse Button` | Mouse binding while Cheat Mode is active. |
| Cheat 1 Stack |  |  | Alternate binding left unbound. |
| Toggle Cheat Mode |  |  |  |

#### JEI — Dev Tools

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Copy Recipe ID to Clipboard |  |  |  |

#### JEI — Edit Mode

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Hide Ingredient | `Ctrl + Left Mouse Button` | `Ctrl + Left Mouse Button` | Edit Mode only. |
| Hide Ingredient (With Wildcard) | `Ctrl + Right Mouse Button` | `Ctrl + Right Mouse Button` | Edit Mode only. |
| Toggle Hide Ingredients Mode |  |  |  |

#### JEI — Hovering over Config Button

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Toggle Cheat Mode |  |  |  |

#### JEI — Hovering with Mouse

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Add/Remove Bookmark |  |  |  |
| Craft Bookmarked Recipe (Many) |  |  |  |
| Craft Bookmarked Recipe (One) |  |  |  |
| Quick Move Ghost Item |  |  |  |
| Share Item to Chat |  |  |  |
| Show Recipe | `R` | `R` | Keyboard binding while hovering an item. |
| Show Recipe | `Left Mouse Button` | `Left Mouse Button` | Mouse binding in the JEI item list. |
| Show Uses | `U` | `U` | Keyboard binding while hovering an item. |
| Show Uses | `Right Mouse Button` | `Right Mouse Button` | Mouse binding in the JEI item list. |

#### JEI — Overlays

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Next Page | `Mouse Wheel Down` | `Mouse Wheel Down` | JEI overlay only. |
| Previous Page | `Mouse Wheel Up` | `Mouse Wheel Up` | JEI overlay only. |
| Select Search Bar | `Ctrl + F` | `Ctrl + F` |  |
| Show/Hide Bookmarked Ingredients |  |  |  |
| Show/Hide JEI Overlays | `Ctrl + O` | `Ctrl + O` |  |

#### JEI — Recipes

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Close Recipes GUI |  |  |  |
| Next Recipe |  |  |  |
| Next Recipe Category |  |  |  |
| Next Recipe Page | `Mouse Wheel Down` | `Mouse Wheel Down` | Recipe view only. |
| Pause Recipe Ingredient Cycling |  |  |  |
| Previous Recipe |  |  |  |
| Previous Recipe Category |  |  |  |
| Previous Recipe Page | `Mouse Wheel Up` | `Mouse Wheel Up` | Recipe view only. |

#### JEI — Search Filter

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Clear Search Filter | `Right Mouse Button` | `Right Mouse Button` | Right-click the search field. |
| Next Search |  |  |  |
| Previous Search | `Up Arrow` | `Up Arrow` | Search field only. |

#### Locked In Slots

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Lock Slot |  |  |  |

#### Pet Vault

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open Pet Vault | `V` | `R` | Part of the pet-control group. |
| Unsummon All Pets |  | `Ctrl + R` | Part of the pet-control group. |

#### Pro Placer

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Toggle Fast Block Placement |  |  |  |

#### Skill Perks

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open Perk Tree | `G` | `G` | Curios moves to `Ctrl + G`. |

#### Sophisticated Backpacks

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Open Backpack | `B` | `V` | `B` is reserved for Better Party. |
| Run Inventory Interaction Upgrades |  |  |  |
| Swap Tool Based on Current Block/Entity |  |  |  |
| Switch Upgrade in the 1st Slot On/Off |  |  |  |
| Switch Upgrade in the 2nd Slot On/Off |  |  |  |
| Switch Upgrade in the 3rd Slot On/Off |  |  |  |
| Switch Upgrade in the 4th Slot On/Off |  |  |  |
| Switch Upgrade in the 5th Slot On/Off |  |  |  |

#### Sophisticated Mods

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Sort Storage/Backpack |  |  |  |
| Transfer to Inventory |  |  |  |
| Transfer to Storage |  |  |  |

#### Sophisticated Item Actions

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|
| Deposit Item into Storages |  |  |  |
| Highlight Storage with Item |  |  |  |
| Restock Item from Storages |  |  |  |

#### Default-key sources

Defaults were last checked on 2026-10-09. Only keys explicitly stated by the game or mod author are copied into the Default key column.

- [Minecraft controls](https://www.minecraft.net/article/minecraft-controls) and [Java Edition screenshot controls](https://help.minecraft.net/hc/en-us/articles/40719065932557-Take-and-Manage-Screenshots-in-Minecraft-Java-Edition)
- [Combat Roll](https://modrinth.com/mod/combat-roll), [Pufferfish's Skills](https://modrinth.com/mod/skills), and [Skill Perks](https://www.curseforge.com/minecraft/mc-mods/skill-perks)
- [Jade](https://modrinth.com/mod/jade), [JEI](https://modrinth.com/mod/jei), [Iris](https://github.com/IrisShaders/Iris/blob/26.1/docs/guide.md), and [Curios](https://modrinth.com/mod/curios)
- [OneKeyMiner](https://www.curseforge.com/minecraft/mc-mods/onekeyminer-nf), [Effortless Building](https://modrinth.com/mod/effortless-building), [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map), and [Sophisticated Backpacks](https://modrinth.com/mod/sophisticated-backpacks)
- [Better Party](https://modrinth.com/mod/better-party), [PatPat](https://modrinth.com/plugin/patpat), [Pet Status](https://www.curseforge.com/minecraft/mc-mods/pet-status), and [Pet Vault](https://www.curseforge.com/minecraft/mc-mods/pet-vault)

### Choosing a race

Press `P` to open the Iourus Races menu. The five choices are Human, Elf, Dwarf, Orc, and Goblin. A race is not merely cosmetic: it is saved with the character and changes strengths, weaknesses, or passive behavior. Read the in-game traits before confirming instead of choosing only by appearance.

Race and class are independent. Race establishes the character's persistent foundation; the class system below comes from equipment and can be changed by changing the loadout. Because the official race documentation confirms persistent saving but does not document a free player reset, do not assume the choice can be changed whenever you like.

See [Races and racial traits](#races-and-racial-traits) for the canonical system entry.

### Choosing a class

There is no permanent class-selection screen. The pack's four RPG Series modules provide six archetypes, and the active playstyle comes from the weapon, class book, spells, and matching equipment currently being used.

| Archetype | Main role | Starting direction | Class book |
|---|---|---|---|
| Archer | Ranged damage | Use a bow or crossbow. | Archery Manual |
| Paladin | Front-line protection and damage | Use a heavy melee weapon; a shield supports a defensive style. | Paladin Libram |
| Priest | Healing and support | Use a holy wand or staff. | Holy Book |
| Rogue | Mobile melee and evasion | Use quick weapons; dual wielding suits the role. | Rogue Manual |
| Warrior | Heavy melee and control | Use slower, heavy weapons. | Warrior Codex |
| Wizard | Arcane, fire, or frost magic | Use a wand or staff. | A matching wizard tome |

To activate the full class kit:

1. Obtain a weapon appropriate for the intended archetype. Some caster weapons already provide a basic spell.
2. Find a Spell Binding Table in a village gazebo, or build one and surround it with bookshelves.
3. Create the corresponding class book at the table.
4. Equip the book and hold a compatible weapon. The spell or skill hotbar should then expose the available abilities.
5. Test the loadout somewhere safe before spending progression points.

Changing weapons or books is how to try another archetype; choosing a race does not lock the character to a class. See [Classes and playstyles](#classes-and-playstyles).

### Skills, abilities, spells, and runes

The pack has two progression screens plus the Spell Engine combat interface. They complement one another but do not represent the same pool of upgrades.

| System | Open it | What it changes |
|---|---|---|
| RPG class and weapon skill trees | `T` | Specializes class books, modifies existing spells and weapon skills, and unlocks passive combat effects. Skill points are earned by gathering XP. |
| Skill Perks | `G` | Spends XP on broader passive survival, movement, utility, and combat perks. |
| Spell or skill hotbar | Appears with a compatible loadout | Selects and casts the active abilities supplied by a class book or weapon. Hold `Left Ctrl` when the Spell Engine hotbar must be bypassed. |

The RPG tree is content supplied through Pufferfish's Skills; Pufferfish's Skills itself is the framework behind the `T` screen. The tree contains meaningful branches rather than one mandatory route, and a dedicated reset item can refund its points if a build needs to be changed later.

Runes are ammunition for casted spells, much like arrows are ammunition for bows. They can be crafted normally, while the Rune Crafting Altar produces them more efficiently. If a spell refuses to cast, check the equipped book and weapon, the selected ability, its cooldown, and the required runes before assuming the keybind is broken.

Better Combat changes ordinary weapon attacks into aimed swings, combos, and dual-wield sequences. Combat Roll adds a directional dodge on `E`; rolling has a cooldown and can consume hunger, so it is an escape tool rather than unlimited movement. Some RPG-tree passives can also trigger from combat events such as attacks, damage, evasion, or rolling.

See [Skill trees and character progression](#skill-trees-and-character-progression), [Abilities, spells, runes, and resources](#abilities-spells-runes-and-resources), and [Combat controls and dodge rolling](#combat-controls-and-dodge-rolling).

### First-session checklist

Complete these steps before the first long expedition:

- Apply the recommended controls, especially `P` for races, `T` for the RPG skill tree, `G` for general perks, `E` for rolling, and `B` for parties.
- Open the race menu with `P`, read every trait, and choose the character's persistent race.
- Pick an initial archetype from the class table, then obtain its appropriate weapon and class book.
- Equip the book and weapon together, confirm that the ability hotbar appears, and verify any rune requirement.
- Open `T` and inspect the class and weapon branches before spending points. Open `G` separately and remember that its perks spend XP.
- Practice a full basic attack sequence and one directional roll in a safe area.
- In multiplayer, create or join a party with `B`, review friendly fire and XP sharing, and set the party HUD to the recommended right-side vertical layout.
- Learn the downed and grave screens before carrying valuable equipment far from the base.
- Place Paper Doll at the upper-left and Coordinates Display directly beneath it so neither overlaps the party HUD.

### Parties, downed players, and death

Press `B` to create, browse, or manage a Better Party group. Parties can be public, invite-only, or password-protected; they support leader, officer, and member roles. The leader can manage friendly-fire protection, nearby XP sharing, and the party-member locator when the server permits them. Use `/p <message>` for party chat.

The recommended HUD layout places the party roster vertically on the right. Each player can move and scale that HUD independently, so one player's layout does not rearrange everyone else's screen.

Death recovery has several layers:

1. **Downed window:** PlayerRevive gives teammates a limited opportunity to rescue a fallen player before death completes. Treat the on-screen timer as authoritative because server settings can change the duration.
2. **Revival and grave:** Better Revive stores inventory, equipment, supported equipped slots, and experience in a protected grave. A helper can use a revive option, including the consent-based Life Pulse interaction when enabled, while the downed player may instead choose **Respawn now (no revival)**.
3. **Compatibility handoff:** The installed Better Revive × PlayerRevive bridge connects both systems and prevents repeated bleed-out or death loops. Players should follow the current on-screen prompt rather than trying to restart an earlier revive state.
4. **Final respawn:** If revival is declined, fails, or times out, Better Respawn normally places the player near the death location. A valid nearby bed or respawn anchor, returning from the End, and deaths across dimensions can use different placement rules.
5. **Recovery:** The grave remains the source of stored items after an ordinary respawn. Use Better Revive's grave tools and location information instead of searching blindly.

See [Death, revival, graves, and respawning](#death-revival-graves-and-respawning) and [Parties and multiplayer cooperation](#parties-and-multiplayer-cooperation).

#### Sources for the RPG onboarding

- [Iourus Races](https://modrinth.com/mod/iourus-races)
- [Archers](https://modrinth.com/mod/archers), [Paladins & Priests](https://modrinth.com/mod/paladins-and-priests), [Rogues & Warriors](https://modrinth.com/mod/rogues-and-warriors), and [Wizards](https://modrinth.com/mod/wizards)
- [Skill Tree (RPG Series)](https://modrinth.com/mod/skill-tree), [Pufferfish's Skills](https://modrinth.com/mod/skills), and [Skill Perks](https://www.curseforge.com/minecraft/mc-mods/skill-perks)
- [Spell Engine](https://modrinth.com/mod/spell-engine), [Runes](https://modrinth.com/mod/runes), [Better Combat](https://modrinth.com/mod/better-combat), and [Combat Roll](https://modrinth.com/mod/combat-roll)
- [Better Party](https://modrinth.com/mod/better-party), [PlayerRevive](https://modrinth.com/mod/playerrevive), [Better Revive](https://www.curseforge.com/minecraft/mc-mods/better-revive), [Better Revive × PlayerRevive](https://www.curseforge.com/minecraft/mc-mods/better-revive-x-playerrevive), and [Better Respawn](https://modrinth.com/mod/better-respawn)

## Part II — Character and Core Systems

### Races and racial traits

- **Iourus Races** — Adds Human, Elf, Dwarf, Orc, and Goblin as persistent character identities with different strengths, weaknesses, and passive behavior.

Race selection defines persistent character traits and belongs at the beginning of character setup. Open the race menu with the recommended `P` binding and read the in-game trait descriptions before confirming.

### Classes and playstyles

#### RPG classes

- **Type:** Integrated character-progression suite
- **Primary topic:** Classes and playstyles
- **Also affects:** Combat, Character, Skills, Magic, Equipment
- **Core idea:** Four installed RPG Series modules provide Archer, Paladin, Priest, Rogue, Warrior, and Wizard loadouts through weapons, class books, spells, and matching equipment.
- **Onboarding:** There is no permanent class-selection screen. Follow [Choosing a class](#choosing-a-class) to assemble and test a loadout.


### Skill trees and character progression

- **Skill Tree (RPG Series)** — Supplies class and weapon trees that modify spells, add passive combat effects, and turn gathered XP into skill points.
- **Pufferfish's Skills** — Provides the configurable progression framework and the `T` skill-tree screen used by the RPG trees.
- **Skill Perks** — Spends XP on a separate set of general survival, mobility, utility, and combat perks opened with `G`.

The `T` and `G` screens are separate systems. Inspect both before spending points so class specialization and general utility choices remain deliberate.

### Abilities, spells, runes, and resources

- **Runes** — Adds craftable ammunition consumed by class spells.

Spell Engine supplies the visible hotbar, casting behavior, targeting, cooldowns, class-book integration, and weapon skills. Runes act as spell ammunition and can be produced more efficiently with a Rune Crafting Altar. This handbook documents the shared mechanics without enumerating every spell.

### Combat controls and dodge rolling

- **Better Combat** — Replaces vanilla melee timing with animated attacks, weapon combos, dual-wield support, and improved hit detection.
- **Combat Roll** — Adds a dodge roll with its own attributes and enchantments.

### Weapons, armor, jewelry, and relics

Equipment is part of the build rather than a simple armor-value ladder. Check attack speed, spell power, healing power, ranged attributes, set bonuses, passive effects, and Curios bonuses before replacing an item solely because its material looks stronger.

#### Building a loadout

| Equipment layer | Main systems | What to decide |
|---|---|---|
| Weapon and class book | RPG classes, Better Combat, Spell Engine | Choose the attack style and active abilities the build is meant to use. |
| Armor | RPG class armor, Armory | Match bonuses to the class or damage type; complete sets can add bonuses beyond ordinary protection. |
| Jewelry | Jewelry | Fill Curios slots with useful combat attributes that support the chosen role. |
| Relic | Relics | Add a more distinctive active or passive mechanic; some effects interact with spells, weapon skills, or rolling. |
| Enchantments | Enchanting Infuser, Universal Enchants, enchantment collection | Refine the finished loadout after its basic weapon, armor, and accessories work together. |

Use the recommended `Ctrl + G` binding to inspect equipped Curios. Hover equipment and read its full tooltip before comparing it; [Enchantment Insights](#information-overlays-and-tooltips) explains unfamiliar enchantments, while `R` and `U` over an item open its JEI recipe and uses.

#### Acquisition and upgrade paths

- Class equipment is the practical starting point because it establishes the intended weapon, spell, and attribute combination.
- Jewelry spans several material tiers. Gems can be found underground and crafted into accessories, while village trades and dungeon loot provide alternative sources.
- Relics are primarily adventure rewards from dungeon chests and major enemies, with a smaller craftable selection connected to Jewelry materials. They are build-changing accessories, not ordinary armor upgrades.
- Arsenal weapons are rare loot rather than normal crafting goals. Look for them in major boss rewards and valuable dungeon chests; every Arsenal weapon carries at least one built-in passive spell.
- Armory sets upgrade existing RPG class armor with materials obtained from rare structure loot and bosses. Their set and spell-modifier bonuses reward keeping the build focused instead of mixing pieces only by armor value.

#### Included equipment systems

- **Alex's Caves equipment** — Exploration rewards support new combat options. See [Alex's Caves](#alexs-caves).
- **Stellarity equipment** — End progression includes additional equipment systems. See [Stellarity](#stellarity).

- **Arsenal** — Places legendary RPG weapons behind exploration and combat rewards instead of ordinary crafting.
- **Armory** — Adds RPG armor sets with distinct designs and set bonuses.
- **Jewelry** — Adds Curios-equipped rings and necklaces with combat attributes, supported by mining, crafting, trading, and loot.
- **Relics** — Adds Curios-equipped trinkets with active or passive mechanics that can reshape a build.
- **Too Many Bows** — Expands ranged builds with bows that have different abilities and combat attributes.
- **Arrow+** — Adds craftable arrow tiers with different damage levels using familiar materials.
- **Shield Upgrades** — Adds durable specialized shields whose attributes or defensive effects support different situations.
- **Enchantments collection** — Adds a broad selection of offensive, defensive, movement, and ranged enchantments.

#### Ranged and defensive equipment

Too Many Bows changes more than appearance: bow damage, draw speed, ranged scaling, and unique abilities can make two bows behave very differently. Better Combat supplies the shared ranged-combat handling, while Arrow+ and Fletching Recipe expand ammunition and crafting. Use JEI rather than guessing which bow and arrow mechanics can be combined.

Shield Upgrades provides side-grades with different attributes and reactive effects. Choose a shield for the expected hazard instead of treating every new shield as a direct replacement for the previous one.

#### Testing and changing equipment

Use a Target Dummy and Floating Damage Indicators to compare complete attack sequences, not only a single hit. Test with the same buffs, distance, runes, arrows, armor, and accessories each time; attack speed, cooldowns, area effects, and passive triggers can make the largest displayed number misleading.

Armor Quick Swap has no required hotkey: right-click an armor piece in the inventory to equip or exchange it, or sneak and right-click an armor stand to swap the full worn set. This is useful for keeping separate combat, travel, or utility sets without manually moving four slots.

### Enchanting and equipment improvement

The pack keeps the vanilla enchanting route but also provides a deterministic alternative. Both still depend on experience and the enchanting setup, so choose the workflow that matches the job instead of assuming one station replaces every other station.

| Station or system | Best use | Important behavior |
|---|---|---|
| Enchanting Table with Easy Magic | Quick vanilla-style enchanting | Items and lapis remain in the table, and the offers can be rerolled for a small lapis and experience-point cost. |
| Enchanting Infuser | Building a specific set of enchantments | The basic infuser lets the player choose enchantments directly; stronger options still depend on surrounding bookshelves and cost experience. |
| Advanced Enchanting Infuser | Reworking finished equipment | Can modify existing enchantments, repair using experience, and recover experience by removing unwanted enchantments. |
| Anvil with Easy Anvils | Combining, repairing, and renaming | Items remain in the anvil, prior-work penalties are reduced, enchanted-book costs are fairer, and renaming is free. |
| Universal Enchants | Expanded compatibility rules | Allows many existing enchantments on more weapon and equipment types and relaxes some vanilla incompatibilities. Server configuration remains authoritative. |

Use Enchantment Insights tooltips and the station interface to confirm what an unfamiliar enchantment does and whether the current item accepts it. Universal Enchants broadens the rules, but it does not mean every enchantment belongs on every item or that every normally exclusive combination is enabled on this server.

#### Included improvement systems

- **Enchanting Infuser** — Provides basic and advanced stations for choosing, modifying, repairing, or removing enchantments without relying entirely on random offers.
- **Easy Anvils** — Keeps items in anvils and makes combining, repairing, and renaming equipment less punitive.
- **Easy Magic** — Keeps items in enchanting tables and adds a low-cost way to reroll random offers.
- **Universal Enchants** — Broadens which equipment can accept existing enchantments and improves selected vanilla enchantment behavior.
- **DarkSmithing** — Adds custom ways to craft smithing templates.
- **OneKeyMiner** — Adds vein mining, crop harvesting, and automatic replanting.
- **Armor Quick Swap** — Swaps complete armor sets quickly from an inventory or armor stand.
- **Target Dummy** — Provides a craftable target for testing damage and character builds.
- **Elytra Trims** — Allows elytra to receive armor trims, dyes, banner patterns, and other visual treatments.
- **Naturally Trimmed** — Applies trims to some equipment generated through mobs, loot, and trades, making found gear visually distinct.
- **Armor Trim Item Fix** — Makes inventory icons show the applied trim pattern instead of a generic trimmed texture.
- **Fletching Recipe** — Gives the fletching table a crafting interface for efficient ordinary-arrow production and explosive arrows.

DarkSmithing makes smithing templates craftable through custom recipes; use JEI for the installed recipes rather than assuming vanilla duplication rules. Trims remain cosmetic unless another item explicitly states otherwise. Naturally Trimmed affects generated equipment, Armor Trim Item Fix changes inventory presentation, and Elytra Trims extends decoration to elytra.

OneKeyMiner is an equipment-use shortcut rather than an enchantment. Hold the recommended `Grave Accent` key while mining, farming, or planting to chain matching actions. It still consumes the relevant tool durability and can be limited by hunger, durability thresholds, block limits, and server settings, so inspect the tool before clearing a large group.

#### Sources for equipment and enchanting

- [Arsenal](https://modrinth.com/mod/arsenal-rpg-series), [Armory](https://modrinth.com/mod/armory-rpg-series), [Jewelry](https://modrinth.com/mod/jewelry), and [Relics](https://modrinth.com/mod/relics-rpg)
- [Too Many Bows](https://www.curseforge.com/minecraft/mc-mods/too-many-bows), [Arrow+](https://www.curseforge.com/minecraft/mc-mods/arrow), and [Shield Upgrades](https://www.curseforge.com/minecraft/mc-mods/shield-upgrades)
- [Enchanting Infuser](https://modrinth.com/mod/enchanting-infuser), [Easy Magic](https://modrinth.com/mod/easy-magic), [Easy Anvils](https://modrinth.com/mod/easy-anvils), and [Universal Enchants](https://modrinth.com/mod/universal-enchants)
- [DarkSmithing](https://modrinth.com/mod/darksmithing), [OneKeyMiner](https://modrinth.com/mod/onekeyminer_nf), [Armor Quick Swap](https://modrinth.com/mod/armor-quick-swap), and [Target Dummy](https://modrinth.com/datapack/target-dummy)
- [Elytra Trims](https://modrinth.com/mod/elytra-trims), [Naturally Trimmed](https://modrinth.com/mod/naturally-trimmed), [Armor Trim Item Fix](https://modrinth.com/mod/armor-trim-item-fix), and [Fletching Recipe](https://modrinth.com/mod/fletching-recipe)

### Death, revival, graves, and respawning

- **PlayerRevive** — Gives downed players a short rescue window before normal death handling completes.
- **Better Revive** — Protects items and experience in graves, supports consent-based or item-assisted revival, and provides grave recovery tools.
- **Better Respawn** — Normally respawns a player near their death location, with exceptions for nearby spawn points, dimension changes, and returning from the End.

The installed compatibility bridge connects PlayerRevive and Better Revive so their downed states hand off cleanly instead of repeating. See [Parties, downed players, and death](#parties-downed-players-and-death) for the player flow.

### Parties and multiplayer cooperation

- **Better Party** — Adds public and private parties, roles, shared nearby experience, friendly-fire protection, party chat, and a party HUD.

Open the party menu with the recommended `B` binding and use `/p <message>` for party chat. Party creation, roles, protection, shared experience, communication, and HUD customization are summarized in [Parties, downed players, and death](#parties-downed-players-and-death).

## Part III — Adventure and Challenge

This pack rewards exploration, but unfamiliar landmarks can begin boss fights, raids, or persistent threats. Before a long trip, open the world map with `M`, confirm that Coordinates Display can be toggled with `N`, record the route home, and carry supplies for both the journey out and the return.

### Expedition checklist

- Place or activate a return point before entering a new dimension or beginning a major encounter.
- Carry food, ordinary combat supplies, and any runes or arrows required by the current build.
- Keep one inventory route available for unexpected loot instead of leaving every slot full.
- In multiplayer, form the party with `B` before combat so the HUD, friendly-fire rules, shared experience, and revive flow are already working.
- Treat unfamiliar structures as active encounters. Observe the site, identify exits, and avoid opening containers or activating blocks until the group is ready.
- Watch the sky and chat at night. Blood Moons and other environmental threats can make an otherwise routine return trip unsafe.

### Bosses and major encounters

- **Stellarity encounters** — The End expansion includes major combat progression. See [Stellarity](#stellarity).

- **World Bosses** — Adds shrine-based encounters summoned through rituals, with multiplayer scaling, changing attack patterns, and shared reward caches.
- **Bosslike Ender Dragon** — Reworks the dragon into a multi-phase fight that scales with the number of players and becomes stronger after previous victories.
- **Ultimate Warden** — Turns the Warden into a boss encounter with a visible boss bar, dedicated dungeon, and custom rewards.
- **Gateway of Doom** — Opens timed, wave-based arena encounters with an on-screen boss bar, escalating enemies, and rewards for completing every wave.

| Encounter | How it begins | Mechanic that changes the plan |
|---|---|---|
| World Bosses | Find a shrine and complete its summoning ritual. | Treat the shrine as an arena and expect the encounter to scale for a group. |
| Bosslike Ender Dragon | Enter the End and begin the dragon fight. | Recalled crystals can become warded; stand in the glowing ring at the pillar base before trying to break that crystal. |
| Ultimate Warden | Discover and enter its dungeon. | This is not an ordinary Warden encounter; prepare for a contained boss area and a longer fight. |
| Gateway of Doom | A gateway is triggered manually or appears through an enabled event. | The boss bar shows the wave, remaining enemies, and timer. Finish the wave before time expires or the configured failure effects can apply. |

For every major encounter, place the respawn and recovery route first, then clear ordinary enemies around the arena. Gateway enemies are kept near their event area, so regroup outside its boundary instead of dragging the wave toward a base. Hell Wards can protect important areas from gateway placement when the server permits them.

Both Bosslike Ender Dragon and Stellarity modify the dragon encounter. Expect their mechanics to overlap, follow the telegraphs and boss-bar state shown by the current server, and do not rely on a vanilla dragon walkthrough.

### Invasions and wave events

- **Invasion** — Starts increasingly difficult Nexus-defense events whose attackers can build, dig, and adapt to the terrain.
- **Illager Invasion** — Adds new illager combat and support roles to raids and places others in structures.
- **It Takes a Pillage** — Adds pillager encampments, fortresses, patrol threats, and related rewards.

These systems are not interchangeable. **Invasion** begins around a placed and activated Nexus, while **Illager Invasion** makes village raids and related encounters more varied, and **It Takes a Pillage** adds hostile pillager locations to exploration.

Do not test a Nexus beside an irreplaceable home. Invasion enemies can bridge gaps, place ladders, and destroy blocks while trying to reach it. Use a prepared defense site, provide sight lines and fallback paths, and move valuable storage outside the likely damage area. A Gateway of Doom is also wave-based, but it is a timed arena challenge rather than a Nexus defense.

### Dangerous nights and environmental threats

#### Withered Lands

- **Type:** Integrated adventure expansion
- **Primary topic:** Dangerous nights and environmental threats
- **Also affects:** Combat, Events, Creatures, Structures, Equipment
- **Core idea:** Makes exploration more hostile through wither-themed enemies, behaviors, encounters, and rewards.
- **Player guidance:** Expect its structures, creatures, and rewards to belong to one hostile theme. Scout new sites from a safe distance and use Jade and JEI to identify unfamiliar content without needing an exhaustive enemy list.

#### The Graveyard

- **Type:** Integrated adventure expansion
- **Primary topic:** Dangerous nights and environmental threats
- **Also affects:** Structures, Creatures, Combat, Equipment
- **Core idea:** Adds graveyard-themed locations, enemies, atmosphere, and adventure rewards.
- **Player guidance:** Graveyard structures are adventure locations rather than decoration. Enter with an escape route, light the surrounding ground, and expect their atmosphere, enemies, and loot to be connected.

- **Blood Moon** — Replaces every fourth full moon with an Overworld event that strengthens hostile mobs, increases phantom pressure, and prevents sleeping through the night.
- **The Darkness Will Find You** — Adds a progression-based Deep Dark curse that can make night, sleep, dark dimensions, sculk spread, and hostile events increasingly dangerous until the player pursues its protection and resolution systems.
- **Armored Foes** — Lets more hostile mob families spawn wearing visible equipment and makes illagers equip armor during raids.

- **Somnora** — Replaces the instant night skip with accelerated world time, including visible sky and weather progression, multiplayer-aware sleeping, plant growth, and configurable ambience.

#### When the night changes

During a Blood Moon, sheltering underground or leaving the Overworld avoids the event, but a bed cannot skip it. If the night is ordinary, Somnora still means sleep advances the world instead of cutting instantly to morning. The Darkness Will Find You can separately make sleep unreliable after its curse begins, so read current messages and effects rather than assuming every failed sleep has the same cause.

### Travel, maps, compasses, and teleportation

- **Waystones** — Adds discoverable and craftable travel points that must be activated before they can be selected as destinations.
- **Bifrost Teleport** — Adds named-marker teleportation using mythic weapons and cinematic Bifrost effects.
- **Explorer's Compass** — Searches for vanilla and modded structures through an item interface.
- **Nature's Compass** — Searches for vanilla and modded biomes and displays information about the selected result.
- **Xaero's World Map** — Builds a full-screen map from terrain the player has explored.
- **Xaero's Map Multiplayer** — Adds multiplayer-oriented features to Xaero's maps.

| Need | Use | Control or interaction |
|---|---|---|
| Review explored terrain | Xaero's World Map | Press `M`; drag to pan and use the mouse wheel to zoom. |
| Read or share exact position | Coordinates Display | Press `N` to toggle the HUD, `Ctrl + Y` for its interface, or `Y` to send the current position in chat. |
| Locate a structure | Explorer's Compass | Use the compass, search the structure list, and select a target. |
| Locate a biome | Nature's Compass | Right-click the compass, search the biome list, and select a target; sneak-right-click resets it. |
| Build a return network | Waystones | Activate destinations as they are discovered, then travel through an available waystone or supported warp item. |
| Travel between named markers | Bifrost Teleport | Establish and name markers before relying on them as the return route. |

A compass identifies a direction and distance; it does not make the destination safe or automatically map the route. Add a map marker before leaving, and verify that the destination belongs to the current dimension. The world map only reveals explored terrain, so blank regions are unknown—not empty.

### Integrated world and dimension expansions

World-generation changes appear in newly generated terrain. Existing explored chunks normally retain their old layout, so travel beyond familiar borders when searching for a new biome or structure after a pack update.

#### Alex's Caves

- **Type:** Integrated world expansion
- **Primary topic:** Integrated world and dimension expansions
- **Also affects:** Combat, Equipment, World Generation, Creatures
- **Core idea:** Adds rare underground ecosystems with their own creatures, materials, equipment, and progression.
- **Player guidance:** Begin with an Underground Cabin and its Cave Compendium. The book teaches the intended discovery route, while the recommended `X` binding is reserved for equipment that exposes Alex's Caves' special ability action.

#### Better Nether

- **Type:** Integrated dimension expansion
- **Primary topic:** Integrated world and dimension expansions
- **Also affects:** World Generation, Structures, Creatures, Equipment
- **Core idea:** Expands Nether terrain, biomes, vegetation, structures, creatures, and exploration rewards.
- **Player guidance:** Expect the Nether to contain unfamiliar plants, materials, mobs, and large structures. Mark the portal immediately and bring spare ignition because ordinary visual landmarks are less reliable in the denser terrain.

#### Stellarity

- **Type:** Integrated dimension expansion
- **Primary topic:** Integrated world and dimension expansions
- **Also affects:** World Generation, Structures, Creatures, Equipment, Bosses
- **Core idea:** Expands the End with new terrain, structures, enemies, equipment, and progression.
- **Player guidance:** Treat the End as a full adventure dimension rather than a short dragon trip. Strongholds, the dragon, outer-island terrain, End Cities, crafting interactions, and boss encounters are all changed; some Stellarity recipes and rewards do not appear normally in JEI, so preserve written or in-world clues.

- **Biomes O' Plenty** — Adds more than 50 biomes plus matching plants and blocks.
- **Terralith** — Rebuilds Overworld terrain around almost 100 realistic and light-fantasy biomes made from vanilla blocks.
- **William Wythers' Overhauled Overworld** — Reinterprets vanilla biomes with larger, more realistic, and atmospheric terrain.
- **Dungeons Dimensions: Nether** — Brings Minecraft Dungeons-inspired detail and encounters to the Nether.
- **Nullscape** — Reworks End terrain and island generation.
- **Clear End City** — Adds cinematic End structures, void gardens, crashed citadels, and new loot locations.
- **Serene Seasons** — Adds seasonal color, temperature, and environmental changes.
- **Abyssal Ocean** — Generates rare, extremely deep offshore ocean regions that become darker toward bedrock.

Serene Seasons affects more than scenery: weather, temperature, and crop growth can change across the year. Nature's Compass finds a biome; Explorer's Compass finds a structure; neither replaces preparation for the seasonal or dimensional conditions around the target.

### Structures and dungeons

- **Integrated-expansion structures** — Several world expansions add structures as part of a larger ecosystem. See [Alex's Caves](#alexs-caves), [Better Nether](#better-nether), [Stellarity](#stellarity), and [The Graveyard](#the-graveyard).

- **Dungeons and Taverns** — Adds taverns, ruins, camps, and dungeons throughout the world.
- **Dungeons and Taverns overhauls** — Rebuilds ancient cities, mineshafts, pillager outposts, and strongholds.
- **Awesome Dungeon collection** — Adds combat dungeons to the Overworld, oceans, and End.
- **Moog's structure collection** — Adds bountiful structures, End and Nether landmarks, ocean structures, reimagined temples, and voyager structures.
- **Towns and Towers** — Expands villages, pillager outposts, and ships while keeping a vanilla-like style.
- **Explorify** — Adds lightweight exploration structures designed to blend with vanilla generation.
- **ATi Structures: Vanilla Edition** — Adds a collection of custom structures and dungeons built with vanilla materials.
- **Much More Dungeons** — Adds more dungeon layouts and exploration targets.
- **Towers of Chambers** — Turns trial chambers into large, vertical combat towers.
- **Underground Villages** — Generates functioning villages below ground.
- **Ocean Lily Pad Village** — Generates villages built across the ocean surface.
- **Antique Trading Ship** — Places a discoverable villager trading ship in ocean biomes.
- **Hopo Better Underwater Ruins** — Replaces small underwater ruins with larger and more interesting sites.
- **Improved Village Placement** — Chooses terrain that produces more natural village layouts.
- **Gazebos** — Adds village gazebos that contain small spell libraries.

#### Exploring generated structures

Use this same loop for unfamiliar structures without requiring a room-by-room spoiler:

1. Mark the entrance and identify a retreat route before descending or activating anything.
2. Use Jade to identify unfamiliar blocks and entities, then `R` and `U` over recovered items to inspect their recipes and uses in JEI.
3. Secure one area at a time. Structure mods can overlap with the pack's expanded hostile roster, so a familiar building shape does not guarantee familiar enemies.
4. Keep unique books, maps, templates, keys, and named components until their purpose is understood.
5. Recheck villages for useful services. Gazebos can supply spell libraries, guards can protect settlements, and some structure overhauls change where progression landmarks appear.

### Creatures and hostile mobs

- **Alex's Caves creatures** — Cave-specific creatures belong to the broader cave ecosystem. See [Alex's Caves](#alexs-caves).
- **Better Nether creatures** — Nether creatures are documented with their dimension systems. See [Better Nether](#better-nether).
- **Stellarity creatures** — End creatures are documented with their dimension progression. See [Stellarity](#stellarity).

#### Shroomcraft

- **Type:** Integrated adventure expansion
- **Primary topic:** Creatures and hostile mobs
- **Also affects:** Creatures, Farming, Food, Building
- **Core idea:** Adds mushroom creatures, crops, colorful shroomwood, and connected survival content.
- **Player guidance:** Treat its creatures, cultivation, food, and building materials as one ecosystem. Use observed behavior, Jade identification, and JEI recipes instead of assuming every mushroom creature is either hostile or decorative.

- **Alex's Mobs** — Adds a large roster of real and fantasy creatures with distinct behaviors and rewards.
- **Animal Garden collection** — Adds a broad collection of land and aquatic wildlife with species-specific behavior.
- **Ender Zoology** — Adds hostile mobs designed to feel like extensions of the vanilla roster.
- **Lullaby's Mobs** — Adds additional custom hostile creatures.
- **Mutants and Zombies** — Adds stronger mutant zombie enemies.
- **Moblets** — Adds small and baby variants of existing mobs.
- **Mounts and Monsters** — Adds rideable creatures and new monsters.
- **Craftable Creatures Evolution** — Makes mobs and mob spawners obtainable through crafting-based progression.
- **Ribbits** — Adds social frog-like villagers to swamps.
- **Guard Ribbits** — Adds armed protectors for Ribbit settlements.
- **Guard Villagers** — Adds armed defenders to ordinary villages.
- **Goblin Traders** — Adds wandering goblins with unusual trades.

Do not judge an unfamiliar creature only by its model. Watch whether it is passive, defensive, territorial, tameable, rideable, or openly hostile before approaching. Jade supplies the name and health information; JEI can explain known drops and uses without turning this handbook into a species catalogue. Guard Villagers and Guard Ribbits are settlement defenders, while Goblin Traders and Ribbits are social or trading encounters rather than ordinary hostile spawns.

### Companions, pets, villagers, and settlements

- **Companions: Dogfolk** — Adds dogfolk companions.
- **Wild Pets** — Makes polar bears, pandas, and foxes tameable; tamed bears can also be ridden.
- **Puffsprout** — Adds a small companion that learns over time.
- **Animal Pen** — Stores groups of farm animals in a single block to simplify husbandry and reduce entity load.
- **Compact Villagers** — Lets players carry villagers and trade through compact booths.
- **Name Tag Upgrade** — Allows on-the-go renaming and adds convenience improvements to name-tag use.
- **Pet Vault** — Gives pets portable storage.
- **Pet Status** — Exposes useful health and status information for owned pets.
- **Iron Wolf Armor** — Adds protective equipment for wolves.
- **Party Creepers** — Replaces destructive creeper damage with a decorative party-style explosion.

| Companion task | Recommended control | Use |
|---|---|---|
| Inspect an owned pet | `Shift + R` | Opens Pet Status without conflicting with the recommended JEI controls. |
| Open portable pet storage | `R` | Opens Pet Vault; use this away from an item-hover context. |
| Review the player party | `B` | Opens Better Party. Pets and player parties are separate systems. |

Pet Status is the quickest health check before travel. Pet Vault adds portable pet storage, but it should not replace an emergency supply kept on the player. Animal Pen and Compact Villagers reduce crowded entity management at settlements; verify that a creature or villager is stored safely before changing or removing the containing block.

#### Sources for adventure and exploration

- [World Bosses](https://www.curseforge.com/minecraft/mc-mods/world-bosses), [Bosslike Ender Dragon](https://www.curseforge.com/minecraft/mc-mods/bosslike-ender-dragon), [Ultimate Warden](https://www.curseforge.com/minecraft/mc-mods/ultimate-warden), and [Gateway of Doom](https://modrinth.com/mod/gateway-of-doom)
- [Invasion Mod Fork](https://modrinth.com/mod/invasion-mod-unofficial), [Illager Invasion](https://modrinth.com/mod/illager-invasion), and [It Takes a Pillage Continuation](https://modrinth.com/mod/it-takes-a-pillage-continuation)
- [Withered Lands](https://modrinth.com/mod/withered-lands), [The Graveyard unofficial port](https://www.curseforge.com/minecraft/mc-mods/the-graveyard-unofficial-port), [Blood Moon](https://modrinth.com/datapack/ks-blood-moon), [The Darkness Will Find You](https://modrinth.com/mod/the-darkness-will-find-you), [Armored Foes](https://modrinth.com/mod/armored-foes), and [Somnora](https://www.curseforge.com/minecraft/mc-mods/somnora)
- [Waystones](https://modrinth.com/mod/waystones), [Explorer's Compass](https://www.curseforge.com/minecraft/mc-mods/explorers-compass), [Nature's Compass](https://www.curseforge.com/minecraft/mc-mods/natures-compass), and [Xaero's World Map](https://modrinth.com/mod/xaeros-world-map)
- [Alex's Caves](https://modrinth.com/mod/alexs-caves), [BetterNether](https://modrinth.com/mod/betternether), [Stellarity](https://modrinth.com/datapack/stellarity), and [Serene Seasons](https://modrinth.com/mod/serene-seasons)
- [Pet Status](https://www.curseforge.com/minecraft/mc-mods/pet-status) and [Pet Vault](https://www.curseforge.com/minecraft/mc-mods/pet-vault)

## Part IV — Survival and Creation

These systems turn gathered resources into long-term infrastructure. The useful order is simple: establish repeatable food, organize portable and base storage, then add faster building, item routing, vehicles, and player trading as those needs appear.

### Farming, food, and cooking

- **Shroomcraft cultivation** — Mushroom crops connect to a wider creature and building ecosystem. See [Shroomcraft](#shroomcraft).
- **Starcatcher food and collection** — Fishing rewards connect collection systems with food and equipment. See [Starcatcher](#starcatcher).

#### Farmer's Delight

- **Type:** Integrated survival or profession expansion
- **Primary topic:** Farming, food, and cooking
- **Also affects:** Farming, Food, Equipment, Building
- **Core idea:** Expands farming, cooking tools, food preparation, meals, and kitchen-centered survival.
- **Player guidance:** Build around preparation, heat, and serving rather than treating every meal as an ordinary crafting-grid recipe. Use JEI to identify the required workstation and container for the food being made.

- **Kaleidoscope Cookery** — Adds more ingredients and recipes around the cooking loop.
- **Better Rotten Flesh** — Adds useful ways to process and consume rotten flesh and new zombie-feeding behavior.
- **Universal Bone Meal** — Makes bone meal work on a wider range of plants.
- **No Crop Destruction** — Prevents farmland and crops from being trampled during normal play.

#### Kitchen workflow

| Job | Main system | How to approach it |
|---|---|---|
| Prepare ingredients | Cutting boards, knives, and other preparation stations | Check the JEI recipe category instead of assuming the crafting grid. Preparation can improve yield or create an ingredient used by another station. |
| Cook directly | Heated cookware and stoves | Supply the required ingredient and heat source, then collect the cooked result. |
| Make batch meals | Cooking pots and compatible containers | Place the pot over heat, follow the displayed recipe, and provide the serving container when the recipe requires one. |
| Use Cookery stations | Kaleidoscope Cookery's kitchen blocks | Its chopping, milling, cooking, and steaming processes have their own recipe categories; follow JEI rather than substituting a similar-looking station. |
| Serve or display food | Feast and display blocks | Some prepared foods are placed in the world and taken in portions instead of being eaten directly from the original block. |

Farmer's Delight and Kaleidoscope Cookery overlap in theme but do not make every workstation interchangeable. Search the desired result in JEI, open its recipe with `R`, and follow the station shown there. Better Rotten Flesh provides a use for a normally poor food source, but its processed results and zombie interactions should still be tested away from villagers and livestock.

Serene Seasons can change crop growth across the year. Universal Bone Meal broadens which plants accept bone meal, while No Crop Destruction protects the farm from ordinary trampling; neither guarantees that every crop will grow equally well in every season or environment.

### Fishing and collection systems

#### Starcatcher

- **Type:** Integrated survival or profession expansion
- **Primary topic:** Fishing and collection systems
- **Also affects:** Food, Equipment, Events, Multiplayer
- **Core idea:** Adds collectible fish, fishing minigames, equipment, trophies, tournaments, and a guidebook.
- **Player guidance:** Fish are selected by conditions such as biome, weather, time, and elevation. Use the guide and catalogue to plan where to fish instead of expecting one location to provide every catch.

#### Starcatcher first-catch flow

1. Open the Starcatcher guide with `Ctrl + Space` and review its help pages before choosing a fishing location.
2. Prepare the rod and tackle box. Hooks, bobbers, and bait alter the setup, and uncommon catches can require the right combination.
3. Cast normally. When the minigame begins, follow its on-screen target and use `Space` for **Minigame Hit**; this shares Jump safely because the action is minigame-specific.
4. Check the catalogue after the catch. It records discoveries and their measurements, while JEI covers recipes that the guide does not.
5. During a tournament, use `Tab` for the tournament overlay. This intentionally shares the key with Inventory because the contexts are separate.
6. Decide whether the catch belongs in food preparation, a display or aquarium, the collection, or the selling system.

Starcatcher's difficulty and presentation have accessibility settings, including an option to disable the minigame. Treat the server configuration as authoritative. This handbook explains the loop without listing every fish or hidden condition.

### Potions and alchemy

#### Alchemia

- **Type:** Integrated survival or profession expansion
- **Primary topic:** Potions and alchemy
- **Also affects:** Magic, Equipment, Food
- **Core idea:** Reimagines potion brewing as a simplified, Potion Craft-inspired alchemy system.
- **Player guidance:** Alchemia is an ingredient-navigation system rather than a replacement skin for the Brewing Stand. Learn it at an Alchemical Cauldron and keep ordinary brewing recipes as a separate workflow.
- **Potion Time Stacker** — Lets repeated potion effects extend their remaining duration.
- **Potions Stack** — Allows ordinary potions to stack in small groups.

#### Alchemia workflow

1. Place an ordinary cauldron over a campfire to create the Alchemical Cauldron setup.
2. Add discovered alchemy ingredients. Each one moves the mixture's bias in a direction on the radial effect map.
3. Continue adjusting the mixture until the water changes to the desired effect. Combining paths can produce a potion with more than one effect; the last effect added is generally the strongest.
4. Use glass bottles on the completed cauldron to collect the result. One full cauldron can fill four bottles.
5. Record useful ingredient paths and confirm unfamiliar ingredients or outputs with JEI before consuming the result.

Potion Time Stacker changes repeated use: drinking another potion with the same active effect adds duration instead of simply replacing the old timer. Splash-potion stacking and the maximum duration are server-configurable, so check the actual effect timer after use. Potions Stack changes inventory capacity only; potions still stack only when their item data matches.

### Building and decoration

#### Macaw's building collection

- **Type:** Integrated building expansion
- **Primary topic:** Building and decoration
- **Also affects:** Building, Storage
- **Core idea:** Adds coordinated bridges, doors, fences, furniture, decorations, lights, paintings, paths, roofs, stairs, trapdoors, and windows.
- **Player guidance:** Treat the collection as coordinated block families. Search by the source mod in JEI or filter by the material being used instead of browsing every decorative variant individually.

- **Display Delight** — Adds decorative ways to display food and related items.
- **Functional Sculptures** — Adds a collection of craftable statues, monuments, and memorials for decorative builds.
- **Armor Statues** — Unlocks detailed posing and customization for armor stands.
- **Stoneworks** — Adds building variants for vanilla stone families.
- **Effortless Building** — Adds tools for placing and editing large shapes and repeated structures quickly.
- **Pro Placer** — Improves precise placement, reach-based building, and bridging.
- **Stonecutting Upgrade** — Expands the stonecutter interface, remembers recipes, and supports quick material refills.
- **Arcane Lanterns** — Uses catalysts to give lanterns different magical area effects.
- **Barricades** — Adds defensive barricades, contact-damage obstacles, and resettable traps.

#### Bulk-building workflow

1. Prototype a small section with ordinary placement and confirm the palette, orientation, and block count.
2. Hold `Left Alt` to open Effortless Building's radial menu, choose a shape, and review its fill and replacement options.
3. With the building block selected, right-click the first position, aim at the endpoint, and right-click again to place the previewed shape. Survival use consumes the correct blocks from inventory.
4. Use `Ctrl + Z` and `Ctrl + Y` for undo and redo when the result is wrong. Server limits still control reach, shape size, breaking, and replacement behavior.
5. Use mirrors or arrays for repeated sections only after one section is correct. Pro Placer supports precise single-block work and bridging where a bulk shape would be excessive.

Effortless Building can place, break, or apply tool interactions across a selected shape. Check the preview before confirming, especially near storage, redstone, or irreplaceable blocks. The Randomizer tool can vary a palette, while an empty weighted slot can intentionally leave gaps.

Stonecutting Upgrade expands the stonecutter recipe area and remembers the previous selection. Press `Space` inside the stonecutter to refill one input item, or hold `Shift` with `Space` to refill a stack. These are interface actions, not global building keybinds.

Armor Statues provides a dedicated interface for poses, body-part rotation, visibility, alignment, and display options. Arcane Lanterns uses a Lantern Maker and catalysts to create area effects; read the JEI description before placement because some effects help farms or movement while others repel, contain, debuff, or damage entities. Barricades and traps are functional defenses, so test their reset and contact behavior before placing them on a shared path.

### Storage and inventory management

- **Sophisticated Backpacks** — Adds upgradeable portable storage with functional upgrade slots.
- **Tom's Simple Storage** — Joins ordinary containers into one searchable storage network.
- **Linked Chests** — Lets separate chests share an inventory.
- **Easy Shulker Boxes** — Allows shulker boxes to be used directly from the inventory.
- **Locked In Slots** — Protects chosen inventory slots from accidental movement or replacement.

| Storage need | Best starting system | Important behavior |
|---|---|---|
| Portable general storage | Sophisticated Backpacks | Open the equipped or carried backpack with the recommended `V` key. Higher backpack tiers add capacity and upgrade slots; installed upgrades determine automation and utility behavior. |
| One searchable base inventory | Tom's Simple Storage | Connect ordinary inventories to a network and access them through a terminal. The terminal hotkey is intentionally unbound, so interact with the placed terminal unless you assign one. |
| Shared storage across locations | Linked Chests | Matching three-dye channels open the same inventory. Personal channels can restrict access, and a linked pouch can provide portable channel access. |
| Temporary packed storage | Easy Shulker Boxes | Browse and move contents while the shulker box remains in the inventory instead of placing and breaking it repeatedly. |
| Protected inventory positions | Locked In Slots | Hover a slot and use its lock action to prevent movement, swapping, or accidental dropping. The pack leaves this key unbound until a player chooses one. |

Sophisticated Backpacks can also be placed as blocks and used with hoppers or other inventory automation. Keep its role distinct from a Tom's network: a backpack is a portable container with upgrades, while Tom's Simple Storage indexes connected inventories rather than moving everything into one giant chest.

Linked Chests share data by channel, not by physical adjacency. Label the three-color combination, decide whether the channel is shared or personal, and test a second chest before trusting it with important materials. Easy Shulker Boxes and Locked In Slots are interaction safeguards; they do not expand a storage network.

### Item transport and logistics

- **Golden Hopper** — Adds a hopper with configurable filtering behavior.
- **Hopper Gadgetry** — Adds more capable hopper controls and item-routing tools.
- **Sophisticated Inventory Interactions** — Brings search, sorting, and transfer controls to eligible container screens.
- **Sophisticated Item Actions** — Finds nearby inventories containing an item and supports direct restocking or depositing.

#### Choosing a transport tool

| Tool | Direction and purpose | Common mistake |
|---|---|---|
| Golden Hopper | Works like a hopper with one physical filter item controlling what can be pulled and pushed. | The filter slot stores a real item; it is not only a ghost reference. |
| Grated Hopper | Collects and transfers only items that match its configured filter slots. | An empty or incorrect filter changes what the system accepts. |
| Duct | Pushes items toward connected containers in any direction and can be chained. | It does not pull items or collect loose drops. |
| Chute | Collects loose items and sends them downward without keeping an internal inventory. | It is a one-way drop path, not a general pipe. |
| Sophisticated Inventory Interactions | Adds search, sort modes, and transfer buttons to supported inventory screens. | Filtered transfer moves items matching something already stored at the destination; verify the destination before using transfer-all. |
| Sophisticated Item Actions | Highlights nearby matching storage or moves selected items without opening each container. | Modifier keys change whether the hotbar, main inventory, empty slots, or unmatched storage slots are included. |

The Sophisticated interaction and item-action keybinds are intentionally left unbound in the recommended profile. Assign only the actions you will use, then test them with disposable items: deposit and transfer operations can affect many stacks at once. Locked In Slots can protect personal slots from ordinary inventory mistakes, but it should not be treated as a substitute for verifying a bulk destination.

### Vehicles and travel equipment

- **Seaworthy Boats** — Gives boats health and armor, then adds faster and tougher reinforcement tiers plus a shipyard repair loop.
- **Vehicle Upgrade** — Improves mount and vehicle behavior, including swimming, terrain handling, rider inventory access, mounted mining, and reduced wandering.

Use a Shipyard near the boat to switch between repair and upgrade work. Repairs consume planks; reinforcement consumes the configured material and normally advances through the available tiers in sequence. The riding HUD shows boat health when enabled, so return for repairs before a damaged boat breaks far from shore.

Vehicle Upgrade affects many rideable entities rather than supplying a new vehicle family. Saddled mounts can stay where they are left, mounts can handle water and leaves more reliably, and passengers interact with nearby blocks more safely. Some features are configurable, so observed server behavior takes precedence over the full feature list.

### Trading and multiplayer economy

- **Market Board** — Provides a shared multiplayer marketplace where players can list, buy, remove, and collect proceeds from item sales.

Right-click a placed board to open the market. Use its search, sorting, and scrolling controls to review existing listings before creating one. When selling, verify the item, quantity, accepted server currency, and price before confirming; completed-sale currency must be collected through the board. The server administrator controls which currencies are registered, and currency items themselves cannot be sold through the system.

Market Board is for asynchronous player listings. Goblin Traders and other unusual traders remain direct creature interactions and are documented under [Creatures and hostile mobs](#creatures-and-hostile-mobs).

#### Sources for survival and creation

- [Farmer's Delight](https://www.curseforge.com/minecraft/mc-mods/farmers-delight) and [Kaleidoscope Cookery](https://modrinth.com/mod/kaleidoscope-cookery)
- [Starcatcher](https://modrinth.com/mod/starcatcher)
- [Alchemia](https://modrinth.com/mod/alchemia), [Potion Time Stacker](https://modrinth.com/mod/potion-time-stacker), and [Potions Stack](https://www.curseforge.com/minecraft/mc-mods/potions-stack)
- [Macaw's building mods](https://www.curseforge.com/members/sketch_macaw/projects), [Effortless Building](https://modrinth.com/mod/effortless-building), [Armor Statues](https://modrinth.com/mod/armor-statues), [Stonecutting Upgrade](https://www.curseforge.com/minecraft/mc-mods/stonecutting-upgrade), [Arcane Lanterns](https://modrinth.com/mod/arcane-lanterns), and [Traps and Barricades](https://www.curseforge.com/minecraft/mc-mods/traps-and-brricades)
- [Sophisticated Backpacks](https://modrinth.com/mod/sophisticated-backpacks), [Tom's Simple Storage](https://modrinth.com/mod/toms-storage), [Linked Chests](https://modrinth.com/mod/new-linked-chests), [Easy Shulker Boxes](https://modrinth.com/mod/easy-shulker-boxes), and [Locked In Slots](https://modrinth.com/mod/locked-in-slots)
- [Golden Hopper](https://www.curseforge.com/minecraft/mc-mods/golden-hopper), [Hopper Gadgetry](https://modrinth.com/mod/hopper-gadgetry), [Sophisticated Inventory Interactions](https://modrinth.com/mod/sophisticated-inventory-interactions), and [Sophisticated Item Actions](https://modrinth.com/mod/sophisticated-item-actions)
- [Seaworthy Boats](https://modrinth.com/mod/seaworthy-boats), [Vehicle Upgrade](https://modrinth.com/mod/vehicle-upgrade), and [The Market Board](https://www.curseforge.com/minecraft/mc-mods/market-board)

## Part V — Interface and Reference

### Information overlays and tooltips

- **AppleSkin** — Shows hunger and saturation information in the HUD.
- **Jade** — Identifies blocks and entities being looked at and shows relevant state information.
- **Effect Insights** — Explains active and available status effects.
- **Enchantment Insights** — Explains enchantments and their behavior.
- **Food Effect Tooltips** — Shows food-related effects before an item is eaten.
- **Floating Damage Indicators** — Displays combat damage numbers above affected entities.
- **Controlling** — Makes keybinds searchable and easier to diagnose.

### Recipe and loot information

- **Just Enough Items** — Provides searchable item, recipe, and usage views.
- **JEI Trades** — Adds villager trade information to the recipe browser.
- **Just Enough Filters** — Adds more ways to filter the item browser.
- **Advanced Loot Info** — Shows detailed loot-table and trade information in the recipe browser.
- **Advanced Worldgen Info** — Shows where world-generation features and resources can appear.
- **Show My Recipes** — Unlocks crafting recipes in the recipe book so players can discover what is available.

Recipe browsing, uses, trades, loot tables, and world-generation lookup tools receive their canonical entries here.

### HUD changes and customization

For each configurable interface, document the available display modes, what each mode changes, the authoritative default when published, the recommended pack setting, known overlaps, and how to open its configuration interface. Undocumented defaults remain blank rather than being inferred from screenshots.

#### Recommended HUD arrangement

- Place **Paper Doll** at the upper-left.
- Place **Coordinates Display** directly beneath Paper Doll.
- Place **Better Party** in a vertical panel on the right.
- Explain alternative Coordinates Display modes rather than presenting the recommendation as the only valid setup.
- Keep the party panel clear of the personal HUD stack and note possible competition with status effects or server scoreboards.

Design asset: `output/playwright/hud-layout-recommended.png`

#### Coordinates Display configuration

Coordinates Display provides position information through a configurable HUD. Its Controls category confirms actions for changing the HUD position, cycling display modes, opening its GUI, toggling the HUD, and controlling related navigation features. The handbook should explain the alternative modes without claiming an undocumented default.

#### Better Party configuration

Better Party provides live party information alongside its party-management mechanics. The recommended right-side vertical layout prevents its panel from covering Paper Doll and Coordinates Display; exact default placement and configuration paths require an authoritative source before publication.

#### Paper Doll configuration

Paper Doll gives immediate visual feedback about the player's character and equipment. The recommended upper-left placement makes it the first element in the personal HUD stack, with Coordinates Display directly below it; exact default placement and configuration paths require an authoritative source before publication.

- **Hovering Hotbar** — Shifts the hotbar upward slightly so it appears to float above the screen edge.
- **Overflowing Bars** — Keeps health, armor, and similar HUD values readable beyond vanilla limits.
- **Paper Doll** — Adds a small on-screen view of the player's character and equipment.

### Cosmetic and animation changes

- **Eating Animation** — Adds visible first-person eating and drinking animations.
- **Distinct Potions** — Makes potion types easier to distinguish visually.
- **Quick Skin** — Makes changing player skins faster.
- **PatPat** — Adds a friendly player-to-player and player-to-creature pat interaction.
- **Kingdom Cats Replacer** — Replaces vanilla cat visuals with seven animated models while preserving vanilla cat behavior.

Cosmetic replacements, equipment appearance, and first-person animation changes receive their canonical entries here.

### Optional client features

- **Coordinates Display** — Shows the player's coordinates and related navigation information on a configurable HUD.
- **Iris** — Enables shader-pack support.
- **Sodium** — Replaces the renderer to improve frame rate and reduce rendering stutter.

### Alphabetical mod index

- [Abyssal Ocean](#integrated-world-and-dimension-expansions)
- [Advanced Loot Info](#recipe-and-loot-information)
- [Advanced Worldgen Info](#recipe-and-loot-information)
- [Alchemia](#alchemia)
- [Alex's Caves](#alexs-caves)
- [Alex's Mobs](#creatures-and-hostile-mobs)
- [Animal Garden collection](#creatures-and-hostile-mobs)
- [Animal Pen](#companions-pets-villagers-and-settlements)
- [Antique Trading Ship](#structures-and-dungeons)
- [AppleSkin](#information-overlays-and-tooltips)
- [Arcane Lanterns](#building-and-decoration)
- [Armor Quick Swap](#enchanting-and-equipment-improvement)
- [Armor Statues](#building-and-decoration)
- [Armor Trim Item Fix](#enchanting-and-equipment-improvement)
- [Armored Foes](#dangerous-nights-and-environmental-threats)
- [Armory](#weapons-armor-jewelry-and-relics)
- [Arrow+](#weapons-armor-jewelry-and-relics)
- [Arsenal](#weapons-armor-jewelry-and-relics)
- [ATi Structures: Vanilla Edition](#structures-and-dungeons)
- [Awesome Dungeon collection](#structures-and-dungeons)
- [Barricades](#building-and-decoration)
- [Better Combat](#combat-controls-and-dodge-rolling)
- [Better Nether](#better-nether)
- [Better Party](#parties-and-multiplayer-cooperation)
- [Better Respawn](#death-revival-graves-and-respawning)
- [Better Revive](#death-revival-graves-and-respawning)
- [Better Rotten Flesh](#farming-food-and-cooking)
- [Bifrost Teleport](#travel-maps-compasses-and-teleportation)
- [Biomes O' Plenty](#integrated-world-and-dimension-expansions)
- [Blood Moon](#dangerous-nights-and-environmental-threats)
- [Bosslike Ender Dragon](#bosses-and-major-encounters)
- [Clear End City](#integrated-world-and-dimension-expansions)
- [Combat Roll](#combat-controls-and-dodge-rolling)
- [Compact Villagers](#companions-pets-villagers-and-settlements)
- [Companions: Dogfolk](#companions-pets-villagers-and-settlements)
- [Controlling](#information-overlays-and-tooltips)
- [Coordinates Display](#optional-client-features)
- [Craftable Creatures Evolution](#creatures-and-hostile-mobs)
- [DarkSmithing](#enchanting-and-equipment-improvement)
- [Display Delight](#building-and-decoration)
- [Distinct Potions](#cosmetic-and-animation-changes)
- [Dungeons and Taverns](#structures-and-dungeons)
- [Dungeons and Taverns overhauls](#structures-and-dungeons)
- [Dungeons Dimensions: Nether](#integrated-world-and-dimension-expansions)
- [Easy Anvils](#enchanting-and-equipment-improvement)
- [Easy Magic](#enchanting-and-equipment-improvement)
- [Easy Shulker Boxes](#storage-and-inventory-management)
- [Eating Animation](#cosmetic-and-animation-changes)
- [Effect Insights](#information-overlays-and-tooltips)
- [Effortless Building](#building-and-decoration)
- [Elytra Trims](#enchanting-and-equipment-improvement)
- [Enchanting Infuser](#enchanting-and-equipment-improvement)
- [Enchantment Insights](#information-overlays-and-tooltips)
- [Enchantments collection](#weapons-armor-jewelry-and-relics)
- [Ender Zoology](#creatures-and-hostile-mobs)
- [Explorer's Compass](#travel-maps-compasses-and-teleportation)
- [Explorify](#structures-and-dungeons)
- [Farmer's Delight](#farmers-delight)
- [Fletching Recipe](#enchanting-and-equipment-improvement)
- [Floating Damage Indicators](#information-overlays-and-tooltips)
- [Food Effect Tooltips](#information-overlays-and-tooltips)
- [Functional Sculptures](#building-and-decoration)
- [Gateway of Doom](#bosses-and-major-encounters)
- [Gazebos](#structures-and-dungeons)
- [Goblin Traders](#creatures-and-hostile-mobs)
- [Golden Hopper](#item-transport-and-logistics)
- [Guard Ribbits](#creatures-and-hostile-mobs)
- [Guard Villagers](#creatures-and-hostile-mobs)
- [Hopo Better Underwater Ruins](#structures-and-dungeons)
- [Hopper Gadgetry](#item-transport-and-logistics)
- [Hovering Hotbar](#hud-changes-and-customization)
- [Illager Invasion](#invasions-and-wave-events)
- [Improved Village Placement](#structures-and-dungeons)
- [Invasion](#invasions-and-wave-events)
- [Iourus Races](#races-and-racial-traits)
- [Iris](#optional-client-features)
- [Iron Wolf Armor](#companions-pets-villagers-and-settlements)
- [It Takes a Pillage](#invasions-and-wave-events)
- [Jade](#information-overlays-and-tooltips)
- [JEI Trades](#recipe-and-loot-information)
- [Jewelry](#weapons-armor-jewelry-and-relics)
- [Just Enough Filters](#recipe-and-loot-information)
- [Just Enough Items](#recipe-and-loot-information)
- [Kaleidoscope Cookery](#farming-food-and-cooking)
- [Kingdom Cats Replacer](#cosmetic-and-animation-changes)
- [Linked Chests](#storage-and-inventory-management)
- [Locked In Slots](#storage-and-inventory-management)
- [Lullaby's Mobs](#creatures-and-hostile-mobs)
- [Macaw's building collection](#macaws-building-collection)
- [Market Board](#trading-and-multiplayer-economy)
- [Moblets](#creatures-and-hostile-mobs)
- [Moog's structure collection](#structures-and-dungeons)
- [Mounts and Monsters](#creatures-and-hostile-mobs)
- [Much More Dungeons](#structures-and-dungeons)
- [Mutants and Zombies](#creatures-and-hostile-mobs)
- [Name Tag Upgrade](#companions-pets-villagers-and-settlements)
- [Naturally Trimmed](#enchanting-and-equipment-improvement)
- [Nature's Compass](#travel-maps-compasses-and-teleportation)
- [No Crop Destruction](#farming-food-and-cooking)
- [Nullscape](#integrated-world-and-dimension-expansions)
- [Ocean Lily Pad Village](#structures-and-dungeons)
- [OneKeyMiner](#enchanting-and-equipment-improvement)
- [Overflowing Bars](#hud-changes-and-customization)
- [Paper Doll](#hud-changes-and-customization)
- [Party Creepers](#companions-pets-villagers-and-settlements)
- [PatPat](#cosmetic-and-animation-changes)
- [Pet Status](#companions-pets-villagers-and-settlements)
- [Pet Vault](#companions-pets-villagers-and-settlements)
- [PlayerRevive](#death-revival-graves-and-respawning)
- [Potion Time Stacker](#potions-and-alchemy)
- [Potions Stack](#potions-and-alchemy)
- [Pro Placer](#building-and-decoration)
- [Pufferfish's Skills](#skill-trees-and-character-progression)
- [Puffsprout](#companions-pets-villagers-and-settlements)
- [Quick Skin](#cosmetic-and-animation-changes)
- [Relics](#weapons-armor-jewelry-and-relics)
- [Ribbits](#creatures-and-hostile-mobs)
- [RPG classes](#rpg-classes)
- [Runes](#abilities-spells-runes-and-resources)
- [Seaworthy Boats](#vehicles-and-travel-equipment)
- [Serene Seasons](#integrated-world-and-dimension-expansions)
- [Shield Upgrades](#weapons-armor-jewelry-and-relics)
- [Show My Recipes](#recipe-and-loot-information)
- [Shroomcraft](#shroomcraft)
- [Skill Perks](#skill-trees-and-character-progression)
- [Skill Tree (RPG Series)](#skill-trees-and-character-progression)
- [Sodium](#optional-client-features)
- [Somnora](#dangerous-nights-and-environmental-threats)
- [Sophisticated Backpacks](#storage-and-inventory-management)
- [Sophisticated Inventory Interactions](#item-transport-and-logistics)
- [Sophisticated Item Actions](#item-transport-and-logistics)
- [Starcatcher](#starcatcher)
- [Stellarity](#stellarity)
- [Stonecutting Upgrade](#building-and-decoration)
- [Stoneworks](#building-and-decoration)
- [Target Dummy](#enchanting-and-equipment-improvement)
- [Terralith](#integrated-world-and-dimension-expansions)
- [The Darkness Will Find You](#dangerous-nights-and-environmental-threats)
- [The Graveyard](#the-graveyard)
- [Tom's Simple Storage](#storage-and-inventory-management)
- [Too Many Bows](#weapons-armor-jewelry-and-relics)
- [Towers of Chambers](#structures-and-dungeons)
- [Towns and Towers](#structures-and-dungeons)
- [Ultimate Warden](#bosses-and-major-encounters)
- [Underground Villages](#structures-and-dungeons)
- [Universal Bone Meal](#farming-food-and-cooking)
- [Universal Enchants](#enchanting-and-equipment-improvement)
- [Vehicle Upgrade](#vehicles-and-travel-equipment)
- [Waystones](#travel-maps-compasses-and-teleportation)
- [Wild Pets](#companions-pets-villagers-and-settlements)
- [William Wythers' Overhauled Overworld](#integrated-world-and-dimension-expansions)
- [Withered Lands](#withered-lands)
- [World Bosses](#bosses-and-major-encounters)
- [Xaero's Map Multiplayer](#travel-maps-compasses-and-teleportation)
- [Xaero's World Map](#travel-maps-compasses-and-teleportation)

### Gameplay-tag index

#### Combat

- [Better Combat](#combat-controls-and-dodge-rolling)
- [Combat Roll](#combat-controls-and-dodge-rolling)
- [RPG classes](#rpg-classes)
- [World Bosses](#bosses-and-major-encounters)
- [Invasion](#invasions-and-wave-events)

#### Character

- [Iourus Races](#races-and-racial-traits)
- [RPG classes](#rpg-classes)
- [Skill Tree (RPG Series)](#skill-trees-and-character-progression)
- [Pufferfish's Skills](#skill-trees-and-character-progression)
- [Skill Perks](#skill-trees-and-character-progression)

#### Skills

- [Skill Tree (RPG Series)](#skill-trees-and-character-progression)
- [Pufferfish's Skills](#skill-trees-and-character-progression)
- [Skill Perks](#skill-trees-and-character-progression)

#### Magic

- [RPG classes](#rpg-classes)
- [Runes](#abilities-spells-runes-and-resources)
- [Alchemia](#alchemia)
- [Arcane Lanterns](#building-and-decoration)

#### Equipment

- [Arsenal](#weapons-armor-jewelry-and-relics)
- [Armory](#weapons-armor-jewelry-and-relics)
- [Jewelry](#weapons-armor-jewelry-and-relics)
- [Relics](#weapons-armor-jewelry-and-relics)
- [Alex's Caves](#alexs-caves)
- [Stellarity](#stellarity)

#### Bosses

- [World Bosses](#bosses-and-major-encounters)
- [Bosslike Ender Dragon](#bosses-and-major-encounters)
- [Ultimate Warden](#bosses-and-major-encounters)
- [Gateway of Doom](#bosses-and-major-encounters)

#### Events

- [Invasion](#invasions-and-wave-events)
- [Blood Moon](#dangerous-nights-and-environmental-threats)
- [Illager Invasion](#invasions-and-wave-events)
- [It Takes a Pillage](#invasions-and-wave-events)
- [Starcatcher](#starcatcher)

#### World Generation

- [Alex's Caves](#alexs-caves)
- [Biomes O' Plenty](#integrated-world-and-dimension-expansions)
- [Terralith](#integrated-world-and-dimension-expansions)
- [Better Nether](#better-nether)
- [Nullscape](#integrated-world-and-dimension-expansions)
- [Stellarity](#stellarity)

#### Structures

- [Dungeons and Taverns](#structures-and-dungeons)
- [Moog's structure collection](#structures-and-dungeons)
- [Towns and Towers](#structures-and-dungeons)
- [The Graveyard](#the-graveyard)

#### Creatures

- [Alex's Mobs](#creatures-and-hostile-mobs)
- [Animal Garden collection](#creatures-and-hostile-mobs)
- [Shroomcraft](#shroomcraft)
- [Ender Zoology](#creatures-and-hostile-mobs)

#### Companions

- [Companions: Dogfolk](#companions-pets-villagers-and-settlements)
- [Wild Pets](#companions-pets-villagers-and-settlements)
- [Puffsprout](#companions-pets-villagers-and-settlements)
- [Pet Vault](#companions-pets-villagers-and-settlements)
- [Pet Status](#companions-pets-villagers-and-settlements)

#### Farming

- [Farmer's Delight](#farmers-delight)
- [Kaleidoscope Cookery](#farming-food-and-cooking)
- [Shroomcraft](#shroomcraft)
- [Universal Bone Meal](#farming-food-and-cooking)

#### Food

- [Farmer's Delight](#farmers-delight)
- [Kaleidoscope Cookery](#farming-food-and-cooking)
- [Starcatcher](#starcatcher)
- [Better Rotten Flesh](#farming-food-and-cooking)

#### Building

- [Macaw's building collection](#macaws-building-collection)
- [Effortless Building](#building-and-decoration)
- [Stoneworks](#building-and-decoration)
- [Pro Placer](#building-and-decoration)

#### Storage

- [Sophisticated Backpacks](#storage-and-inventory-management)
- [Tom's Simple Storage](#storage-and-inventory-management)
- [Linked Chests](#storage-and-inventory-management)

#### Travel

- [Waystones](#travel-maps-compasses-and-teleportation)
- [Bifrost Teleport](#travel-maps-compasses-and-teleportation)
- [Xaero's World Map](#travel-maps-compasses-and-teleportation)
- [Seaworthy Boats](#vehicles-and-travel-equipment)
- [Vehicle Upgrade](#vehicles-and-travel-equipment)

#### Multiplayer

- [Better Party](#parties-and-multiplayer-cooperation)
- [Market Board](#trading-and-multiplayer-economy)
- [PlayerRevive](#death-revival-graves-and-respawning)
- [Better Revive](#death-revival-graves-and-respawning)

#### Controls

- [Better Combat](#combat-controls-and-dodge-rolling)
- [Combat Roll](#combat-controls-and-dodge-rolling)
- [OneKeyMiner](#enchanting-and-equipment-improvement)
- [Effortless Building](#building-and-decoration)

#### HUD

- [Paper Doll](#hud-changes-and-customization)
- [Coordinates Display](#optional-client-features)
- [Better Party](#parties-and-multiplayer-cooperation)
- [Hovering Hotbar](#hud-changes-and-customization)
- [Overflowing Bars](#hud-changes-and-customization)

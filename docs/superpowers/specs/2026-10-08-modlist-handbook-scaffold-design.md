# Modlist Handbook Scaffold Design

Date: 2026-10-08  
Status: Awaiting final user review

## Purpose

Revamp `docs/MODLIST.md` into the content scaffold for a future player-facing PDF handbook. The intended reader understands vanilla Minecraft but is new to this modpack. The page is not a technical dependency inventory and is not intended to catalog every item, creature, block, recipe, structure, or loot drop.

The handbook will work primarily as a topic-by-topic reference. A short Start Here section will introduce the systems a player encounters immediately: controls, keybind conflicts, races, classes, skills, abilities, spells, parties, and death or revival behavior.

## Organizational model

Use a handbook-first structure organized around player questions and gameplay systems. Do not force every mod into an exclusive content-type category.

Each mod receives one canonical entry under its strongest player-facing topic. When it affects other topics, those sections use concise cross-references rather than repeating the full entry.

Broad mods that substantially combine several content types use an integrated-expansion classification. This prevents mods containing world generation, structures, creatures, equipment, tools, and progression from being described as though they only belong to one asset category.

## Handbook architecture

### Part I — Start Here

This is the only part intended to be read in order.

1. Essential keybinds and conflict resolution
2. Choosing a race
3. Choosing a class
4. Understanding skills, abilities, spells, and runes
5. First-session checklist
6. Joining or creating a party
7. What happens when a player is downed or dies

### Part II — Character and Core Systems

1. Races and racial traits
2. Classes and playstyles
3. Skill trees and character progression
4. Abilities, spells, runes, and resources
5. Combat controls and dodge rolling
6. Weapons, armor, jewelry, and relics
7. Enchanting and equipment improvement
8. Death, revival, graves, and respawning
9. Parties and multiplayer cooperation

### Part III — Adventure and Challenge

1. Bosses and major encounters
2. Invasions and wave events
3. Dangerous nights and environmental threats
4. Travel, maps, compasses, and teleportation
5. Integrated world and dimension expansions
6. Structures and dungeons
7. Creatures and hostile mobs
8. Companions, pets, villagers, and settlements

### Part IV — Survival and Creation

1. Farming, food, and cooking
2. Fishing and collection systems
3. Potions and alchemy
4. Building and decoration
5. Storage and inventory management
6. Item transport and logistics
7. Vehicles and travel equipment
8. Trading and multiplayer economy

### Part V — Interface and Reference

1. Information overlays and tooltips
2. Recipe and loot information
3. HUD changes
4. Cosmetic and animation changes
5. Optional client features
6. Alphabetical mod index
7. Cross-reference index by gameplay tag

## Canonical entry format

Use the following fields as needed. Do not render empty or irrelevant fields merely to make entries uniform.

```md
### Alex's Caves

- **Type:** Integrated world expansion
- **Primary topic:** Worlds and exploration
- **Also affects:** Creatures, equipment, resources, combat
- **Core idea:** Adds rare underground ecosystems with their own creatures, materials, equipment, and progression.
- **Future guide:** Discovery, preparation, progression, notable rewards, controls, and configuration.
```

Cross-references remain short:

```md
- **Alex's Caves creatures** — Cave-specific creatures and drops.
  See: Alex's Caves under Worlds and Exploration.
```

Do not add early-game, midgame, or late-game labels. The mods do not share a reliable common progression scale. When timing matters, state the actual prerequisite naturally, such as requiring access to the End.

## Entry vocabulary

### Entry types

- **Core system** — Changes fundamental gameplay rules or controls.
- **Integrated expansion** — Combines several major content types into one experience.
- **Focused content** — Adds content primarily serving one gameplay topic.
- **Utility** — Improves a task such as storage, travel, or building.
- **Interface feature** — Changes information display, controls, or presentation.
- **Optional client feature** — Is not required for the shared gameplay experience.

### Integrated-expansion subtypes

- Integrated world expansion
- Integrated dimension expansion
- Integrated adventure expansion
- Integrated character-progression suite
- Integrated survival or profession expansion
- Integrated building expansion

### Cross-reference tags

Use only tags that materially help a reader find related information:

`Combat`, `Character`, `Skills`, `Magic`, `Equipment`, `Bosses`, `Events`, `World Generation`, `Structures`, `Creatures`, `Companions`, `Farming`, `Food`, `Building`, `Storage`, `Travel`, `Multiplayer`, `Controls`, `HUD`

## Content-depth policy

Prioritize:

- What changes compared with vanilla Minecraft
- The mechanic's purpose and basic gameplay loop
- How the player activates or interacts with it
- Relevant controls and keybinds
- Player-facing modes and customization
- Important prerequisites or consequences
- Connections to other documented systems

Avoid exhaustive lists of specific mobs, items, blocks, recipes, structures, enchantments, and loot. Use specific examples only when they are required to explain a mechanic.

## Keybind tables

Display the complete keybind inventory supplied in the screenshots. Do not reduce it to only important or conflicting actions.

Create one table per in-game category and preserve the screenshots' top-to-bottom category and action order. This lets a player work through the Minecraft Controls screen sequentially without jumping between handbook sections.

Use this table structure:

| Action | Default key | Recommended key | Conflict or notes |
|---|---|---|---|

Rules:

- Use the screenshots to identify categories, actions, and ordering only.
- Do not treat the keys visible in the screenshots as authoritative defaults.
- Populate **Default key** only when the mod's official website or documentation explicitly states it.
- Leave **Default key** blank when no authoritative default is published. Do not guess.
- Populate **Recommended key** during the later conflict-resolution pass.
- Include unbound actions.
- Preserve duplicate-looking actions when the game lists separate bindings for them.
- Clean raw localization-key category names into readable headings without altering their meaning.
- Keep conflict explanations beside the affected action rather than in a distant section.

The screenshot category sequence is:

1. LibTooltips
2. Starcatcher
3. Movement
4. Miscellaneous
5. Multiplayer
6. Gameplay
7. Inventory
8. Creative Mode
9. Spectator
10. Debug
11. Puffish Skills
12. PatPat
13. Jade
14. Iris
15. Coordinates Display
16. Xaero's World Map
17. Effortless Building
18. Spell Engine
19. Better Combat
20. Better Party
21. Curios
22. Hovering Hotbar
23. Iourus Races
24. Item Interactions
25. JEI — Cheat Mode
26. JEI — Dev Tools
27. JEI — Edit Mode
28. JEI — Hovering over Config Button
29. JEI — Hovering with Mouse
30. JEI — Overlays
31. JEI — Recipes
32. JEI — Search Filter
33. Locked In Slots
34. Pet Vault
35. Pro Placer
36. Skill Perks
37. Sophisticated Backpacks
38. Sophisticated Mods
39. Sophisticated Item Actions
40. Tool Belt

## HUD and configuration documentation

For mods with configurable interfaces, explain:

- Available display modes
- What each mode changes
- The authoritative default mode or position, when documented
- The recommended pack mode and position
- Known overlap with other HUD elements
- How to open the relevant configuration interface
- Alternative modes players may prefer

Use screenshots when spatial placement is easier to understand visually.

The approved recommended layout is:

- Paper Doll at the upper-left
- Coordinates Display directly beneath Paper Doll
- Better Party as a vertical panel on the right

The approved layout mockup is currently saved at `output/playwright/hud-layout-recommended.png`. It can be copied into the final handbook asset location when PDF production begins.

## Source and uncertainty policy

- Prefer official mod websites and official documentation for default controls and configuration claims.
- Use installed JAR metadata to confirm that a mod is present and to establish its core idea.
- Do not infer undocumented default keys from the submitted screenshots.
- Leave unknown fields empty instead of presenting assumptions as facts.
- Keep libraries, APIs, compatibility bridges, and invisible server internals out of the user-facing catalog.
- A support mod may still appear as a keybind-table heading when it exposes controls in the player's Controls screen; this does not make it a canonical gameplay entry.

## Scope of the first revamp

The first implementation restructures `docs/MODLIST.md` as the handbook scaffold and adds the full screenshot-derived keybind tables. It does not create the PDF, exhaustively research every mechanic, choose all recommended bindings, or enumerate mod content.

Future passes will:

1. Research authoritative default keys and configuration modes.
2. Resolve conflicts and populate recommended keys.
3. Expand core mechanic explanations.
4. Add verified screenshots and diagrams.
5. Produce and visually verify the final PDF.

## Validation criteria

- Every current user-facing catalog entry remains represented once canonically or by an explicit cross-reference.
- Multi-system mods use an integrated-expansion subtype where appropriate.
- No progression-stage labels are introduced.
- Keybind categories and actions follow the screenshots from top to bottom.
- Every keybind category has its own table.
- Screenshot bindings are not mislabeled as official defaults.
- No undocumented default key is invented.
- The recommended HUD layout and its saved image agree.
- Libraries and APIs do not appear as player-facing mod entries.
- Markdown headings, internal links, and duplicate entry labels are checked before handoff.

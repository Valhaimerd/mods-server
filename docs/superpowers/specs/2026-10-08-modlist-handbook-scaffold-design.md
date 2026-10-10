# Modlist Handbook Scaffold Design

Date: 2026-10-08  
Status: Implemented; front matter and icon staging updated on 2026-10-10

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

Use this player-facing table structure:

| Action | Recommended key |
|---|---|

Rules:

- Preserve the Minecraft Controls category and action order so players can work top to bottom.
- Show only **Action** and **Recommended key** in the handbook; keep defaults and conflict explanations out of these tables to reduce visual load.
- Leave **Recommended key** blank for actions intended to remain unbound.
- Include unbound actions.
- Preserve duplicate-looking actions when the game lists separate bindings for them.
- Clean raw localization-key category names into readable headings without altering their meaning.
- Use the same full table width and column proportions for every keybind category. The print style fixes the columns to 65% for Action and 35% for Recommended key.
- Check the recommended profile for conflicts before publishing; do not add a conflict-notes column to the player tables.

The current 41-category sequence is: LibTooltips, Moonstone (月之石), Starcatcher keybinds, Movement, Miscellaneous, Multiplayer keybinds, Gameplay, Inventory, Creative Mode, Spectator, Debug, Puffish Skills, Vital Relics, PatPat, Jade, Iris, Coordinates Display, Xaero's World Map, Effortless Building, Spell Engine, Better Combat, Better Party, Curios, Hovering Hotbar, Iourus Races, Item Interactions, JEI — Cheat Mode, JEI — Dev Tools, JEI — Edit Mode, JEI — Hovering over Config Button, JEI — Hovering with Mouse, JEI — Overlays, JEI — Recipes, JEI — Search Filter, Pet Vault, Pro Placer, Skill Perks, Sophisticated Backpacks, Sophisticated Mods, Sophisticated Item Actions, and Tool Belt. Locked In Slots was removed from the current controls reference.

Keep these tables together in Part I. Other handbook chapters may mention a key in prose when it helps explain an interaction, but do not repeat controls in tables elsewhere. Prefer prose or concise lists for ordinary mod guidance; reserve comparison tables for genuinely different workflows.

## Print design

- Use black for body text and a restrained accent palette: forest green for part openers and amber for emphasis.
- Give the cover and each part a clear visual entry point without wasting pages on decorative spacer pages.
- Keep body text, lists, captions, and tables compact and readable; avoid broad cell padding and excessive vertical margins.
- Keep screenshot captions with their images and retain page numbering and a discreet handbook footer.
- Preserve clickable internal and source links in the generated PDF.

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

- Prefer official mod websites and official documentation for player-facing behavior and configuration claims.
- Use installed JAR metadata to confirm that a mod is present and to establish its core idea.
- Use the confirmed profile for recommended keys; leave unconfirmed assignments blank instead of guessing.
- Keep default-key and conflict-detail columns out of the player-facing keybind tables.
- Keep libraries, APIs, compatibility bridges, and invisible server internals out of the user-facing catalog.
- A support mod may still appear as a keybind-table heading when it exposes controls in the player's Controls screen; this does not make it a canonical gameplay entry.

## Scope of the first revamp

The implementation restructured `docs/MODLIST.md` as the handbook scaffold, then expanded it into the maintained player handbook. It includes the full screenshot-derived keybind tables, approved recommended bindings, topic-by-topic mechanic guidance, a quick reference, verified gameplay screenshots, print styling, and a maintenance runbook. It intentionally does not enumerate every item, creature, block, recipe, structure, enchantment, or loot drop.

The print handbook is titled **26.2 RPG Series by Valhaimerd**. Its linked Table of Contents is followed by the alphabetical mod index and separately titled Gameplay-tag index in the opening pages, then the Quick Reference and five topic-based parts. Verified project icons are staged in `docs/assets/mod-icons/` and rendered beside catalog titles. Grouped collections may require multiple icons, and no similarly named project artwork should be substituted when a source is uncertain.

Implementation outcome:

1. The complete keybind inventory is shown in 41 ordered category tables with only Action and Recommended key columns; all tables share fixed 65/35 widths.
2. The recommended profile preserves the confirmed control assignments and leaves unbound actions blank.
3. Core mechanics were expanded and reviewed against official documentation and installed JAR evidence.
4. Fifteen verified gameplay screenshots and one HUD layout diagram were embedded with descriptive captions.
5. PDF production was requested and completed on 2026-10-09. The October 10 revision tightens the print layout, confines keybind tables to Part I, and updates the player guidance across all five parts; regenerate and visually inspect the PDF before handoff.

## Validation criteria

- Every current user-facing catalog entry remains represented once canonically or by an explicit cross-reference.
- Multi-system mods use an integrated-expansion subtype where appropriate.
- No progression-stage labels are introduced.
- Keybind categories and actions follow the screenshots from top to bottom.
- Every keybind category has its own table.
- Every keybind table uses only Action and Recommended key columns with consistent widths.
- Unbound recommended actions remain blank.
- The recommended HUD layout and its saved image agree.
- Libraries and APIs do not appear as player-facing mod entries.
- Markdown headings, internal links, and duplicate entry labels are checked before handoff.

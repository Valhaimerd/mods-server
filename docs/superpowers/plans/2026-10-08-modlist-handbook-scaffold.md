# Modlist Handbook Scaffold Implementation Plan

**Status:** Implemented on 2026-10-09; print design, front matter, and icon staging revised on 2026-10-10. A later pass will resolve grouped/ambiguous icon sources and decide how icons are placed in entries.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Revamp `docs/MODLIST.md` into a topic-based scaffold for a future player handbook and add a complete, category-ordered keybind reference.

**Architecture:** Keep one Markdown source organized as a short onboarding path followed by topic reference chapters. Focused mods remain compact entries; multi-system mods receive canonical integrated-expansion entries and short cross-references from secondary topics. The keybind reference follows the Minecraft Controls screen order and uses only Action and Recommended key columns.

**Tech Stack:** GitHub-flavored Markdown, PowerShell validation commands, installed JAR metadata, official mod documentation for player-facing behavior

**Spec:** `docs/superpowers/specs/2026-10-08-modlist-handbook-scaffold-design.md`

**Current presentation additions:** The print title is `26.2 RPG Series by Valhaimerd`; the linked contents is followed by the alphabetical mod index and Gameplay-tag index near the front. Verified project icons are stored in `docs/assets/mod-icons/` and rendered beside catalog titles; unresolved collection icons are omitted, and Bifrost's supplied custom illustration is included.

**Current keybind-table revision:** The player-facing tables now contain only `Action` and `Recommended key`, with one shared 65/35 column split across all categories. The current count is 41: Moonstone is immediately before Starcatcher, Vital Relics follows Puffish Skills, and Tool Belt is last; the removed Locked In Slots category is not included. These updates supersede the original category numbering below.

## Global Constraints

- The intended reader understands vanilla Minecraft but is new to this modpack.
- Organize the handbook around player topics and questions, not dependency or asset types.
- Keep libraries, APIs, compatibility bridges, and invisible server internals out of the canonical gameplay catalog.
- A support mod may appear as a Controls-table heading when it exposes user-facing controls.
- Do not enumerate every mob, item, block, recipe, structure, enchantment, or loot drop.
- Do not assign early-game, midgame, or late-game labels.
- Give each mod one canonical entry and use concise cross-references from secondary topics.
- Use only the approved entry types, integrated-expansion subtypes, and cross-reference tags from the spec.
- Use submitted screenshots for keybind category names, action names, ordering, and current recommended assignments.
- Keep the player-facing keybind tables to `Action` and `Recommended key` only.
- Leave recommended-key cells blank when an action should remain unbound.
- Preserve all unbound and duplicate-looking actions shown by the Controls screen.
- The recommended HUD layout is Paper Doll upper-left, Coordinates Display directly beneath it, and Better Party as a right-side vertical panel.
- Do not stage or commit the user's existing JAR deletions while executing documentation tasks.

## File Structure

- Modify: `docs/MODLIST.md` — single source for the handbook scaffold, catalog entries, cross-references, and keybind tables.
- Modify only if the existing link is missing or incorrect: `README.md` — concise link to the handbook scaffold.
- Read: `docs/superpowers/specs/2026-10-08-modlist-handbook-scaffold-design.md` — approved design and acceptance criteria.
- Read: `output/playwright/hud-layout-recommended.png` — approved HUD mockup; do not relocate it during this scaffold pass.
- Do not modify: `mods/*.jar`, `mod store/*.jar`, `.superpowers/**`, or `.playwright-cli/**`.

---

### Task 1: Establish the handbook shell and catalog conventions

**Files:**
- Modify: `docs/MODLIST.md`
- Read: `docs/superpowers/specs/2026-10-08-modlist-handbook-scaffold-design.md`

**Interfaces:**
- Consumes: the approved five-part handbook architecture and vocabulary from the spec.
- Produces: stable Part and chapter headings that Tasks 2–4 populate.

- [x] **Step 1: Verify the source inventory before editing**

Run:

```powershell
(Get-ChildItem -LiteralPath '.\mods' -Filter '*.jar' -File).Count
(Get-ChildItem -LiteralPath '.\mod store' -Filter '*.jar' -File).Count
(Select-String -Path '.\docs\MODLIST.md' -Pattern '^- \*\*').Count
```

Expected: `277` synchronized JARs, `4` client-store JARs, and `155` current catalog bullets.

- [x] **Step 2: Replace the current numbered impact hierarchy with the approved handbook shell**

Use this exact top-level structure:

```md
# Modpack Player Handbook Scaffold

## About this scaffold
## How entries are organized

## Part I — Start Here
### Essential keybinds and conflict resolution
### Choosing a race
### Choosing a class
### Skills, abilities, spells, and runes
### First-session checklist
### Parties, downed players, and death

## Part II — Character and Core Systems
### Races and racial traits
### Classes and playstyles
### Skill trees and character progression
### Abilities, spells, runes, and resources
### Combat controls and dodge rolling
### Weapons, armor, jewelry, and relics
### Enchanting and equipment improvement
### Death, revival, graves, and respawning
### Parties and multiplayer cooperation

## Part III — Adventure and Challenge
### Bosses and major encounters
### Invasions and wave events
### Dangerous nights and environmental threats
### Travel, maps, compasses, and teleportation
### Integrated world and dimension expansions
### Structures and dungeons
### Creatures and hostile mobs
### Companions, pets, villagers, and settlements

## Part IV — Survival and Creation
### Farming, food, and cooking
### Fishing and collection systems
### Potions and alchemy
### Building and decoration
### Storage and inventory management
### Item transport and logistics
### Vehicles and travel equipment
### Trading and multiplayer economy

## Part V — Interface and Reference
### Information overlays and tooltips
### Recipe and loot information
### HUD changes and customization
### Cosmetic and animation changes
### Optional client features
### Alphabetical mod index
### Gameplay-tag index
```

- [x] **Step 3: Add concise catalog rules near the beginning**

Include:

```md
Each mod has one primary entry. Secondary topics link back to that entry instead of repeating it. Broad mods use an Integrated expansion type when they combine multiple substantial systems. This scaffold records mechanics, controls, configuration, and relationships; it does not enumerate every item, creature, block, recipe, structure, or loot drop.
```

List the approved entry types and tag vocabulary exactly as defined in the spec. Do not include relevance stages.

- [x] **Step 4: Add compact Start Here navigation**

Under each Part I chapter, add a two- or three-sentence orientation that tells the future handbook author what the chapter must teach and links to the detailed Part II topic. Do not repeat full mod descriptions in Part I.

- [x] **Step 5: Validate the shell**

Run:

```powershell
Select-String -Path '.\docs\MODLIST.md' -Pattern '^## Part [IVX]+ —'
Select-String -Path '.\docs\MODLIST.md' -Pattern '\b(early game|midgame|late game)\b' -CaseSensitive:$false
```

Expected: five Part headings; no progression-stage matches.

- [x] **Step 6: Commit the shell**

```powershell
git add -- 'docs/MODLIST.md'
git commit -m "docs: establish player handbook structure"
```

---

### Task 2: Reclassify the mod catalog without losing entries

**Files:**
- Modify: `docs/MODLIST.md`

**Interfaces:**
- Consumes: the chapter headings from Task 1 and the 156 existing player-facing entries.
- Produces: one canonical placement per mod or family plus concise secondary-topic cross-references.

- [x] **Step 1: Preserve focused entries as compact records**

Use this format for mods centered on one topic:

```md
- **Combat Roll** *(Core system; Combat, Controls)* — Adds a dodge roll with its own attributes and enchantments.
```

Keep the existing core-idea sentence unless installed metadata proves it inaccurate. Add only approved entry types and material tags.

- [x] **Step 2: Convert multi-system mods to canonical expansion entries**

Use this format:

```md
#### Alex's Caves

- **Type:** Integrated world expansion
- **Primary topic:** Integrated world and dimension expansions
- **Also affects:** Combat, Equipment, World Generation, Creatures
- **Core idea:** Adds rare underground ecosystems with their own creatures, materials, equipment, and progression.
- **Future guide:** Explain discovery, preparation, major mechanics, controls, configuration, and relationships to other systems.
```

Apply an integrated-expansion subtype when a mod substantially spans three or more gameplay concerns. At minimum, evaluate Alex's Caves, Stellarity, Better Nether, The Graveyard, Withered Lands, Shroomcraft, Starcatcher, Farmer's Delight, Alchemia, and the RPG Series bundle.

- [x] **Step 3: Add cross-references from secondary topics**

Use this format:

```md
- **Alex's Caves creatures** — Cave-specific creatures and rewards. See [Alex's Caves](#alexs-caves).
```

Keep the cross-reference conceptual. Do not list the creatures or rewards individually.

- [x] **Step 4: Place focused entries under their player-facing topics**

Use these ownership rules:

- Race, class, skill, spell, and combat-control entries belong in Part II.
- Boss, invasion, danger, world, structure, creature, companion, and travel entries belong in Part III.
- Food, fishing, alchemy, building, storage, logistics, vehicle, and trading entries belong in Part IV.
- Overlay, tooltip, recipe-browser, HUD, cosmetic, animation, and optional-client entries belong in Part V.
- Death, party, and multiplayer-cooperation entries receive canonical Part II placements and Part I onboarding links.

- [x] **Step 5: Populate the alphabetical mod index**

Under `### Alphabetical mod index`, add one alphabetized Markdown link for each of the 156 original user-facing catalog labels. Link each name to its canonical entry or owning topic. Do not add libraries or APIs merely to reach the expected count.

- [x] **Step 6: Populate the gameplay-tag index**

Under `### Gameplay-tag index`, list only the approved tags from the spec. Under each tag, link to the canonical entries that materially affect that topic. Do not introduce synonyms such as `Mobs` when the approved tag is `Creatures`.

- [x] **Step 7: Verify catalog preservation and uniqueness**

Run:

```powershell
$labels = Select-String -Path '.\docs\MODLIST.md' -Pattern '^- \*\*([^*]+)\*\*' | ForEach-Object { $_.Matches[0].Groups[1].Value }
$labels | Group-Object | Where-Object Count -gt 1 | Format-Table Count,Name -AutoSize
($labels | Sort-Object -Unique).Count

$doc = Get-Content -LiteralPath '.\docs\MODLIST.md' -Raw
$index = [regex]::Match($doc, '(?s)### Alphabetical mod index\s+(.*?)\s+### Gameplay-tag index').Groups[1].Value
$indexCount = ([regex]::Matches($index, '(?m)^- \[')).Count
if ($indexCount -ne 156) { throw "Expected 156 alphabetical index entries; found $indexCount" }
```

Expected: no accidental duplicate canonical labels. Review any intentional cross-reference labels manually because they must be qualified names such as `Alex's Caves creatures`, not a repeated `Alex's Caves` label.

- [x] **Step 8: Check excluded implementation entries**

Run:

```powershell
Select-String -Path '.\docs\MODLIST.md' -Pattern '^#{3,4} (Architectury|Balm|Bookshelf|GeckoLib|Puzzles Lib|TerraBlender|Curios API)$'
```

Expected: no canonical gameplay headings for these libraries or APIs. Controls tables added in Task 3 may still contain support-mod category names.

- [x] **Step 9: Commit the reclassified catalog**

```powershell
git add -- 'docs/MODLIST.md'
git commit -m "docs: organize mods by player topic"
```

---

### Task 3: Add the complete category-ordered keybind reference

**Files:**
- Modify: `docs/MODLIST.md`

**Interfaces:**
- Consumes: the Part I `Essential keybinds and conflict resolution` chapter from Task 1.
- Historical implementation produced 42 tables; the current handbook has 41 after Locked In Slots was removed.

- [x] **Step 1: Add the keybind-source notice**

Insert this text before the first table:

```md
The tables follow the Minecraft Controls screen from top to bottom. They show only each action and its recommended key, so players can configure one category at a time without comparing against default-key or conflict-note columns.
```

- [x] **Step 2: Use one table per category**

Render each category as an H4 heading, for example `#### LibTooltips`, then use this exact table header every time:

```md
| Action | Recommended key |
|---|---|
```

Leave an action's recommended-key cell empty when it should remain unbound.

- [x] **Step 3: Add categories 1–10 in this exact order**

1. **LibTooltips:** Expand Tooltip
2. **Starcatcher:** Minigame Hit; Open Guide; Toggle Tournament Overlay
3. **Movement:** Jump; Roll; Sneak; Sprint; Strafe Left; Strafe Right; Walk Backward; Walk Forward
4. **Miscellaneous:** Advancements; Alex's Caves Special Ability; Quick Actions; Take Screenshot; Toggle Cinematic Camera; Toggle Fullscreen; Toggle GUI; Toggle Perspective; Toggle Spectator Shader Effects
5. **Multiplayer:** Friends Screen; List Players; Open Chat; Open Command; Social Interactions Screen
6. **Gameplay:** Attack/Destroy; Chain Mining Key (Hold); Open Config Menu; Pick Block; Use Item/Place Block; [Tom's Simple Storage] Open Terminal
7. **Inventory:** Drop Selected Item; Hotbar Slot 1; Hotbar Slot 2; Hotbar Slot 3; Hotbar Slot 4; Hotbar Slot 5; Hotbar Slot 6; Hotbar Slot 7; Hotbar Slot 8; Hotbar Slot 9; Open Pet Status; Open/Close Inventory; Swap Item With Off Hand
8. **Creative Mode:** Load Hotbar Activator; Save Hotbar Activator
9. **Spectator:** Highlight Players; Select On Hotbar
10. **Debug:** Toggle Overlay; Debug Modifier Key; Clear Chat; Copy Data; Copy Location; Cycle Spectator; Debug Crash; Debug Options; Dump Dynamic Textures; Dump Version Info; Game Mode Switcher; Reload Chunks; Reload Resource Packs; Show Advanced Tooltips; Show Chunk Boundaries; Show Hitboxes; Start/Stop Profiling; Toggle Lost Focus Pause; Profiling Chart; FPS Charts; Network Charts; Lightmap Texture

- [x] **Step 4: Add categories 11–20 in this exact order**

11. **Puffish Skills:** Open Skill Tree
12. **PatPat:** Pat Entity
13. **Jade:** Narrate Target; Open Config; Show Details; Show Overlay; Show Recipes; Show Uses; Toggle Fluid; Use Profile #0; Use Profile #1; Use Profile #2; Use Profile #3
14. **Iris:** Reload Shaders; Shaderpack Selection Screen; Toggle Shaders; Wireframe (SP only)
15. **Coordinates Display:** Change HUD Position; Copy Current Position as /tp Command; Copy Current Position to Clipboard; Cycle Display Mode (hold Shift to go back); Mark a Position; Open Coordinates GUI; Send Current Position in Chat; Toggle 3D Compass Rendering; Toggle HUD
16. **Xaero's World Map:** Open Server Settings; Open Settings; Open World Map; Quick Manual Confirmation; Toggle Chunk Claims; Toggle Dimension; Toggle Tracked Players; Zoom In (alternative); Zoom Out (alternative)
17. **Effortless Building:** Open Modifier Settings; Open Radial Menu; Redo; Undo
18. **Spell Engine:** Bypass Spell Hotbar; Spell Hotbar Slot 1; Spell Hotbar Slot 2; Spell Hotbar Slot 3; Spell Hotbar Slot 4; Spell Hotbar Slot 5; Spell Hotbar Slot 6; Spell Hotbar Slot 7; Spell Hotbar Slot 8; Spell Hotbar Slot 9; Tooltip Spell Details
19. **Better Combat:** Feint; Toggle Mine with Weapons
20. **Better Party:** Open Party Menu

- [x] **Step 5: Add categories 21–30 in this exact order**

21. **Curios:** Open/Close Curios Inventory
22. **Hovering Hotbar:** Move Hotbar Down; Move Hotbar Up
23. **Iourus Races:** Open Race Menu
24. **Item Interactions:** Toggle Item Held By Cursor Tooltip; Toggle Item Storage Tooltip
25. **JEI — Cheat Mode:** Cheat 1 Item; Cheat 1 Item; Cheat 1 Stack; Cheat 1 Stack; Toggle Cheat Mode
26. **JEI — Dev Tools:** Copy Recipe ID to Clipboard
27. **JEI — Edit Mode:** Hide Ingredient; Hide Ingredient (With Wildcard); Toggle Hide Ingredients Mode
28. **JEI — Hovering over Config Button:** Toggle Cheat Mode
29. **JEI — Hovering with Mouse:** Add/Remove Bookmark; Craft Bookmarked Recipe (Many); Craft Bookmarked Recipe (One); Quick Move Ghost Item; Share Item to Chat; Show Recipe; Show Recipe; Show Uses; Show Uses
30. **JEI — Overlays:** Next Page; Previous Page; Select Search Bar; Show/Hide Bookmarked Ingredients; Show/Hide JEI Overlays

- [x] **Step 6: Add categories 31–39 in this exact order**

31. **JEI — Recipes:** Close Recipes GUI; Next Recipe; Next Recipe Category; Next Recipe Page; Pause Recipe Ingredient Cycling; Previous Recipe; Previous Recipe Category; Previous Recipe Page
32. **JEI — Search Filter:** Clear Search Filter; Next Search; Previous Search
33. **Locked In Slots:** Lock Slot
34. **Pet Vault:** Open Pet Vault; Unsummon All Pets
35. **Pro Placer:** Toggle Fast Block Placement
36. **Skill Perks:** Open Perk Tree
37. **Sophisticated Backpacks:** Open Backpack; Run Inventory Interaction Upgrades; Swap Tool Based on Current Block/Entity; Switch Upgrade in the 1st Slot On/Off; Switch Upgrade in the 2nd Slot On/Off; Switch Upgrade in the 3rd Slot On/Off; Switch Upgrade in the 4th Slot On/Off; Switch Upgrade in the 5th Slot On/Off
38. **Sophisticated Mods:** Sort Storage/Backpack; Transfer to Inventory; Transfer to Storage
39. **Sophisticated Item Actions:** Deposit Item into Storages; Highlight Storage with Item; Restock Item from Storages

- [x] **Step 7: Validate table count and order**

Run:

```powershell
$doc = Get-Content -LiteralPath '.\docs\MODLIST.md' -Raw
@(
  '#### LibTooltips','#### Moonstone (月之石)','#### Starcatcher','#### Movement','#### Miscellaneous','#### Multiplayer',
  '#### Gameplay','#### Inventory','#### Creative Mode','#### Spectator','#### Debug',
  '#### Puffish Skills','#### Vital Relics','#### PatPat','#### Jade','#### Iris','#### Coordinates Display',
  "#### Xaero's World Map",'#### Effortless Building','#### Spell Engine','#### Better Combat','#### Better Party',
  '#### Curios','#### Hovering Hotbar','#### Iourus Races','#### Item Interactions','#### JEI — Cheat Mode',
  '#### JEI — Dev Tools','#### JEI — Edit Mode','#### JEI — Hovering over Config Button','#### JEI — Hovering with Mouse','#### JEI — Overlays',
  '#### JEI — Recipes','#### JEI — Search Filter','#### Pet Vault','#### Pro Placer',
  '#### Skill Perks','#### Sophisticated Backpacks','#### Sophisticated Mods','#### Sophisticated Item Actions','#### Tool Belt'
) | ForEach-Object { if (-not $doc.Contains($_)) { throw "Missing keybind category: $_" } }

$tableHeaders = (Select-String -Path '.\docs\MODLIST.md' -SimpleMatch '| Action | Recommended key |').Count
if ($tableHeaders -ne 41) { throw "Expected 41 two-column keybind tables; found $tableHeaders" }
$css = Get-Content -LiteralPath '.\docs\handbook-print.css' -Raw
$tableRule = [regex]::Match($css, '(?s)\.keybind-section table\s*\{([^}]*)\}').Groups[1].Value
$firstRule = [regex]::Match($css, '(?s)\.keybind-section th:first-child,\s*\.keybind-section td:first-child\s*\{([^}]*)\}').Groups[1].Value
$lastRule = [regex]::Match($css, '(?s)\.keybind-section th:last-child,\s*\.keybind-section td:last-child\s*\{([^}]*)\}').Groups[1].Value
if ($tableRule -notmatch 'table-layout:\s*fixed;' -or $tableRule -notmatch 'width:\s*100%;' -or $firstRule -notmatch 'width:\s*65%;' -or $lastRule -notmatch 'width:\s*35%;') { throw 'Keybind table column widths are not consistent.' }
```

Expected: no exception, exactly 41 two-column keybind tables, and a consistent 65/35 width rule.

- [x] **Step 8: Commit the keybind reference**

```powershell
git add -- 'docs/MODLIST.md'
git commit -m "docs: add complete keybind reference"
```

---

### Task 4: Add HUD and configuration guidance scaffolds

**Files:**
- Modify: `docs/MODLIST.md`
- Read: `output/playwright/hud-layout-recommended.png`

**Interfaces:**
- Consumes: the approved HUD placement and configuration policy.
- Produces: a future-proof configuration section without inventing undocumented settings.

- [x] **Step 1: Add the configuration-entry template**

Under `HUD changes and customization`, include:

```md
For each configurable interface, document the available display modes, what each mode changes, the authoritative default when published, the recommended pack setting, known overlaps, and how to open its configuration interface. Leave undocumented defaults blank rather than inferring them from screenshots.
```

- [x] **Step 2: Add the approved HUD recommendation**

Include:

```md
### Recommended HUD arrangement

- Place Paper Doll at the upper-left.
- Place Coordinates Display directly beneath Paper Doll.
- Place Better Party in a vertical panel on the right.
- Explain alternative Coordinates Display modes rather than presenting the recommendation as the only valid setup.
- Keep the party panel clear of the personal HUD stack and note possible competition with status effects or scoreboards.
```

Reference the saved mockup as a future asset source without embedding it in the scaffold:

```md
Design asset: `output/playwright/hud-layout-recommended.png`
```

- [x] **Step 3: Add per-feature configuration scaffolds without unknown values**

Create concise subsections for Coordinates Display, Better Party, and Paper Doll containing only verified purpose and the approved layout relationship. Do not add claimed menu paths, default modes, or option names until official documentation is researched.

- [x] **Step 4: Validate recommendation consistency**

Run:

```powershell
Select-String -Path '.\docs\MODLIST.md' -Pattern 'Paper Doll.*upper-left|Coordinates Display.*beneath|Better Party.*right' -CaseSensitive:$false
Test-Path -LiteralPath '.\output\playwright\hud-layout-recommended.png'
```

Expected: all three placement statements are present and the image exists.

- [x] **Step 5: Commit the HUD scaffold**

```powershell
git add -- 'docs/MODLIST.md'
git commit -m "docs: add HUD configuration guidance"
```

---

### Task 5: Validate the complete scaffold and README entry point

**Files:**
- Modify only if required: `README.md`
- Verify: `docs/MODLIST.md`

**Interfaces:**
- Consumes: all documentation produced by Tasks 1–4.
- Produces: a discoverable, internally consistent handbook scaffold ready for research passes.

- [x] **Step 1: Verify the README link**

Ensure `README.md` contains one concise link to `docs/MODLIST.md` and says libraries, APIs, and compatibility-only JARs are omitted from the user-facing catalog. Preserve unrelated README content.

- [x] **Step 2: Scan for forbidden placeholders and invented staging**

Run:

```powershell
Select-String -Path '.\docs\MODLIST.md' -Pattern '\b(TBD|TODO|FIXME|XXX)\b' -CaseSensitive:$false
Select-String -Path '.\docs\MODLIST.md' -Pattern '\b(early game|midgame|late game)\b' -CaseSensitive:$false
```

Expected: no matches.

- [x] **Step 3: Validate Markdown and duplicate labels**

Run:

```powershell
$labels = Select-String -Path '.\docs\MODLIST.md' -Pattern '^- \*\*([^*]+)\*\*' | ForEach-Object { $_.Matches[0].Groups[1].Value }
$duplicates = $labels | Group-Object | Where-Object Count -gt 1
$duplicates | Format-Table Count,Name -AutoSize
git diff --check
```

Expected: only intentionally qualified cross-reference labels, if any, require review; `git diff --check` reports no whitespace errors.

- [x] **Step 4: Verify repository inventory facts without modifying files**

Run:

```powershell
if ((Get-ChildItem '.\mods' -Filter '*.jar' -File).Count -ne 277) { throw 'Unexpected mods JAR count' }
if ((Get-ChildItem '.\mod store' -Filter '*.jar' -File).Count -ne 4) { throw 'Unexpected mod store JAR count' }
$duplicateHashes = Get-ChildItem '.\mods' -Filter '*.jar' -File | Get-FileHash -Algorithm SHA256 | Group-Object Hash | Where-Object Count -gt 1
if ($duplicateHashes) { throw 'Duplicate JAR hashes detected' }
```

Expected: no exception.

- [x] **Step 5: Inspect the final diff and protect unrelated work**

Run:

```powershell
git status --short
git diff -- README.md docs/MODLIST.md
```

Confirm that the three existing JAR deletions remain unstaged and are not included in documentation commits.

- [x] **Step 6: Commit the entry-point update if README changed**

If and only if `README.md` still needs the catalog link:

```powershell
git add -- 'README.md'
git commit -m "docs: link player handbook scaffold"
```

- [x] **Step 7: Report the handoff**

Report:

- The final handbook and keybind-table structure
- The number of keybind categories and tables
- Which recommended bindings are populated
- Which actions are intentionally left unbound
- The saved HUD mockup path
- Validation commands and results
- Unrelated working-tree changes that remain untouched

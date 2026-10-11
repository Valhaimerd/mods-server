# Handbook Pack Sync and Group Compaction

**Status:** Scope approved by the user on 2026-10-11.

## Intent

Keep the player handbook aligned with the current Minecraft 26.2 pack, explain the new skill and enchantment systems accurately, and reclaim handbook space by combining related mod families into compact entries.

## Goals

- Reconcile player-facing handbook content with the current `mods/` directory and update the distribution counts.
- Remove obsolete guidance for Skill Perks, Easy Magic, and Enchanting Infuser.
- Add current guidance for Adventurer Skills, Stronger Skill Tree, Penchant, Max Protection, and Toughness Enchantment.
- Replace each of the seven existing grouped collections with one compact entry, one representative member icon, and a concise list of its installed members. Retain member names in the body for findability; remove their separate full entries and index rows.
- Update the icon manifest and fetch workflow so future refreshes retain representative group icons.
- Refresh the screenshot queue, handbook maintenance notes, indexes, page numbers, and distributable PDF.

## Current pack evidence

At scope approval, `mods/` contained 288 JARs and `mod store/` contained 4 optional client JARs. The user synced the earlier mod changes to `main`. During the handbook review Patchouli was temporarily absent, then reappeared in the live directory; Penchant was also updated in place. The current live inventory is 288 main-pack JARs plus 4 optional client JARs; the current `mods/` working diff includes Patchouli and the updated Penchant JAR.

- The preceding sync removed Easy Magic, Enchanting Infuser, and Skill Perks, and added Adventurer Skills, Stronger Skill Tree, Max Protection, Penchant, Toughness Enchantment, and Patchouli.
- Patchouli is present in the current pack but is a support dependency, so it remains outside the gameplay catalog.

Recount immediately before updating the handbook because the user may continue changing the pack. Count only the JARs currently present; omit support libraries such as Patchouli from player-facing entries.

## Content and grouping decisions

### Skill progression

Describe Pufferfish's Skills as the framework and document the installed RPG Series, Adventurer Skills, and Stronger Skill Tree content without treating the former Skill Perks tree as present. Keep the existing recommended key only where the repository's current keybind guidance supports it. Remove the Skill Perks keybind rows, onboarding copy, index/tag rows, screenshot reference, and obsolete screenshot asset.

The installed Stronger Skill Tree archive contains Combat, Mining, and Chopping Wood categories. Its project page says it requires Pufferfish's Attributes, which is not present in the current `mods/` directory. Do not claim its attribute effects are confirmed to work; report this dependency gap to the user. Adding a dependency or otherwise changing JARs is outside this approved documentation scope.

### Enchanting

Rewrite the old Easy Magic/Enchanting Infuser workflow around the installed systems. Explain Penchant's core use-based enchantment leveling and distinguish its world-selectable modules from confirmed pack-wide behavior because no world save or datapack selection is present in the repository. Include Max Protection and Toughness Enchantment with descriptions based on the installed JAR contents and project sources. Keep remaining systems such as Easy Anvils and Universal Enchants only where the current JAR inventory confirms them.

### Seven compact collections

Use one representative icon from an installed member for each collection. The icon is representative member artwork, not an official combined collection logo. Keep a compact member-name list in each entry and omit separate full member entries and individual member rows from the alphabetical index.

| Group entry | Representative member artwork |
| --- | --- |
| RPG classes | Archers |
| Enchantments collection | Ice Enchantments |
| Dungeons and Taverns overhauls | DnT Ancient City Overhaul |
| Awesome Dungeon collection | Awesome Dungeon |
| Moog's structure collection | Moog's Bountiful Structures |
| Animal Garden collection | Animal Garden: Capybara |
| Macaw's building collection | Macaw's Bridges |

Keep one primary catalog location for each mod. Penchant remains an individual detailed entry in the enchanting section because it changes the central progression mechanic; it is not duplicated as a separate member row in the enchantment collection.

## Constraints

- `docs/MODLIST.md` is the handbook source; `docs/build-handbook.ps1` creates the repository-root and `output/pdf/` PDF copies.
- Preserve the handbook's existing decision to omit libraries, APIs, loaders, compatibility bridges, and invisible internals from gameplay entries.
- Use official project artwork and retain the source project title, URL, icon URL, and local filename in the manifest. Remove grouped-member icon assets only when no remaining catalog entry references them.
- Keep user changes to the `mods/` directory intact. This task edits handbook material and support files, not mod JAR contents.
- Do not claim which Penchant optional modules are active without world data.
- Do not create replacement screenshots without a real capture from the current pack.

## Out of scope

- Changing the mod pack, adding Pufferfish's Attributes, or changing any JAR.
- Rewriting unrelated handbook topics or updating unrelated mod descriptions.
- Creating a new compatibility mod or pack.

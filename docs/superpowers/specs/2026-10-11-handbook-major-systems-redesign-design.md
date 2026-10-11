# Player Handbook Major Systems Redesign

Design reviewed against the current handbook sources and installed-pack inventory on 2026-10-11.

## Intent

Reshape the player handbook so a new reader understands the pack's biggest gameplay changes early. The guide should lead with the RPG build and progression systems, then move into exploration, collection, and companion systems. The handbook already has a strong visual identity and an RPG foundation; this change improves the reading order and gives the pack-defining systems enough explanation to be useful without expanding every mod entry equally.

## Why this change

The current guide has useful material, but some major systems are easy to miss or are introduced late. RPG onboarding is already present in Quick Reference and Part I, and Part II explains races, classes, skills, spells, combat, and equipment. The major content loops are less prominent: Alex's Caves is a short subsection in Part III, Starcatcher appears in Part IV, and Pet Vault is one bullet in the late companion section. The world map is mentioned early but its main guide is later in Part III. Curios appears primarily as an equipment slot reference, while the installed Penchant JAR is absent from the handbook.

The intended result is a field guide organized around what changes the player's decisions and routines, rather than a catalog in which every mod receives similar space.

## Goals

- Present an easy-to-follow reading path: **RPG first, then exploration**.
- Help players understand how the RPG systems connect: persistent race choice, flexible class loadouts, separate skill trees, abilities and runes, equipment stats, Curios slots, and enchantment progression.
- Promote the major exploration and collection loops: Xaero's World Map, Alex's Caves, Starcatcher, and Pet Vault.
- Explain a large system through its effect on ordinary play, its first steps, its main loop, its progression or rewards, and relevant cautions or connections.
- Keep the existing visual language for mod headings, icons, entry metadata, screenshots, navigation, and print styling.
- Display standalone handbook screenshots at a restrained, readable size: centered, approximately 75% of the text-column width, aspect ratio preserved, and capped at 150 mm tall. This applies to screenshot figures, not the small mod-entry icons.
- Preserve concise summaries for small equipment add-ons, individual structures, and other lower-priority content.
- Keep one canonical explanation for each player-facing mod and use links elsewhere instead of duplicating its full description.

## Non-goals

- Do not change mod JARs, game behavior, server configuration, or the modpack itself.
- Do not redesign the cover, typography, color system, page dimensions, icon treatment, or the handbook's five-part visual structure.
- Do not turn the handbook into an exhaustive item, fish, creature, structure, recipe, or enchantment encyclopedia.
- Do not add Curios API to the gameplay mod catalog as if the API itself supplied every slot or item. Explain the player-facing equipment-slot system and this pack's configured uses.
- Do not expand every shield, wolf armor, structure, or decorative content entry to the same depth as the major systems.

## Decisions and constraints

- Keep the current cover, front matter, alphabetical and gameplay-tag indexes, Quick Reference, five named parts, keybind table format, entry icon placement, and PDF visual hierarchy.
- Keep the complete keybind tables together at the beginning of Part I, as required by `docs/HANDBOOK_MAINTENANCE.md`.
- Put a short major-systems route in Quick Reference, ahead of the multi-page keybind reference. This gives readers the overview without disturbing the controls chapter.
- Keep the RPG as the first major topic. The next discovery path is world navigation and exploration.
- Use existing `####` mod-entry headings and the current icon renderer for the prominent mod profiles. Do not introduce a new card or callout style.
- Keep the five-part outline. Reorder the high-impact content within it: RPG and equipment in Part II; world navigation, Alex's Caves, Starcatcher, and Pet Vault near the start of Part III; ordinary survival, food, building, and storage topics remain in Part IV.
- Move the detailed Starcatcher profile from Part IV into Part III's exploration and collection flow. Leave a short link in its former Part IV context so readers looking for fishing from the survival section can find it.
- Treat the installed JAR metadata and the current pack's in-game behavior as authoritative for pack-specific claims. Official mod pages and guides are research inputs, not proof that every upstream feature or default is enabled in this pack.
- Keep screenshots real and drawn from the current 26.2 pack. Use the existing screenshot queue and capture standards; never use fabricated or unrelated promotional images.
- Do not stretch screenshots to the full printable width or near-full page height. Scale their display in the handbook; keep the original screenshot files intact.
- Preserve the current one-page Table of Contents and update all destinations and page numbers after PDF pagination changes.

## Proposed reading flow

### Front matter and Quick Reference

Keep the current cover, indexes, and Quick Reference placement. Add a short section such as **Major systems that change how you play** inside Quick Reference. Its ordered signposts are:

1. **Build an RPG character:** race, class loadout, RPG tree, general perks, spells, runes, and gear.
2. **Build around equipment:** Curios slots and the installed Penchant enchanting rework.
3. **Explore with intent:** the world map, expedition planning, and Alex's Caves discovery route.
4. **Start a collection loop:** Starcatcher fishing, its guide, minigame, equipment, and catalog.
5. **Manage companions:** Pet Vault registration, storage, summoning, status, and travel use.

Keep these explanations short and link each signpost to the canonical guide later in the handbook. The current first-session checklist can be edited to follow the same order. The Quick Reference must stay compact enough to read cleanly in print.

### Part I — Start Here

Keep the existing complete controls chapter first in Part I, followed by the race/class and first-session onboarding. Make the onboarding read as one connected first build: choose a race, assemble a flexible class loadout, inspect both progression trees, test a spell/weapon combination, and then prepare for exploration. Keep existing key recommendations and tested behavior intact.

### Part II — Character and Core Systems

Retain the core RPG topics, but connect them as a deliberate build path rather than isolated mod descriptions. Explain which choices persist, which can be changed through equipment, how the `T` RPG tree differs from the `G` Skill Perks tree, how stats or tooltip values affect a loadout, and where the ability bar, runes, combat style, and accessories fit.

Expand the equipment topic with a **Curios loadout and cosmetics** guide. Explain the slot screen as the place where pack systems equip accessories and class books; distinguish functional slots from the purple vanity/cosmetic layer; describe the current head, face, and hat combinations for hats and functional headgear; and point to the real Curios screenshot. Keep Curios as an API/support dependency in the maintenance classification, not a new gameplay-catalog entry.

Give Penchant a canonical player-facing entry in the existing enchanting topic. Explain its installed-version behavior and how it changes the enchantment loop, then relate it to the pack's Enchanting Table, Infuser, Anvil, tooltip, gear, and XP systems. Verify world datapack selections and current in-game behavior before writing claims about defaults or rewards. The installed JAR metadata describes Penchant as a rework centered on leveling enchantments through use; confirm exact details against the packaged guide and current port before publication.

### Part III — Exploration, Adventure and Challenge

Keep the part's visual treatment but reorder the opening around preparation and discovery:

1. Expedition checklist.
2. Travel and maps, with a dedicated, icon-backed Xaero's World Map profile early in the topic.
3. World and dimension expansions, with Alex's Caves as the detailed lead feature.
4. Fishing and collection systems, with the detailed Starcatcher guide moved here.
5. Companions and pets, with a dedicated Pet Vault profile before short summaries of the other companion mods.
6. Bosses, events, dangerous nights, structures, and creature summaries.

The Alex's Caves guide should explain why this is an exploration destination, its actual discovery route, use of the Cave Compendium, what a player should prepare for, and how to understand its cave-specific mechanics and rewards. Include the biome names only where they help a player identify or choose a destination; do not list every block or creature. Use the installed version's guide and verified in-game content.

The world-map guide should establish that the map is a navigation tool for explored terrain, show how it supports route planning and locating known landmarks, and make clear what it cannot reveal. Retain the current `M` binding only if still verified in the installed pack.

The Starcatcher guide should explain its complete player loop: first guide/rod, choosing a location, fish-specific conditions, minigame, hooks/bobbers/bait, catalog, progression rewards, and useful multiplayer or accessibility settings. Emphasize the experience rather than enumerating every fish.

The Pet Vault guide should explain the current pack's exact Keeper's Locket workflow, how registration differs from summon/store, the permanent unregister interaction, status and last-known information, and why vaulting pets changes expedition logistics. **Resolve the existing version-sensitive discrepancy first:** the current handbook says to equip the locket in a Curios necklace slot, while the currently published upstream page describes keeping it in inventory and separately notes optional Curios integration. Test the installed pack build and document only its actual behavior.

### Part IV — Survival and Creation

Keep food, farming, cooking, alchemy, building, storage, transportation, vehicles, and economy in their current concise, task-oriented structure. Replace Starcatcher's old detailed location with a single cross-reference to its promoted guide. Leave small gear and structure entries at their current summary depth.

### Part V — Interface and Reference

Preserve the current help topics, HUD guidance, cosmetic summaries, and optional client features. Update only cross-links affected by the moves.

## Prominence rubric

Use a full player guide for a system that changes a recurring gameplay loop, meaningful progression, exploration decisions, loadouts, or multiplayer preparation. A full guide should cover:

- what changes from ordinary Minecraft and why the player should care;
- how to begin and what to look for;
- the repeatable play loop and progression/reward path;
- relevant equipment, controls, stats, co-op, and links to connected pack systems;
- common confusion, risk, or version/configuration caveats;
- a real screenshot when it teaches an interface or action better than prose.

Use a short overview bullet for a mod that adds a narrow equipment option, a small set of structures, cosmetic variants, or a secondary convenience. Major integrated mods can mention their sub-content without reproducing the entire content list.

## Source and asset notes

- Penchant's installed mod metadata and the [NeoForge port README](https://github.com/WoXayZ/PenchantNeoforged) are the starting point for that guide; confirm 26.2-specific behavior in the installed build.
- Use the [Alex's Caves project page](https://www.curseforge.com/minecraft/mc-mods/alexs-caves) and its [Getting Started guide](https://alexscaves.wiki.gg/wiki/Guides/Getting_Started), then verify the pack's actual discovery route.
- Use the [Starcatcher project page](https://www.curseforge.com/minecraft/mc-mods/starcatcher) as an authoritative feature reference, then verify current pack controls and configuration.
- Use the [Pet Vault project page](https://www.curseforge.com/minecraft/mc-mods/pet-vault) as a reference, but resolve the installed-version locket workflow in-game.
- Use the [Curios API project page](https://www.curseforge.com/minecraft/mc-mods/curios) to explain the boundary between the shared slot API and the items/slots supplied by integrated mods and pack configuration.
- Use the [Xaero's World Map project page](https://www.curseforge.com/minecraft/mc-mods/xaeros-world-map) for feature terminology; verify map behavior and controls in the pack.
- Keep icon sourcing within the existing `docs/assets/mod-icons/README.md` policy. Add Penchant artwork only after confirming an exact official match and compatible reuse terms; otherwise leave its icon unresolved rather than substituting art.

## Acceptance criteria

- Quick Reference leads with the RPG path and then shows exploration systems and the other named major systems with working links.
- The detailed guides for RPG/Curios/Penchant, World Map/Alex's Caves, Starcatcher, and Pet Vault are visible in Parts II and III without forcing a reader through the lower-priority catalog first.
- Starcatcher has one full canonical guide after its move; the old Part IV location contains only a cross-reference or is cleanly merged.
- Penchant appears in the canonical catalog, alphabetical index, a relevant gameplay tag, and the correct source list; its icon is verified or intentionally unresolved.
- The Curios description accurately explains functional versus vanity slots and the installed hat/head/face behavior without presenting the API as a standalone content mod.
- Pet Vault's installed locket behavior is tested and stated consistently in all relevant guidance.
- Minor equipment and structure entries retain their concise treatment.
- The original cover, five-part organization, typography, keybind table style, icon presentation, TOC page, and print hierarchy are preserved.
- The rebuilt PDF has correct links and page numbers, readable real screenshots, no clipping or unintended blank pages, and a one-page TOC.
- Standalone screenshots are centered, visibly smaller than the printable text column, legible at approximately 75% column width, and do not dominate a page; mod-entry icons retain their existing dimensions.

# Modded Swords x Punchy! 26.2 Compatibility Plan

> **For agentic workers:** Implement this plan task by task after the user asks for the conversion. Keep the supplied ZIP intact and use an isolated client profile for runtime checks.

**Goal:** Make the installed Punchy 2.8f and Better Combat 3.2.2 show one correctly animated held weapon during attacks in Minecraft 26.2, while retaining relevant modded-weapon tuning from the supplied resource pack.

**Architecture:** Convert the supplied 1.21.1 resource pack into a client-side NeoForge resource JAR, then isolate the duplicate held-item render. A resource JAR can change Punchy tuning, but it cannot repair a duplicate draw call in either mod; that result would need a configuration or code fix instead.

**Tech stack:** Minecraft 26.2, NeoForge 26.2.0.90+, Punchy 2.8f, Better Combat 3.2.2, JSON resource-pack assets, Python `zipfile` for inventory and validation.

**Spec:** This document's [scope and acceptance](#scope-and-acceptance) section.

## Scope and acceptance

- Source: `shaders/Modded Swords x Punchy! 1.21.1 v1.3.2.zip` (read-only; despite its folder, this is a **resource pack**, not a shader pack).
- Installed mods: `mod store/punchy-2.8f-neoforge-26.2.jar`, `mods/bettercombat-neoforge-3.2.2+26.2.jar`, and the RPG Series weapon modules in `mods/`.
- Result: `mod store/modded-swords-x-punchy-compat-26.2-1.3.2.jar`, with a build script under ignored `tmp/punchy-26-2-client/`. Keep the original ZIP for comparison. The source pack is marked All Rights Reserved; redistribution permission has not been verified.
- In an isolated 26.2 client, sustained attack input and ordinary Better Combat combos show one moving held weapon, with no frozen duplicate. Check both hands, idle, mining, use, weapon switching, and reload after enabling the pack.
- If the same duplicate appears with the compatibility ZIP disabled, stop treating the ZIP as the cause. Investigate Punchy's Better Combat setting or the renderer interaction before editing 174 mappings.
- No changes to the player's personal `.minecraft` or worlds during development. Decide separately whether and how the finished resource pack is distributed; the installer currently syncs mod JARs, not resource packs.

## Findings from the installed files

- The legacy ZIP contains **174 compat JSON files** at `assets/minecraft/punchy/compat/`, plus `pack.mcmeta`, `pack.png`, and a blacklist note. It declares `pack_format: 15` and `supported_formats: [15, 34]`.
- Punchy 2.8f's embedded 26.2 pack declares `pack_format: 88`, `min_format: 88`, and `max_format: 88`. Use that installed pack as the metadata reference; do not assume the old format range covers 26.2.
- Punchy 2.8f's generated `config/punchy/punchy_config.json` has `bettercombatCompat: false` by default, as confirmed in the isolated client. This is a plausible contributor to duplicate held-item animation and must be tested visually before claiming the root cause.
- The pack's Punchy 2.8f JAR now changes only `punchy/config/PunchyConfig$Data.class`: its constructor sets `bettercombatCompat` to `true` instead of `false`. A fresh isolated client generated `bettercombatCompat: true` and loaded the compatibility JAR. Existing client configs preserve their saved value and must be switched separately.
- The old JSON already uses Punchy's `customGroups`, `item`, `itemSpecific`, `customAnimation`, and `betterCombat` fields. It is therefore **not** merely missing a Better Combat definition. Punchy 2.8f still has a `bettercombatCompat` setting and a `BetterCombatEntry` type with `melee_combo` attack slots; verify how the current parser applies each legacy field before changing values.
- The old pack references **107 weapon IDs** across the five installed `archers`, `arsenal`, `wizards`, `rogues`, and `paladins` namespaces. The corresponding item filenames exist in the installed 26.2 JARs. That establishes useful starting coverage, not proof that animations or item semantics still match.
- The legacy pack also targets many absent mods. Keep those mappings out of the first 26.2 build, then add installed families when they serve an observed need. Check duplicate item IDs and overlapping groups before packaging.
- Punchy's [official compatibility guide](https://github.com/punchy-guys/punchy-wiki/wiki/Punchy!-Resource-Pack-Compatibility-Guide-EN-US) says resource-pack compat files load after mod defaults and local config, with `itemSpecific` taking precedence over `item` groups. It documents `/punchy debugposition`, `config/punchy/creator_tuning_helper.json`, and `F3 + T` for iteration. The [source pack's official page](https://modrinth.com/resourcepack/punchy-compatibility) says its latest 1.21.1 version already targets Better Combat; it does not list 26.2.

## File map for implementation

| Path | Responsibility |
| --- | --- |
| `shaders/Modded Swords x Punchy! 1.21.1 v1.3.2.zip` | Immutable reference archive. |
| `tmp/punchy-26-2-client/` | Ignored isolated client, build script, logs, and captured comparisons. |
| `mod store/modded-swords-x-punchy-compat-26.2-1.3.2.jar` | Client-only NeoForge resource JAR with the source pack's 174 compat JSON files and 26.2 pack metadata. |
| `docs/superpowers/plans/2026-10-16-punchy-modded-swords-26-2.md` | Decisions, test observations, and any scope changes. |
| `README.md` and `docs/MODLIST.md` | Update only when a working pack is actually installed or distributed. |

## Tasks

### Task 1: Isolate the frozen weapon

- [x] Prepare a separate Minecraft 26.2.0.90 client with Punchy 2.8f, Better Combat 3.2.2, Player Animation Library 1.2.6, and Cloth Config 26.2.155. No personal world or options were copied. An RPG weapon module and flat-world combat pass remain to be added.
- [ ] Record the exact weapon, hand, camera view, shader/resource-pack state, and whether the duplicate occurs only while holding attack. The original generated Punchy `bettercombatCompat` value was `false`; the repack now generates `true` in a fresh isolated profile for a later visual comparison.
- [ ] Compare four states: Better Combat alone; Punchy + Better Combat with no legacy ZIP; both mods with the legacy ZIP; and both mods with Punchy's Better Combat support toggled. Keep each state otherwise identical.
- [ ] **Decision:** If the duplicate occurs without the ZIP, locate the draw/config conflict first. If it appears only with the ZIP, continue with the resource-pack conversion. Preserve screenshots or a short capture of the before state for comparison.

### Task 2: Map the legacy pack to the installed 26.2 data

- [ ] Inventory all 174 JSON files, their `customGroups`, item IDs, duplicate IDs, `betterCombat` entries, and custom-animation names; record malformed or unsupported fields separately.
- [ ] Cross-check RPG weapon IDs against the installed 26.2 JAR registries/assets and current Better Combat weapon attributes. Prioritize one sword, dagger, spear, staff, and dual-wield example from the pack.
- [ ] Compare those five definitions with Punchy 2.8f's embedded compat template and the current official guide. Do not copy an obsolete attack name or transform just because its JSON parses.
- [ ] Choose the smallest set of rules that addresses the observed defect. Avoid broad `globalItem` overrides and overlapping `itemSpecific` rules that could restore a stationary model over Better Combat's moving one.

### Task 3: Build a 26.2 JAR candidate

- [x] Create a client-only NeoForge JAR with `pack.mcmeta` at its root and the exact compat path `assets/minecraft/punchy/compat/`; base format metadata on Punchy 2.8f's embedded 26.2 pack (`88`).
- [x] Preserve all 174 original compat JSON files in the initial conversion. A narrower override remains an option after reproducing the visual defect.
- [ ] Keep a brief manifest of carried, changed, and omitted mappings with reasons. Never overwrite the supplied 1.21.1 ZIP.
- [x] Validate JAR integrity and parse every compat JSON document. The full source pack intentionally includes mappings for mods not installed in this pack; the visual effects of overlapping rules remain unverified.

### Task 4: Exercise the candidate in game

- [x] Load the candidate in the isolated NeoForge 26.2.0.90 client. The startup log recognizes the mod and includes `mod/modded_swords_x_punchy_compat` in resource reload. No pack-format warning appeared in the first launch. The metadata was then updated to avoid NeoForge's deprecated `lowcodefml` warning and relaunched.
- [ ] Repeat held attack, click attack, combo completion, hand switching, mining, item use, weapon swapping, and `F3 + T` on the representative weapons. Check both first-person and third-person views.
- [ ] Compare captures with Task 1. Require a single animated weapon during Better Combat attacks and no frozen copy at any tested moment.
- [ ] Expand to the remaining installed families only after the representative set passes. Recheck each added family, including non-sword weapons and dual wielding. If a resource-only solution cannot eliminate the duplicate, document the exact failing state and switch to a Punchy/Better Combat configuration or code fix.

### Task 5: Integrate the proven result

- [x] Place the converted resource JAR in `mod store/`, the pack's client-only JAR folder. The installer can discover it by content hash after it is committed, subject to the source author's redistribution terms.
- [ ] Update the README and Punchy handbook entry with the exact enable order and any required Punchy setting, only after observing a working result.
- [x] Identify the original pack's **All Rights Reserved** terms. Redistribution permission has not been verified; the user requested a main-branch push of this converted JAR.
- [ ] Record tested versions, results, and any remaining affected weapons. Commit or push only when requested.

## Review focus

1. A resource-pack warning disappearing after a metadata edit does not prove the duplicate-render problem is fixed.
2. Punchy's pack compat can override local tuning; test pack priority and config toggles in a controlled order.
3. A weapon ID may still exist while its Better Combat attack type or animation changed in 26.2.
4. A broad group and a specific item rule can both match the same weapon; check effective precedence.
5. Third-person combat animation can look correct while a stationary first-person item remains visible; inspect both views.

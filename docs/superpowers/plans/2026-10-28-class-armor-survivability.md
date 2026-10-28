# Class armor survivability Implementation Plan

> Execute inline with superpowers:executing-plans; user requests minimum workers. No workers or Git commits/pushes for this task.

**Goal:** Apply the approved three-class defense progression across19 native sets and14 Armory sets.
**Architecture:** Extend the existing Armor.Entry registration/configuration stage with a small namespaced defense helper. Preserve recent doctrine/healing patches and all non-defense item data. Set item durability with the native registration path, avoiding per-tick attributes or debuffs.
**Tech Stack:** Java25, Minecraft26.2, NeoForge26.2.0.90, existing ASM patch tooling and Python zip packaging.
**Spec:** ../specs/2026-10-28-class-armor-survivability-design.md (approved by ?Implement them now?).

## Constraints
Same active JAR filenames. Preserve Paladin plate, class modifiers, set bonuses, IDs, recipes, enchantments and other patches. No personal profile or separate server changes. Entry/advanced/netherite toughness totals0/0/4, durability iron/diamond/netherite; Armory uses netherite.

## Review focus
Stale equipment configs must not override approved defenses. Avoid duplicate toughness attributes. Full armor totals and individual durability must match native baselines. Existing damaged/enchanted stacks retain their data and must not silently refill. Excluded Paladin plate and non-defense stats must remain byte/data equivalent.

- [x] Snapshot five current JARs and inspect exact registration/durability flow; record current native item stats and failing target checks in isolated server.
- [x] Add minimal defense helper and registration hooks; build same-name candidates under tmp/class-armor-survivability/built. Exact targets live in patch policy.json; build manifest records changed archive entries.
- [x] Run native probes against defaults, equipped attributes, old generated configs, damaged/enchanted save/load, all target items and preserved other modifiers. Compare candidates to baseline. Inspect real client item tooltips/defense values.
- [x] Review exact archive diff; install active/fixed copies with ignored retired backups, update class MDs and player/patch documentation.
- [x] Rebuild handbook PDF, inspect pages/links/layout, run artifact/hash/diff checks, report actual results. No push.

Ruling: native Armor.CustomItem calls humanoidArmor and overwrites Entry.create durability. Pass a copied ArmorMaterial with only durability changed through the existing creation hook; preserve all other material fields. The initial candidate runtime failed224 durability checks and confirmed this path.

Completion: five same-name JARs installed with matching fixed copies. Fresh and legacy native runs each PASS1626; actual client PASS462. Exact scope: five registration classes changed, five helpers added,4159 existing entries byte-identical.79-page handbook rendered/reviewed; all51 contents destinations match printed pages. Git diff whitespace clean. No workers, commit or push. Existing unrelated Paladin tier-one work preserved.

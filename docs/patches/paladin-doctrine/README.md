# Paladin doctrine and Healing Power balance

The Paladin hand restriction below remains. Its earlier plate values and helper revision are superseded by the [Priest plate restriction and further armor progression](../priest-plate-doctrine/README.md). This record retains its original tested artifacts.

Installed28October2026 for Minecraft26.2 / NeoForge26.2.0.90 / Java25. Same-filename replacements: Skill Tree1.6.1, Paladins & Priests3.1.2 and Armory1.5.2. Use matching client/server files and restart. No push requested; personal installations/worlds remain untouched.

## Player behavior

Selecting **Path of the Paladin** prevents wielding healing staves and wands in **either hand**. The root tooltip explains this before selection. Trying to equip one shows: **?Your Paladin oath forbids healing staves and wands.?** Warnings are rate-limited; merely storing those items does not repeat the message.

Rejected weapons return to an empty storage slot or safely swap with an allowed stored item. Nothing is deleted or automatically dropped. If every storage slot is another prohibited weapon, the displaced items are saved with the player until inventory space is available, with a recovery message. Normal death includes those items in ordinary drops; keepInventory preserves them. Resetting the Paladin path lifts the restriction. Priest roots and other weapon types remain usable.

The class and ordinary Paladin Healing Power rewards increase from **+0.2 to +0.25**. Both category resources and runtime defaults agree; existing unlocked rewards reconcile when the server loads the updated definitions. Class membership uses the actual unlocked Paladin root in the class tree, not equipped armor or a copied spell book. Native healing_staff/healing_wand item tags cover supported weapons without a namespace-only blacklist. Optional ruby/aether staves are restricted by the same tags when their supplying mods register them.

| Plate set | Flat Healing Power per piece | Flat bonus from four pieces |
|---|---:|---:|
| Paladin | +0.75 | +3 |
| Crusader | +1 | +4 |
| Netherite Crusader | +1.15 | +4.6 |
| Justicar | +1.25 | +5 |
| Lightbringer | +1.25 | +5 |

Existing set bonuses apply separately: Justicar retains its two-piece **+2 Healing Power**, so all four Justicar pieces give **+7 total** including that bonus. Priest/Prior and Avatar/Absolution robes, other attributes, armor/toughness, haste, criticals, damage, recipes, IDs and legendary bonuses remain. Both defaults and loaded legacy equipment configs receive the new per-piece values. Explicit custom ItemStack attribute overrides remain outside this default-armor adjustment.

## Implementation and preservation

`PaladinDoctrine.java` checks server-side class allocation and repairs hands before native equipment collection. A native equipment-update invoker removes old weapon modifiers immediately, including a rapid Priest-to-Paladin role/slot change. Spell Engine's cast-attempt hook covers channel/release/trigger attempts; attack/use guards reject the initiating forbidden action. A normal player-tick hook maintains the restriction and restores displaced items. None of these classes references client-only code.

Fallback displaced items use strict ItemStack codec serialization under PlayerPersisted. Data is encoded before clearing a hand and copied before updates, preserving native save/load and respawn behavior. No inventory-add API that can delete creative items or drop overflow is used. Storage is normally empty; it exists only for an inventory containing no legal swap destination. Normal death follows native drop handling; it does not create a keep-on-death item bank.

The Skill Tree overlay changes five existing entries (two definition files, runtime NodeTypes, English language file and mixin configuration) and adds five focused classes. Each armor mod changes only its existing HealingArmorBalance helper. **2,106 other archive entries** remain byte-identical. [Installed hashes](installed.json), [skill artifact manifest](skill-artifacts.json) and [armor artifacts](armor/artifacts.json) pin the exact tested files. Ignored originals are in `mod fixing/retired packs/paladin-doctrine-20261028/`, with final matching copies in `mod fixing/1 fixed packs/`. Unrelated pending Delight work is preserved.

## Verification

| Check | Result |
|---|---|
| Original native path/reward probe | Seven expected failures: old0.2 rewards/defaults and missing doctrine |
| Rapid role/slot switch regression | Reproduced stale15.25 Healing Power; fixed expected10.25 |
| Final isolated server | **84/84 pass**, Java25 and full JVM verification |
| Armor fresh/legacy native registration | **29/29 pass in each mode**; both original modes fail25 requested-value checks |
| Actual client/integrated server | **49/49 pass**, exact installed candidates |
| Archive preservation | **2,106 entries unchanged**, server-safe Java25 helpers and matching active/fixed hashes |
| Review | No Critical/Important findings; lifecycle coverage comment resolved by native tests |

Server checks exercise real Puffish root/ordinary allocations, every registered standard healing weapon in both hands, full safe-swap inventory and all-prohibited inventory, strict reserve round-trip, stale attributes, queued/cast/attack rejection, Priest respec and ordinary swords. Actual native player save/load, keepInventory, ServerPlayer.restoreFrom respawn, exact recovery without duplication, copy independence and normal-death drop handling pass. The minimal server does not register optional ruby/aether material staves; those tag entries remain conditional, not dummy items.

The [client record](client/README.md) includes actual hotbar selection packets, offhand/tick synchronization, exact item counts, localized non-repeating action-bar warning, all equipped armor tiers and four screenshots. Its hovered root screenshot shows **+0.25 Healing Power** and the restriction. Justicar's separately preserved +2 set bonus is verified. These are isolated native server/client tests, not a live-server or whole-pack performance run.

Player README, current Priest reference, handbook text and PDF are synchronized. The earlier [uniform0.25 armor revision](../healing-armor-balance/README.md) is marked historical. PDF review/checks and plan completion are recorded in the implementation plan.

## Reproduce

```powershell
python docs/patches/paladin-doctrine/armor/build.py
python docs/patches/paladin-doctrine/armor/run_server.py
python docs/patches/paladin-doctrine/armor/run_server.py --legacy
python docs/patches/paladin-doctrine/build_skill.py
python docs/patches/paladin-doctrine/run_server.py
python docs/patches/paladin-doctrine/armor/verify.py
python docs/patches/paladin-doctrine/verify.py --installed
```

Build scripts use this task's immutable current-pack snapshots under ignored `tmp/paladin-doctrine/before/`, preserving all earlier fixes. Rebuilding after a published test requires regenerating the artifact/test manifests; added ZIP-entry timestamps can differ even with identical compiled class bytes. Runtime instances and raw logs stay under `tmp/paladin-doctrine/`. Test classes are never packaged in production JARs. Client reproduction lives in its own record.

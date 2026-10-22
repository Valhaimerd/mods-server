# Rogue skills - raw attributes, secondary nodes and passive pairs

Inspected **22 October 2026** from the installed JARs. Current installed values after the first-tier Rogue rework. Later tiers retain their extracted values.

## Reading the values

- **M** = caster melee attack damage / physical-melee spell power. **D** = dual-melee school power: main-hand damage plus the engine's calculated offhand damage. These are raw values before criticals, defenses, vulnerabilities and the pack's PvP retention.
- 20 ticks = 1 second; 2 HP = 1 heart. Internal tiers 2/3/4 are active tiers I/II/III. Secondary nodes below mean each skill's improved root and two alternative branch upgrades.
- Missing engine fields: instant standard cast, haste affected; cooldown 0 seconds, haste affected, nonproportional; damage coefficient 100%, knockback 1; status duration 10 seconds, zero-based amplifier 0, SET, refresh true; stash duration 10 seconds, amplifier 0. Base resource costs default to 0.1 exhaustion and one durability unless overridden. These defaults apply only when that mechanic is present.
- All listed Rogue cooldowns use the native default haste scaling; attack-speed multipliers and configured spell haste contribute to the melee school. Fixed status/cloud lifetimes do not automatically scale with haste.
- ADD status effects start at one level when increment is 1. Serialized caps are zero-based; cap 9 means ten levels. Percent attribute modifiers use ADD_MULTIPLIED_BASE unless explicitly stated otherwise.
- The pack protects party players and player-owned pets through shared target/final-impact relationship checks. **rogues:shock** now bypasses native boss/status-immunity restrictions and uses the custom duration rules below. **rogues:bear_trap** retains its native maximum-health/boss/immunity restrictions. Poison remains in the universal bypass list.
- The first-tier rework forces its revised Slice, Fleet Footed, Smoke Screen and Fracture attribute registrations even with old generated effect configs. Other effect registrations remain native defaults and can be affected by generated configs.
- Rogues & Warriors shares one JAR. This document includes **Rogue class skills only**, excluding Warrior skills and the separate weapon trees.

## Three active tiers

| Tier | Subtlety choice | Blade choice |
| --- | --- | --- |
| I | Shock Powder | Slice & Dice |
| II | Shadowstep | Bear Trap |
| III | Vanish | Mutilate |

## Tier I - Shock Powder OR Slice & Dice

| Attribute | Shock Powder | Slice & Dice |
| --- | --- | --- |
| Cast / base cooldown | Instant / **12 s**, haste affected | Instant / **12 s**, haste affected |
| Main effect | Per-target stun, **1 s or 3 s** | Next **10 successful melee hits** add **15% M** damage each and gently pull their targets toward the caster |
| Mob duration | Maximum HP **at most 50 + 2M: 3 s**; above that: **1 s**, including bosses | Hit charges; no stacking attack-damage attribute bonus |
| Player duration | Current HP **below 24: 3 s**; **24 or more: 1 s** | Shared with the same native melee-hit trigger; misses and party-protected targets do not consume charges |
| Area / activation window | **5-block radius**, vertical multiplier **0.5** | Retains a **10 s activation window**; charges expire if unused |
| Evasion | No baseline evasion effect | Each empowered hit gives **5 percentage points**, cap **10 stacks / 50 points**, **4 s**, refreshed |
| Pull | None | Horizontal impulse up to **0.2** toward caster; respects knockback resistance and stops short of crossing caster |
| Exhaustion | **0.3** | **0.2** |

### Tier-I secondary nodes

| Position | Shock Powder | Slice & Dice |
| --- | --- | --- |
| Improved/root | **Improved Shock Powder:** +1 s, giving **2 s or 4 s** | **Improved Slice and Dice:** **12 hits**, replaces the old cooldown reduction |
| Branch A | **Explosive Powder:** **100%** chance per qualifying impact, **100% M**, **2.5-block splash**, no distance falloff. Its hidden companion no longer adds stun | **Blades of Fury:** +3 hits, giving **15 with Improved**; replaces the old damage-stack cap upgrade |
| Branch B | **Smoke Screen:** **5-block-radius cloud for 8 s**, pulses every **0.5 s**, refreshing a **1 s** buff granting **50 percentage points evasion** at the native base stat | **Fleet Footed:** empowered hits add **5% base movement speed**, up to **12 stacks / +60%**, lasting **4 s**, refreshed |

Both stun durations bypass boss tags and ordinary effect rejection; the shorter boss stun is intentional. The maximum-health threshold is only used for mobs. The player's threshold uses current health, with exactly 24 HP on the short-stun side. Damage percentages are raw before defenses, criticals and pack PvP retention. Slice's 15% bonus remains constant across its charges.

### First passive pair - Coated Blades OR Fracture

| Attribute | Coated Blades | Fracture |
| --- | --- | --- |
| Trigger | Melee hit or active melee-spell impact | Melee hit or active melee-spell impact |
| Chance | **25%** | **25%**, retained |
| Base cooldown | **1 s**, haste affected | **1 s**, haste affected |
| Effect | Adds stacking Poison, **8 s**, refreshed; power-scaled Poison cap retained | **Flat armor reduction of 2 + 10% M per stack**, up to **3 stacks** |
| Direct bonus damage | None | **Removed** |
| Duration | **8 s**, refreshed | **6 s**, refreshed by successful procs |
| Example at M = 40 | Poison cap retains native formula | **6 armor points per stack**, **18 at three stacks** |

Fracture snapshots the latest applying caster's melee power for the whole current stack set. Native -2 armor per level combines with a saved -10% M-per-level modifier; one stable modifier ID prevents accumulation. Removal and natural expiry clear the scaling modifier and restore armor. This replaces the old -30% base-armor effect.

## Tier II - Shadowstep OR Bear Trap

| Attribute | Shadowstep | Bear Trap |
| --- | --- | --- |
| Cast / cooldown | Instant / **12 s** | Instant / **15 s** |
| Main effect | Teleport behind an aimed enemy within **15 blocks**; Shadowstep status **1.5 s** | Places **3 traps**, each **100% D** damage, zero knockback, plus **3 s root** |
| Geometry / life | Required enemy aim; harmful teleport | Traps placed 2 blocks away at 0/120/240 degrees, delayed 0/3/6 ticks; each radius **0.6**, vertical multiplier **0.5**, active life **20 s**, spawn/despawn **20/15 ticks** |
| Trap activation | No direct damage in base skill | Checks every **2 ticks**, impact cap **1** per trap; root gate maximum HP **100 + 2D** |
| Improved/root | **Improved Shadow Step:** +3 range, **18 blocks** | **Improved Bear Trap:** +5 percentage points critical chance |
| Branch A | **Cloak of Shadows:** appends **5 s** protection; native initial amplifier 0 means one incoming protected hit, with serialized ADD cap 1 | **Serrated Traps:** adds **6 s Bleed**, serialized amplifier **1 + trunc(0.25D)**; zero-based level interpretation applies |
| Branch B | **Ambush:** readies **+50% base attack damage** for the next melee hit within **5 s** | **Extensive Coverage:** adds 3 traps at **4 blocks**, angles 60/180/300 degrees, delayed 9/12/15 ticks; **6 traps total** |

Bear Trap's native root modifiers are -200% base movement/jump and ROOT action impairment. The damage impact and root gate are separate. Shadowstep status changes mob tracking; it does not itself specify damage immunity.

## Tier III - Vanish OR Mutilate

| Attribute | Vanish | Mutilate |
| --- | --- | --- |
| Cast / cooldown | Instant / **30 s** | Instant / **12 s** |
| Main effect | **8 s Stealth**, -50% base movement speed | Native dual-melee strike, not an added JSON damage coefficient |
| Delivery | Self status; stealth removed by attacks, other spell casts or item use | **0.5 s attack delay**; hitbox width **0.5**, height **0.2**, arc **160 degrees**, no additional same-target hits |
| Power | No direct hit | Uses dual-melee school **D** for relevant appended spell impacts; native melee delivery follows its own attack rules |
| Improved/root | **Improved Vanish:** +2 s, **10 s total** | **Improved Mutilate:** cooldown -2 s, **10 s total** |
| Branch A | **Stealth Speed:** +50% base movement speed counters the stealth slow; appended effect base **8 s**, also receives Improved duration modifier | **Crimson Strikes:** heals caster for **10% D per enemy struck** |
| Branch B | **Deep Stealth:** +8 s, **18 s total** with Improved | **Envenom:** adds **8 s Poison**; one level per application, maximum level **2 + trunc(0.5D)** |

## Later passive pairs - unchanged, shown with their tiers when reviewing

| Pair | Left choice | Right choice |
| --- | --- | --- |
| II | **Sidestep:** rolling adds one evasion level, +10 points per level at native base, cap five, 12 s; removed on damage | **Opportunist:** roll has 50% chance to ready its critical-chance boost for the next melee attack within 5 s; 10 s cooldown |
| III | **Cheat Death:** fatal incoming damage trigger; 3 s defensive effect; 60 s cooldown | **Preparation:** evade has 25% chance to reset #rogues:rogue active cooldowns; 30 s cooldown |

### Status defaults checked in installed bytecode

| Effect | Native registration |
| --- | --- |
| Slice & Dice | No attack-damage modifier; status levels represent remaining charges |
| Slice Evasion | spell_engine:evasion_chance, +0.05 ADD_MULTIPLIED_BASE per level, cap ten |
| Shock | movement_speed and jump_strength, -1 ADD_MULTIPLIED_BASE; STUN action impairment |
| Fleet Footed | movement_speed, +0.05 ADD_MULTIPLIED_BASE per level, cap twelve |
| Smoke Screen | spell_engine:evasion_chance, +0.5 ADD_MULTIPLIED_BASE |
| Fracture | minecraft:armor, -2 ADD_VALUE per level plus -0.1M per level from runtime snapshot |
| Sidestep | spell_engine:evasion_chance, +0.1 ADD_MULTIPLIED_BASE per level |
| Ambush | attack_damage, +0.5 ADD_MULTIPLIED_BASE; consumed on melee hit |
| Opportunist | critical_strike:chance, +1 ADD_MULTIPLIED_BASE; consumed by the stashed melee trigger |
| Cheat Death | spell_engine:damage_taken, -1 ADD_MULTIPLIED_TOTAL; custom CheatDeathStatusEffect also applies |
| Stealth / Stealth Speed | movement_speed, -0.5 / +0.5 ADD_MULTIPLIED_BASE |

Evasion's native base is 100; its chance conversion is (attribute - 100) / 100. Thus Smoke Screen's base-scaled +50 attribute gives +50 percentage points at the native base. Other sources, server angle restrictions, casting/item-use restrictions and damage eligibility can affect whether an attack is evaded.

Verification scope: archive/resource extraction, hidden reward mapping, default schema fields and installed effect/engine bytecode inspection. The first-tier implementation is additionally exercised by the isolated Rogue fixture; see docs/patches/rogue-tier-one/README.md for current results and limits.

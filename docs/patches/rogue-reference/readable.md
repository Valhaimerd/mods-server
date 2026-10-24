# Rogue skills - raw attributes, secondary nodes and passive pairs

Updated **25 October 2026** from installed archives and the Rogue tier I/II follow-up. Includes the earlier tier III rework.

## Reading the values

- **D** = combined dual-melee school power: main-hand damage plus the engine's calculated offhand damage. These are raw values before criticals, defenses, vulnerabilities and the pack's PvP retention.
- 20 ticks = 1 second; 2 HP = 1 heart. Internal tiers 2/3/4 are active tiers I/II/III. Secondary nodes below mean each skill's improved root and two alternative branch upgrades.
- Missing engine fields: instant standard cast, haste affected; cooldown 0 seconds, haste affected, nonproportional; damage coefficient 100%, knockback 1; status duration 10 seconds, zero-based amplifier 0, SET, refresh true; stash duration 10 seconds, amplifier 0. Base resource costs default to 0.1 exhaustion and one durability unless overridden. These defaults apply only when that mechanic is present.
- All listed Rogue cooldowns use the native default haste scaling; attack-speed multipliers and configured spell haste contribute to the melee school. Fixed status/cloud lifetimes do not automatically scale with haste.
- ADD status effects start at one level when increment is 1. Serialized caps are zero-based; cap 9 means ten levels. Percent attribute modifiers use ADD_MULTIPLIED_BASE unless explicitly stated otherwise.
- The pack protects party players and player-owned pets through shared target/final-impact relationship checks. **rogues:shock** now bypasses native boss/status-immunity restrictions and uses the custom duration rules below. **rogues:bear_trap** also bypasses maximum-health/boss/type-immunity restrictions and physically roots living enemies. Poison remains in the universal bypass list.
- Prepared Counter uses a separate beneficial readiness effect with no attack-damage attribute modifier. On the next permitted basic melee hit, add 15% of combined dual-wield power to its incoming damage before defenses and PvP retention. At 40 dual-wield power, a 20-damage hit becomes 26. Crits and enchantments remain native; the bonus itself is a flat 15% of dual-wield power.

The first-tier rework forces its revised Slice, Fleet Footed, Smoke Screen and Fracture attribute registrations even with old generated effect configs. Other effect registrations remain native defaults and can be affected by generated configs.
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
| Cast / base cooldown | Instant / **14 s**, haste affected | Instant / **16 s**, haste affected |
| Main effect | Per-target stun, **1 s or 3 s** | Next **10 successful melee hits** add **8% D** damage each and gently pull their targets toward the caster |
| Mob duration | Maximum HP **at most 50 + 2D: 3 s**; above that: **1 s**, including bosses | Hit charges; no stacking attack-damage attribute bonus |
| Player duration | Current HP **below 24: 3 s**; **24 or more: 1 s** | Shared with the same native melee-hit trigger; misses and party-protected targets do not consume charges |
| Area / activation window | **4-block radius**, vertical multiplier **0.5** | Retains a **10 s activation window**; charges expire if unused |
| Evasion | No baseline evasion effect | Each empowered hit gives **5 percentage points**, cap **10 stacks / 50 points**, **4 s**, refreshed |
| Pull | None | Horizontal impulse up to **0.2** toward caster; respects knockback resistance and stops short of crossing caster |
| Exhaustion | **0.3** | **0.2** |

### Tier-I secondary nodes

| Position | Shock Powder | Slice & Dice |
| --- | --- | --- |
| Improved/root | **Improved Shock Powder:** +1 s, giving **2 s or 4 s** | **Improved Slice and Dice:** cooldown **-2 s**, giving **14 s** before haste; retains **10 hits** |
| Branch A | **Explosive Powder:** **100%** chance per qualifying impact, **40% D**, **4-block splash**, no distance falloff. Its hidden companion no longer adds stun | **Blades of Fury:** +4 hits, giving **14 total** |
| Branch B | **Smoke Screen:** **5-block-radius cloud for 8 s**, pulses every **0.5 s**, refreshing a **1 s** buff granting **50 percentage points evasion** at the native base stat | **Fleet Footed:** empowered hits add **3% base movement speed**, up to **12 stacks / +36%**, lasting **4 s**, refreshed |

Both stun durations bypass boss tags and ordinary effect rejection; the shorter boss stun is intentional. The maximum-health threshold is only used for mobs. The player's threshold uses current health, with exactly 24 HP on the short-stun side. Damage percentages are raw before defenses, criticals and pack PvP retention. Slice's 8% bonus remains constant across its charges.

### First passive pair - Coated Blades OR Fracture

| Attribute | Coated Blades | Fracture |
| --- | --- | --- |
| Trigger | Melee hit or active melee-spell impact | Melee hit or active melee-spell impact |
| Chance | **25%** | **25%**, retained |
| Base cooldown | **1 s**, haste affected | **1 s**, haste affected |
| Effect | Adds stacking Poison, **8 s**, refreshed; power-scaled Poison cap retained | **Flat armor reduction of 2 + 5% D per stack**, up to **3 stacks** |
| Direct bonus damage | None | **Removed** |
| Duration | **8 s**, refreshed | **6 s**, refreshed by successful procs |
| Example at D = 40 | Poison cap retains native formula | **4 armor points per stack**, **12 at three stacks** |

Fracture snapshots the latest applying caster's combined dual-wield power for the whole current stack set. Native -2 armor per level combines with a saved -5% D-per-level modifier; one stable modifier ID prevents accumulation. Removal and natural expiry clear the scaling modifier and restore armor. This replaces the old -30% base-armor effect.

## Tier II - Shadowstep OR Bear Trap

| Attribute | Shadowstep | Bear Trap |
| --- | --- | --- |
| Cast / base cooldown | Instant / **12 s** | **0.2 s** / **4 s** |
| Haste | Cast/cooldown haste enabled | Cast/cooldown haste enabled |
| Main effect | Teleport behind an aimed enemy within **14 blocks** | Place **one trap 3 blocks directly ahead** |
| Damage | No direct base damage | **40% D**, zero knockback |
| Tracking | Nearby hostile mobs within **4 blocks of arrival** lose their targets and cannot reacquire for **1.5 s**; players, self, party allies and owned pets are excluded | No tracking effect |
| Control | No baseline stun | **1 s unrestricted root** plus **Slowness I for 3 s** |
| Lifetime / visibility | Native teleport; disruption expires after 30 ticks; one cooldown reset on an attributed enemy kill within **4 s** after a successful cast | Visible **1 s arming**; hidden while armed; visible on activation; retained **20 s active lifetime**, one detonation |
| Activation radius | Required aimed enemy | **1 block**, or **1.5 blocks with Extensive Coverage** |
| Hit radius | Single target for Ambush | **1.5 blocks**, or **2.25 with Extensive Coverage**; every eligible enemy in the hit volume is affected by the one activation |
| Trigger geometry | Arrival-centered mob lookup | Cylinder **2.5 blocks tall**; entity bounding-box contact, including airborne/jumping enemies; checked every tick |
| Improved/root | **Improved Shadow Step:** +4 range, **18 blocks** | **Improved Bear Trap:** **60% D** hit damage; no extra critical chance, and does not scale Bleed power |
| Branch A | **Cloak of Shadows:** **0.5 s immunity to repeated incoming damage**, replacing one-hit protection | **Serrated Traps:** **2.5 s flat Bleed**, no power scaling |
| Branch B | **Ambush:** immediately deals **30% D** damage and applies **Blindness I, Weakness I and Slowness I for 2 s**; replaces the next-hit damage buff | **Extensive Coverage:** expands only the trap's hit/activation radii to **2.25 / 1.5 blocks**; still one trap |

Trap root bypasses health, boss tags and ordinary status rejection. Native party/friendly-pet protections apply before activation and final impacts. Protected allies cannot activate it. Arming, active lifetime, root and debuff durations stay fixed while cast time/cooldown scale with haste. Radius tests use entity bounding-box contact, so a large entity can touch the trigger while its center is farther away. The cloud remains noncolliding and cannot be attacked or intercepted by projectiles.

Shadowstep retains a 12-second base cooldown; an attributed enemy kill within four seconds of a successful teleport grants one reset for that cast. Failed teleports, protected allies/pets, ownerless kills, and canceled/revived deaths do not grant a reset. The reset is deferred one tick so an immediate Ambush kill cannot be overwritten by the cast applying its cooldown.

Tracking disruption is an arrival-centered mob effect. It clears the native target and suppresses LivingChangeTargetEvent acquisition while present; native targeting resumes after expiry. The old global caster tracking status is not applied.

### Second passive pair - Sidestep OR Opportunist

| Attribute | Sidestep | Opportunist |
| --- | --- | --- |
| Trigger / chance | Combat roll / **100%** | Combat roll / **100%** |
| Effect | **20 percentage points evasion**, no stacking | Next melee hit has guaranteed critical chance; **no added critical damage** |
| Duration | **2 s**, refreshed by another roll | Retained **5 s** ready window |
| Consumption | Does not disappear on damage | Consumed by the native next-melee-hit stash |
| Base cooldown | **None**, roll recharge still applies | **6 s**, haste affected |

## Tier III - Vanish OR Mutilate

| Attribute | Vanish | Mutilate |
| --- | --- | --- |
| Cast / cooldown | Instant / **8 s**, haste affected | Native attack animation / **12 s**, haste affected |
| Main effect | **7 s Stealth**, -50% base movement speed | **100% D per enemy**, one combined dual-wield hit |
| Delivery | Self; normally breaks on attacks, other casts, item use and incoming attributed hits | Equipped melee reach, **160-degree sweep**; damage lands halfway through attack animation, not after a fixed 0.5 s |
| Improved/root | **Improved Vanish:** reduces movement penalty to **-40%**, no added duration | **Improved Mutilate:** cooldown -2 s, **10 s** before haste |
| Branch A | **Stealth Speed:** another 20 percentage points less movement penalty, leaving **-20%** with Improved | **Crimson Strikes:** heals **15% D once per successful Mutilate cast**, regardless of enemies struck |
| Branch B | **Shadow Stealth:** actions and taking hits no longer cancel Stealth; still expires after **7 s** and can be explicitly dispelled | **Bloody Strikes:** applies the existing Rogue Bleed for **10 s**, replacing Poison |

Mutilate uses the native full-strength player attack with no additional damage bonus, combining main-hand power and the calculated off-hand contribution. Crits, enchantments and defenses still apply. Bloody Strikes and Serrated Traps apply the same flat, single-level Bleed. Every 25 ticks / 1.25 seconds it deals 1 raw HP while stationary or 4 raw HP with horizontal movement, regardless of D or old amplifier values. Vertical movement alone does not increase it. Reapplication can refresh duration but cannot add levels. Serrated lasts 2.5 seconds; Bloody Strikes lasts 10 seconds. Native defenses still apply. Tick damage remains ownerless magic, so the player/summon-attributed 35% PvP modifier does not apply. This is separate from Mutilate's initial strike. Vanish's native mob tracking reduction remains unchanged.

## Third passive pair - Cheat Death OR Prepared Counter

| Attribute | Cheat Death | Prepared Counter |
| --- | --- | --- |
| Trigger / chance | Qualifying fatal incoming hit / **100%** | Evade an enemy attack / **100%** |
| Effect | **3 s damage immunity**, **Speed III for 3 s**, **Jump Boost III for 3 s**, **+30 evasion percentage points for 6 s** | Ready one basic melee hit that adds **15% D**; no immediate retaliation or cooldown reset |
| Base cooldown | **60 s**, haste affected | **2 s**, haste affected |
| Limits | Existing fatal-hit trigger requires an attributed attacker; no heal | Charge stays until used or dispelled; no stacking. Melee skills/spells cannot consume it. Party allies and owned pets remain protected; attacker-less evasion cannot ready it |

### Status defaults checked in installed bytecode

| Effect | Native registration |
| --- | --- |
| Slice & Dice | No attack-damage modifier; status levels represent remaining charges |
| Slice Evasion | spell_engine:evasion_chance, +0.05 ADD_MULTIPLIED_BASE per level, cap ten |
| Shock | movement_speed and jump_strength, -1 ADD_MULTIPLIED_BASE; STUN action impairment |
| Fleet Footed | movement_speed, +0.03 ADD_MULTIPLIED_BASE per level, cap twelve |
| Smoke Screen | spell_engine:evasion_chance, +0.5 ADD_MULTIPLIED_BASE |
| Fracture | minecraft:armor, -2 ADD_VALUE per level plus -0.05D per level from runtime snapshot |
| Sidestep | spell_engine:evasion_chance, +0.2 ADD_MULTIPLIED_BASE; fixed single level |
| Ambush | Legacy unused next-hit status retains +0.5 attack_damage; current Ambush instead performs the immediate 40% impact above |
| Opportunist | critical_strike:chance +1 ADD_MULTIPLIED_BASE; no critical-damage modifier; consumed by the stashed melee trigger |
| Cheat Death | spell_engine:damage_taken, -1 ADD_MULTIPLIED_TOTAL; custom CheatDeathStatusEffect also applies |
| Stealth / Improved Stealth / Stealth Speed | movement_speed, -0.5 / +0.1 / +0.2 ADD_MULTIPLIED_BASE; seven seconds each |
| Cheat Death Evasion | spell_engine:evasion_chance, +0.3 ADD_MULTIPLIED_BASE; six seconds |

Evasion's native base is 100; its chance conversion is (attribute - 100) / 100. Thus Smoke Screen's base-scaled +50 attribute gives +50 percentage points at the native base. Other sources, server angle restrictions, casting/item-use restrictions and damage eligibility can affect whether an attack is evaded.

Verification scope: archive/resource extraction, hidden reward mapping, default schema fields and installed effect/engine bytecode inspection. The isolated Rogue fixtures additionally exercise implemented combat behavior; see docs/patches/rogue-tier-three/README.md for tier III results and limits.

Latest follow-up: [Crimson Strikes heals once per successful cast](../crimson-laststand/README.md).

Latest balance: [Rogue dual-power scaling](../rogue-dual-balance/README.md). Rogue offensive schools, poison scaling and Shock Powder's mob threshold now use D. Ordinary RPG Rogue attack-speed nodes grant 1%; Adventurer ordinary attack speed grants 2%.

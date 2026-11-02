# Mage Offensive Damage Scaling Plan

**Status:** Implemented and installed in Phase 1 on 2 November 2026. [Current values, builds and verification](../../patches/combat-phase-one/README.md). The formulas and comparison below describe the approved design and its limits.

**Batch assignment:** This damage pass belongs to **Phase 1** of the [two-phase combat rework](2026-11-01-combat-rework-two-phase-handoff.md). Finish its verification and record final archive hashes before Phase 2 changes share those JARs.

**Goal:** Reduce the Arcane, Fire and Frost mage lineup's offensive scaling to approximately 80% of its current values, with clean displayed percentages. Preserve the separately balanced class passives.

**Related plan:** [Echoing Blast charges and mage cooldown/passive adjustments](2026-11-01-echoing-blast-charges.md). This damage pass supersedes that plan's former 260% ordinary Explosion and 160% Echoing values; their new planned values are 210% and 130%.

## Confirmed scope

- Multiply offensive scaling by **0.8**, not subtract 20 percentage points.
- Cover all three mage schools' active skills, their offensive upgrade effects, wand/staff basic spells and Arcane Emitter/Hydra/Frost Elemental attacks. The user explicitly confirmed weapon spells and summons.
- Exclude the separate school passive nodes. Fissile Magic, traps, wards and other passive damage/healing/buffs do not receive this damage multiplier. Their previously requested cooldown/Ward adjustments remain in the related plan.
- The user confirmed an enchantment comparison using four Sunfire V or Soulfrost V armor pieces plus Spell Power V on the weapon.

## Rounding recommendation

Use the original effective **per-impact percentage**, after channel normalization and any explicit branch override:

1. Multiply by 0.8.
2. If the original is **100% or more**, round to the nearest **5 percentage points**. This gives the requested fives/tens without an unnecessarily coarse universal ten-point step.
3. Below 100%, round to the nearest **whole percentage point**.
4. Resolve exact ties upward. Avoid language/runtime banker's rounding.

Examples: 360% → 288% → **290%**; 260% → 208% → **210%**; 180% → 144% → **145%**; 26% → 20.8% → **21%**; 14% → 11.2% → **11%**; 18% → 14.4% → **14%**.

This deliberately produces an approximate 20% reduction. For example, 13% becomes 10% (about 23.1% less), while 16% becomes 13% (18.75% less). These are consequences of clean small percentages, not errors to compensate with hidden multipliers.

Set the real per-hit coefficient to the rounded target; do not merely round the description while retaining a different value. Engine coefficients may still use decimals internally. For channels, derive the raw coefficient from the rounded effective hit: Missiles' proposed 21% per missile requires raw coefficient 0.63 under its existing 4/12 normalization. Full-channel totals follow the actual hit count; do not independently round the total and change the damage again.

Preserve smooth internal ramps such as Charged Beam. Show whole-number start/end values instead of exposing fractional growth steps. Actual HP damage remains floating-point; the request concerns skill scaling descriptions, not rounding every health change to whole HP.

## Installed offensive values

Percentages below use the caster's matching power unless marked **summon's power**. They are base/branch impact coefficients, before criticals, defenses and existing player-source damage retention. Existing multiplicative upgrade benefits remain and apply to the newly reduced coefficient; do not also nerf a +10% power upgrade to +8% and thereby reduce it twice.

### Arcane

| Skill / damage path | Current effective percentage | Planned percentage |
| --- | ---: | ---: |
| Arcane Missiles, each base missile | 26% | **21%** |
| Arcane Explosion, ordinary / Chain main blast | 260% | **210%** |
| Echoing Blast main explosion | 160% | **130%** |
| Chain Detonation secondary explosion | 30% | **24%** |
| Arcane Beam, each ordinary hit | 20% | **16%** |
| Charged Beam, per-hit start → cap | 10% → 15% | **8% → 12%** |
| Arcane Barrage, each emitter bolt | 40% of summon's power | **32% of summon's power** |
| Burst Blink, each departure/arrival explosion | 50% | **40%** |
| Arcane Bolt, wand | 70% | **56%** |
| Arcane Blast, staff | 80% | **64%** |

Evocation deals no direct damage: retain its buffs/drawbacks. Arcane Charge, Exposure and Presence of Mind retain their percentages; reducing both the spell and its bonus would compound the reduction and violate the passive exclusion.

### Fire

| Skill / damage path | Current effective percentage | Planned percentage |
| --- | ---: | ---: |
| Fire Breath, each hit | 13% | **10%** |
| Flame Slash, each impact | 100% | **80%** |
| Meteor main impact | 150% | **120%** |
| Explosive Breath damaging follow-up | 30% | **24%** |
| Meteor Splash, each cloud impact | 10% | **8%** |
| Firestorm, each normalized hit | 40% | **32%** |
| Wall of Flames, each impact | 20% | **16%** |
| Hydra Fireball | 55% of summon's power | **44% of summon's power** |
| Ancient Hydra breath, each normalized hit | 24% of summon's power | **19% of summon's power** |
| Scorch, basic weapon spell | 60% | **48%** |
| Fireball, basic weapon spell | 80% | **64%** |
| Pyroblast, basic staff spell | 100% | **80%** |

Healing Flames remains healing: retain its healing amount and friendly-target path. Do not mechanically change a shared helper field if the engine interprets it as healing. Native burning duration, immunity rules and fixed vanilla fire ticks remain; this pass adjusts power-based offensive scaling, not global fire damage. Inspect any custom power-scaled damage-over-time separately so it receives the reduction once.

### Frost

| Skill / damage path | Current effective percentage | Planned percentage |
| --- | ---: | ---: |
| Frost Nova main impact | 180% | **145%** |
| Frost Spikes, ordinary eligible impact | 80% | **64%** |
| Frost Fan calibrated branch impact, Improved already included | 25% | **20%** |
| Ice Lance direct hit | 200% | **160%** |
| Shattering Lance splash | 50% | **40%** |
| Frost Nova's Frost Splinters delayed splash | 20% | **16%** |
| Blizzard, each normalized shard impact | 26⅔% | **21%** |
| Elemental Frost Shard | 100% of summon's power | **80% of summon's power** |
| Elemental / Legion Super Nova | 180% of summon's power | **145% of summon's power** |
| Frostbolt, basic weapon spell | 80% | **64%** |
| Player/weapon Frost Shard helper, wherever assigned | 60% | **48%** |

Frost Shield's protection/expiry freeze and all freeze/slow durations remain. Frost Elemental summon forms retain their health, armor, size, lifetime and power inheritance. Reduce the melee damage scaling once: base 50% → **40% of caster Frost power**, Legion 40% → **32%**, Colossus 100% → **80%**. Preserve outside Companion-node bonuses; those are separate Adventurer rewards, not another mage coefficient to nerf.

### Classification safeguards

Chain Detonation, Explosive Breath, Meteor Splash and Frost Splinters are active-skill upgrades even where the implementation uses `PASSIVE` triggers. Include their offensive damage. Exclude the actual school passive pairs by explicit node identity, not by blindly including/excluding every JSON `type: PASSIVE` entry.

Audit damage overrides, custom handlers, delayed impacts, clouds, calibrated upgrade branches and summon-only spell clones. The tables are the inspected starting ledger; compare all reachable mage damage paths during implementation before declaring coverage complete. A branch with an explicit coefficient override must get its own rounded target, not retain the old override over a reduced base spell.

## Enchantment calculation

### Verified installed values

Inspected `mods/spell_power-neoforge-1.6.1+26.2.jar`:

- `sunfire.json`: **3% base Arcane and Fire Power per level per armor piece**, max level V. Four level-V pieces add **60%** to each matching school's base multiplier.
- `soulfrost.json`: the same **3% per level** for Frost/Soul. Four level-V pieces add **60%** Frost's base multiplier.
- `spell_power.json`: **5% generic power per level**, main hand, max V: **25%** at V.
- `energize.json` affects Healing/Lightning, not the Arcane/Fire/Frost comparison.

The armor enchantments do not each multiply final damage independently. Their school modifiers add to other base multipliers. Furthermore, installed `SpellPower.getSpellPower()` adds a generic-power contribution based on flat school modifiers; it does not simply multiply the complete final school result by 1.25. `getAttributeFlatValue()` sums `ADD_VALUE` modifiers and excludes the attribute's innate base value.

Therefore “four armor pieces plus weapon” is not automatically `1.15⁴ × 1.25`, or even a universal final `×1.85` for every character. Existing school modifiers, total multipliers, flat gear power and generic modifiers matter.

### Controlled maximum-enchantment comparison

For illustration, keep the same gear with **1 innate + 19 flat gear power**, no total-multiplier modifiers and otherwise default generic power. Change only the specified enchantments. Let the other school base-multiplier bonuses vary:

| Other school bonuses | Power without these enchantments | Power with armor V ×4 + weapon V | Damage after ×0.8, versus unenchanted | Reduction needed for exact old strength |
| --- | ---: | ---: | ---: | ---: |
| None | 20 | 36.75 | **147%** of old damage | **45.58%** |
| +50% | 30 | 46.75 | **124.67%** | **35.83%** |
| +100% | 40 | 56.75 | **113.50%** | **29.52%** |
| +150% | 50 | 66.75 | **106.80%** | **25.09%** |
| +235% | 67 | 83.75 | **100%** | **20%** |

Example calculation: with no other school bonuses, armor adds 60% of 20 = 12 power, and Spell Power V adds 25% of the 19 flat gear power = 4.75. New power is 36.75. A 20% coefficient reduction leaves 29.4 effective power, compared with the old 20: **47% higher**, not equal. The earlier simplified “about 48% higher” estimate is the flat-power-dominant approximation; this table includes the installed default innate point.

These are calculated scenarios, not measurements of the user's actual build. Armor-only at this same 20-power example would give 32 power; ×0.8 leaves **28% more damage**, requiring **37.5%** reduction for exact compensation when no other school multipliers exist.

### What can be concluded

- Multiplying damage by **0.8 exactly compensates a 25% final increase** in effective power, before rounding.
- Four armor V enchants plus weapon V can exceed that increase. The stronger the character's existing additive school bonuses, the smaller the enchantments' relative increase tends to be.
- An exact build-specific multiplier is **power without those enchantments ÷ power with them**, comparing otherwise identical gear, allocations and buffs. Use the actual engine result, not tooltip sums.
- Critical/critical-damage and haste enchants can further increase burst or sustained output; the table does not claim to cancel those. Passive damage also remains empowered by school power because the user excluded passives.

**Recommendation:** retain the requested **approximately 20%** clean-coefficient pass as the initial universal change, while treating “back to pre-enchant strength” as a comparison target to validate on actual early/mid/endgame fixtures. Do not silently replace it with a 35–46% universal nerf based on a low-bonus example. If exact enchanted/unenchanted equality across builds is required later, that needs a different, build-dependent policy; one fixed coefficient multiplier cannot accomplish it universally.

## Implementation and verification plan

- [ ] Export a ledger from the installed Wizards/Skill Tree/Spell Engine data and runtime overrides, grouped by user-facing active, weapon, owned summon and excluded passive. Confirm all table entries and record exact archive hashes before editing.
- [ ] Add failing effective-impact tests, including normalized channels, Echoing/Charged overrides, delayed upgrade damage, calibrated Frost Fan and summon-only clones. Keep a control fixture for each excluded school passive and healing branch.
- [ ] Apply the rounded targets at the layer responsible for each coefficient. Do not apply a global magic/player multiplier, reduce school attributes or nerf enchantments: those would also affect excluded passives, healing, other classes and derived stats.
- [ ] Reduce each summon attack once. Keep its power inheritance and its bolt/area coefficient from both receiving independent ×0.8 reductions. Retain player attribution and existing 35% PvP / 80% PvE damage policy; do not apply those multipliers a second time.
- [ ] Retain damage-buff multipliers, critical rules, projectile counts, cast durations, radii and non-damage effects except changes explicitly approved in the related plan. Verify no vanilla mobs/non-owned summon users or Priest/Paladin spells inherit this mage-only adjustment.
- [ ] Measure identical-build enchanted and unenchanted effective power through the installed Spell Power API. Test school armor alone, weapon alone and both; include outside base/total modifiers and Arcane Charge. Compare non-critical single-hit damage separately from full-channel totals and haste-driven DPS.
- [ ] Update real tooltip values using the established concise description style. Show rounded coefficients and ramp endpoints; keep raw normalization formulas out of player-facing text. No misleading whole-number description over an unrounded coefficient.
- [ ] When implementation is requested, rebuild same-filename final JARs, run targeted and existing regression suites, validate ZIP changes and update raw class references, current patch notes, handbook and PDF as required. Preserve unrelated work and ignored backups. No live installation or push is authorized by this planning request.

## Acceptance

All covered mage offensive paths use their rounded 80% targets once, including weapon spells and owned class summons. The separately balanced passive nodes and healing amounts remain unchanged. Enchantment comparisons clearly distinguish an approximate universal reduction from actual build-specific pre-enchantment equivalence. These values are installed in the repository; no personal/live server or GitHub push was performed.

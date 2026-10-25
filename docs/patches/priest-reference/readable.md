# Priest skills - raw attributes and nodes

Extracted from the installed pack on **25 October 2026**. This is an inspection, not a balance change. Priest is supplied by Paladins & Priests; it is separate from that mod's Paladin class.

## Reading the values

- **P = caster Healing Power.** Both Priest healing and its holy damage use the Healing school unless a raw impact specifies another source. Percentages below are spell coefficients, before criticals, recipient modifiers, defenses and current pack damage scaling.
- **20 ticks = 1s; 2 HP = 1 heart.** User tiers I/II/III correspond to active resource tiers 2/3/4. Tier-0/1 weapon/book spells are listed separately.
- Cast time and cooldown haste scaling default to enabled. An omitted cooldown duration is **0s**; an omitted trigger chance is **100%**. Lightwell explicitly disables cooldown haste scaling.
- Proportional cooldowns scale with channel completion; the listed resource cooldown is the full-completion value before haste, not a fixed tap cooldown.
- ADD status effects start at level one and cap at serialized amplifier cap + 1. Further successful applications refresh duration by default. Healing Focus's cap 4 means five stacks; Incanters' Cadence's cap 2 means three.
- Healing targeting follows the pack's shared caster/party eligibility rules. Harmful impacts protect party players and protected pets. Raw data describes potential impacts, not permission to bypass these checks.
- Registered effect defaults are inspected from active Java classes. A separate server's generated effect configuration may override defaults; this report does not read or alter personal installations.

## Skill pairs by tier

| Tier | Holy choice | Discipline choice | Passive pair |
|---|---|---|---|
| I | Holy Light | Levitate | Healing Focus / Incanters' Cadence |
| II | Circle of Healing | Penance | Fade / Divine Favor |
| III | Lightwell | Barrier | Pain Suppression / Celestial Orbs |

## Tier I - Holy Light / Levitate

| Attribute | Holy Light | Levitate |
|---|---|---|
| Cast | Channel 5s, 25 deliveries | Channel 1.5s, 4 deliveries; movement disabled during channel |
| Cooldown | 10s at full completion, proportional | 24s at full completion, proportional |
| Range | 32-block beam | Self; Uplift adds a 2-block secondary area |
| Each delivery | Heal eligible allies for 40% P; damage eligible enemies for 80% P, knockback 0.5 | Reset velocity and push upward by 0.15; apply Levitate for 5s |
| Full-channel coefficient total | 1,000% P healing or 2,000% P damage if every delivery lands | No damage or direct healing |
| Levitate effect | Not applicable | -99% gravity; grants Slow Falling for 3s on natural expiry |
| Resource | Healing Stone item cost; 0.2 exhaustion | Healing Stone item cost; default exhaustion |

The raw Levitate push has `frame: LOOK`; it is not documented here as a guaranteed fixed world-space lift distance. Repeated applications refresh the effect rather than adding five seconds per pulse.

| Node | Holy Light | Levitate |
|---|---|---|
| Improved | **Improved Holy Light:** cooldown reduced by 2s, to 8s full completion | **Improved Levitate:** applied effects last 2s longer; Levitate becomes 7s |
| Branch A | **Graceful Channeling:** 2 additional deliveries, 27 total | **Uplift:** applies Levitate impacts to eligible allies within a 2-block impact area |
| Branch B | **Searing Light:** +10% spell power for this spell; appends ignition (default 2s plus the engine's 10-tick fire offset) | **Serenity:** each delivery adds 20% incoming-damage reduction, up to 4 stacks / 80%, for 6s, refreshed |

Searing Light changes source power for the spell, so the modifier also affects its healing coefficient, despite the upstream description mentioning damage only. Improved Levitate's general effect-duration modifier also adds 2s to Serenity when combined, making 8s. Serenity uses the Spell Engine damage-taken attribute, not the vanilla Resistance potion. The coefficients shown for full channels are arithmetic totals, not a runtime DPS claim.

### Tier-I passive pair

| Attribute | Healing Focus | Incanters' Cadence |
|---|---|---|
| Trigger | An active spell's healing impact | An active spell's damage impact |
| Chance | 100% | 50% per qualifying impact |
| Recipient | The healed target, not automatically the caster | Caster |
| Per stack | +5% healing received through Spell Engine | +5% spell haste |
| Maximum | 5 stacks / +25% | 3 stacks / +15% |
| Duration | 5s, refreshed | 8s, refreshed |
| Cooldown | No configured cooldown | No configured cooldown |

Healing Focus does not add outgoing Healing Power. Its attribute is `spell_engine:healing_taken`. Incanters' Cadence modifies `spell_power:haste`. Neither passive's trigger filter requires a particular school; both require an ACTIVE spell and their stated impact type.

## Registered effect defaults

| Effect | Exact default |
|---|---|
| Healing Focus | `spell_engine:healing_taken` +0.05 per level, ADD_MULTIPLIED_BASE |
| Incanters' Cadence | Spell haste +0.05 per level, ADD_MULTIPLIED_BASE |
| Serenity | `spell_engine:damage_taken` -0.20 per level, ADD_MULTIPLIED_BASE |
| Divine Favor | Spell critical chance +1.0, ADD_MULTIPLIED_BASE; upstream describes a guaranteed critical |
| Pain Suppression | `spell_engine:damage_taken` -0.50, ADD_MULTIPLIED_BASE |
| Hysteria | Attack speed, ranged haste and spell haste +0.08 per level, ADD_MULTIPLIED_BASE |
| Chastise | `spell_engine:damage_taken` +0.10 per level, ADD_MULTIPLIED_BASE |
| Levitate | Gravity -0.99, ADD_MULTIPLIED_TOTAL; natural expiry applies 60 ticks Slow Falling |
| Priest Absorption | Maximum absorption +2 HP per effect level, ADD_VALUE; raw spell data below defines levels/scaling |

## Extraction scope

The following sections contain all six class actives, eighteen upgrades, six passives, Priest base/weapon spells and helper spells (including Holy Mote and Lightwell Cleanse), both class/weapon tree definitions, placements, relevant connections and source hashes. Presentation assets are omitted from JSON; every mechanical field is retained. Tables for later tiers are generated as raw paired data to avoid hiding cooldown, summon, area or status details.

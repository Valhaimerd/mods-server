# Priest skills - raw attributes and nodes

Inspected and updated **26 October 2026**, including the [Priest rework](../priest-rework/README.md). Priest is supplied by Paladins & Priests, separately from Paladin.

## Reading the values

- **P = Healing Power.** Priest holy damage also scales from Healing Power. Coefficients precede criticals, defenses, recipient modifiers and pack damage scaling.
- **20 ticks = 1s; 2 HP = 1 heart.** Resource tiers 2/3/4 correspond to user tiers I/II/III.
- Native haste affects casts and cooldowns unless explicitly disabled. Holy Light smoothly scales 12-24s and Penance 8-12s by completion. Ascension has a fixed 16s base cooldown. Lightwell now has an 80s haste-affected base cooldown; its 0.8s firing interval also uses inherited haste. Fixed buff durations remain unchanged.
- Better Party eligibility protects caster, party players and protected pets. Each explosion recipient is classified separately for damage or support.
- Raw JSON alone cannot express all behavior. PriestRules implements coefficient overrides, mixed explosions, shields, pulse timing and rounded regeneration; source archive hashes appear below.
- Spell IDs remain stable. Penance upgrades retain their original tier_3 IDs but occupy tier I. Ascension upgrades retain tier_2 IDs but occupy tier II. Complete branches move in both tree categories.

## Skill pairs

| Tier | Holy | Discipline | Passive pair |
|---|---|---|---|
| I | Holy Light | Penance | Healing Focus / Incanters' Cadence |
| II | Circle of Healing | Ascension | Fade / Divine Favor |
| III | Lightwell | Barrier | Pain Suppression / Celestial Orbs |

## Tier I - Holy Light / Penance

| Attribute | Holy Light | Penance |
|---|---|---|
| Cast | Channel 10s, 25 deliveries | Channel 5s, 10 bolts, one every 0.5s |
| Cooldown | Smooth 12-24s by completion | Smooth 8-12s by completion |
| Range | 32-block beam | 26-block projectile flight; 4-block explosion radius |
| Healing | 10% P per delivery | No direct healing |
| Damage | 20% P per delivery | 15% P per explosion; 150% total if ten hit |
| Knockback | 1.0 | 0.5 |
| Support | Healing allies | Adds 1 current absorption HP per ally per blast; ten stacks / 10 HP cap; lasts 3s, refreshed |

Penance explodes once on ally, enemy or block contact. No additional direct-hit damage or distance falloff. Each ally inside receives its own stack. Contact with a living enemy or ally also grants the caster absorption and selected Hysteria outside the blast, once per bolt; inside-radius casters are not duplicated. Terrain-only impacts do not grant remote support; killing hits do. Absorption adds only newly earned HP: 6 HP minus 4 damage plus two bolts equals 4 HP. Capped applications refresh time without refilling.

| Node | Holy Light | Penance |
|---|---|---|
| Improved | Healing 15% P; damage 25% P; no cooldown reduction | Damage 20% P; absorption lasts 4s |
| Branch A | Graceful Light: healing 20% P, knockback 2.0; no extra deliveries | Hysteria: +1% attack speed, ranged haste and spell haste per stack; ten stacks / 10%; 4s refreshed |
| Branch B | Searing Light: damage 35% P and ignites enemies | Chastise: enemies take +1% damage per stack; ten stacks / 10%; 4s refreshed |

### First passive pair

| Attribute | Healing Focus | Incanters' Cadence |
|---|---|---|
| Trigger | Active-spell healing impact | Active-spell damage impact |
| Chance | 100% | 100% |
| Recipient | Healed target | Caster |
| Bonus | +4% healing and absorption received per stack; five stacks / 20% | +2% spell haste per stack; five stacks / 10% |
| Duration | 6s refreshed | 6s refreshed |
| Stack interval | 0.25s base, haste affected | 0.25s base, haste affected |

## Tier II - Circle of Healing / Ascension

| Attribute | Circle of Healing | Ascension |
|---|---|---|
| Cast | 0.5s | Stationary 2s channel; pulses at 0.5/1/1.5/2s before haste |
| Cooldown | 16s | 16s, no completion-based extension |
| Range | 8-block healing radius | 8-block support and knockback radius |
| Healing | 50% P to eligible allies, including caster | None |
| Damage | None | None |
| Support | Healing | Levitate 6s; each pulse grants +2% melee, ranged and offensive magic power; four stacks / 8%; 6s refreshed |
| Hostile knockback | None | 2.0 on pulses one-three; 5.0 on pulse four |

| Node | Circle of Healing | Ascension |
|---|---|---|
| Improved | Healing becomes 60% P; does not boost source power for other effects | Levitation and associated buffs last 8s |
| Branch A | Mass Dispel: removes up to two harmful effects, preserving Trial Omen | Uplift: lifts eligible allies within 3 blocks |
| Branch B | Sanctuary: Regeneration for 5s, level 1 plus rounded 6% P; P27 gives Regeneration III | Serenity: absorption per pulse equals 1 plus rounded 10% P; four stacks; 6s/8s refreshed; replaces damage reduction |

Serenity adds only newly earned shield HP, never refilling from total stack capacity. Levitation reduces gravity by 99%, followed by 3s Slow Falling on natural expiry. Ascension's power-buff icon reuses Levitate artwork; absorption buffs reuse existing shield artwork.

### Second passive pair

| Attribute | Fade | Divine Favor |
|---|---|---|
| Trigger | Combat roll | Combat roll |
| Chance | 100% | 100% |
| Effect | Clears aggression within 15 blocks; full invisibility including armor and held items for 2s | Readies a counter: the next qualifying damaging attack stuns its attacker and eligible enemies within 2 blocks of the Priest for 1s |
| Duration | Concealment 2s | Readiness 15s |
| Cooldown | 6s before haste | 15s before haste |

## Tier III - Lightwell / Barrier

| Attribute | Lightwell | Barrier |
|---|---|---|
| Cast | Instant | 1s |
| Cooldown | 80s before haste | 60s before haste |
| Range | 16 blocks from the well | 10-block radius, native 10-block vertical extent |
| Lifetime | 16s active plus 1s spawn/despawn phases | 10s |
| Main behavior | Unattackable, unpushable, no-collision well follows above caster head | Stationary barrier repeatedly pushes enemies outward and protects current allies inside |
| Power/healing | Exactly 5% caster Healing Power inherited; each mote heals 100% own power | No direct healing without Sacred Refuge |
| Motes | Two per cycle; speed 0.6, 0.8s base firing interval with inherited haste | No motes |
| Targeting | Injured party allies/pets first, lowest raw HP; two different injured recipients if available, otherwise both motes to the sole injured ally; useful full-health upgrade targets only when no injured target | Caster/party allies/protected pets only |
| Protection | Not applicable | Ordinary combat/DoT damage events, including poison/Bleed/fire; excludes void, admin kill and direct skill health costs |

| Node | Lightwell | Barrier |
|---|---|---|
| Improved | +4s active lifetime, total 20s | +2s duration, total 12s; no cooldown reduction |
| Branch A | Cleansing Light: one harmful effect removed per allied mote hit, no separate cleanse cooldown; Trial Omen excluded | Sacred Refuge unchanged: separate 4-block/10s cloud, 10% P healing and cleanse every 2s |
| Branch B | Empowered Well: +1 maximum heart per mote hit, 2-stack cap per recipient across wells, 6s refreshed; unfilled, normal mote healing fills capacity | Deflective Shield: intercepted hostile projectiles retaliate once against the attacker for 25% P native holy damage |

Empowered Well applies capacity before healing. Cap refreshes do not directly heal or add HP. Expiry/removal clears only its own maximum-health modifier and clamps excess current HP. Motes do not bounce. Orphan wells disappear. Barrier uses current volume checks instead of a lingering immunity lease; friendly projectiles and square-corner paths are not intercepted.

### Tier-III passive pair

| Attribute | Pain Suppression | Celestial Orbs |
|---|---|---|
| Trigger | Current health checked every 0.25s | Critical active spell impact |
| Chance | Always active while equipped | 100% when eligible |
| Effect | Priest takes 6% / 12% / 18% / 24% less damage below 50% / 40% / 30% / 20% of current maximum HP | Five retaliatory orbs on caster |
| Duration | Tracks current health; weakens or clears after healing | 15s readiness |
| Cooldown | None | None configured |
| Retaliation | None | 30% P per orb, knockback 0.1; one orb when caster or eligible ally within 8 blocks of caster takes attributed hostile damage, including absorbed hits |
| Presentation | Original effect icon, particles and sound retained; activation FX play on entering or strengthening protection | Original orb icon, orbiting visuals, activation and impact FX retained |

Health boundaries are strictly below each threshold. Exactly 50% has no protection; below 20% grants the full 24%. Maximum-HP changes affect the next check. Removing the passive or dying clears the protection. Healed recipients no longer receive Pain Suppression. Ally retaliation includes party members and owned/party pets, excludes unrelated entities and friendly attackers, and never triggers other ready buffs. Own-hit retaliation stays native and consumes only one orb. See [the rework and verification](../priest-low-health/README.md).

Healing Focus remains a received-healing/absorption attribute, not outgoing Healing Power.

## Absorption follow-up

The Priest, Wizard, Arsenal and Tenacity shield-start handlers add only positive level differences. Existing-effect refreshes do not refill consumed HP. Removal/expiry clears tracking so genuinely new shields can grant normally. New Priest shields implement explicit current-HP increments and caps. The raw reference is supported by the patch's isolated runtime checks and original [audit](absorption-audit.md).

## Extraction scope

Six class actives, eighteen upgrades, six passives, eight base/weapon/helper resources, both tree definitions, placements/connections and source hashes. Presentation fields alone are omitted; all serialized mechanical fields remain below.

Latest revision: [Priest passive tiers I/II](../priest-passives/README.md). Healing Focus applies to newly granted absorption from native and custom shield effects. It never increases previously consumed shield HP. Existing shields are not retroactively refilled. Divine Favor no longer increases critical chance or consumes on spell casts. The targeted attacker is stunned even outside the two-block secondary radius; party/pet protection remains.

Latest tuning: [Holy Staff and Priest tuning](../priest-tuning/README.md). Holy Staff Specialisation now gives Holy Shock +5% damage only, without its former three-second cooldown reduction or any healing bonus. Lightwell picks two motes together each native cycle, reevaluating current raw HP and eligible support needs. Earlier coefficients in historical patch records are superseded by the current tables.

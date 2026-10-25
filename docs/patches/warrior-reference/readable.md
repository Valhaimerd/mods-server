# Warrior skills - raw attributes, secondary nodes and passive pairs

Inspected **25 October 2026** from the currently installed pack. Includes all three Warrior tiers, the revised health passives and independent shield damage reduction. Warrior shares the Rogues & Warriors JAR, but its spell and node IDs are separate from the Rogue class.

## Reading the values

- **M** = caster physical-melee spell power / attack damage before criticals, target defenses, vulnerabilities and pack PvP retention. **H** = health-school power, sourced from maximum health. Percent attack-damage attributes modify base attack damage unless stated otherwise.
- **20 ticks = 1 second; 2 HP = 1 heart.** User tiers I/II/III correspond to internal active tiers 2/3/4. Each active has an improved/root node and two alternative branches. Passive pairs are separate choices, not additional active skills.
- Verified omitted engine defaults: standard cast is instant, cast/cooldown haste scaling enabled, cooldown nonproportional; damage coefficient 100%, knockback 1; projectile launch velocity 1, steering 1 degree/tick, ricochet search radius 5; status duration 10s, SET, amplifier 0, refresh true. Default resource costs are 0.1 exhaustion and one durability unless overridden. Defaults matter only when the corresponding mechanic is present.
- Charges use a linear curve and permit release after 20% completion by default. Let q be charge completion from 0 to 1. Throw Weapon and Throw Net damage interpolate from a fixed minimum at earliest release to their maximum at full charge; the native output multiplier is disabled for these two spells. Range bonuses scale with q. Status durations are not multiplied by this charge output in the installed status-impact path.
- Cooldowns/cast times below are before native haste. All Warrior active casts and active/passive cooldowns explicitly enable haste scaling. Melee-school haste uses the native attack-speed/spell-haste sources; fixed buff durations do not automatically shrink with haste.
- ADD effects with increment 1 start at one level; serialized cap 2 gives three levels. Successful applications refresh duration unless explicitly disabled. Missing chance means 100%.
- Shared acquisition/final-impact guards protect the caster, party players and player-owned pets. Throw Weapon no longer applies Shatter. Net retains its maximum-HP gate, but type/boss immunity cannot reject a qualifying net. Head Throw uses the universal stun policy.
- Effects below are installed registration defaults. Reworked effect registrations override stale generated settings for their revised attributes. Personal Minecraft/server configurations were not modified or treated as pack defaults.
- Scope: six Warrior actives, eighteen secondary nodes, six class passives, registered effect attributes and exact Warrior class-tree grants. Separate sword/axe/hammer/shield weapon trees are not part of these class pairs.

## Three active tiers

| Tier | Fury choice | Protection choice |
| --- | --- | --- |
| I | Throw Weapon | Throw Net |
| II | Wild Charge | Demoralizing Shout |
| III | Berserker | Last Stand |

## Tier I - Throw Weapon OR Throw Net

| Attribute | Throw Weapon | Throw Net |
| --- | --- | --- |
| Full charge | 0.5s | 0.5s |
| Base cooldown | 5s | 6s |
| Minimum / full-charge damage | 40% / 80% M | 7.5% / 15% M |
| Range | 12-24 blocks | 12-24 blocks; Improved reaches 30 |
| Speed / homing | 1.0; 2 degrees per tick | 1.0; 2 degrees per tick |
| Enemy effect | Slowness I for 1s | Root for 2s, maximum HP at most 100 + 2M |
| Caster tradeoff | Weakness IV for 2s after launch; projectile retains pre-Weakness power | None |
| Extra behavior | No block bounce or base ricochet | Pulls eligible enemies rapidly toward 2 blocks in front of the caster, stopping at solid obstructions |

Charge damage rises linearly between earliest release and full charge; releasing early never drops below the listed minimum. Status duration is fixed. Net damage is independent of its root/pull HP gate. Party members and owned pets are protected from both damage and control. Net type immunity is bypassed after the HP gate passes. Haste reduces casts and cooldowns, including other Warrior tiers.

### Tier-I secondary nodes, by pair

| Position | Throw Weapon | Throw Net |
| --- | --- | --- |
| Improved/root | **Improved Throw Weapon:** 50%-90% M from earliest release to full charge | **Improved Throw Net:** +6 charged range; actual projectile flight reaches 30 blocks |
| Branch A | **Strong Throw:** pierces once for two enemy hits total, seeking the next unhit enemy within 24 blocks with 2-degree steering; no ricochet | **Bigger Net:** same damage, root and pull within 5 blocks of impact, with per-enemy HP checks and no damage falloff |
| Branch B | **Head Throw:** 60%-100% M from earliest release to full charge and 0.25s universal stun | **Multiple Nets:** base cooldown 4s; root duration 1.5s |

Damage upgrades use impact coefficients rather than increasing the source power, so they do not strengthen unrelated effects. Strong Throw does not reset its total travel budget between enemies. Improved Net fixes the old launcher omission of selected-node range bonuses.

### First passive pair - Killing Spree OR Endured Vitality

| Attribute | Killing Spree | Endured Vitality |
| --- | --- | --- |
| Trigger | Melee and active melee-skill hits; kills not required | Direct attack damage taken; excludes damage over time and environment |
| Chance | 100% | 100% |
| Per stack | +2% base melee damage; no damage reduction | +1 armor plus 6% M rounded to the nearest 0.5 armor point; +2 maximum HP (one heart) |
| Cap | 5 stacks: +10% damage | 10 stacks; maximum +20 HP; armor still obeys the native 30-point attribute cap |
| Duration | 8s, refreshed | 8s, refreshed |
| Base interval | 0.25s, haste affected | 0.25s, haste affected |
| Healing | Each credited kill heals 4% maximum HP, independently of the hit interval | Heals only newly added maximum HP; refreshing at cap never heals |

The scaling portion rounds to the nearest half point: 0.44 becomes 0.5, 0.24 becomes 0, and ties round upward. At M = 40, each Vitality stack adds 1 + 2.5 = 3.5 armor before the attribute cap. Refreshing recalculates the armor bonus from current melee power, but does not heal at the stack cap.

Existing effect IDs remain compatible. Revised registrations override stale generated effect configurations. The incoming-damage modifier is not vanilla potion Resistance.

## Shield rules

An activated blocking item reduces frontal damage by **80%**, separately from armor, enchantments and Resistance. A hit reduced to 5 damage by 50% Resistance becomes 1 damage after the shield layer. Rear and source-less environmental hits receive no shield reduction. Normal activation delay and durability remain. Axe/component disabling is cancelled; directional bypass attacks such as sonic booms and piercing projectiles use the same frontal reduction. The independent multiplier runs after native armor/Resistance and before absorption hearts. This does not turn a shield into full damage immunity.

## Tier II - Wild Charge OR Demoralizing Shout

| Attribute | Wild Charge | Demoralizing Shout |
| --- | --- | --- |
| Cast / base cooldown | Instant / 16s | Instant / 16s |
| Base duration | 2s | 6s enemy debuff and reveal |
| Damage | None | None without Challenging Shout |
| Range | Self | 12 blocks; 16 with Improved |
| Main effect | +50% base movement speed; +25 percentage points of knockback resistance | -12% base melee, ranged and offensive magic power; nonstacking |
| Control protection | Removes existing movement/control impairments and prevents new ones for 2s, or 3s with Improved | Same protection for 3s |
| Cast during control | Yes, including stun/frozen; other skills remain restricted | Yes |
| Enemy eligibility | Not applicable | No maximum-HP, boss or type restriction; party allies and pets protected |
| Improved/root | +1s Wild Charge and control-protection duration, making both 3s | +4 blocks; Shout and Battle Shout both reach 16 blocks |
| Branch A | Endurance: full knockback resistance for the entire Wild Charge buff | Battle Shout: +8% base melee, ranged and offensive magic power for caster/party allies for 6s, matching the Shout radius |
| Branch B | Concussion Blow: next melee hit stuns for 1s; readiness 4s; no additional cooldown | Challenging Shout: adds 10% M damage, mob aggro toward caster and a universal 0.2s stun |

The user's final clarification retains a 2s base Wild Charge and gives Improved +1s. Improved Wild Charge extends both its movement buff and control protection to 3s. Purification includes Frozen, stun, roots, Slowness, silence and status effects with negative movement/flying/jump-speed modifiers; it does not erase unrelated Weakness or equipment attributes. Protection rejects incoming control on the native and pack forced-application paths. The client casting check exempts only these two instant skills, while native cooldown/ammunition checks remain. Shout includes the caster, so its protection still works without enemies nearby.

Shout reduces Arcane, Fire, Frost, Lightning and Soul attributes when present; Healing Power is not reduced. Demoralize and reveal both last 6s and refresh rather than stack. Aggro affects attackable mobs; it cannot control player inputs, force mobs to attack Creative/Spectator players, or override a mob's native inability to attack a target.

### Second passive pair - Warm Up OR Momentum

| Attribute | Warm Up | Momentum |
| --- | --- | --- |
| Trigger / chance | Combat roll / 100% | Combat roll / 100% |
| Immediate effect | Heal 1 HP + rounded 6% M | Ready five melee hits within 6s |
| Each eligible hit | Not required | +2% base attack speed and +2% base movement speed; five stacks, maximum +10% each |
| Buff duration | Immediate heal | 3s, refreshed by each hit |
| Base cooldown | None | 12s, haste affected |

Warm Up rounds only the 6% M portion to the nearest 0.5 HP; 1 HP remains the fixed minimum. At M = 40 it heals 3.5 HP; M = 4 heals 1 HP. It uses noncritical power. Momentum grants no melee-damage or resistance bonus. Each hit consumes one readiness charge; unused charges expire six seconds after the roll.

## Tier III - Berserker OR Last Stand

| Attribute | Berserker | Last Stand |
| --- | --- | --- |
| Channel | Five pulses over 2.5s, stationary, Last Stand presentation | Five pulses over 2.5s, stationary |
| Base cooldown | 60/70/80/90/100s after 1/2/3/4/5 pulses | Same |
| Per pulse | Full heal; +15% base attack speed, +10% basic melee attack damage only, +15 knockback-resistance points, +20% base incoming-damage modifier | +20% base maximum health and heal the health added; refresh Regeneration I and full knockback resistance |
| Five-pulse totals | +75% attack speed, +50% basic-attack damage, +75 knockback-resistance points, +100% damage taken | +100% base maximum health; Regeneration I remains level I; no damage immunity |
| Buff duration | 14s, refreshed by pulses | 15s, refreshed by pulses |
| Speed | Speed I/II/III/IV/V for 2s after pulses 1/2/3/4/5; refreshed by each pulse | Five health pulses total +100% base maximum HP; no separate full-heal reward |
| Natural expiry | Full heal | Health bonus and buffs expire normally |
| Fatal-hit outcome | Prevent death, end Berserker, full heal, current cooldowns on Throw Weapon/Throw Net/Wild Charge/Shout, Nausea 10s and stun 5s | No special fatal-hit rescue or damage immunity |
| Improved/root | Duration 17s | Duration 20s; no damage immunity |
| Branch A | **Brutality:** +4 physical critical-chance points per pulse, maximum 20; ends with Berserker | **Juggernaut:** +10% base size and -3% base incoming-damage modifier per pulse, up to +50%/-15%; matches 20s Improved duration |
| Branch B | **Lethality:** successful melee impacts heal 2 HP, with a 0.2s base healing interval; lose 1 HP every 0.5s directly through armor, Resistance, absorption and immunity | **Sentinel:** direct damage during Last Stand adds +1% base melee damage per stack, maximum ten. Damage buff and Regeneration I refresh for 5s; gains are limited by a 0.25s base interval; longer existing regeneration is preserved |

The channels and cooldowns use native melee haste. These cooldown values are before haste; Berserker's own attack-speed stacks contribute to haste. Zero-pulse cancellation costs no cooldown. Interrupting after a pulse retains that pulse's cooldown; stun/freeze interrupts but roots do not. Buff durations remain fixed. Berserker's basic-attack bonus never changes the attack-damage attribute used by skills, throws and scaling. Lethality's self-cost uses raw health subtraction; a lethal cost takes the same fatal-hit rescue path. Early dispelling does not count as natural expiry. No new spell IDs replace the old saved IDs.

### Third passive pair - Enrage OR Tenacity

| Attribute | Enrage | Tenacity |
| --- | --- | --- |
| Trigger | Current HP checked every 0.25s, no damage event needed | Current HP 1-30%, checked every 0.25s |
| Chance / base cooldown | 100% / none | 100% / 25s, haste affected |
| Thresholds | At or below 70/60/50/40/30/20% HP | Inclusive 1-30% range |
| Benefit | Each threshold adds +3% base melee damage and +6% base size; maximum +18%/+36% | Absorption 6 + round(0.30M) HP, rounded to nearest whole HP |
| Duration | 3s refreshed; recovered health lowers/removes the level at the next check | Absorption 14s; no refill while cooling down |
| Nearby control | None | Universal stun 2s within 5 blocks; party allies and owned pets protected |

Confirmed follow-up: Enrage size stacks per threshold. Tenacity triggers at 30% HP or lower and its absorption lasts fourteen seconds. At M = 10, Tenacity grants 9 HP; at M = 41 it grants 18 HP. Buffs do not require another enemy hit to activate.

## Registered Warrior effect attributes

All operations here are **ADD_MULTIPLIED_BASE**, per effect level, unless a separate behavior is listed.

| ID | Installed registration defaults |
| --- | --- |
| rogues:shatter | minecraft:armor -0.3 |
| rogues:net_trap | movement_speed -2; jump_strength -2; knockback_resistance +100; ROOT action impairment plus explicit KnockbackImmunity |
| rogues:charge | movement_speed +0.5 ADD_MULTIPLIED_BASE; knockback_resistance +0.25 ADD_VALUE |
| rogues:demoralize | attack_damage, ranged_weapon:damage and registered offensive spell_power attributes -0.12 ADD_MULTIPLIED_BASE |
| rogues:last_stand | knockback_resistance +1 ADD_VALUE; max_health +0.2 ADD_MULTIPLIED_BASE per pulse, up to five |
| skill_tree_rpgs:killing_spree | attack_damage +0.02; credited kills heal 4% maximum HP |
| skill_tree_rpgs:vitality | armor +1 and max_health +2 ADD_VALUE; additional rounded 6% M armor per level, cleaned up on removal; heal only new maximum HP |
| skill_tree_rpgs:battle_shout | melee, ranged and offensive magic power +0.08 ADD_MULTIPLIED_BASE |
| skill_tree_rpgs:berserker | attack_speed +0.15; knockback_resistance +0.15 ADD_VALUE; damage_taken +0.20; separate +10% basic-attack multiplier per level |
| skill_tree_rpgs:berserker_crit | critical_strike:chance +0.04; native base 100 means four percentage points |
| skill_tree_rpgs:juggernaut | scale +0.1; spell_engine:damage_taken -0.03 |
| skill_tree_rpgs:sentinel | attack_damage +0.01 |
| skill_tree_rpgs:enrage | attack_damage +0.03; scale +0.06 |
| skill_tree_rpgs:warrior_freedom | No attributes; synchronized control purification and immunity marker |
| skill_tree_rpgs:warrior_endurance | knockback_resistance +1 ADD_VALUE, clamped to full resistance |
| skill_tree_rpgs:warrior_momentum_ready | No attributes; 6s/five-hit readiness |
| skill_tree_rpgs:warrior_momentum | movement_speed +0.02; attack_speed +0.02 |
| skill_tree_rpgs:tenacity_ward | max_absorption +1 ADD_VALUE per level; one HP absorption per level |

Latest revision: [Throw Weapon hit fix](../throw-weapon-hit/README.md): Strong Throw hits at most two enemies; quick recasts ignore the skill's own Weakness when capturing projectile power.

Previous revision: [Warrior follow-up](../warrior-followup/README.md): minimum charge damage, health pulses, death cleanup, effect icons and removal of Lethality's obsolete spike trigger. Berserker plays the spike impact sound on natural expiry or fatal rescue. Ultimate effects and tracked state do not survive death.

Previous revision: [Tenacity confirmation](../tenacity-followup/README.md), following [Warrior tier III](../warrior-tier-three/README.md).

Verification and rebuild instructions: [Warrior/shield patch](../warrior-tier-one/README.md). The local patch includes isolated-server probes; personal installations and worlds remain untouched. No GitHub push is part of this revision.

Latest follow-up: [Last Stand damage immunity removed](../crimson-laststand/README.md).

Latest follow-up: [Berserker fully heals on every pulse](../berserker-pulse-heal/README.md), historically granting Speed IV only at pulse five; superseded by the following revision.

Latest revision: [Warrior balance](../warrior-balance/README.md): kill healing, stronger Demoralize, matched Improved Wild Charge protection, shorter Concussion stun, per-pulse Speed I-V, 14/17s Berserker, smaller Enrage and 14s Tenacity.

Follow-up: Sentinel stacking uses a 0.25s base interval; Lethality hit healing uses a 0.2s base interval. Both use native haste scaling and share their interval across targets.

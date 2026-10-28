## Tier III - Battle Banner OR Purgatory

| Attribute | Battle Banner | Purgatory |
|---|---|---|
| Cast | Instant | Up to30s stationary channel, Last Stand sword-ground stance |
| Base cooldown |100s, haste-affected |2s, haste-affected, starts on channel end |
| Duration |20s; Persistent25s |60 pulses, first0.5s, last30s |
| Pulse interval |Fixed0.5s; haste does not change it |Fixed0.5s; haste does not change stages |
| Area |6-block moving aura; Improved8 |Damage6, pull/taunt/explosion/shield12; Improved7/14 |
| Targets |Caster, party players and protected owned pets |Harmful effects exclude allies/pets; regeneration and shields exclude caster |
| Healing |1 HP +1 per complete10 P each pulse |Ally Regeneration I from10s, III from20s;4s refreshed |
| Rescue |One2s ordinary-damage immunity per unique ally per cast, inside the current aura, below30% HP or facing lethal damage |Final stage ordinary immunity0.2s +0.1s per complete10 P, capped0.7s per pulse |
| Interruption |Aura stops on death/logout/dimension change/respec |Stun/Frozen interrupt; root does not. Dispel, logout/dimension/respec/teleport clean up without explosion |

**Banner gains per pulse.** All buff amounts last3s, refreshed. Caster powers/armor are snapshotted. Healing remains immediate.

| Stat | Gain per pulse | Normal cap | Intense cap |
|---|---|---|---|
| Base attack speed |0.5% +0.1% per10 M |20% |25% |
| Draw speed |1% +0.1% per10 M |20% |25% |
| Spell haste |0.25% +0.1% per5 P |10% |15% |
| General damage reduction |0.25% +0.1% per5 caster armor |10% |15% |
| Additional magic reduction |Same armor scaling |10% |15% |
| Knockback resistance |1% +0.5% per10 M |30% |35% |

Percentage scaling is proportional, including fractional M/P/armor. Only flat HP steps use complete power increments. General and extra magic reduction multiply:10% plus10% leaves81% incoming magic damage, before other defenses. Overlapping Banners retain independent source amounts/expiry and use the strongest unexpired amount per stat. A weaker cast cannot prolong stronger caps. Rescue is tracked separately per cast/UUID, including departure/re-entry; it excludes direct HP costs, void and administrator kills. Recipients retain the final3s buff tail after the owner stops.

**Purgatory stages.** Holy hybrid is75% P +25% M. Stage changes occur on the10s/20s delivery itself.

| Stage | Pulse damage | Direct caster HP cost | Ally regeneration | Pull |
|---|---|---|---|---|
| Before10s |10% hybrid |0 |None |Small |
|10?20s |20% hybrid |2 HP each pulse |I |Moderate |
|20?30s |30% hybrid |3 HP each pulse |III |Strong |

Each pulse burns enemies for4s. Pull uses normal collision and knockback resistance, without teleporting through walls; eligible mobs are taunted. Later HP costs bypass armor, absorption, damage reduction and ordinary invulnerability. A full channel costs103 HP before outside healing:40 in the middle stage and63 in the final stage. The caster cannot walk, jump, attack or roll during the channel. No additional knockback immunity is granted.

Release before20s gives no explosion. A voluntary release during20?30s deals200?700% M, gaining50 percentage points per second. Completion or actual final-stage death gives the full700% M burst,1s stun and8s ally absorption:10 HP +1 per complete2 P, capped20 HP before received-absorption bonuses. The caster receives no shield. Refreshing a shield never restores spent HP; only a newly larger capacity grants the difference. End state is reserved before impacts, so simultaneous events cannot explode twice. Stun/Frozen interruption gives only the earned partial blast, without completion stun/shield.

### Tier-III upgrades, side by side

| Node | Battle Banner | Purgatory |
|---|---|---|
| Improved |Aura radius8 blocks |Damage7; pull/taunt/explosion/shield14 blocks |
| Branch A |Persistent Banner:25s aura |Damnation:15/25/35% hybrid pulse damage; final-phase explosion200?900% M, gaining70 percentage points per second |
| Branch B |Intense Banner:+5 percentage points to each buff cap, healing unchanged |Final Resolute:below5% HP or a fatal outcome in the final phase ends with the earned blast, ally shield and3s caster immunity; no heal |

Final Resolute also prevents a fatal direct HP cost. Fatal-defense order is Final Resolute, available Banner rescue for ordinary damage, then Ardent benefits. Nonfatal low-health order is Final Resolute, Ardent healing, then Banner if still below30%. A stopped/dispelled/administratively removed channel cannot keep dealing damage, costs, glint or protection.

### Tier-III passive pair - Ardent Defender OR Reinforced

| Attribute | Ardent Defender | Reinforced |
|---|---|---|
| Trigger |Taking damage above1% and below30% current HP, or facing a potentially fatal hit |Current health below70/60/50/40%, checked every0.25s |
| Targets |Only triggering party player/self within6 blocks; no pets |Self |
| Buff |Resistance I,+10 armor,+5% general damage reduction |+5/10/15/20% base armor and Healing Power |
| Healing |40% recipient maximum HP, normal received-healing modifiers |None |
| Duration |10s defenses; immediate heal |Updates down or clears when health recovers, allocation changes or player dies |
| Cooldown |90s shared by the owning Paladin, haste-affected |None |
| Survival caveat |Benefits do not cancel the hit; a large fatal hit can still kill |No damage immunity or resurrection |
| Internal resource |paladin_tier_3_passive_1 |paladin_tier_3_passive_2 |

The old maximum-health buff and Divine Hammer attack are removed from current application. Existing IDs are retained. Resistance I and the separate5% reduction multiply to24% combined reduction before other layers. Effect Insights shows actual received Banner percentages, Reinforced amounts, Purgatory stage/cost and remaining absorption.

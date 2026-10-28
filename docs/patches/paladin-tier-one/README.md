# Paladin tier-I ranged combat and conditional healing

Current tier-III behavior and fixed Banner/Purgatory timing are documented in [the later Paladin tier-III update](../paladin-tier-three/README.md). Earlier build/test hashes below remain historical evidence.

Current tier-I stuns/slows and tier-II support are documented in the [Paladin revamp](../paladin-revamp/README.md). The original wave and conditional-heal implementation below remains its foundation.

Implemented28 October2026 for Minecraft26.2 / NeoForge26.2.0.90 / Java25. Follows the [approved design](../../superpowers/specs/2026-10-28-paladin-tier-one-rework-design.md) and [inline plan](../../superpowers/plans/2026-10-28-paladin-tier-one-rework.md). M means physical-melee school power; P means Healing Power. Existing spell, node, scroll and binding IDs remain compatible.

## Current skills

| Detail | Purge Heal | Blessed Strikes |
| --- | --- | --- |
| Activation |0.5s cast,5s cooldown |2.5s channel,5 pulses,14s cooldown |
| Target/range |Enemy only,24 blocks, line of sight |20-block straight aimed wave, including upward/downward aim |
| Damage |50% M |30% M per released wave |
| Healing |25% P after actual enemy damage, once per cast |5% P self-heal per preparation pulse |
| Prepared state |None |One charge per pulse, maximum5;15s refreshed duration |
| Improved |60% M damage and30% P healing |Cooldown12s; still five deliveries |
| Branch A |Divine Strength:40% P self-heal plus equal healing to allies within4 blocks |Seal of Wrath:40% M, maximum2 total enemy hits per wave |
| Branch B |Absolution:80% M,3-block explosion; Improved healing remains30% P |Seal of Light:10% P self-heal per pulse; fifth pulse also heals allies within4 blocks for10% P |

Purge Heal retains the internal flash_heal ID. Cleanse is visibly Absolution; the old cleanse and Divine Strength damage-buff actions are removed. Splash does not double-hit the primary target or multiply healing per victim. No enemy damage means no healing. Successful absorption damage also qualifies. Shared party and pet checks protect allies.

Blessed Strikes retains its prepared effect and weapon glint. Its old extra hybrid melee-hit damage and charge-consumption healing are gone. Actual Better Combat melee execution releases one wave even when the sword hits nothing; held left-click follows the weapon's repeated swing interval. A server cadence guard rejects duplicate/too-rapid callbacks. No mouse-press-only handler or damage-on-hit proc is used. Shields, item use, empty hands, invalid weapon attacks and impaired attacks do not release waves. Base waves stop at one enemy; Wrath stops after two distinct enemies. Protected allies do not consume the hit budget. Each enemy can be hit once per wave. Terrain stops the projectile; no homing, ricochet or distance falloff.

Wave speed is1.4, collision width4.2 (1.2x Arsenal's3.5), visual scale4.8 (1.2x its4). The separate golden model reuses Arsenal's shockwave geometry and Minecraft's gold-block texture; Arsenal's original resources are untouched. Death, class reset and expiration clear prepared effects; older six-charge effects normalize to five.

## Haste and armor

All Paladin actives and passives explicitly enable native cooldown haste, and casts enable native cast haste. Already-enabled native behavior is retained rather than scaled twice. Zero-cooldown passives stay zero. Banner and Consecration cloud pulse intervals now use their owner's school haste; decorative clouds remain inert. Durations, counts, coefficients and projectile speed are unchanged by haste.

Justicar now provides1.5 flat Healing Power per piece:6 from four items plus the preserved2 set bonus =8. Lightbringer remains1.75 per piece. Other armor stats, equipment doctrines and the independently completed class-armor survivability work are preserved. The latter changed Paladins/Armory inputs during this task; their disjoint armor-registration/helper entries were carried into the final input snapshots, recorded in preserved-concurrent-armor.json.

## Scope and build

Five same-name JARs change: Paladins resources/model/localization, Skill Tree Paladin modifiers/wording/haste flags, Spell Engine conditional-delivery helper and two narrow hooks, Better Combat's server swing callback, and Armory's existing healing helper. No new dependency or standalone mod is added; this integration uses the pack's existing Better Combat and Spell Engine together. Keep matching files on server and clients.

`python docs/patches/paladin-tier-one/build.py` builds from ignored immutable snapshots in tmp/paladin-tier-one/before. artifacts.json records hashes and exact entry changes. Source helper uses native SpellImpacts for healing/damage and native ProjectileLauncher for flight. This engine resolves impacts from the spell holder rather than its nominal list parameter, so coefficient variants scale the native power context; no global impact behavior is altered.

## Verification

- Original resources failed the new exact-value assertions before editing.
- Final isolated native server:75 checks passed with fresh settings, and75 with stale generated equipment configs.
- Tests cover base/improved/branch damage and healing,80% PvE applied once, missing/invulnerable targets, blast deduplication/once-only healing, nearby protected pet healing, final-pulse-only ally healing, five pulses/cap/duration, native projectile speed/width/upward aim, native one/two-hit collision behavior, haste cooldown/channel/cloud intervals, zero decorative pulses, death/respec and old six-charge migration, and armor values.
- Actual isolated client:24 checks passed. Holding left-click produced exactly five server waves at13-tick sword intervals, all synchronized; the waves damaged an airborne target beyond ordinary melee reach. Names, percentages, charge depletion/glint lifecycle and native tree opening passed. Justicar granted exactly8 total set power; switching to Priest still removed it and its bonuses. Screenshots show the golden traveling wave and current tree.
- Tests were stabilized on an isolated flat world: random terrain had correctly blocked line of sight in an earlier fixture. Initial client fixture issues were stale hotbar selection and checking projectile spell data before synchronization, both corrected in the fixture. These were not relaxed into product targeting rules.

See candidate/checks and client evidence. `python docs/patches/paladin-tier-one/verify.py --installed` checks exact artifacts, unchanged entries, resources and tested hashes. Isolated test data remains ignored. Personal profiles/worlds and the separate desktop server were untouched. No Git commit/push was requested.

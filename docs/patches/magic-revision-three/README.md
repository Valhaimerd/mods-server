# Magic balance revision three

> Historical revision: latest effective values are in [revision four](../magic-revision-four/README.md).


20 October 2026. Minecraft 26.2, NeoForge 26.2.0.90, Java 25+. Same-filename repacks of Wizards 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5, layered on the previous first- and second-tier revisions. No new mod or migration/reset.

## Confirmed interpretation

- Missiles cooldown is based on **channel completion**, not damage: 8 + 8 × completion seconds, before haste.
- Charged Beam adds **0.04 times the original width per delivery**, not 0.4. First delivery starts at 1.3 times original width, maximum three times original width.
- Echoing Blast changes **Arcane Explosion with that node selected** to 200%. The separate Arcane Blast weapon spell is unchanged.
- Shattering Lance keeps its **200% direct hit and four-second delayed freeze**. Its surrounding splash alone deals **50% and one-second delayed freeze**.
- Improved Frost Nova adds **0.5 seconds** to its existing two-second Frozen duration, for 2.5 total.
- To make Echoing Blast's reset guaranteed on every eligible hit, its old ten-second proc cooldown is removed too. The one-per-tick cap remains; one reset is sufficient for all hits in that tick. Chain Detonation has no chance roll or proc cooldown.

## Current first active tier

Damage percentages multiply the corresponding school's effective damage before critical hits and defenses. Tables show base spell damage before improved/root bonuses, except the explicitly calibrated Frost Fan path.

| Spell | Damage | Cast / channel | Cooldown before haste | Reach and effects |
| --- | --- | --- | --- | --- |
| Arcane Missiles | 24% per missile, 288% over 12 | 4 s channel | 8–16 s by completion | 64 blocks, sticky aim/homing, bounce 1, pierce 2 |
| Arcane Explosion | 280% per target; 200% with Echoing Blast | 1.5 s | 10 s; eligible Echoing hits reset it | Required aimed mob within 64 blocks; radius 7, improved 9 |
| Fire Breath | 15% per delivery, 375% over 25 | 5 s channel | 4 s at full channel, ordinary zero-to-full proportional behavior | 8 blocks, 40-degree cone, no falloff, two-second burning |
| Flame Slash | 130% per impact | 0.1 s standard cast | 5 s | 16 blocks, velocity 0.9, three-second burning, high pierce limit |
| Frost Nova | 180% per target | 0.3 s | 10 s | Radius 10, Frozen 2 s |
| Frost Spikes | 80% per eligible impact | Instant | 8 s | Eight spikes at centers 1–8 blocks, Frozen 2 s |

### First-tier node choices

- **Missiles:** Improved adds +10% spell damage scaling. Choose Conjured Missile (one extra every third delivery, 16 total over the base channel) or Crippling Missiles (four-second stacking slowdown, -10% base movement per level, maximum three levels).
- **Explosion:** Improved raises radius to 9. Choose Echoing Blast (100% reset chance, no proc cooldown, Explosion becomes 200%) or Chain Detonation (guaranteed 30% secondary explosion after 10 ticks / 0.5 s, radius 4 with retained squared distance falloff, no proc cooldown). Chain now uses the event's struck target rather than searching around the caster. Each eligible original hit may trigger one secondary explosion; these can overlap on groups. A removed/invalid target can still invalidate a delayed delivery under the engine's normal rules.
- **Breath:** Improved adds five critical-chance percentage points. Choose Explosive Breath (10% proc chance, 30% damage, radius 2.5 with falloff, 0.2 s proc cooldown) or Flame Throwing (+4 reach, giving 12).
- **Slash:** Improved adds five reach, giving 21. Choose Towering Slash (+45% projectile scale) or Wave after Wave (-1 s remaining cooldown per successful impact).
- **Nova:** Improved adds 0.5 s Frozen, giving 2.5 s. Choose Frost Splinters (20%, 3-block splash, 0.35 s delay, retained falloff) or Deep Freeze (+1 amplifier).
- **Spikes:** Improved adds +10% damage scaling. Choose Glacial Ridge (12 extra spikes, 20 total reaching 20 blocks) or Frost Fan (two side rows at ±10 degrees, eight per row, 24 total; calibrated to 25% per eligible impact with the required improved root included). Spike count does not guarantee that many hits on one target.

## Current second active tier

| Spell | Damage | Cast / channel | Cooldown before haste | Reach and effects |
| --- | --- | --- | --- | --- |
| Arcane Beam | 20% per delivery, 500% over 25 | 5 s channel | 15–30 s by completion | Range 32, beam width 0.104 = 1.3 × original 0.08 |
| Arcane Barrage | Bolts deal 70% of emitter damage; emitter damage = 2.5 + 25% owner Arcane damage | Instant | 6 s, not haste affected | Three stationary, unattackable emitters, seven-second lifetime; 16-block targeting range |
| Meteor | 150% each; 450% if all three meteors connect | 2 s | 16 s | Target range 32; splash radius 6 with no falloff; launch radius 5; model size 1.5 × original, impact ring scale 6 |
| Firestorm | 40% per delivery, 320% over eight | 5 s channel | 15–25 s by completion | Radius 15 around caster, zero casting movement factor, two-second burning |
| Frost Shield | No direct damage | Instant | 12 s | Three seconds, -100% base movement, blocking/fire protection |
| Ice Lance | 200% at full charge | 1 s full charge | 12 s | Range 16, full-charge 32; velocity 1.5, full-charge 2.3; knockback 2; Frozen 4 s after 0.5 s |

### Second-tier node choices

- **Beam:** Improved adds five critical-chance percentage points. Choose Beam Exposure (three seconds, +4% Arcane vulnerability per level, maximum 25 levels / +100%) or Charged Beam, replacing Beam Propulsion's movement/jump buff.
- **Charged Beam:** Ten-second channel, 50 deliveries. Zero-based delivery index `n` deals `min(10 + 0.25n, 20)%`. Thus shot 1 is 10%, shot 41 reaches 20%, and the full base channel totals 795%. Width is `min(1.3 + 0.04n, 3)` times the original width: shot 1 is 1.3×, shot 44 reaches the 3× cap. Haste changes time, not these base delivery indices. Cooldown remains 15–30 s. The improved root's critical bonus still applies normally.
- **Barrage:** Improved adds ten emitter critical-chance attribute points. Choose Arcane Battery (+1 emitter, four total) or Attuned Emitters (adds owner haste minus 100 to emitter haste). Their Arcane Bolt has a one-second base cast plus a five-tick AI action cooldown; this is not a fixed four shots/second rate.
- **Meteor:** Improved reduces cooldown 16→14 s. Choose Meteor Shower (+1 meteor, four total) or Meteor Splash (starts after 0.25 s, radius 5, lifetime 6 s, 10% per impact, existing eight-tick / 0.4 s impact interval).
- **Firestorm:** Improved adds +10% spell damage scaling. Choose Flame Whirlpool (Slowness II for one second refreshed by hits; inward -0.45 / upward +0.1 velocity across the same 15-block target area) or Raging Firestorm (+2 deliveries, ten total / 400% before root, +100% base knockback).
- **Shield:** Improved reduces cooldown 12→9 s. Choose Nimble Shield (+50% base movement for three seconds; offsets half of Shield's penalty) or Durable Shield (+1 s, four total). Blocking behavior remains unchanged, including bypass exceptions.
- **Lance:** Improved reduces cooldown 12→10 s. Choose Colossal Lance (+50% projectile scale, +15 critical-chance percentage points) or Shattering Lance (stops at first enemy; direct hit remains 200% and four-second delayed Frozen; splash radius 8, 50% damage and one-second delayed Frozen, no falloff). Shattering's -999 pierce modifier cancels the base 999 pierce budget. Ordinary Lance keeps its pierce budget.

Frozen still breaks on eligible direct damage; health thresholds remain removed. Explicit entity/boss immunities, combat permissions, and invalid-target checks remain. Rune costs are one matching school rune for each active spell. Existing class/weapon allocation IDs, exclusion choices, third tiers, ordinary attributes and XP speed are preserved.

## Implementation and rebuild

```powershell
python docs/patches/magic-revision-three/build.py <saved-current-input-jars>
python docs/patches/magic-revision-three/verify.py tmp/magic-revision-three/built
```

Use the active archives from before this revision, saved under ignored `tmp/magic-revision-three/before/`. The build needs the existing isolated Java/NeoForge classpath cache and JDK paths recorded in the script. It writes candidates, not installed files. This stage follows the earlier skill-overhaul, first-tier and second-tier stages. Do not rerun earlier stages against stale inputs and overwrite later changes.

The Spell Engine repack preserves the previous delayed-freeze and minimum-cooldown hooks. It adds `RevisionRules.class`, replaces the cooldown dispatcher, intercepts per-impact data using shallow copies, adjusts two cast-duration reads for the selected Charged Beam node, and replaces the common `SpellCaster.Entity.getBeam` appearance accessor. Damage and splash adjustments never mutate shared spell registry objects. The common appearance accessor uses synchronized cast length/speed for remote viewers; it does not require client-only renderer classes on a dedicated server.

Skill Tree's Arcane Exposure code constant changes from 0.02 to 0.04. The old node ID remains, while English title/description become Charged Beam. Other translated languages remain intact. Only English descriptions for the changed nodes are updated.

Production JARs retain active filenames and go to `mods/` and ignored fixed packs. Prior bytes stay in ignored `mod fixing/retired packs/magic-revision-three-2026-10-20/`. No test classes or ASM tools ship, and no personal instance/world is changed. No commit or push is requested.

## Verification

- The prior installed archives fail the new Missiles cooldown assertion (4 versus 16 full-channel seconds). **37 structural checks pass** on candidates: normalized damage, timings, proc settings, new node fields, radius/model changes, English JSON, and archive CRCs.
- **91 isolated dedicated-server checks pass**: all cooldown endpoints and half-channel values; normal/Charged cast duration and 50 deliveries; actual damage of every Charged delivery; 795% full-channel sum; common appearance width and caps; actual 25-level Exposure vulnerability; controlled damage for the eight changed base spells; calibrated Fan damage; Echoing tradeoff and live cooldown reset with an equipped spell source; direct/splash Lance damage, radius boundary and delayed Frozen durations; real Chain trigger with ten-tick delay and target-centered splash, retained falloff, and no damage beside the caster.
- Charged Beam coefficient checks isolate critical chance; the mandatory improved node's +5-point critical bonus is preserved and checked in the resource diff, rather than relying on random critical rolls in deterministic damage samples.
- Logs and test worlds: `tmp/magic-revision-three/`. Probe source: [RevisionProbe.java](RevisionProbe.java). Measurements: [runtime-results.txt](runtime-results.txt). Archive change inventory: [artifacts.json](artifacts.json).
- The raw reference was regenerated from installed JARs with explicit runtime-override notes. The handbook remains 74 pages; only page 16 changes text, its rendered layout fits, and generated internal links resolve. Installed and fixed-pack archive hashes match the tested candidates. A final rebuild changed only two English spell descriptions; gameplay entries remain byte-identical to the runtime-tested archives.

These are controlled registry, impact, cast-parameter, common-appearance and event-path checks. They are not a full client visual playtest, multiplayer match, every modded boss interaction or a guarantee that all projectiles/cloud ticks hit. Existing custom configs may override effect attributes.

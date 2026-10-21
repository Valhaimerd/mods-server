# Magic balance revision five

Current installed third-tier and targeting rules: [third-tier rework](../magic-third-tier/README.md). First- and second-tier values below remain in effect.

21 October 2026. Minecraft 26.2, NeoForge 26.2.0.90, Java 25+. Same-filename updates layered over [revision four](../magic-revision-four/README.md), retaining its mechanics and earlier pack fixes.

Magistrate loses its ten spell-critical-chance points in both the live attribute helper and its displayed description. It retains +10% Arcane/Fire/Frost power, +5% haste and +5% magic resistance. Ordinary critical nodes still add 2.5 points each. Existing allocations are preserved; the helper removes an existing transient critical bonus on refresh.

## Current first active tier

Damage percentages multiply the corresponding school's effective damage before critical hits and defenses. Tables show base spell damage before improved/root bonuses, except the explicitly calibrated Frost Fan path.

| Spell | Damage | Cast / channel | Cooldown before haste | Reach and effects |
| --- | --- | --- | --- | --- |
| Arcane Missiles | 26% per missile, 312% over 12 | 4 s channel | 8–16 s by completion | 32 blocks, sticky aim/homing, bounce 1, pierce 2 |
| Arcane Explosion | 260% per target; 160% with Echoing Blast | 2 s | 10 s; eligible Echoing hits reset it | Required aimed mob within 64 blocks; radius 7, improved 9 |
| Fire Breath | 13% per delivery, 325% over 25 | 5 s channel | 4-8 s by completion | 8 blocks, 40-degree cone, no falloff, two-second burning |
| Flame Slash | 100% per impact | 0.1 s standard cast | 5 s | 16 blocks, velocity 0.9, three-second burning, high pierce limit |
| Frost Nova | 180% per target | 0.3 s | 12 s | Radius 10, Frozen 2 s |
| Frost Spikes | 80% per eligible impact | Instant | 8 s | Eight spikes at centers 1–8 blocks, Frozen 2 s |

### First-tier node choices

- **Missiles:** Improved adds +10% spell damage scaling. Choose Conjured Missile (one extra every third delivery, 16 total over the base channel) or Crippling Missiles (four-second stacking slowdown, -10% base movement per level, maximum three levels).
- **Explosion:** Improved raises radius to 9. Choose Echoing Blast (100% reset chance, no proc cooldown, Explosion becomes 160%) or Chain Detonation (guaranteed 30% secondary explosion after 10 ticks / 0.5 s, radius 4 with retained squared distance falloff, no proc cooldown). Chain now uses the event's struck target rather than searching around the caster. Each eligible original hit may trigger one secondary explosion; these can overlap on groups. A removed/invalid target can still invalidate a delayed delivery under the engine's normal rules.
- **Breath:** Improved adds five critical-chance percentage points. Choose Explosive Breath (10% proc chance, 30% damage, radius 2.5 with falloff, 0.2 s proc cooldown) or Flame Throwing (+4 reach, giving 12).
- **Slash:** Improved adds five reach, giving 21. Choose Towering Slash (+45% projectile scale) or Wave after Wave (-1 s remaining cooldown per successful impact).
- **Nova:** Improved adds 0.5 s Frozen, giving 2.5 s. Choose Frost Splinters (20%, 3-block splash, 0.35 s delay, retained falloff) or Deep Freeze (+1 amplifier).
- **Spikes:** Improved adds +10% damage scaling. Choose Glacial Ridge (12 extra spikes, 20 total reaching 20 blocks) or Frost Fan (two side rows at ±10 degrees, eight per row, 24 total; calibrated to 25% per eligible impact with the required improved root included). Spike count does not guarantee that many hits on one target.

## Current second active tier

| Spell | Damage | Cast / channel | Cooldown before haste | Reach and effects |
| --- | --- | --- | --- | --- |
| Arcane Beam | 20% per delivery, 500% over 25 | 5 s channel | 15–30 s by completion | Range 32, beam width 0.104 = 1.3 × original 0.08 |
| Arcane Barrage | Bolts deal 40% of emitter damage; emitter damage = 2.5 + 25% owner Arcane damage | Instant | 6 s, not haste affected | Three stationary, unattackable emitters, six-second lifetime; 16-block targeting range |
| Meteor | 150% each; 450% if all three meteors connect | 2 s | 20 s | Target range 32; splash radius 6 with no falloff; launch radius 5; model size 1.5 × original, impact ring scale 6 |
| Firestorm | 40% per delivery, 320% over eight | 5 s channel | 15–25 s by completion | Radius 15 around caster, zero casting movement factor, two-second burning |
| Frost Shield | No direct damage | Instant | 12 s | Three seconds of incoming-damage immunity, -100% base movement; natural expiry freezes enemies within five blocks for two seconds |
| Ice Lance | 200% at full charge | 1 s full charge | 12 s | Range 16, full-charge 32; velocity 1.5, full-charge 2.3; knockback 2; Frozen 4 s after 0.5 s |

### Second-tier node choices

- **Beam:** Improved adds five critical-chance percentage points. Choose Beam Exposure (three seconds, +4% Arcane vulnerability per level, maximum 25 levels / +100%) or Charged Beam, replacing Beam Propulsion's movement/jump buff.
- **Charged Beam:** Ten-second channel, 50 deliveries. Zero-based delivery index `n` deals `min(10 + 0.15n, 15)%`. Thus shot 1 is 10%, shot 35 reaches 15%, and the full base channel totals 664.15%. Width is `min(1.3 + 0.04n, 3)` times the original width: shot 1 is 1.3×, shot 44 reaches the 3× cap. Haste changes time, not these base delivery indices. Cooldown remains 15–30 s. The improved root's critical bonus still applies normally.
- **Barrage:** Improved adds ten emitter critical-chance attribute points. Choose Arcane Battery (+1 emitter, four total) or Attuned Emitters (adds owner haste minus 100 to emitter haste). Their Arcane Bolt has a one-second base cast plus a five-tick AI action cooldown; this is not a fixed four shots/second rate.
- **Meteor:** Improved reduces cooldown 20 to 18 s. Choose Meteor Shower (+1 meteor, four total) or Meteor Splash (starts after 0.25 s, radius 5, lifetime 6 s, 10% per impact, existing eight-tick / 0.4 s impact interval).
- **Firestorm:** Improved adds +10% spell damage scaling. Choose Flame Whirlpool (Slowness II for one second refreshed by hits; inward -0.45 / upward +0.1 velocity across the same 15-block target area) or Raging Firestorm (+2 deliveries, ten total / 400% before root, +100% base knockback).
- **Shield:** Improved reduces cooldown 12→9 s. Choose Nimble Shield (+50% base movement for three seconds; offsets half of Shield's penalty) or Durable Shield (+1 s, four total). Shield now cancels incoming damage, including damage types that bypass its old blocking protection. On natural expiry (three seconds, or four with Durable), it releases a non-damaging five-block burst applying Frozen for two seconds. Early removal does not release the burst.
- **Lance:** Improved reduces cooldown 12→10 s. Choose Colossal Lance (+50% projectile scale, +15 critical-chance percentage points) or Shattering Lance (stops at first enemy; direct hit remains 200% and four-second delayed Frozen; splash radius 8, 50% damage and one-second delayed Frozen, no falloff). Shattering's -999 pierce modifier cancels the base 999 pierce budget. Ordinary Lance keeps its pierce budget.

Frozen still breaks on eligible direct damage; health thresholds remain removed. Explicit entity/boss immunities, combat permissions, and invalid-target checks remain. Rune costs are one matching school rune for each active spell; the shield expiry burst has no additional cost. Existing class/weapon allocation IDs, exclusion choices, third tiers, ordinary attributes and XP speed are preserved.

## Implementation and rebuild

```powershell
python docs/patches/magic-revision-five/build.py <saved-revision-four-input-jars>
python docs/patches/magic-revision-five/verify.py tmp/magic-revision-five/built
```

The default inputs are ignored `tmp/magic-revision-five/before/`. Use the previously patched pack, not pristine upstream archives. Cached Java/NeoForge dependencies and the JDK path follow the preceding revisions. The build writes candidates, never installs them automatically. The build order is skill-overhaul, first tier, second tier, revision three, revision four, then revision five.

Missiles and Breath JSON coefficients are 0.78 and 0.65 because channels normalize impacts by duration/delivery count: 0.78 * 4/12 = 26%; 0.65 * 5/25 = 13%. Explosion's base coefficient becomes 2.6. The existing per-caster `RevisionRules` override becomes 1.6 for Echoing Blast; all other helper behavior is retained. Nova and Meteor base cooldowns become 12/20 seconds; Improved Meteor retains its two-second deduction.

Barrage's emitters previously cast the same `wizards:arcane_bolt` used by wands. A copied registry-only `wizards:arcane_barrage_bolt` has coefficient 0.4, and Barrage's summon action casts that copy. Its projectile/cast behavior is otherwise identical. Wand Arcane Bolt stays at 0.7. No learnable node or item is added.

Magistrate's old fixed +10 critical points are removed from `SkillOverhaul.applyCritical`; ordinary nodes remain. Its Adventurer definition and canonical rebuild description are updated together. No allocation reset or new migration marker is introduced. Tooltip descriptions also clarify target-centered Explosion, fixed-cast Slash, Breath deliveries and full-duration Shield immunity.

**Frost Shield confirms full-duration immunity, not a one-hit block.** Its existing `ShieldRework` cancels every incoming damage event while the status is present without removing or consuming it. The code and three-second duration remain unchanged (four seconds with Durable). Natural expiry still releases the non-damaging five-block/two-second Frozen burst. As before, direct entity deletion or direct health assignment that bypasses damage events is outside damage immunity.

Production filenames remain unchanged in `mods/` and ignored fixed packs. Prior JARs are retained in ignored `mod fixing/retired packs/magic-revision-five-2026-10-21/`. Test/probe classes do not ship. The user's personal Minecraft and separate desktop server are untouched.

## Verification

- **33 structural checks pass** against the final candidates: normalized coefficients, cooldowns, Barrage-only projectile routing, ordinary wand damage preserved, Magistrate tooltip/rewards/blue title, shield duration/burst, JSON parsing and archive CRCs. The previous revision fails the changed Missiles assertion, confirming the check distinguishes old values.
- **242 isolated dedicated-server checks pass**, including revised actual hit damage at 40 school power: Missiles 10.4 HP, Explosion 104 HP, Echoing 64 HP, Breath 5.2 HP/delivery, Slash 40 HP, emitter bolt 16 HP and unchanged wand bolt 28 HP. Nova cooldown is 12 seconds, Meteor 20 / 18 improved. Earlier channel, Charged Beam, Exposure, delayed freeze and target-centered chain checks remain green.
- The loaded Magistrate node adds no critical chance, refresh removes a simulated old +10 transient modifier, and both ordinary critical nodes still add 2.5 points each. Its 10% school power remains. The fixture uses Spell Power's base-100 critical baseline for these attribute checks.
- Shield receives six damage types followed by a hit on every active tick, clearing vanilla invulnerability frames before each hit. HP remains unchanged and the status is not consumed throughout all 60 ticks. After expiry the fixture accepts damage again. Natural expiry freezes nearby enemies without damage, excludes enemies beyond five blocks, and early removal does not burst. Durable retains its four-second expiry.
- The handbook remains 74 pages; text changes only on pages 16 and 18. All pages were rendered and reviewed, those two pages inspected at full size, and internal HTML links resolve. Installed main/client counts remain 309/24. The raw reference covers 27 base/helper spells, including the new emitter-only bolt. A fresh rebuild matches the tested production archives by every ZIP entry.
- [Runtime probe](RevisionProbe.java), [measurements](runtime-results.txt) and [archive inventory](artifacts.json) record the checks. Local server/logs are under ignored `tmp/magic-revision-five/server` and `tmp/magic-revision-five/runtime.log`. Production/fixed copies match the tested candidates.

These controlled dedicated-server checks do not claim a full client animation or multiplayer balance playtest. Shield uses a non-player living fixture to avoid fake-player invulnerability producing a false positive; player-cast durations still come from the actual parsed spell definitions.

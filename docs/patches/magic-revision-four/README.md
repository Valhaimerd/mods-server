# Magic balance revision four

Current installed balance: [revision five](../magic-revision-five/README.md). Numbers below describe this historical revision.

21 October 2026. Minecraft 26.2, NeoForge 26.2.0.90, Java 25+. Same-filename repacks of Wizards 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5, layered on the previous first- and second-tier revisions. No new mod or migration/reset.

## Confirmed interpretation

This revision changes targeted Arcane Explosion to a two-second standard cast; the separate Arcane Blast weapon spell remains unchanged. Fire Breath now uses 4 + 4 times channel completion seconds before haste. The shield burst is non-damaging and occurs on natural expiry only.

- Missiles cooldown is based on **channel completion**, not damage: 8 + 8 × completion seconds, before haste.
- Charged Beam adds **0.04 times the original width per delivery**, not 0.4. First delivery starts at 1.3 times original width, maximum three times original width.
- Echoing Blast changes **Arcane Explosion with that node selected** to 180%. The separate Arcane Blast weapon spell is unchanged.
- Shattering Lance keeps its **200% direct hit and four-second delayed freeze**. Its surrounding splash alone deals **50% and one-second delayed freeze**.
- Improved Frost Nova adds **0.5 seconds** to its existing two-second Frozen duration, for 2.5 total.
- To make Echoing Blast's reset guaranteed on every eligible hit, its old ten-second proc cooldown is removed too. The one-per-tick cap remains; one reset is sufficient for all hits in that tick. Chain Detonation has no chance roll or proc cooldown.

## Current first active tier

Damage percentages multiply the corresponding school's effective damage before critical hits and defenses. Tables show base spell damage before improved/root bonuses, except the explicitly calibrated Frost Fan path.

| Spell | Damage | Cast / channel | Cooldown before haste | Reach and effects |
| --- | --- | --- | --- | --- |
| Arcane Missiles | 28% per missile, 336% over 12 | 4 s channel | 8–16 s by completion | 32 blocks, sticky aim/homing, bounce 1, pierce 2 |
| Arcane Explosion | 280% per target; 180% with Echoing Blast | 2 s | 10 s; eligible Echoing hits reset it | Required aimed mob within 64 blocks; radius 7, improved 9 |
| Fire Breath | 15% per delivery, 375% over 25 | 5 s channel | 4-8 s by completion | 8 blocks, 40-degree cone, no falloff, two-second burning |
| Flame Slash | 130% per impact | 0.1 s standard cast | 5 s | 16 blocks, velocity 0.9, three-second burning, high pierce limit |
| Frost Nova | 180% per target | 0.3 s | 10 s | Radius 10, Frozen 2 s |
| Frost Spikes | 80% per eligible impact | Instant | 8 s | Eight spikes at centers 1–8 blocks, Frozen 2 s |

### First-tier node choices

- **Missiles:** Improved adds +10% spell damage scaling. Choose Conjured Missile (one extra every third delivery, 16 total over the base channel) or Crippling Missiles (four-second stacking slowdown, -10% base movement per level, maximum three levels).
- **Explosion:** Improved raises radius to 9. Choose Echoing Blast (100% reset chance, no proc cooldown, Explosion becomes 180%) or Chain Detonation (guaranteed 30% secondary explosion after 10 ticks / 0.5 s, radius 4 with retained squared distance falloff, no proc cooldown). Chain now uses the event's struck target rather than searching around the caster. Each eligible original hit may trigger one secondary explosion; these can overlap on groups. A removed/invalid target can still invalidate a delayed delivery under the engine's normal rules.
- **Breath:** Improved adds five critical-chance percentage points. Choose Explosive Breath (10% proc chance, 30% damage, radius 2.5 with falloff, 0.2 s proc cooldown) or Flame Throwing (+4 reach, giving 12).
- **Slash:** Improved adds five reach, giving 21. Choose Towering Slash (+45% projectile scale) or Wave after Wave (-1 s remaining cooldown per successful impact).
- **Nova:** Improved adds 0.5 s Frozen, giving 2.5 s. Choose Frost Splinters (20%, 3-block splash, 0.35 s delay, retained falloff) or Deep Freeze (+1 amplifier).
- **Spikes:** Improved adds +10% damage scaling. Choose Glacial Ridge (12 extra spikes, 20 total reaching 20 blocks) or Frost Fan (two side rows at ±10 degrees, eight per row, 24 total; calibrated to 25% per eligible impact with the required improved root included). Spike count does not guarantee that many hits on one target.

## Current second active tier

| Spell | Damage | Cast / channel | Cooldown before haste | Reach and effects |
| --- | --- | --- | --- | --- |
| Arcane Beam | 20% per delivery, 500% over 25 | 5 s channel | 15–30 s by completion | Range 32, beam width 0.104 = 1.3 × original 0.08 |
| Arcane Barrage | Bolts deal 70% of emitter damage; emitter damage = 2.5 + 25% owner Arcane damage | Instant | 6 s, not haste affected | Three stationary, unattackable emitters, six-second lifetime; 16-block targeting range |
| Meteor | 150% each; 450% if all three meteors connect | 2 s | 16 s | Target range 32; splash radius 6 with no falloff; launch radius 5; model size 1.5 × original, impact ring scale 6 |
| Firestorm | 40% per delivery, 320% over eight | 5 s channel | 15–25 s by completion | Radius 15 around caster, zero casting movement factor, two-second burning |
| Frost Shield | No direct damage | Instant | 12 s | Three seconds of incoming-damage immunity, -100% base movement; natural expiry freezes enemies within five blocks for two seconds |
| Ice Lance | 200% at full charge | 1 s full charge | 12 s | Range 16, full-charge 32; velocity 1.5, full-charge 2.3; knockback 2; Frozen 4 s after 0.5 s |

### Second-tier node choices

- **Beam:** Improved adds five critical-chance percentage points. Choose Beam Exposure (three seconds, +4% Arcane vulnerability per level, maximum 25 levels / +100%) or Charged Beam, replacing Beam Propulsion's movement/jump buff.
- **Charged Beam:** Ten-second channel, 50 deliveries. Zero-based delivery index `n` deals `min(10 + 0.15n, 15)%`. Thus shot 1 is 10%, shot 35 reaches 15%, and the full base channel totals 664.15%. Width is `min(1.3 + 0.04n, 3)` times the original width: shot 1 is 1.3×, shot 44 reaches the 3× cap. Haste changes time, not these base delivery indices. Cooldown remains 15–30 s. The improved root's critical bonus still applies normally.
- **Barrage:** Improved adds ten emitter critical-chance attribute points. Choose Arcane Battery (+1 emitter, four total) or Attuned Emitters (adds owner haste minus 100 to emitter haste). Their Arcane Bolt has a one-second base cast plus a five-tick AI action cooldown; this is not a fixed four shots/second rate.
- **Meteor:** Improved reduces cooldown 16→14 s. Choose Meteor Shower (+1 meteor, four total) or Meteor Splash (starts after 0.25 s, radius 5, lifetime 6 s, 10% per impact, existing eight-tick / 0.4 s impact interval).
- **Firestorm:** Improved adds +10% spell damage scaling. Choose Flame Whirlpool (Slowness II for one second refreshed by hits; inward -0.45 / upward +0.1 velocity across the same 15-block target area) or Raging Firestorm (+2 deliveries, ten total / 400% before root, +100% base knockback).
- **Shield:** Improved reduces cooldown 12→9 s. Choose Nimble Shield (+50% base movement for three seconds; offsets half of Shield's penalty) or Durable Shield (+1 s, four total). Shield now cancels incoming damage, including damage types that bypass its old blocking protection. On natural expiry (three seconds, or four with Durable), it releases a non-damaging five-block burst applying Frozen for two seconds. Early removal does not release the burst.
- **Lance:** Improved reduces cooldown 12→10 s. Choose Colossal Lance (+50% projectile scale, +15 critical-chance percentage points) or Shattering Lance (stops at first enemy; direct hit remains 200% and four-second delayed Frozen; splash radius 8, 50% damage and one-second delayed Frozen, no falloff). Shattering's -999 pierce modifier cancels the base 999 pierce budget. Ordinary Lance keeps its pierce budget.

Frozen still breaks on eligible direct damage; health thresholds remain removed. Explicit entity/boss immunities, combat permissions, and invalid-target checks remain. Rune costs are one matching school rune for each active spell; the shield expiry burst has no additional cost. Existing class/weapon allocation IDs, exclusion choices, third tiers, ordinary attributes and XP speed are preserved.

## Implementation and rebuild

```powershell
python docs/patches/magic-revision-four/build.py <saved-revision-three-input-jars>
python docs/patches/magic-revision-four/verify.py tmp/magic-revision-four/built
```

Use saved current inputs, not pristine upstream JARs. The local inputs are under ignored `tmp/magic-revision-four/before/`. The build uses the existing cached Java/NeoForge classpath and JDK path recorded in the script, writes candidate archives, and does not install or push. Keep the build order: broader skill-overhaul, first tier, second tier, revision three, revision four. The revision-four `RevisionRules.java` supersedes the earlier helper with the same production class name, preserving its other behavior.

Only spell/resource data, two English node descriptions and the helper implementation change for the offensive spells. Frost Shield adds one call at the end of `WizardsEffects.register`, a `ShieldRework.class` helper, and a registry-only `wizards:frost_shield_burst` definition. The burst is not an independently learnable active spell and grants no new skill node.

The helper cancels `LivingIncomingDamageEvent` while Frost Shield is present. It listens to non-canceled `MobEffectEvent.Expired` events for that effect, then delivers the five-block Frozen burst through ordinary Spell Engine targeting and impact eligibility. It does not burst on dispel, logout, or death. The caster must still be alive. Damage-event immunity does not promise protection from a mod or command that directly deletes an entity or sets health without dealing damage.

Production JARs retain active filenames and go to `mods/` and ignored fixed packs. Prior bytes stay in ignored `mod fixing/retired packs/magic-revision-four-2026-10-21/`. No test classes or ASM tools ship; personal instances and the separate desktop server are untouched. No commit or push is requested.

## Verification

- **19 structural checks pass**: revised Missiles range/normalized damage, cast duration, cooldown data, emitter lifetime, shield burst radius/status/no health threshold/no damage, JSON parsing and archive CRCs.
- **114 isolated dedicated-server checks pass**, including the revised channel cooldown endpoints, all 50 actual Charged Beam delivery samples and 664.15% total, unchanged width growth, Exposure, Echo damage and reset, Shattering direct/splash damage and delayed Frozen, and target-centered Chain Detonation.
- Shield tests first prove an unshielded living fixture takes damage. The fixture then takes no HP loss from generic, magic, fire, fall, out-of-world or generic-kill damage while shielded. Tick-by-tick effect expiry produces no early burst, freezes the nearby enemy for 40 ticks without HP loss, excludes an enemy beyond five blocks, and restores ordinary damage acceptance. Early removal produces no burst. Durable's 80-tick duration delays its burst accordingly.
- The shield expiry fixture executes the real effect ticker. A non-player living fixture is used for damage acceptance so a fake player's independent invulnerability behavior cannot produce false-positive immunity results. Spell durations and modifiers are still read from the player-cast definitions.
- Probe: [RevisionProbe.java](RevisionProbe.java). Measurements: [runtime-results.txt](runtime-results.txt). Archive inventory: [artifacts.json](artifacts.json). Local logs/world: `tmp/magic-revision-four/`.
- The raw reference now documents 26 base/helper spell records, including the automatic shield burst and the engine overrides. The handbook remains 74 pages; only page 16 changes text, its rendered layout fits, and internal links resolve. Installed/fixed-pack hashes match tested candidates, and a fresh rebuild is byte-identical by archive entry to the tested JARs.

These are controlled dedicated-server checks, not a full client animation or multiplayer playtest. Existing effect-config overrides and explicit target immunities can affect other modifiers and freeze acceptance.

The current shared `RevisionRules.java` helper includes the later Charged Beam width adjustment: 1.3x plus 0.10x per delivery, capped at 5x. The tables above record this revision?s historical values; see magic-revision-five for the current follow-up.

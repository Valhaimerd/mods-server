# Third-tier magic and party combat rework

21 October 2026. Minecraft 26.2, NeoForge 26.2.0.90, Java 25+. Same-filename repacks of Wizards 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5, layered over [revision five](../magic-revision-five/README.md). Better Party 1.1.7 and Better Party Tamed Mobs 1.0.4 retain their installed archives. No skill IDs or allocations change. The subsequent [Wizard passive rework](passives.md) covers all nine exclusive pairs, haste, next-spell buffs and universal stun eligibility.

## Shared targeting and protection

All Spell Engine class spells use the same owner-aware relation and final-impact policy. Self, own pets, Better Party members and their owned pets are allies. Other players, their pets, passive mobs, neutral mobs and hostile mobs are offensive targets. Shared healing/buffs apply only to self or party allies/pets; mixed Holy Light impacts choose the helpful or harmful path per target. Falling Judgement can damage non-party players. The final guard also protects direct, delayed, area, cloud and summon impacts, regardless of the party friendly-fire toggle. Vanilla combat permissions and the pack's attributed 50% PvP damage retention remain.

The universal frost statuses are `wizards:frozen`, `wizards:frost_slowness`, `skill_tree_rpgs:blizzard_slow` and `minecraft:slowness`: their spell application ignores health thresholds, the engine's boss-tag rejection and entity-type effect rejection. Native effect callbacks preserve attributes/synchronization. Frozen additionally holds physical `Entity.move` displacement so custom flying movement such as the Ender Dragon cannot ignore it. Frozen still breaks on eligible direct damage; removing it immediately restores movement. It does not disable attacks, rotation, scripted teleports or direct position assignment. Slowness remains a movement-speed attribute effect; bespoke AI that ignores that attribute can behave differently.

Fire spell impacts establish burning regardless of type-based ignition rejection; native fire damage still respects Fire Resistance and intrinsic fire immunity. This opens eligibility, not resistance bypass. Spell clouds remain non-combat effect entities rather than attack targets or projectile shields.

## Third-tier spell pairs

Percentages use the corresponding school power before crits, defenses and PvP retention. Cooldowns are before haste.

| Spell | Current mechanics | Improved node | Alternative upgrades |
| --- | --- | --- | --- |
| Burst Blink | Instant; nominal 10 blocks; 12 s cooldown. Two 50% Arcane bursts, radius 5: departure immediately, fixed arrival after 0.25 s | +5 blocks, nominal 15 total | Slipstream: -3 s cooldown, 9 s total. Purge: Speed I / 1.2x movement for 4 s |
| Evocation | 5 s channel / 10 deliveries; 35 s proportional full-channel cooldown; 10 s buff. Each stack: +5% base critical chance, +3% base haste, +10% base damage taken. Base cap 10 stacks | -5 s cooldown, 30 s at full channel | Rapid: 12 deliveries and 12-stack cap. Lasting: 15 s buff |
| Wall of Flames | Four separate key presses within 5 s of first cast; fourth cast or timeout starts normal 24 s cooldown. Five segments, 8 s lifetime, 20% Fire damage per impact, radius 0.9 and height 2 | +2 s lifetime, 10 s total | Great Wall: seven segments, radius 1.2, height 3 and living collision. Healing Flames: 0.5% power healing per eligible impact to self and party allies |
| Fire Hydra | 30 s cooldown affected by haste; three stationary heads using 55%-own-power Fireballs, nearest visible enemy within 12 blocks, 20 s active lifetime | +5 s lifetime, 25 s total | Hydra Brood: four heads. Ancient Hydra: one 3x head with visible continuous mouth-origin Fire Breath: 24% own Fire power/delivery, 40-degree cone, nearest visible enemy within 12 blocks |
| Blizzard | 8 s channel / 12 deliveries; raw coefficient 40%, normalized 26.67% per shard impact; 12-20 s proportional cooldown; range 32, splash radius 3 without falloff | +3 deliveries, 15 total | Hail Storm: +20% power. Snow Storm: existing 3 s stacking slow |
| Frost Elemental | 30 s cooldown affected by haste; one owned pet, 20 s active lifetime; formulas below | +5 s lifetime, 25 s total | Elemental Legion: three at 80% of base attributes, normal size, Nova radius 6 / 0.5 s Frozen. Elemental Colossus: one at 200% attributes / 1.7x size, inheriting caster knockback resistance |

Native Blink safety checks retain half-block collision padding, so an unobstructed nominal 10-block Blink lands 9.5 blocks forward (nominal 15 lands 14.5). Blocked/too-short teleports fizzle; unsuccessful movement cannot grant a free Purge buff. Both bursts use Arcane Explosion-style visuals and no damage falloff. Arrival is captured at teleport time, not the caster's later position.

Wall damage volumes are cylinders from ground to 2 blocks; Great Wall reaches 3 blocks. They use entity hitbox overlap, so jumping targets are affected while intersecting the volume. Meteor Splash's cloud also reaches 2 blocks. Ordinary obstacle visibility checks remain. Great Wall's native collision is active only for living entities while the segment is active; it places no world blocks. Projectiles pass through, clouds cannot be selected for attacks, and collision ends with the cloud. The five-second cast counter belongs to the player across equipment/hands; unused casts expire without requiring another cast. Normal hosting-item cooldown behavior starts only when the burst closes.

Ancient Hydra continuously channels 25 deliveries over 5 seconds at 5% of its own Fire power per delivery, with no cooldown or gap between channels. Acquisition and breath reach are 12 blocks, cone 20 degrees. It aims and emits from the sustained native firebreath pose's mouth. Its in-game node description remains simply "Summon one giant hydra." Existing ordinary Hydra formulas/fireballs remain unchanged.

## Frost Elemental attributes

Let P be the summoner's effective non-critical Frost power at summon time. These replace the old additive elemental defaults after native summoning. Summons remain owned and discoverable by Better Party's existing tamed-mob collector and HUD payload; no inheritance/addon replacement is necessary.

| Attribute | Base elemental | Legion:each of 3 | Colossus:one |
| --- | --- | --- | --- |
| Health | 30 + 0.5 P | 0.8(30 + 0.5 P) | 2(30 + 0.5 P) |
| Armor | 5 + 0.08 P | 0.8(5 + 0.08 P) | 2(5 + 0.08 P) |
| Scale | 1 x | 1 x | 1.7 x |
| Frost power | 0.2 P | 0.16 P | 0.4 P |
| Melee damage attribute | 0.5 P | 0.4 P | 1 P |
| Knockback resistance | Native base | Native base | Caster's attribute value |

At P 40, base is 50 HP/8.2 armor/8 Frost power/20 melee; Legion is 40 HP/6.56 armor/6.4 power/16 melee each; Colossus is 100 HP/16.4 armor/16 power/40 melee. Only Frost power inheritance changed to 20%; health, armor and melee retain their previous base formulas. Native health/armor caps are bypassed only for elemental entity accessors so the requested formulas remain intact at higher power; native armor mitigation still applies. Ordinary companion attribute bonuses can still modify owned summons.

Elementals cast the summon-only `wizards:elemental_frost_shard` at 100% of their own Frost power; the original `wizards:frost_shard` remains unchanged. Super Nova uses separate summon-only definitions and deals 180% of the elemental's own Frost power: at caster P 40, base/Legion/Colossus deal 14.4/11.52/28.8 raw damage per target before mitigation and criticals. Legion Nova reaches 6 blocks and applies Frozen for 0.5 s; base and Colossus retain range 10 and 2 s Frozen. Each elemental retains its own spell/AI cooldowns and close-range Nova trigger. Player Frost Nova is unchanged. Summon behavior is cloned per entity so Legion cannot alter other forms. Other melee behaviors remain. Node marker entries identify the selected summon form without mutating shared registry definitions; unknown marker attributes have empty modifiers and are skipped by ordinary scaling.

## Build and validation

```powershell
python docs/patches/magic-third-tier/build.py
python docs/patches/magic-third-tier/verify.py tmp/magic-third-tier/built
python docs/patches/magic-third-tier/run_probe.py
python docs/patches/magic-third-tier/check_client.py
```

Default inputs are saved revision-five JARs under ignored `tmp/magic-third-tier/before`. Do not apply to pristine upstream archives. The build uses the cached audit/NeoForge Java classpath and JDK path in the scripts. It compiles helpers to Java 25, asserts every ASM hook, writes candidate archives and inventories changed entries; it does not install or publish. The test runner uses the existing isolated revision-five minimal dependency set, adds Better Party/Tamed Mobs/PvP retention, and needs its `libraries` linked to the cached NeoForge 26.2.0.90 library directory. It never uses a personal profile or the separate desktop server.

**128 structural checks and 855 isolated dedicated-server checks pass.** The actual patched client `keyHeld` guard also passes with JVM `-Xverify:all`, without launching a client. [Runtime measurements](runtime-results.txt) and [archive fingerprints/entry inventory](artifacts.json) record the final candidates.

- Real Better Party membership maps are seeded only in the isolated fixture and restored afterward. Both friendly-fire settings are tested with direct/area damage, burn, frost and healing. Loaded Falling Judgement and mixed Holy Light run through public SpellImpacts for party/enemy players and pets. Player fixtures retain native hurtServer and damage events after disabling FakePlayer-only invulnerability and its always-false PvP predicate.
- Villagers, golems, cows, hostile mobs and enemy pets take actual area damage/burning; healing rejects enemies. An immune, boss-tagged 1,000-HP fixture receives all four frost statuses and Stun. Stun also physically holds a real Ender Dragon and preserves party/pet protection. A real Ender Dragon moves before Frozen, cannot move while Frozen, and moves again after removal.
- The actual Better Party tamed-mob collector discovers a spawned elemental; its actual HUD payload round-trips through the codec. Elemental/Legion/Colossus formulas and high-value health/armor accessors are checked. Each form casts its actual summon-only Nova through MobCastController: damage follows its own Frost power, Legion freezes for ten ticks and excludes the seven-block target, while player Nova and the shared summon definition remain unchanged. Ancient Hydra deals 50 actual hits across two consecutive five-second channels, 25 per channel at 24% own power, with four-tick cadence and no cooldown gap. Close-target facing checks span distances 1/3/5/6/10 in four directions, with independent mouth aiming and cone inclusion. Actual entity ticks prove nearest-target selection, escape/reacquisition and the 12-block limit; cone fixtures distinguish 15 from 25 degrees. Mouth-origin particle packets produce real flame spawn instructions. Ordinary Hydra uses a private 55% Fireball clone, leaving player Fireball at 80%. An old 9.1 raw hit scales to 6.25625 with identical power and mitigation; damage remains power-scaled rather than a fixed 6-7 HP.
- Blink departure damage is immediate, arrival waits five ticks and remains anchored after the caster moves. Native teleport testing uses a non-player living fixture because FakePlayer's dummy connection does not perform real player teleportation. Native collision padding remains intact.
- Great Wall clips a real ArmorStand movement at its bounding box, does not intercept projectiles and has synchronized radius/height. Actual wall and Meteor Splash cloud ticks damage a target whose feet are 1.8 blocks above ground, and exclude one whose feet are above two blocks. Real interactor casts with a hosting item allow the first three casts, start 480 ticks on the fourth and reject the fifth. The client guard accepts fresh presses and suppresses held-key repeats only for Wall of Flames; explicit zero-cooldown synchronization follows the first three casts. Four casts close the window immediately; a partial burst remains open at tick 99 and automatically starts cooldown at tick 100 without another cast.
- Earlier first-/second-tier damage, proportional cooldowns, Charged Beam, delayed Lance freeze, target-centered Chain Detonation, and repeated-hit Shield protection remain covered. Production/fixed copies match the tested archives. No test classes or patch tools are shipped.
- The rebuilt handbook has 74 pages and 461 internal links. All pages were visually reviewed, with detailed review of changed pages 16 and 17; no clipping or pagination changes were found. Main/client JAR counts remain 309/24.

Client Hydra model bytecode passes ASM checking and JVM `-Xverify:all` linkage without initializing a client; rendered mouth alignment and particles have not been visually playtested. The follow-up is published on main after verification. Client model bytecode/linkage verification does not substitute for an observed client animation or full multiplayer playtest.

## Hydra and wall follow-up

Ancient Hydra's special channel bypassed the normal channel FX completion callback, and native casting particles are rendered only for players. The channel now sends coordinate-based flame batches at the computed mouth position with its independently calculated breath yaw/pitch every delivery, so mobs have a visible breath. Its full cone is 40 degrees (20 each side), with 24% own Fire power per delivery. Both Hydra variants select the closest visible eligible enemy within a strict 12-block center radius every server entity tick; owner-hit/revenge/native target goals cannot retain a distant target. Party allies/pets stay excluded. Ordinary Hydra Fireballs use a summon-only 55% coefficient instead of the player's 80%; caster Fireball and all other class spells are untouched.

The apparent first-cast wall cooldown came from client `ClientCastController.keyHeld`: instant spells call `startSpellCast` every held tick regardless of `freshForStart`, exhausting all four charges in a short press. The hotbar supplies STOP/START freshness in the opposite order to the parameter names; the guard checks both flags, including the held-after-START (true,false) case. Wall alone now requires a fresh press for each cast; other instant spells retain their controls. The server sends zero-cooldown confirmation after casts one through three, then applies the native haste-adjusted 24-second base cooldown after cast four or five seconds from cast one. The three remaining walls must still be cast inside that window.

The isolated server tests cover targeting, damage and packet-to-particle instruction conversion. They do not claim an observed rendered flame or a manual full-pack multiplayer playtest. Frost Elemental changes were pushed separately as `28113e0`; this follow-up is published afterward on main.

## Close-target facing and short debuffs

The old Ancient rotation used the mouth-to-target vector for both beam aim and model yaw. At three-times size the mouth is about5.24blocks ahead of the base; when enemies crossed inside it, that vector faced backward. Model/body/head yaw now follows the base-to-enemy heading. The mouth-origin breath cone and particle packet use their own target vector, retaining close-range hits without reversing the model. A regression first reproduced a backward-facing dot product of-0.99937 at one block, then passed at20 combinations of distance and heading, including cone inclusion. Breath remains a40-degree cone and now deals24% own Fire power per delivery (raw coefficient1.2 normalized by5/25); at own power40,50 deliveries across10seconds deal480raw damage. Ordinary Hydra Fireballs remain55%.

Scorching Flames keeps4% Fire vulnerability per stack but lasts2seconds. Winter's Chill now adds8critical-vulnerability points per stack, at most40points at five stacks, with2seconds duration. Native vulnerability lookups and actual status durations are included in the855server checks. Rendered presentation still needs a manual client playtest.

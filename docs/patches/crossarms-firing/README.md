# CrossArms Frenzy balance

Minecraft 26.2 / NeoForge 26.2.0.90, CrossArms 1.4. Same active JAR filename.

Capacity is removed entirely: its enchantment definition, functions, predicates, item modifiers and acquisition/exclusion references no longer ship. Capacity can no longer be obtained or applied in this pack. CrossArms now supplies seven enchantments. The Multishot/Piercing integration remains.

## Frenzy rules

| Heat display | Heat score | Shot damage retained | Normal spread | Magic self-damage per 20 ticks |
| --- | --- | --- | --- | --- |
| Zero bars | 0-10 / no score | 15% | 115% | None |
| First bar | 11-150 | 15% | 125% | None |
| Second bar | 151-300 | 15% | 140% | None |
| Third / red bar | 301-450 | 20% | 160% | 1 HP |
| Fourth / dark red bar | 451-610 | 30% | 185% | 2 HP |
| Fifth / black bar | 611+ | 40% | 220% | 4 HP |

The timer begins on entering damaging heat and resets on cooling below it; sustained heat deals damage every 20 ticks (one second at 20 TPS). The amount uses the current bar at the damage tick. Damage uses `minecraft:magic`, so Fire Resistance does not cancel it. Heat cools by two units per world tick and remains attached to the player when switching weapons; death clears heat and its timer.

Shot damage is a multiplier on the existing projectile damage, preserving weapon bonuses and Ranged Weapon API scaling. The projectile reads its actual owner's heat, rather than a nearby player's. Spread multiplies the original crossbow inaccuracy by the heat-table value: 115% is 1.15x ordinary spread, not a miss probability. Missing or negative heat counts as cold. Every actual Frenzy shot immediately applies Slowness II to its owner: 30% movement reduction for 20 ticks (one second), refreshed on each shot. This includes the first shot and works in either hand; it expires after firing stops. Ordinary crossbows do not gain this slowdown and retain their spread. The firing hook reads the actual shooter's heat and counts each firing action once, after capturing Frenzy eligibility before durability can destroy the stack.

Overheating no longer forces the crossbow down to two durability. Normal one-durability firing wear remains, and auto-feeding still stops at one remaining durability. Without Infinity, Frenzy consumes one normal arrow per feed. Infinity on the firing crossbow preserves arrows, still requires at least one normal arrow in Survival, and loads intangible projectiles that cannot be collected. Infinity on a weapon in the other hand does not grant free shots. The custom reload function previously bypassed Infinity by directly clearing inventory arrows; the new regression test reproduced that failure before the fix. Frenzy auto-feeds on vanilla and modded crossbows in either hand. The earlier firing fix uses `#minecraft:enchantable/crossbow`, rather than hardcoding the vanilla item. The former Capacity magazine repair is superseded by full Capacity removal.

## Burst and normal crank

The 26th actual firing action fires its arrow, then sets a player-owned exhausted flag and displays **Empty - Reload** for 60 ticks (three seconds). Auto-feeding stops; a full normal crossbow crank is required to load ammunition and clear the exhausted flag. The notice expires after 60 ticks independently of the exhausted flag, and blocked firing attempts do not restart it. Heat display resumes afterward. A successful early crank or death clears the notice. There is no automatic three-second reload. Interrupted or incomplete cranks do not reset the burst. Normal weapon charging and draw-speed bonuses apply. Manual Frenzy loading accepts normal arrows only, preserving the auto-feed ammunition rule; fireworks and other arrow types cannot bypass the damage policy through native loading. Infinity preserves ammo on both auto-feeding and manual cranking, but still requires one normal arrow in Survival.

A partial counter resets after 60 consecutive world ticks without firing. An exhausted counter does not. Counters and exhaustion are shared across a player's Frenzy crossbows and both hands; a preloaded spare is blocked before vanilla clears its ammo or applies wear. Its loaded arrow is preserved. Normal crossbows remain independent. Timers advance once per world tick through the global tick function rather than once per equipped enchanted item. Death clears burst state; exhaustion persists through weapon changes, reconnects and server restarts until a successful crank.

Frenzy's original `ammo_use=999999` and `crossbow_charge_time=10000` effects are removed to restore native loading. The native successful-load hook clears exhaustion; the auto-feed function cannot. Native charge animations, sounds, ammo use and haste remain in charge. The explored custom true-damage type remains deferred; current heat self-damage is still `minecraft:magic`.

## Rebuild

```powershell
python docs/patches/bows-balance/build.py --classpath-file tmp/bows-balance/classpath.txt
python docs/patches/bows-balance/verify.py tmp/bows-balance
```

The canonical build applies `overrides/`, removes Capacity data/references, and compiles only `src/` into production. It registers `crossarms-pack.mixins.json` for the heat spread/counting hook and the native manual-loading guard. Provide a Java 25+ compiler and the NeoForge-patched 26.2 server/FML/Mixin dependency classpath. Original inputs are hash-pinned in the combined builder's `upstream.json`; final active hashes are in [artifacts.json](../bows-balance/artifacts.json). Tests never enter production archives. Personal worlds and configs are untouched.

## Verification

The final isolated server passed **1341 current Frenzy checks** with vanilla and Arsenal 1.5.1 crossbows, both hands, Ranged Weapon API 4.0.0 and Expanded Crossbow Enchantings 1.11. Checks cover actual 15/20/30/40% projectile/impact scaling, spread at every display boundary and a missing/negative score, the 26th-shot cutoff, no passive exhausted reset, notice remaining through tick 59 and expiring at tick 60 without unlocking, interrupted/early/full manual cranks, spare-weapon blocking, partial-counter timeout/restart, Infinity consumption/pickup, ordinary firing wear, first-shot slowdown and the exact 20-tick heat damage interval. A regression reproduced the review finding that a breaking final shot was not counted; eligibility is now captured before durability mutation, and a breaking 26th shot still blocks a preloaded spare.

A 96-arrow-per-stage trajectory sample measured centered angular spread projected to a 20-block plane: about 0.22 blocks at cold heat versus 0.43 blocks at five bars in this run. This measures launch-direction scatter, not hit probability or a full flight/collision simulation. The first sample incorrectly measured distance from the aim axis rather than scatter around the sample center; the corrected centered variance removes the launch offset.

The isolated client used a real player and the real Use key: it received the empty crossbow after shot 26, remained exhausted beyond 60 idle ticks, displayed the empty message initially and verified it gone after three seconds, and completed a native manual crank to unlock and load the weapon. The screenshot and result remain under `tmp/skill-overhaul/client/`; test source is `tests/ClientBurstProbe.java`.

The controlled impact targets have zero armor, and random arrow critical damage is disabled for comparisons; impact velocity is normalized to 3.15 to isolate damage scaling from random spread changing speed. With the test player's ranged-damage attribute at zero (no equipment modifiers applied), ordinary impacts were 7 HP; with that attribute explicitly set to 64, ordinary impacts were 45 HP. Frenzy scales that same projectile's existing damage, rather than replacing it with a fixed 40-50 HP value. The first shot already received the reduction; tests found no tap-to-reset bypass. Actual player damage still depends on weapon, gear, skills, critical hits and target defenses, and integer impact rounding can shift ratios by one HP. Logs are in `tmp/crossarms-firing/server/frenzy-notice-final.log`.

The first regression run failed because Capacity still existed. Review also caught an orphaned Capacity function and malformed actionbar continuations; both are removed/fixed, and the current archive has no function parse errors. The historical modded-crossbow repair passed 260 checks before Capacity was removed; those magazine checks describe the previous build, not this one.

Sources and attribution: [CrossArms](https://modrinth.com/datapack/crossarms), AlmazmanElytra, Din37r and IceBan; upstream content CC-BY-4.0. Original metadata and credits are retained.

21 October damage revision: cold/first/second bars retain **15%**, third **20%**, fourth **30%**, fifth **40%** of the normal shot. The updated isolated probe passed **1,341 checks**; spread, heat self-damage, Infinity and burst/reload rules are preserved. The resource-only repack and fingerprints are recorded in [the physical balance follow-up](../skill-overhaul/README.md#physical-balance---21-october-follow-up).

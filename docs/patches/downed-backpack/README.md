# Downed-player automatic consumption guard

Minecraft 26.2, NeoForge 26.2.0.90, Sophisticated Core 1.5.0.2337 and Backpacks 3.26.2.2154.
Same-filename Core replacement; the Backpacks archive is unchanged.

## Behavior

Feeding and Alchemy skip players downed by either PlayerRevive or Better Revive.
This covers worn storage and the shared target checks used by placed storage.
Food, drinks and automatic consumable buffs are not used on downed players.
An Alchemy drink already in progress pauses without advancing its timer or applying
its effect. Its reserved item stays with the upgrade; after rescue the original
drink completes normally. A downed wearer's Alchemy also pauses.

Healthy-player consumption, filters, normal cooldowns, inventory transactions and
consumption effects remain upstream behavior. Other upgrades are unaffected.
The revive integrations are optional; Core still works without either revive mod.

## Cause and implementation

PlayerRevive's `ReviveEventServer.playerTick` resets a bleeding player's food level
to its configured remaining-hunger value. Feeding previously checked only hunger,
consuming another food item each time that value remained low. Alchemy likewise
had no downed check when selecting a player or completing an already-started drink.

The shared guard reads the actual PlayerRevive bleeding capability and Better Revive
UUID state, behind loaded-mod checks. It runs before Feeding's per-player consumption
method, Alchemy's target selection and application, and Alchemy's tick advancement.
No global eating/potion restriction or new hard dependency is introduced.

## Build and verification

Preserve the original Core archive as
`tmp/downed-backpack/before/sophisticatedcore-26.2-1.5.0.2337.jar`, SHA-256
`3479a05f86fc65064c77099ec93e67c6f8e31953b1ef506ce5f83eec556c71a6`.
The scripts reuse the isolated NeoForge libraries and Java compilation classpath
under `tmp/magic-third-tier` and `tmp/nameless-balance`. Java 25+ is required.

```powershell
python docs/patches/downed-backpack/build.py
python docs/patches/downed-backpack/run_probe.py --baseline
python docs/patches/downed-backpack/run_probe.py
python docs/patches/downed-backpack/run_probe.py --no-revive
```

The baseline is expected to fail: repeated native feeding consumes food while the
PlayerRevive bleeding capability is set. The replacement passed **19 isolated runtime
checks**, including native food consumption, both downed flags, target rejection,
in-progress potion preservation and resuming the Speed potion after rescue.
A separate Core/Backpacks-only instance passed **7 checks**, including healthy
feeding and alchemy without either optional revive mod. Both ran with JVM class
verification enabled. Test-only probe and ASM tool classes are not packaged.

The Better Revive test inserts its native downed-state record without invoking
grave creation. This tests the actual public state predicate independently of
PlayerRevive, rather than claiming a complete multiplayer death/grave playthrough.
No new client UI test was performed. Personal Minecraft and the desktop server
were not changed. Upstream metadata and license notices are retained.

The rebuilt handbook has 74 pages and 461 internal links. Every page was rendered; all except changed page 14 are pixel-identical to the previously reviewed handbook. Page 14 was visually reviewed with no clipping or pagination change.

## Maximum health on knockdown

PlayerRevive now uses the player's current maximum HP on its normal death-to-downed
path and its server `startBleeding` API. Better Revive uses current maximum HP when
entering its downed phase and restoring that phase after login. These are entry
assignments, not continuous healing or extra invulnerability. Post-rescue health,
hunger settings, bleedout, final-death and grave rules are preserved.

`build-health.py` layers this over the existing pack builds. Preserve the two inputs
in `tmp/downed-backpack/before/`; `health-artifacts.json` records their pinned hashes.
Only PlayerRevive's two server classes and Better Revive's `DownedPlayerManager`
change. No new production classes or dependencies are added to either revive mod.

```powershell
python docs/patches/downed-backpack/build-health.py
python docs/patches/downed-backpack/run_probe.py --health --health-baseline
python docs/patches/downed-backpack/run_probe.py --health
```

The original PlayerRevive path failed the maximum-health check. The replacement
passed **38 combined health/consumption checks**: native death interception and API
entry at 20, 40 and 73.5 maximum HP, Better Revive entry at each value, gear adding
13 HP to a 20-HP base, configured post-rescue health remaining 7 HP, and the existing
food/potion tests. The Better Revive login path is patched and structurally checked;
a complete reconnect/grave multiplayer playthrough was not performed.

The health follow-up handbook still has 74 pages. All pages were rendered and compared with the reviewed consumption-fix edition; only page 26 changed. Page 26 was visually reviewed with no clipping or pagination change.

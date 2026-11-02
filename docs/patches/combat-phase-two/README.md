# Combat phase two: spatial behavior

This patch continues from the final Phase 1 archives. It changes reach validation, target eligibility and projectile movement; Phase 1 balance values, charges, Ward capacity, Frenzy impacts, grave preservation and accessory scaling remain intact.

- **Size and reach:** native player interaction checks now use the same proportional size multiplier as Better Combat targeting. Growth extends usable melee reach and shrinking reduces it. There is no additional persistent attribute modifier to become stale after an effect or accessory is removed. Block reach is unchanged. This repairs a demonstrated mismatch; the original affected player's exact saved loadout was unavailable.
- **Vanish:** hostile targeted skills cannot acquire or keep a vanished enemy. The server also rejects stale target lists. A dedicated synchronized flag lets remote client targeting distinguish Vanish from ordinary invisibility. Untargeted area damage, physical collisions and friendly targeting remain eligible. Existing violet breathing visuals and mark cleansing remain.
- **Flame Slash:** free-aim waves seek the nearest visible eligible enemy within eight blocks, steering at most two degrees per tick. Acquisition is local and refreshed every four ticks; range, speed, piercing and the Phase 1 damage coefficient remain.
- **Barrage:** its ordinary arrows already enter the Archer path's shared slight assist. This patch does not add another homing system or increase its strength. Shared arrow assist and Magic Arrows also reject vanished hostile targets.
- **Purgatory:** its first pulse applies a strong bounded pull. An orange five-block-high cylindrical boundary contains hostile living entities at the 12-block outer radius, or 14 with Improved. The caster, party allies and their pets can cross. It is open above, does not stop projectiles or teleports, places no blocks, and disappears when channeling ends. Inner damage radius, later-stage pulls, control rules, true HP costs and the 12s cooldown remain unchanged.

The Flame Slash steering/detection values and hostile-only wall policy are implementation rulings from the plan's recommendations, recorded in [the ledger](ledger.md).

## Build and checks

```powershell
python docs/patches/combat-phase-two/build.py
python docs/patches/combat-phase-two/run_probe.py
python docs/patches/combat-phase-two/run_regression.py
python docs/patches/combat-phase-two/run_client.py
```

Build inputs are the exact archives in ignored `tmp/combat-phase-two/before`. `baseline.json`, `artifacts.json` and the final completion record identify inputs, modified entries and installed outputs. Tests use isolated native NeoForge instances and Java verification. Client captures are reviewed separately from entity synchronization checks. No probe classes or test worlds are installed in production.

Both server and clients need matching replacement JARs and a restart. No configuration deletion is needed. Personal installations and the separate desktop server are outside this patch; no GitHub push is included.

## Completed validation

Installed five matching replacements in repository `mods/` and ignored fixed packs. **78 spatial server checks**, **196 Phase 1 regressions**, **9 integrated-client checks** and **59 archive/resource checks** passed. The [orange enclosure capture](purgatory-boundary.png) and the handbook's changed page were visually reviewed. The 80-page PDF retains 51 correct printed contents destinations and 471 valid internal links. [Completion record](completion.json).

The [2 November continuous-pull follow-up](../purgatory-continuous-pull/README.md) supersedes this build's half-second stage pulls and upper-only boundary: it adds continuous escalating pull, five-block underground depth, corrected upgrade range UI and 30% Ardent healing.

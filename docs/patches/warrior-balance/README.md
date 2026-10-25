# Warrior balance — 25 October 2026

Same-version overlay over the current Rogues & Warriors 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5 archives. Active filenames, saved skill IDs, tree titles and unrelated patches are preserved. Install the matching three JARs on client and server, then restart.

## Current behavior

- Killing Spree retains +2% melee damage per hit, five stacks and an 8s refresh. It grants no damage reduction. Each credited player kill heals 4% current maximum HP, independently of the hit-stack cooldown. Canceled deaths and protected targets do not heal; the node must be selected.
- Demoralizing Shout reduces melee, ranged and offensive magic power by 12%. Battle Shout remains an 8% ally bonus.
- Improved Wild Charge extends both its buff and control protection from 2s to 3s. Concussion Blow stuns for 1s; its 4s readiness remains.
- Each Berserker pulse grants Speed I/II/III/IV/V for 2s, refreshing on every pulse. Berserker lasts 14s, or 17s with Improved. Brutality matches that duration. Every pulse still fully heals; existing vulnerability, basic-attack bonus, rescue, expiry sound and death cleanup remain.
- Stun and Frozen interrupt both ultimates; roots do not. Inspection and a failing isolated test showed Frozen did not satisfy the previous generic casting-impairment predicate. The predicate now explicitly recognizes Frozen and stun alongside other casting-blocking effects.
- Enrage grants 3% melee damage and 6% size per health threshold, up to 18%/36%. Thresholds and refresh behavior remain.
- Tenacity absorption lasts 14s; its 30% HP trigger, shield amount, 2s nearby stun and haste-affected cooldown remain.

Sentinel now gains stacks at a 0.25s base interval. Lethality heals on successful melee impacts at a 0.2s base interval. Both use native haste scaling and one shared cooldown per player across targets. Lethality's 2 HP heal and 0.5s self-damage remain unchanged.

## Descriptions

Follows the approved [description rewrite](../skill-wording-review/RPG%20Skills%20-%20description%20rewrite.md): short effect text, escaped `%` values, no formulas or impact placeholders, and no repeated base cooldown/haste sentences. Existing yellow statistics and activation panels are preserved. Technical scaling stays in the [Warrior reference](../warrior-reference/readable.md).

## Build and verification

```powershell
python docs/patches/warrior-balance/build.py
python docs/patches/warrior-balance/run_probe.py
python docs/patches/warrior-balance/verify.py --installed
python docs/patches/warrior-reference/export.py
```

Build reads pinned local snapshots from `tmp/warrior-balance-20261025/before/`, creates candidates in `built/`, and records hashes/changed entries in `artifacts.json`. Narrow ASM edits retain current combat classes instead of recompiling historical sources. A small helper supplies effect registration overrides, kill healing, control detection, pulse-dependent values and Lethality healing through the native cooldown manager. Predecessors are in ignored `mod fixing/retired packs/warrior-balance-20261025/`; matching installed/fixed copies have identical bytes.

The isolated NeoForge 26.2.0.90 server passed **1,010 Warrior, Rogue, shield and party checks** with `-Xverify:all`. Coverage includes native kill healing, canceled/unselected kill rejection, removal of Killing Spree reduction, default/improved protection, Concussion stun, Shout reduction, all five Speed levels and refreshes, ultimate durations/interruption/death cleanup, Enrage scaling, Tenacity duration and repeated-hit/boundary checks for both new proc intervals. **4,520 archive preservation and tooltip checks passed.** The fixture excludes the separate global PvP multiplier to check raw skill behavior. No connected-client visual test or personal-world modification is claimed.

The handbook and owner Warrior reference are synchronized. No commit or GitHub push is part of this request.

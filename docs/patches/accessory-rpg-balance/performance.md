# Accessory tick overhead repair — 1 November 2026

Reported symptom: the multiplayer server becomes much slower with several players wearing full trinket loadouts, possibly during combat. No live server profile or exact loadout was available. The following redundant work was reproduced in the installed build; this is evidence of avoidable cost, not proof that it accounts for all live lag.

## Measured and changed

| Path | Before | After | Test scope |
| --- | --- | --- | --- |
| Woundbearer, unchanged stored bonus | 200 attack-attribute dirty updates | 0 | 200 native worn callbacks with a stable bonus |
| Tick, one-second proc cooldown active | 200 nearby-entity queries | 0 | 200 native worn callbacks; world-query calls instrumented in the test archive only |
| Duplicate-item guard | 5,544 slot reads | 984 | 24 simulated wearers, each with 21 distinct items, during one tick |

The earlier duplicate-prevention patch introduced a full slot scan before every native worn callback. With many accessories, this repeats increasingly more work per wearer. Server worn callbacks now share a per-entity, per-tick index of the first slot for each item. Item-change and slot-layout events invalidate it; equip/unequip permission paths still use live scans. A cached primary is checked directly so removal during a callback can promote the next copy before a notification. Values contain item/slot identifiers, not player/inventory references, and weak entity keys prevent offline players from being retained by this cache. Client callbacks retain the prior live lookup.

Woundbearer previously removed and reapplied its damage modifier before the existing idempotent update helper could compare it. It now removes the modifier only at zero stored damage, compares unchanged positive amounts and writes its component only when the stored value changes. Caps, effective damage, decay and immediate changes remain.

Tick now checks the existing persisted wearer cooldown before searching entities. It still claims the cooldown only after finding eligible targets, so an empty area does not delay a later target. Damage, targeting, party protection, radius, cost and one-second interval are unchanged. It still searches each tick when ready with no eligible target; that retained behavior is not included in the zero-query result above.

No Artifacts JAR change was justified by this investigation. Existing stat-scaling helpers already compare modifiers before replacement. Native effect refreshing, pet AI, networking and other combat handlers can still cost time; none was declared harmless solely from source inspection.

## Regression evidence

- The original installed archive failed all three new overhead assertions.
- The final staged archive passed **654 focused checks**, including all unique slots, same-tick duplicate insertion/removal, earlier-slot insertion invalidation, cosmetic exclusion and refresh on the next entity tick.
- The existing capped-scaling/balance suite passed **171 checks** with Nameless/Artifacts/Curios and **181 checks** with Better Party, Spell Power and Ranged Weapon API. These repeated suites are not additional unique tests to sum together.
- Operation counters are deterministic engine-loaded tests with fake players, not 24 real connected clients or a measured TPS improvement. Tick query instrumentation exists only in the disposable test copy.

The loadout counter exercises duplicate lookup with 21 distinct item types in Curios test slots; it does not run 21 native combat abilities simultaneously. The Woundbearer and Tick fixtures invoke their actual worn callbacks separately.

Reproduction uses `HotspotProbe.java`, `CountQueries.java` and `run_hotspots.py`. With the project's cached NeoForge libraries and Nameless development classpath prepared:

```powershell
# Before changing the installed build:
python docs/patches/accessory-rpg-balance/run_hotspots.py --label before
# After building the replacement with the existing Nameless pipeline:
python docs/patches/accessory-rpg-balance/run_hotspots.py --staged --label after
python docs/patches/accessory-rpg-balance/run_probe.py --staged --start
python docs/patches/accessory-rpg-balance/run_probe.py --staged --start --integrations
```

The latter two commands launch an isolated process; inspect its `result.txt` after it shuts down before launching another run. Evidence is retained in `performance-before-results.txt`, `performance-after-results.txt`, `performance-archive.json` and the ignored `tmp/accessory-performance/` / `tmp/accessory-rpg-balance/` logs. Baseline results predate three extra mutation checks per simulated wearer; the three operation-count fixtures are unchanged.

## Adventurer Skills and remaining diagnosis

Installed archive inspection found zero executable classes in the Adventurer Skills add-on itself; it supplies data consumed by Pufferfish Skills and the pack's compatibility mod. The installed `SkillOverhaul.tick` refreshes critical and owned-pet stats once per 20 ticks; `Utilities.tick` checks its player-only rations path every 10 ticks. The inspected Skill Tree conditional-attribute hook responds to equipment changes, and Pufferfish's ordinary attribute reward checks whether a modifier exists before adding it. These observations do not benchmark every node or combat effect. Pet damage/skill evaluation and other class spell paths remain profiling candidates when many summons or rapid hits are active.

If the actual server still spikes after updating, collect a local spark profile during the busy period, covering both quiet play and combat. See [the performance audit](../performance-audit/README.md) for compatible candidates and commands. spark remains a candidate, not an installed pack dependency. The live server must run the changed build before an improvement can be assessed; client-only updates do not change server ticks.

## Distribution

Same active filename: `mods/Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar`. Repository and ignored fixed-pack copies are updated. The original is backed up under ignored `mod backup/accessory-performance-2026-11-01/`. Only executable classes changed (33 replacements and two cache records, all Java 25); balance resources and tooltips are byte-identical. No probe or counter hook ships. Personal installations and live servers were not edited, and no push was requested.

The gameplay catalog, indexes, controls, item values and screenshots do not change for this internal performance repair, so it needs no additional handbook/PDF edit. The preceding grave fix's rebuilt handbook is preserved.

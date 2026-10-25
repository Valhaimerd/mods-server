# PvP combat limits and status HUD

25 October 2026. Extends the existing pack damage layer on Minecraft 26.2 / NeoForge 26.2.0.90. Sync all three same-filename JAR replacements to clients and server, then restart.

## Player behavior

A confirmed damaging player hit flags both players for 40 seconds, including sources resolved through the existing projectile and RPG summon owner rules. Further hits refresh combat silently. Zero, canceled, self, environment and protected party/team hits do not flag players. The existing 35% PvP / 80% player PvE damage retention is unchanged.

During combat:

- Sophisticated Building advanced placement, replacement, breaking, mirrors/arrays and undo/redo are blocked. Pending builds are discarded on the next queue tick and do not resume. Choose **Disable** in its radial menu for ordinary placement/mining.
- OneKeyMiner chain actions are blocked, including delayed verified mining and chained tool interactions. Ordinary native actions remain available within the quotas.
- Ordinary building allows **20 placed blocks** and **30 broken blocks**, each in its own **30-second window**. Each timer starts with the first corresponding attempt. Only successful changes consume allowance. A multi-place that would exceed the remaining budget is rejected as a whole.
- Failed attempts cannot extend a window. Combat refreshes cannot refund either budget. Limits end with combat, even if a block timer still has time remaining.

One private chat message announces a fresh flag. Short, throttled action-bar messages explain blocked tools and capped actions. Three touching 22-by-22 GUI-pixel squares sit exactly at the bottom-left screen edge: **PvP / Breaking / Placement**. The skill panel's native height is 22 pixels. Countdown text is inside each supplied icon; block timer 0 means ready. They hide outside combat and with the game's hidden HUD.

[Connected-client HUD preview at GUI scale 3](hud-preview.png).

## Implementation

`CombatState` holds absolute millisecond deadlines and counters. `CombatRules` stores only this small namespaced record in NeoForge's existing persisted player data. Clone copies that record, so reconnects, death and dimension changes do not refund allowances. Offline time still expires. No live entity maps, new entity types, scheduled executors or background processes are added. Expired data is removed on the next player-only maintenance tick or login; client countdowns hide at their own deadline. Status packets are server-to-client only, on state changes and once per second while data exists.

Native break and place permission events reject over-budget actions; successful native removal/accepted placement hooks charge only completed actions. NeoForge's original placement rollback restores denied predictions and inventory. Sophisticated Building's shared permission, undo/redo and queue execution paths are guarded. OneKeyMiner's shared chain executor rejects advanced work before target traversal. Optional bridges leave either tool mod usable without PvP Flagging.

The source files and `build.py` preserve every unrelated archive entry, including the prior axis patch. The existing player-source/party/damage class is retained byte-for-byte. `artifacts.json` lists exact baselines, final hashes and entry changes. User-supplied source images are retained under `assets/` and packaged without image edits; the renderer scales them to the icon slots. No diagnostic classes are included in production.

## Reproduction and verification

```powershell
python docs/patches/pvp-combat-limits/build.py
python docs/patches/pvp-combat-limits/run_server.py
python docs/patches/pvp-combat-limits/run_client.py
python docs/patches/pvp-combat-limits/verify.py --installed
```

The server fixture also reruns the preceding damage-retention regression. Connected-client captures inspect two GUI scales, real payload delivery, fresh versus refreshed notices, hidden-HUD behavior and expiry. The final run passes 45 pure timer/budget checks, 118 damage/party/summon checks, 85 native combat/block/tool checks and 10 connected HUD checks/capture records, plus the exact two-fresh-notice check with a silent refresh. Adjacent results files record the individual assertions. The tests use disposable repository worlds and the cached runtime, never personal saves or the separate desktop server. Native removal refusal has a dedicated regression after review identified that destroyBlock returning true does not itself prove block removal.

Respawn/tag restore is exercised by the server fixture; this is not a full multiplayer disconnect/reconnect stress test. Full-pack manual mouse play and high-latency networking are not claimed. The GUI expiry fixture waits on wall-clock time rather than a fixed number of client ticks, since GUI resize catch-up ticks initially produced a premature test assertion.

Final production artifacts are installed in repository `mods/` and `mod fixing/1 fixed packs/`; matching predecessors are preserved in `mod fixing/retired packs/pvp-combat-limits-2026-10-25/`. The handbook PDF was rebuilt and visually checked, with all table-of-contents destinations audited. No commit or push requested.

## Live damage command overlay

The later [damage commands](../damage-commands/README.md) make the 35%/80% defaults configurable without changing combat timers or tool rules. Use that overlay?s artifact verification for the installed PvP JAR; this page retains the earlier combat-test baseline.

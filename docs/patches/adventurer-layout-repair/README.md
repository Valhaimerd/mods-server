# Adventurer Defense and Utilities layout repair

4 November 2026. Corrects the installed phase 1-3 graph without changing the approved defensive balance or Utility benefits.

The previous patch rewired Defense but retained its old coordinates, producing long diagonal links and a malformed Colossus merge. It also moved Utilities without authorization: Fitness's first node and Aegis's fifth node both occupied `(-340, -204)`. They were separate graph nodes drawn on top of each other, creating an apparent DefenseÃ¢â‚¬â€œUtilities connection. Earlier checks covered links and rewards but missed node overlap and edge geometry.

- Restore every Utility node's original position and original ordinary-node connections from the pinned pre-phase-1-3 layout. Remove the mastery spokes from earlier nodes; only each path's two final approach nodes connect to its mastery.
- Arrange Defense in three horizontal rows. Each has five sequential nodes, a two-node split and a specialty merge. Keep the Defensive entrance in place.
- Retain all allocated node IDs, rewards, exclusivity, runtime cooldowns and complete-path checks. No refund or new migration is needed. Full-path totals remain Aegis +4 HP/+2 armor, Colossus +6 HP/+2 armor, Bulwark +2 HP/+4 armor, with the approved other benefits unchanged.
- Keep the existing item icons and normal node size. Improve ordinary Defense labels to `Max HP` and `Armor`.

Only the Adventurer Skills JAR's `skills.json`, `connections.json` and `definitions.json` change. The compatibility runtime contract, all class mechanics, and Purelight/Hydra follow-ups remain untouched. `layout.json` records the recovered Utility layout so reproducing this fix does not require an ignored historical JAR.

## Verification

The regression check fails against the installed input (26 failures, including Utility positions, graph differences and coincident nodes), then passes against the candidate (231 checks plus overlap, edge-through-node and crossing-edge scans).

```powershell
python docs/patches/adventurer-layout-repair/build.py
python docs/patches/adventurer-layout-repair/verify.py
python docs/patches/adventurer-layout-repair/run_client.py
```

Connected Minecraft validation and installation results are recorded in `progress.md`. Personal worlds and the separate desktop server are untouched. This routine graph fix does not rebuild the handbook or push to GitHub.

Final result: **231 resource checks and 46 connected-client/server checks passed** on Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25. Defense close-up and combined Defense/Utilities screenshots were reviewed. The exact tested same-name Adventurer JAR is installed in repository active/fixed packs; both originals are backed up under `retired packs/adventurer-layout-repair-2026-11-04/`. Restart both sides with matching files.

# Agritech / Delight compatibility verification

Installed Agritech 3.0.0.5+neoforge-mc-26.2 with Farmer's Delight 1.3.3-fix5, Expanded Delight 0.1.4-fix1, Display Delight 1.7.0-fix2, Ocean's Delight 1.0.4-port1, Large Meals 1.0.1-port1 and End's Delight 2.6.1-port1.

Verdict: targeted client/server coexistence and base Farmer's Delight farming PASS; automatic support for Expanded Delight crops is absent. This is not full-pack performance certification.

| Check | Command | Result | Notes |
|---|---|---|---|
| Server integration | `python tmp/agritech-check/run.py` | PASS | 489 checks: 92 Agritech checks plus 397 existing Delight checks; NeoForge 26.2.0.90, Java 26, 2GiB heap |
| Client integration | `python tmp/agritech-check/client_check.py` | PASS | 434 checks; NeoForge 26.2.0.90, Java 25, JEI 30.32.0.215, 3GiB heap |
| Installed bytes unchanged | SHA-256 comparison against installed.json | PASS | Seven installed JARs unchanged |
| Upstream identity | Modrinth version-file SHA-512 lookup | PASS | Exact installed Agritech archive matches official 26.2 NeoForge release; upstream-version.json |
| Full-pack soak/performance | Not run | SKIPPED | Isolated functional verification only |

Four FD crops (cabbage, tomato, onion and rice) accept ordinary farmland and rich-soil farmland in planters and raised beds. Eight planter harvests were exercised through native tick/harvest paths with accelerated growth counters; expected produce appeared and seeds remained. Raised-bed crop validation and native drop generation passed. Plain dirt and untilled rich soil are correctly rejected for these recipes; the author documents right-clicking with a hoe to till inserted soil. Rice produces rice panicles.

All four Expanded Delight crop inputs (asparagus seeds, chili pepper seeds, peanut and sweet potato) were confirmed unsupported without extra Agritech recipes. No crop recipes reference any of the four installed add-on namespaces; Ocean's Delight and Large Meals are principally food mods, so this is not itself a loading failure. Display Delight's availability, traders and existing add-on content passed regression checks.

Client verification covers Agritech's two populated JEI categories, its visible items and nonempty item render states, plus existing Delight client/JEI/End Stove sync/retrieval regressions. No missing-texture or model-binding warnings were detected in this run. It does not prove all planter GUIs, block-entity rendering, multiplayer automation, long-term growth timing, or large-farm tick performance.

First exploratory server pass had eight test expectation failures: the fixture incorrectly expected dirt and untilled rich soil to be accepted. The shipped farmland_soils tag confirmed those expectations were wrong; no production fix was made. Corrected positive/negative soil checks passed on a fresh instance. Old copied test-world Puffish gamerule and offline authentication errors are known fixture noise; Windows also logs unavailable Linux/BSD Netty backend diagnostics. No related Agritech/Delight loading or recipe errors were found.

Evidence: installed.json, upstream-version.json, AgritechChecks.java, run.py, client_check.py, ../delight-batch/agritech-combined-2/console.log, client/client-results.txt and client/launcher-output.log. All worlds and probes are isolated under tmp. No production JAR bytes, personal .minecraft, or separate desktop server was modified. The subsequent GitHub synchronization adds Agritech and updates the handbook; no additional crop recipes are installed.

## Distribution update - 30 October 2026

The Agritech addition brings the pack to 308 main JARs, 23 client extras and 185 gameplay catalog entries. Its official Modrinth icon, Farming/Storage index links, tested crop limitations and source link are included in the rebuilt 79-page handbook. All 51 table-of-contents page numbers match their PDF destinations; all pages were rendered and visually reviewed.

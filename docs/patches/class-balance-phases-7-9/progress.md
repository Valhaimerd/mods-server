# Combined phases 7-9 ledger
Status: phases7-9 complete; four exact tested replacements installed in repository active/fixed packs.
Inputs: current installed phases 1-6 plus Hydra detection overlay; baseline.json pins exact bytes.
Execution: inline. No extra workers, personal/live server changes or Git push. User's latest handbook exception overrides the older phase completion checkboxes: patch notes only, no PDF work.
Confirmed by user: other lifesavers/totems first; Purelight then cancels fatal hit/full heal, no added cooldown, rearms only after actual downing/death, drain persists through dispel/respec/reconnect. Onslaught readiness5s; direct player melee/ranged/spell damage only, excluding periodic/summon damage.
- [x] Priest tuning and fixed Ascension support
- [x] Paladin passives, block windows, dome, Purelight
- [x] Archer clouds, projectiles and passives
- [x] Isolated native/client integration and matching installation

Verification checkpoint: baseline resource test0/17; candidate17/17. First native probe73/73; expanded83/83 with revive mods present. Fixed stale window reuse on dispel/reapply, and excluded effective-owner summon spell hits from Onslaught. Client fixture corrected selected-slot order and actual Paladin path allocation (channel doctrine checks remain intact). Final combined reruns pending.
Final review checkpoint: added respec cleanup for Onslaught active buffs and stale Deflection parents; applied the new direct-player scope even when summon spell damage attributes ownership to a player. Fade had a second runtime20-tick override, now patched40. Baseline17 requested resource failures -> candidate17 passes. Prior-pass regressions104 shared +97 mage +17 Hydra and22 connected-client checks pass; exact final hashes are being revalidated.


## Final evidence
- Final resource assertions17/17; baseline0/17 (17 expected failures).
- Java25 / Minecraft26.2 / NeoForge26.2.0.90 isolated server96/96 final checks, with the installed PlayerRevive, Better Revive, their integration bridge and CreativeCore present. Covers actual healing, fixed Ascension under50/100/200 haste, self/ally range differences, real50% physical/magic reduction, Serenity no refill, Guardian15/20%, Vanguard15/25%, roof/bottom collision, actual native Purelight rescue/drain/save-load, clouds, Onslaught and windows at ticks0/4/5/10/14/15.
- Exact final-byte regression:104/104 shared phase1-3;97/97 mage phase4-6;17/17 Hydra detection. Four runtime manifests pin candidate bytes.
- Fresh connected client26/26: status/absorption sync, actual body movement against dome roof/underside, hostile teleport event denial, early stun interruption/late stun immunity, cleanup, totem priority, actual Purelight client-player rescue, tick drain/dispel/respec, Onslaught sync, real PlayerRevive knockOut/revive and Better Revive downed-state API integration.
- Final artifact review and install verification:90 checks, including CRC/unique entries/original metadata/no probes, exact mutation records, all five runtime-tested hash manifests, active/fixed copies, and unchanged prior Hydra/Mage/Warrior/charge helpers.
- Ruling: a legal third concurrent Roots cloud retires the oldest, preserving existing cooldown rather than granting banked casts. Reason: requested two concurrent clouds. Cost if wrong: third legal cast replaces an old field rather than being refused.
- Ruling: use the existing +/-5-block height and12/14-block outer radius for a closed ellipsoidal collision volume. Reason: closes the old wall's top/underside without increasing reach. Cost if wrong: reachable space narrows near the roof/bottom compared with the old open cylinder. Existing orange presentation is retained.
- Confirmed Purelight lifecycle: other lifesavers/totems first; prevent fatal hit/full heal; no added cooldown; one active loop until downing/death; persists through dispel/respec/reconnect. Direct HP drain bypasses absorption and cannot retrigger itself. No arbitrary cooldown or resurrection immunity added.
- Confirmed Onslaught scope: five-second readiness, next direct player melee/ranged/spell damage only. Passive/periodic/summon spell damage excluded even with player-attributed ownership.
- Self-review performed inline per the approved no-extra-workers plan. Found and fixed stale removed/reapplied block windows, true runtime Fade override, respec effect cleanup and summon ownership scope. Tests were rerun after the final production edits. No required phase implementation deferred.
- Runtime fixtures are isolated and do not cover every arbitrary third-party damage type or teleport implementation. Native shared damage/teleport hooks and current installed revive integration are exercised; no claim of manual testing every mod is made.
- Original active/fixed copies backed up under mod fixing/retired packs/class-balance-phases-7-9-2026-11-04. Unrelated World Map/user changes preserved. No personal/live-server changes, handbook/PDF rebuild, commit or push.

# Phase-two execution ledger

Plan: docs/superpowers/plans/2026-11-02-combat-followup-phase-2-multiplayer.md

- Start: phase-one installed outputs preserved on the existing work branch. No commit or push requested. Candidate/test data isolated under ignored tmp/combat-followup-phase-two.
- Investigation: native handleMovePlayer calls Entity.move but can then absSnapTo the original client coordinates. Its small-movement tolerance and non-block enclosure geometry allow repeated short crossings to overwrite clipped motion. Live Better Party membership API is already queried; no permanent party exemption was found in the boundary code.
- Baseline dedicated-server/real-client RED: outsiders entered/exited; former, kicked and dissolved-party members exited. Current party passage worked. Large-size native attack validation failed despite correct entityInteractionRange.
- Reach investigation: native handleAttack uses isWithinAttackRange and the item's AttackRange component, not entityInteractionRange. Better Combat client targeting uses scaled weapon range and a shoulder origin; optional direct server validation also used unscaled weapon range. Correct the actual validation paths, preserving native item gates and non-Better-Combat behavior.
- Ruling: keep the native packet processing intact, then reconcile only accepted physical player displacement against active hostile walls on the server thread. No saved player exemptions or periodic world scans. Genuine teleports remain under their prior policy.
- Ruling: Better Combat registered non-piercing weapons validate from the same scaled shoulder origin/range as client selection, retaining the native validation buffer. Native piercing components retain their own behavior; optional Better Combat direct checks receive the same size multiplier.
- Fixture correction: client gameDirectory is unavailable during mod construction; use the process-local relative isolated exchange directory.
- Fixture correction: actual Better Party vanilla-team reconciliation requires its team bridge to synchronize the fixture party. A raw manager-map injection can be discarded by the mod's next reconciliation tick.
- Fixture correction: use non-burning Creepers for measured hit health. Daylight-burning Zombies otherwise produce false damage successes. Unlock the real Enrage node and lower current health; a manually applied Enrage effect is correctly removed by native passive upkeep when unselected.
- Fixture correction: give the synthetic caster to BarrierEntity's native owner cache, since an unspawned FakePlayer is absent from world ID lookup. Repeat baseline with this resolved owner; the physical player remains a real remote connection. Real hostile-wall entry/exit and former-party bypasses reproduced again.
- Fixture correction: separate token-named test exchange files avoid Windows rename/read locks; this is isolated harness I/O, not production network logic.
- Fixture correction: Titan and Enrage size combine multiplicatively (1.25 × 1.24 = 1.55). A baseline size-expectation failure was a fixture assumption, not a production defect, and was corrected before the final candidate suite.
- Final checks: 61 real-client/dedicated-server checks, 14 native lifecycle checks and 51 geometry checks passed. Installed two candidate JARs, preserving 2,631 unrelated entries and phase-one outputs.
- Final review: one read-only reviewer found no important actionable defect. No changes requested after review.
- Documentation/PDF: current phase status and player notes updated; 80 pages, 469 valid internal links and 51 correct TOC destinations; pages18–19 and contents visually reviewed. No personal profile/server/world changes, commit or push.

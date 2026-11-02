# Phase-one execution ledger

Plan: docs/superpowers/plans/2026-11-02-combat-followup-phase-1-class-reworks.md

- Baseline: six current active JARs snapshotted; unrelated untracked planning documents preserved. Work isolated on `work/combat-followup-phase-1` with candidate JARs and test instances under ignored tmp.
- Resource RED: all nine initial checks failed against the baseline for the requested changed values. Runtime RED reproduced old/overridden effect values; later native registration hooks identified as the cause and final registration overrides added.
- Ruling: use the installed `rogue_root` as Assassin path selection, carrying a hidden modifier reward; no new tree entry or allocation cost.
- Ruling: base Last Stand contributes 2% and Juggernaut contributes the additional 2% per pulse, reaching the requested 4% total without another magic damage layer.
- Ruling: critical bonuses are six native attribute points per pulse before other modifiers. Effective values can receive existing outside total multipliers.
- Ruling: Mutilate has no extra native half-swing delay; keep the original full-strength dual-wield delivery and node hooks, but constrain melee-hit range separately from cast range.
- Fixture correction: FakePlayer does not reproduce real teleport movement. Use connected integrated-client evidence for destination/synchronization; native fixture verifies landing geometry and other runtime effects.
- Fixture correction: force local test chunks and wait thirty ticks before local entity searches; a fresh server-start fixture otherwise searches inaccessible entity sections.
- No personal profile, separate desktop server or user world modified. No commit or push requested.
- Final review: one read-only reviewer identified a real Killing Spree eligibility issue: nondamaging active impacts could apply the old stack effect. The added heal now uses successful damaging impacts/native damage events, preserving the old stack behavior. Negative Shout-healing coverage passes.
- Final runtime evidence: 70 native checks and 11 connected integrated-client checks passed. Includes actual Player.attack sprint scope, native Mutilate cast rejection and validated delivery, Assassin unlock/reset, six physical/magic mitigation cases and Lethality no-cost thresholds/intervals.
- Installation complete: six same-filename replacements, 4,161 unaffected entries preserved, matching ignored fixed copies. Handbook80 pages/469 links/51 TOC destinations, changed pages reviewed. Phases 2/3 remain planned.

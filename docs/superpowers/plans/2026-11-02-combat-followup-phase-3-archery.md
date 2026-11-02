# Combat follow-up phase 3: archery reliability

> **For agentic workers:** Use superpowers:executing-plans and systematic debugging, inline without additional agents.

**Status:** Implemented and installed, 2 November 2026. [Evidence and limits](../../patches/combat-followup-phase-three/README.md). Phases 1/2 preserved; not committed or pushed.

**Goal:** Repair Frenzy reload reliability and keep slight arrow correction useful at higher projectile speeds.

**Spec:** [Shared detailed specification](2026-11-02-purgatory-player-wall-enforcement.md).

**Architecture:** First resolve authoritative firing/reload state transitions without removing Frenzy's intended iframe bypass. Then tune the existing shared slight-assist steering once, without adding another homing system.

**Tech stack:** Current crossbow/Frenzy implementation, shared Archer arrow assist, native projectile and network paths.

## Assigned specification sections

- Frenzy rapid-click reload desynchronization investigation.
- Archer slight homing at higher projectile speed, including image-reference interpretation and acceptance criteria.

## Tasks

- [x] Snapshot current outputs; identify affected weapon/game mode and actual client/server hashes and configs.
- [x] Reproduce native premature reload and remote loaded-magazine lockup; compare multiplayer Survival (the corrected affected mode) and Creative with rapid loopback input. Trace authoritative shot/ammunition/reload transitions. WAN delay/backlog is unmeasured.
- [x] Audit the iframe-bypass hook and fix the demonstrated state error. Preserve valid manual reloads, game-mode ammo policy, damage/heat and protected targets.
- [x] Verify empty/partial/loaded magazine behavior, reload interruption/completion and hand/item-copy changes. Preserve persisted recovery data and the byte-identical iframe hook. Reconnect playtesting and a new iframe combat run are not claimed.
- [x] Measure existing slight assist at baseline and **1.5x projectile speed**. Use the attached small target box as a qualitative near-miss goal, not an invented world-distance measurement.
- [x] Tune bounded speed-aware steering modestly; start evaluating about **1.5x current turn allowance at 1.5x speed**, then record final measured values. Preserve velocity magnitude, narrow acquisition, visibility and party/Vanish safeguards.
- [x] Verify native ordinary/fast arrows and larger misses; preserve the shared Barrage/Frenzy assist path and byte-identical acquisition, safeguards and Magic Arrows profiles. Moving/competing-target visual testing remains unmeasured.
- [x] Install matching same-filename outputs, preserve phases 1/2, and update evidence, descriptions and required handbook artifacts.

## Completion checkpoint

Rapid input does not create false reloads or permanently prevent valid reloads, and clients converge on server state. Slight homing corrects small fast-arrow near-misses without sharp turns or broad target tracking. Report final tuning and observed multiplayer limitations; push only when requested.

Completed: 21 native checks, eight remote assertions, 128 trajectory checks per run and 2,529 preservation checks. Hits in the static near-miss fixture: 7/18 original, 8/18 candidate. Final turn allowance: 0.25? at speed 3, 0.375? at speed 4.5, capped at 0.5?; total steering remains 2?. Documentation and handbook synchronized.

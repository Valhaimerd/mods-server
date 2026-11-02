# Combat follow-up phase 2: multiplayer movement and reach

> **For agentic workers:** Use superpowers:executing-plans and systematic debugging, inline without additional agents.

**Status:** Implemented over phase-one outputs and installed in repository pack folders on 2 November 2026. No commit or push.

**Goal:** Fix demonstrated multiplayer wall bypass and size/reach mismatches through authoritative player validation.

**Spec:** [Shared detailed specification](2026-11-02-purgatory-player-wall-enforcement.md).

**Architecture:** Trace remote-player movement and attack paths before changing them. Repair the narrow failing server/client boundary while retaining the existing enclosure, party rules and configured size-to-reach relationship.

**Tech stack:** Current Spell Engine, Paladins, Better Combat, Better Party integration, Nameless Trinkets and Warrior size effects.

## Assigned specification sections

- Purgatory requested behavior and interpretation, existing approaches, implementation tasks and acceptance criteria.
- Multiplayer melee reach investigation, including its complete checklist and acceptance criteria.

## Tasks

- [x] Snapshot phase 1 outputs and inspect actual installed hooks. Compare affected server/client evidence if available; do not alter external installations without authorization.
- [x] Reproduce wall crossing using a real remote client, including party membership changes. Trace accepted movement packets and existing clipping.
- [x] Fix authoritative wall enforcement and current-party passage permissions. Handle overlapping bodies and swept movement without repeated correction traps; preserve existing dimensions and cleanup.
- [x] Reproduce remote attack reach at measured distances with ordinary size, Titan's Mark, revised Enrage, combined growth and shrinking.
- [x] Trace each server hit-validation gate and effective scale/range. Separate reach rejection from canceled damage, iframe behavior, stale attributes, ping and tick backlog.
- [x] Fix only demonstrated mismatches. Preserve weapon-specific reach, target geometry, configured proportional size scaling and unchanged block reach.
- [x] Verify outsider entry/exit, join/leave/kick/dissolution, overlapping walls, death/end cleanup and measured damage-registration reach with a dedicated server and real client. Keep tests focused on changed paths.
- [x] Repack current archives without losing phase 1 changes; update patch evidence and required documentation/handbook artifacts.

## Completion checkpoint

Outsiders and former party members cannot physically cross an active wall; current party members can. Size changes affect actual accepted melee reach consistently on multiplayer, with baseline restored on removal. Record proven causes and remaining limitations; integrated-only or geometry-only checks cannot establish dedicated-server success.

Evidence: [multiplayer patch record](../../patches/combat-followup-phase-two/README.md). Dedicated-server/real-client checks and geometry checks passed. External live-server version/config comparison and separate reconnect/dimension playtests remain unavailable; these limitations are recorded explicitly.

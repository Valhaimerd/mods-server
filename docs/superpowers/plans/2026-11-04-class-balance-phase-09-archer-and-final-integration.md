# Phase 9 - Archer changes and full-pack integration

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, isolated-runtime tested and installed in repository pack. **Depends on:** 01-08.

**Goal:** Finish Archer tuning using the shared defense implementation and verify the complete combined class release.

**Architecture / owned scope:** Archers, Skill Tree, Spell Engine projectile/roll/readiness/defense hooks; all preceding phase artifacts for integration tests.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Archer rework and Ascension revision - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#archer-rework-and-ascension-revision---4-november-2026)
- [Shared attack-block windows: Divine Protection and Archer Deflection](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#shared-attack-block-windows-divine-protection-and-archer-deflection)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Implement Roots and early passive tuning

- [x] Roots 8/6s cooldown, 4.5s clouds, maximum two concurrent clouds, one-second roll lock refreshed by pulses; no doubled slow. Rhythm 4%x5=20%; Concussive four-second cooldown.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Connect Deflection to the shared windows

- [x] Three charges, 30s cooldown, direct physical/magic qualification, five-tick windows; opening a window grants 1.3x speed two seconds, refreshing not stacking. Preserve low-health trigger and ten-second parent duration.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Tune offensive arrows and Onslaught

- [x] Magic Arrow speed 1.3x (baseline 1.95), Conjured seeking delay calibrated for speed; Barrage 70% normal shot. Onslaught roll readies next damage to grant 30% ranged attack speed/15% ranged damage four seconds, eight-second cooldown; replace two-Rhythm-stack grant.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Verify independent Archer behavior

- [x] Test two-cloud lifetime/over-limit policy, roll denial client/server, timed windows and speed refresh, real weapon shot multipliers, magical seekers, readiness one-use and Rhythm coexistence.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 5. Run cross-class and saved-build integration

- [x] Verify final mitigation/penetration, shields and no-refill overlap, control/escape against dome, healing locks, fatal-defense ordering, ordinary and enchanted projectiles, high haste, saved skill IDs/gear/Curios and respec. Summons must not inherit unintended projectile multipliers or class procs.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 6. Finalize player documentation and delivery evidence

- [x] Confirm every approved requirement has an owner/test and no unresolved behavior is claimed complete. Use focused patch notes/raw-reference notices; handbook/PDF rebuilding is waived by the user?s newer instruction. Verify active/fixed JARs match tested hashes; provide exact installed-versus-deferred report. No push unless requested.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Resolve Onslaught readiness duration and qualifying periodic/summon damage. Calibrate Conjured delay; do not silently add banked Roots casts or revive the old Ascension immunity.

## Completion gate

Archer and all combined preceding phases pass native and connected-client checks; gameplay, focused patch notes and tested JAR identities agree.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update focused player references/patch notes under the user?s newer handbook exception before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

Final implementation, exact hashes, settled lifecycle choices and verification: [combined phases 7-9](../../patches/class-balance-phases-7-9/README.md).

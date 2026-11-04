# Phase 6 - Frost damage status and targeted spike rework

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, isolated-runtime tested and installed in repository pack. **Depends on:** 01, 05.

**Goal:** Implement the new Frost statuses, targetable spike, proc passives and charged/homing Lance while preserving explicit final coefficients.

**Architecture / owned scope:** Wizards, Skill Tree, Spell Engine status/timing/projectile helpers. Basic weapon speed/homing already belongs to phase 05.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Frost Mage, trap and Warrior additions - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#frost-mage-trap-and-warrior-additions---4-november-2026)
- [Precedence with this plan's new Frost rework](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#precedence-with-this-plans-new-frost-rework)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Implement finite damaging Frostbite separately from Frozen control

- [x] Five seconds at the native freezing-damage threshold; ten after a successful Frozen application, no infinite cold or duplicated periodic damage. Settle class/basic/summon scope and immunity policy.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Update Nova and replace spike delivery

- [x] Nova Deep Freeze +1s, 3.5s with Improved. Splinters exactly 20% P plus Slowness III four seconds. Spikes target 24 blocks, cast 0.2s, fixed-location erection after 0.25s, 3x visual, radius two, 80% P, Frozen one second. Glacial instant/no-delay 100%; Fan one-second-later radius-three/20% P/0.5s Frozen explosion.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Revise Frost passives and Shield

- [x] Winter's Chill next-basic-staff readiness ten seconds, no cooldown; Near Zero replaces qualifying Frozen duration with five seconds while preserving its proc cooldown. Shield expiry burst 30% P/Frozen one second. Audit Arctic Reflex damage x1.2 once.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Update Lance and Blizzard control

- [x] Lance 0.75s charge, speed 1.6-2.4, slight homing; Colossal no crit bonus, projectile-only 30% armor penetration. Blizzard 0.25s Frozen per target with 1.25s freeze interval; damage continues normally.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 5. Verify damage, control and visual geometry

- [x] Test actual native cold damage and expiry, delayed locations/dodging, branch priority/Improved interaction, armor penetration, per-target clocks, boundaries, party exclusions and normalized damage.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Resolve Winter's Chill qualifying casts/consumption, Near Zero override scope, Frostbite source scope and spike Improved coefficient interaction. These block only their dependent tasks, not independent Lance/Shield resource work.

## Completion gate

New Frost damage/control meanings are distinct; targeted spike is dodgeable; explicit final coefficients and per-target freeze intervals pass native/client checks.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update relevant player references/handbook and review changed renders before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

Verification and exact input/output hashes: [combined phases 4-6 evidence](../../patches/class-balance-phases-4-6/README.md). The settled rulings and completion record are in its ledger.

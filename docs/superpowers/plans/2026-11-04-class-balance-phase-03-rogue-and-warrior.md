# Phase 3 - Rogue and Warrior mechanics

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, verified and installed together on 4 November 2026. **Depends on:** 01, 02.

**Goal:** Implement the close-combat class changes together over their shared Rogues/engine archives, with separate task tests.

**Architecture / owned scope:** Rogues & Warriors, Skill Tree and Spell Engine class helpers. Phase 02 owns Multi Roll; phase 05 owns mage-trap checks.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Rogue requested changes](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#rogue-requested-changes)
- [Preserved behavior and exact interpretations](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#preserved-behavior-and-exact-interpretations)
- [Details to settle before dependent implementation](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#details-to-settle-before-dependent-implementation)
- [Warrior changes](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#warrior-changes)
- [Timing, healing and cleanup](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#timing-healing-and-cleanup)
- [Shared traps and Warrior projectiles](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#shared-traps-and-warrior-projectiles)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Implement Rogue resource and effect tuning

- [x] Shock 12s; Smoke 65 evasion; Explosive Weakness/Blindness 3s; Vital Hit 1+floor(D/13) armor per stack plus 5% target-max-HP physical proc; Shadowstep root 1s; trap lifetime 25s, Serrated/Bloody Bleed 5s, Extensive radius 2, Bloody roll lock 5s.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Implement Rogue pressure and stealth behavior

- [x] Coated Blades retains Poison plus the defined 8s regeneration lock. Calibrate Slice pull. Base Vanish permits sprint/roll while preserving break distinctions, movement penalties, visibility and lock-on protection.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Tune Warrior throws and ordinary defenses

- [x] Net speed 1.4, weapon speed 1.8, existing charge curves intact. Vitality +1 armor/+2 HP per stack; Endurance eight total armor.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Revise the Warrior ultimates

- [x] Five pulses over four seconds, 0.8s base spacing with existing channel haste; cooldowns 40/45/50/55/60. Last Stand adds 20% max-HP healing each pulse, no base Regeneration I/no fifth full heal; retain new-HP healing and first-pulse knockback immunity. Berserker rescue stun eight seconds.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 5. Verify actual combat and cleanup

- [x] Exercise native hit/proc paths, animation timing, pull under walking/speed, poison/healing prevention, recasts, party protection, roll locks, ultimate interruption at each pulse and save/death/respec cleanup.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Resolve Coated Blades regeneration scope first. Pull strength is calibration, not approval for an unconditional root. Existing true costs and healing protections must remain explicit.

## Completion gate

Rogue and Warrior native and connected tests pass on the same final shared JARs; no recursive max-HP proc, stale debuff, stealth regression or ultimate-state leak.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update relevant player references/handbook and review changed renders before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

## Execution record

Completed in [the combined phases 1-3 overlay](../../patches/class-balance-phases-1-3/README.md). Final artifacts passed 104 isolated server checks, 13 connected-client checks and 39 resource checks on Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25.0.4.1. Installed same-name files in `mods/` and `mod fixing/1 fixed packs/`; original copies are retained. Later phases remain planned.

Coated Blades ruling: the user selected natural regeneration and the Regeneration effect only. Direct spell/item/passive healing and absorption remain available; the native tests verify the distinction.

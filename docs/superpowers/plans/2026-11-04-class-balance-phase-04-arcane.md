# Phase 4 - Arcane mechanics and emitter speed

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, isolated-runtime tested and installed in repository pack. **Depends on:** 01, 03.

**Goal:** Deliver the Arcane proc, control escape, banked Blink, Evocation and Ward changes with slower emitter projectiles.

**Architecture / owned scope:** Wizards, Skill Tree, Spell Engine cast/cooldown/summon helpers. Priest values are deferred to phase 07.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Arcane Mage follow-up - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#arcane-mage-follow-up---4-november-2026)
- [Arcane Emitter and Priest follow-up - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#arcane-emitter-and-priest-follow-up---4-november-2026)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Update Fissile and Evocation

- [x] Fissile 100%, original target included, 30% P, 6s cooldown. Evocation three seconds/ten pulses, no damage-received drawback; Rapid twelve pulses over the same three seconds. Preserve positive buffs and approved cooldowns.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Implement controlled Blink and Blitz

- [x] Burst Blink cleanses movement impairment and casts while controlled subject to legal landing/boundaries. Blitz replaces Purge: two saved serial-recharge casts, 7.5-block travel, 2.5-block/20% P blasts, no Speed effect.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Update Presence and reactive Ward

- [x] Presence 15s, all Arcane class skills arm it with correct next-cast ordering. Ward adds safe four-block movement away from attacker once per activation, preserving shield/no-refill behavior.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Slow emitter-only bolts

- [x] Resolve current native launch speed and apply 0.8x once; preserve player Arcane Bolt and other summons.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 5. Verify charge and teleport state end-to-end

- [x] Prove cooldown ordering, partial channels, restart/death/item switch, stale configs, blocked teleports, party exceptions, Ward shield states and no stuck channel/hotbar.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Resolve whether weapon basics arm Presence. Verify no-absorption Ward teleport and no-valid-landing behavior. Do not alter old Purgatory geometry here; phase 08 strengthens that boundary later.

## Completion gate

Two-charge Blink and reactive escape work with authoritative cooldowns and current boundary rules; Arcane procs, Evocation and emitter speed match exact values.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update relevant player references/handbook and review changed renders before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

Verification and exact input/output hashes: [combined phases 4-6 evidence](../../patches/class-balance-phases-4-6/README.md). The settled rulings and completion record are in its ledger.

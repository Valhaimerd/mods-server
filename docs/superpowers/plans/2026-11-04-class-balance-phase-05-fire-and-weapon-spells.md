# Phase 5 - Fire, weapon spells and Ancient Hydra

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, isolated-runtime tested and installed in repository pack. **Depends on:** 01, 04.

**Goal:** Deliver Fire pressure/retaliation, gently assisted weapon projectiles, and slow large-Hydra pursuit without replacing target selection.

**Architecture / owned scope:** Wizards, Skill Tree, Spell Engine weapon binding/projectile/summon helpers; Paladins weapon definitions for Holy range only.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Fire Mage: large Ancient Hydra movement](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#fire-mage-large-ancient-hydra-movement)
- [Basic weapon spells and Fire Mage adjustments - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#basic-weapon-spells-and-fire-mage-adjustments---4-november-2026)
- [Shared traps and Warrior projectiles](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#shared-traps-and-warrior-projectiles)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Audit weapon bindings and update their actual delivery paths

- [x] Fire/Frost basic projectile spells: mild homing and 1.3x speed. Arcane/Holy basic targeting range 24 blocks. Resolve direct/projectile discrepancies from actual item bindings; no silent conversion or summon speed spillover.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Confirm all mage trap lifetimes

- [x] Arcane/Flame/Frost traps remain five seconds; preserve their own cooldowns, damage and debuffs. No old ten-second upgrade remains.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Tune Fire damage and crowd pressure

- [x] Breath final 14% P per delivery and gentle centerline pull, including Flame Throwing extension. Whirlpool walking-pressure pull with Slowness I for one second. Hot Impact 100% chance with existing stun/cooldown.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Rework retaliation

- [x] Eruption 30% P centered on attacker with strong upward knockback and 8s cooldown. Flame Ward 20% P retaliation while its shield is active, fixed shared 0.5s interval; no beam-per-hit recursion.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 5. Add large-Hydra movement and matching presentation

- [x] Single large Hydra approaches existing target at about one block/second on clear ground, no target-system changes. Enlarge its existing ground pulse only visually. Ordinary Hydras unchanged.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 6. Verify trajectories, pressure and summon targeting

- [x] Test walking versus sprinting, line of sight, near/far targets, faster projectiles, per-recipient retaliation gates, final shield depletion, old/new parties, obstacles and despawn visual cleanup.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Resolve actual staff/wand binding mismatches and the final-hit Ward retaliation boundary. Do not broaden every Fire spell into a projectile or give Hydra a larger targeting radius.

## Completion gate

Weapon scope is verified, Fire timing/damage/retaliation is correct, traps stay five seconds, and large Hydra moves slowly with unchanged target decisions and no visual damage duplication.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update relevant player references/handbook and review changed renders before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

Verification and exact input/output hashes: [combined phases 4-6 evidence](../../patches/class-balance-phases-4-6/README.md). The settled rulings and completion record are in its ledger.

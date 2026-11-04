# Phase 8 - Paladin defenses, Purgatory dome and Purelight

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, isolated-runtime tested and installed in repository pack. **Depends on:** 01, 03, 07.

**Goal:** Deliver Paladin changes and a tested shared block-window mechanism ready for Archer reuse.

**Architecture / owned scope:** Paladins & Priests, Skill Tree, Spell Engine defense/casting/containment helpers; actual revival integration APIs.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Paladin and shared defense additions - 3 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#paladin-and-shared-defense-additions---3-november-2026)
- [Shared attack-block windows: Divine Protection and Archer Deflection](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#shared-attack-block-windows-divine-protection-and-archer-deflection)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Revise the ordinary Paladin passives and cooldowns

- [x] Vanguard retains damage/stun; successful stun grants 15% general reduction two seconds or 25% with shield, no permanent reduction. Guardian base five percent plus five 2%/shield 3% stacks: 15/20% total. Protection 20s, Oath 18/15s, Break Restriction three-second knockback immunity.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Implement shared qualifying-attack windows using Protection

- [x] One charge opens five-tick protection; burst hits inside do not consume charges or extend time. Exclude periodic/source-less/true costs; coordinate overlapping defenses. Build regression coverage for Archer consumer without introducing its later tuning early.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Close the Purgatory containment volume

- [x] Cooldown 16s, +30% opening armor three seconds, full roof/underside with hostile Pearl/Chorus escape denial and all lifecycle exits. Preserve control immunity after eight seconds; stun escape works only before that.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Replace Reinforced with Being of Purelight

- [x] Fatal-hit full heal and one HP every half second directly from health, bypassing absorption, until real downing/death. Implement once with explicit rescue/rearm/lifecycle decisions; preserve unrelated fatal defenses.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 5. Verify remote movement and revival boundaries

- [x] Test edge/top/bottom movement, portal-like item denial and resource handling, party exemptions, interrupted/late channels, duplicate lethal events, downed single/multiplayer states and all cleanup paths.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Purelight rearm/cooldown, lethal-hit cancellation, cleanse/respec, totem ordering and persistence require explicit decisions. Do not implement assumptions as though approved. Advanced safe movement in phases 03-06 must be retested against the final dome.

## Completion gate

No dome escape/desync or state leak; new block windows survive burst tests; Purelight cannot recurse, be silently cleansed by reconnect, or keep ticking a downed player.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update focused player references/patch notes under the user?s newer handbook exception before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

Final implementation, exact hashes, settled lifecycle choices and verification: [combined phases 7-9](../../patches/class-balance-phases-7-9/README.md).

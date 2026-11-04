# Phase 7 - Priest support, Ascension and cooldowns

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, isolated-runtime tested and installed in repository pack. **Depends on:** 01, 06.

**Goal:** Deliver current Priest requests, especially fixed-pulse Ascension with reduction instead of immunity.

**Architecture / owned scope:** Paladins & Priests, Skill Tree and Spell Engine Priest/support helpers. Phase 01 already proves Pain Suppression magic coverage.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Arcane Emitter and Priest follow-up - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#arcane-emitter-and-priest-follow-up---4-november-2026)
- [Ascension final revision](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#ascension-final-revision)
- [Explicit shield and Priest protection coverage](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#explicit-shield-and-priest-protection-coverage)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Update straightforward Priest tuning

- [x] Circle 60% P / Improved 75%; Fade full equipment invisibility two seconds; Divine Favor stun three seconds; Lightwell cooldown 60s; Barrier cooldown 40s. Preserve all unrelated timings and upgrade functions.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Rebuild Ascension pulse/state handling

- [x] Four fixed pulses at 0.5/1/1.5/2 seconds regardless of haste. Caster/allies gain 6% offensive power per pulse, four stacks/24%. Caster gets 50% general reduction six seconds refreshed; Uplift grants it to lifted allies instead of full immunity. No leftover planned-immunity path.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Extend Serenity recipients

- [x] Grant existing per-pulse absorption within eight blocks, including preserved caster benefit; independent recipients, four stacks and no refill of consumed shields. Preserve the separate three-block Uplift lift area.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Verify support lifecycle and party interactions

- [x] Check six-second defensive refresh, correct Improved duration relationships, zero double-self applications, damage layer single application, dispel/death/levitation cleanup, ward overlaps, actual healing and cooldown haste.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Reconcile early levitation removal with defensive lease expiry; do not increase Uplift's radius to eight merely because Serenity uses eight.

## Completion gate

Ascension has exactly 50% ordinary reduction and 24% offensive cap, never the withdrawn immunity; all Priest cooldowns, recipients and shield accounting pass.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update focused player references/patch notes under the user?s newer handbook exception before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

Final implementation, exact hashes, settled lifecycle choices and verification: [combined phases 7-9](../../patches/class-balance-phases-7-9/README.md).

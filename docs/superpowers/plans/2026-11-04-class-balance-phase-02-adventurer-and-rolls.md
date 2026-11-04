# Phase 2 - Adventurer progression, Utilities and Multi Roll

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, verified and installed together on 4 November 2026. **Depends on:** 01.

**Goal:** Deliver equal useful Defense paths, clean Utilities progression and the capped extra-roll enchantment.

**Architecture / owned scope:** Adventurer Skills, Orb/Puffish Utilities compatibility runtime, Combat Roll enchantment data; Magistrate and Nullstone remain phase 01 ownership.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Adventurer Utilities layout, wording and Fitness - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#adventurer-utilities-layout-wording-and-fitness---4-november-2026)
- [Approved Adventurer Defense redesign, Magistrate and Nullstone - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#approved-adventurer-defense-redesign-magistrate-and-nullstone---4-november-2026)
- [Multi Roll enchantment](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#multi-roll-enchantment)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Rebuild the Defense graph and allocation contract

- [x] Seven ordinary nodes plus specialty per exclusive path, no dead ends, both penultimate nodes connect, stable icons and size 1. Preserve total earned points and scope any required refund to Defense.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Implement exact path rewards and masteries

- [x] Aegis: +4 HP/+2 armor/4% Resistance/4% healing received, reactive 15% reduction 3s on a 20s fixed cooldown. Colossus: +6 HP/+2 armor/2% Resistance/8% healing received, recover 10% max HP over 5s below 40% on a fixed 30s cooldown. Bulwark: +2 HP/+4 armor/8% Resistance/20 knockback points total.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Repair Utilities presentation and real mastery gating

- [x] Replace seven visible mastery spokes with penultimate-only links while preserving all-path prerequisites server/client. Size 1, melon slice/diamond boots/writable book icons. Fix title newline/control glyphs and shorten descriptions.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Buff Fitness without fake HUD values

- [x] At the existing hunger threshold, fill actual hunger and saturation to supported caps; preserve 180s cooldown and death/downed exclusions. Verify full golden saturation HUD and subsequent normal depletion.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 5. Cap Multi Roll enchantment contribution

- [x] At most +2 rolls from the enchantment, including existing higher-level gear; preserve other roll sources, recharge/distance and other enchantments. Verify saved allocations/equipment and connected graph/HUD.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Document the chosen saved-level/cap presentation. Keep Majesty/Resistance semantics distinct: Magistrate remains magic-specific; new defensive Resistance is general.

## Completion gate

Exact branch totals and fair migration pass; Utilities can unlock correctly with clean links; Fitness shows real full hunger/saturation; Multi Roll cannot give more than two extra rolls.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update relevant player references/handbook and review changed renders before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

## Execution record

Completed in [the combined phases 1-3 overlay](../../patches/class-balance-phases-1-3/README.md). Final artifacts passed 104 isolated server checks, 13 connected-client checks and 39 resource checks on Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25.0.4.1. Installed same-name files in `mods/` and `mod fixing/1 fixed packs/`; original copies are retained. Later phases remain planned.

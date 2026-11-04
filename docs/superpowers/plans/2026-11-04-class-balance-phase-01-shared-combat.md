# Phase 1 - Shared damage rules and mage baseline

> **For agentic workers:** Use superpowers:executing-plans; execute inline. No extra agents or unrequested commit/push.

**Status:** Implemented, verified and installed together on 4 November 2026. **Depends on:** None.

**Goal:** Make ordinary RPG magic interact correctly with armor, Resistance and directional shields; restore the mage coefficients and remove the redundant anti-magic accessory.

**Architecture / owned scope:** Spell Power, Spell Engine, Wizards, Skill Tree, Paladins/shared shield hooks, Adventurer Skills (Magistrate only), and Nameless Trinkets as identified by installed ownership.

**Tech stack:** Minecraft 26.2; NeoForge 26.2.0.90+; Java 25; current pack's JSON resources and focused Java/ASM helpers. No new dependency assumed.

**Spec sections:**
- [Shared Resistance coverage and mage coefficient restoration - 4 November 2026](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#shared-resistance-coverage-and-mage-coefficient-restoration---4-november-2026)
- [Verified current armor and Resistance behavior](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#verified-current-armor-and-resistance-behavior)
- [Explicit shield and Priest protection coverage](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#explicit-shield-and-priest-protection-coverage)
- [Magistrate](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#magistrate)
- [Remove Fractured Nullstone](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md#remove-fractured-nullstone)

## Shared execution rules

- Follow the [phase index](2026-11-03-class-balance-and-behavior-follow-up.md#execution-rules) and pack `AGENTS.md`; work inline without spawning agents unless separately authorized.
- Start from the currently installed, preceding-phase-tested JAR bytes. Never rebuild a later phase over an older captured whole-JAR baseline. Record input/output hashes and exact archive changes.
- Preserve IDs, saved allocations, party/pet protection, no-refill absorption, true-damage exceptions and unrelated user changes. Same-version repacks keep their active filenames.
- Use the dedicated description-rewrite MD: concise skill text; actual received amounts in effect details; native trigger/cooldown/range fields.
- Implement only this phase's owned changes. Resolve its real open balance choices before dependent code; independently verifiable work may continue. Do not reinterpret a numerical requirement to avoid asking about a genuine ambiguity.
- Every task ends with focused proof. No commit/push, personal-world edit or live-server deployment is part of this plan.

## Tasks

### 1. Trace the real damage pipeline and pin baseline fixtures

- [x] Record physical/direct magic/periodic/true sources, armor bypass tags, resistance hooks, 80% directional shield multiplier and current source-retention ordering. Reproduce the old missing coverage before edits.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 2. Implement equal ordinary magic defense

- [x] Armor/toughness must mitigate ordinary RPG magic. General Resistance must cover physical and magic once. Prove Priest Pain Suppression at all four thresholds and shields at 80% frontal reduction, preserving exclusions and independent layers.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 3. Restore the exact mage damage ledger

- [x] Use pre-reduction effective coefficients and normalization, not rounded values divided by 0.8. Preserve all newer mechanics. New explicit coefficients for an existing damage path win (Fire Breath 14%, Splinters 20%); do not introduce later replacement deliveries early. If an old delivery is replaced in phase 06, document the intermediate original-path coefficient and replace it there.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 4. Apply the approved anti-magic rebalance

- [x] Magistrate grants exactly 15% Magic Resistance total, preserving its other rewards. Retire nameless_trinkets:fractured_nullstone, its 15% effect and acquisition; test equipped/saved-item handling without removing other trinkets.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

### 5. Verify and package this baseline

- [x] Exercise representative direct, projectile, summon and channel damage with armor/resistance/enchantments, no duplicate magic mitigation and unchanged global PvE/PvP retention. Verify safe Nullstone retirement and unchanged unrelated entries.
- [x] Run the focused native/resource checks for this task; exercise the actual runtime seam where code overrides JSON. Record baseline failure or unchanged-behavior proof, candidate result and relevant limits.

## Open details and risks

Unresolved incidental damage-source classifications or save-retirement behavior require evidence; no reason to block the independent coefficient audit. Preserve new Frost/Fire final coefficients over historical ledgers.

## Completion gate

All classes have tested physical/magic mitigation; exact mage coefficients and Magistrate 15% are verified; Nullstone is unavailable and cannot leave mitigation behind.

- [x] Final tests use the exact production candidate bytes; no probes ship.
- [x] If installing this phase, back up originals, replace active and fixed-pack JARs only after verification, confirm hashes and preserve prior phases. Update relevant player references/handbook and review changed renders before reporting it installed.
- [x] Record unresolved/deferred items, tested versions and next-phase input hashes. Mark completion only when this phase's required work is actually done.

## Execution record

Completed in [the combined phases 1-3 overlay](../../patches/class-balance-phases-1-3/README.md). Final artifacts passed 104 isolated server checks, 13 connected-client checks and 39 resource checks on Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25.0.4.1. Installed same-name files in `mods/` and `mod fixing/1 fixed packs/`; original copies are retained. Later phases remain planned.

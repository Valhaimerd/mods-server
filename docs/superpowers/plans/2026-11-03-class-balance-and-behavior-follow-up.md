# Class Balance and Behavior Follow-up - Phase Index

**Updated:** 4 November 2026. **Status:** Phases 1-9 implemented, isolated-runtime tested and installed on 4 November 2026.

**Latest balance correction:** Magistrate grants **15% Magic Resistance total**, replacing the proposed 25%. Its other rewards remain unchanged. Bulwark remains **8% full-path physical/magic Resistance**.

The large chronological plan has been preserved as the [detailed specification](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md), with the Magistrate correction applied. The following phase documents are the execution plan. Read this index, the current phase and its referenced specification sections; do not repeatedly load the whole historical document or run its old task lists independently.

## Phases

| Phase | Scope | Why it comes here |
| --- | --- | --- |
| [1. Shared damage rules and mage baseline](2026-11-04-class-balance-phase-01-shared-combat.md) | Make ordinary RPG magic interact correctly with armor, Resistance and directional shields; restore the mage coefficients and remove the redundant anti-magic accessory. | Establish one correct mitigation/damage baseline before balancing class interactions. |
| [2. Adventurer progression, Utilities and Multi Roll](2026-11-04-class-balance-phase-02-adventurer-and-rolls.md) | Deliver equal useful Defense paths, clean Utilities progression and the capped extra-roll enchantment. | Set consistent progression and roll capacity before testing class roll/healing synergies. |
| [3. Rogue and Warrior mechanics](2026-11-04-class-balance-phase-03-rogue-and-warrior.md) | Implement the close-combat class changes together over their shared Rogues/engine archives, with separate task tests. | The two classes share the Rogues JAR and close-combat/control hooks; separate tasks keep verification focused. |
| [4. Arcane mechanics and emitter speed](2026-11-04-class-balance-phase-04-arcane.md) | Deliver the Arcane proc, control escape, banked Blink, Evocation and Ward changes with slower emitter projectiles. | Banked Blink and safe reactive teleport depend on stable damage/control behavior. |
| [5. Fire, weapon spells and Ancient Hydra](2026-11-04-class-balance-phase-05-fire-and-weapon-spells.md) | Deliver Fire pressure/retaliation, gently assisted weapon projectiles, and slow large-Hydra pursuit without replacing target selection. | Audit weapon bindings once, then calibrate Fire pressure, retaliation and Hydra movement. |
| [6. Frost damage status and targeted spike rework](2026-11-04-class-balance-phase-06-frost.md) | Implement the new Frost statuses, targetable spike, proc passives and charged/homing Lance while preserving explicit final coefficients. | Frost needs its own pass for native freezing damage, delayed geometry and projectile penetration. |
| [7. Priest support, Ascension and cooldowns](2026-11-04-class-balance-phase-07-priest.md) | Deliver current Priest requests, especially fixed-pulse Ascension with reduction instead of immunity. | Stabilize support buffs, healing and shields before testing Paladin fatal/rescue combinations. |
| [8. Paladin defenses, Purgatory dome and Purelight](2026-11-04-class-balance-phase-08-paladin.md) | Deliver Paladin changes and a tested shared block-window mechanism ready for Archer reuse. | Implement shared charge windows and complex dome/fatal-defense lifecycle together. |
| [9. Archer changes and full-pack integration](2026-11-04-class-balance-phase-09-archer-and-final-integration.md) | Finish Archer tuning using the shared defense implementation and verify the complete combined class release. | Reuse those windows for Deflection, then run final cross-class and saved-pack integration. |

## Execution rules

1. Execute in numbered order by default. The dependency chain serializes shared-JAR mutations; it is not permission to work on overlapping snapshots in parallel.
2. Build each phase on the **latest verified preceding-phase bytes**. Existing builders that pin old inputs must be rebased safely; never overwrite later fixes by reusing an old full JAR.
3. Keep build/test evidence in phase-scoped patch folders. Record input/output hashes, changed entries, resource checks, meaningful native tests and connected client observations where movement/HUD/network behavior matters.
4. Preserve IDs, saved data and party/pet safeguards. Keep same-version filenames; backups/fixed packs follow pack instructions. No personal installation/world changes or automatic GitHub work.
5. Resolve only the current phase's genuine open choices before dependent edits. Earlier clarified decisions are final: Vanguard 25% shield benefit after successful stun, late Purgatory stun immunity retained, Splinters 20% total, Roots two active clouds, Ascension reduction rather than immunity, Colossal projectile-only penetration.
6. Do not add more Resistance to magic when an existing general layer already covers it. Preserve intentional true-health costs. Class-specific extra magic layers remain explicit, not accidental duplication.
7. Every phase has a completion gate. If a phase is installed, synchronize current player docs/handbook before claiming completion. Final phase additionally verifies the combined pack and all final rendered documentation.
8. Current gameplay stays authoritative until a phase is actually built, tested and installed. Planning checkboxes do not imply runtime work. No extra approval is needed for routine authorized implementation choices, but unresolved balance choices are not assumptions to silently ship.

## Delivery status

- [x] Phase 1 implemented, verified and installed.
- [x] Phase 2 implemented, verified and installed.
- [x] Phase 3 implemented, verified and installed.
- [x] Phase 4 implemented, verified and installed.
- [x] Phase 5 implemented, verified and installed.
- [x] Phase 6 implemented, verified and installed.
- [x] Phase 7 implemented, verified and installed.
- [x] Phase 8 implemented, verified and installed.
- [x] Phase 9 implemented, verified and installed.

## Preserved details and final-value precedence

- [Complete detailed requirements and investigation notes](../specs/2026-11-04-class-balance-and-behavior-follow-up-spec.md)
- [Existing installed Defense audit](../../patches/adventurer-defense-audit/README.md) - historical baseline, not the approved redesign.
- The specification retains every request and original validation detail; phase files assign ownership and execution order rather than duplicating the whole specification.
- Existing source/patch filenames referenced there describe inspected owners, not a requirement to rebuild old artifacts. Current installed metadata, code and each phase's input manifest govern implementation.

Combined delivery: [implementation, values and verification](../../patches/class-balance-phases-1-3/README.md).

Latest completion: [combined phases 7-9 and exact verification](../../patches/class-balance-phases-7-9/README.md). Routine balance fixes use patch notes; no handbook/PDF rebuild was requested.

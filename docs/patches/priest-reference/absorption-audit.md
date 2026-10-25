# Absorption stacking audit - 26 October 2026

Read-only audit during the Priest rework design. No implementation or runtime proof is claimed here.

## Installed findings

Scanning active spell resources and all active JAR classes for `setAbsorptionAmount` identified four custom implementations:

| Implementation | Used by | Current initialization |
|---|---|---|
| `net/paladins/effect/PriestAbsorptionStatusEffect` | Penance; existing Sanctuary | At effect start, sets absorption to at least `2 * (amplifier + 1)` HP |
| `net/skill_tree_rpgs/effect/WizardAbsorbEffect` | Arcane, Flame and Frost Wards | Same total-capacity initialization |
| `net/arsenal/spell/AbsorptionStatusEffect` | Guardian healing-triggered shield | Same total-capacity initialization |
| `net/spell_engine/entity/TenacityAbsorbEffect` | Warrior Tenacity | At effect start, sets absorption to at least `amplifier + 1` HP |

Penance explicitly uses ADD with a power-scaled increment and cap. Sanctuary and the other listed wards are not the same stacking mechanic; their fresh casts/procs must be distinguished from updating an existing stack. Vanilla/mod effects that inherit vanilla Absorption behavior need coverage in the eventual shared-path audit too; this class scan alone does not establish their refresh behavior.

The credible failure mechanism is reinitializing absorption from the **whole new amplifier** when an existing effect gains a level. That loses the distinction between shield capacity already consumed and shield HP newly earned. The exact native callback sequences on equal-level refresh, level increase, expiry, replacement and loading still require runtime checks.

## Confirmed user rule for the rework

- A new Penance bolt adds only 1 current shield HP, up to its cap, while refreshing duration.
- Example: 6 HP -> receive 4 damage -> 2 HP -> two bolts -> 4 HP. Never restore to the old six-stack capacity first.
- The same principle applies to other stacking absorption effects: add only the newly earned shield amount. A duration-only refresh or an application at the maximum stack count must not refill consumed HP.
- Planned Serenity must follow this rule using its per-stack shield amount.
- Separate fresh shields/procs (Wizard wards, Tenacity, Guardian) must not be indiscriminately disabled: an intentional new shield grant is distinct from incidental refilling on a stack update. Preserve each ability's cooldown and eligibility.
- Avoid changing or erasing unrelated absorption when multiple effects coexist. Validate mixed wards, exhausted shields, same-tick impacts, expiry, death and save/load before claiming a fix.

This audit extends the pending Priest design; it does not change the installed pack.

## Implemented follow-up

The [Priest rework](../priest-rework/README.md) now implements incremental grants and no-refill refreshes for the four audited custom shield classes, plus the new Priest stacking shields. Runtime checks cover spent-HP increments, unchanged-level refresh and fresh grants after removal. The audit above records the original pre-patch behavior.

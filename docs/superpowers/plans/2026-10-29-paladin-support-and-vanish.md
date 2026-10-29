# Paladin support balance and Vanish breathing visuals

Approved direction: specialized support progression plus the outstanding explicitly requested skill changes. Implement in the current workspace, preserving the installed attack-block correction and all unrelated changes. No commit or push requested.

## Concrete balancing specification

Support melee power (S) uses native noncritical M passed through Puffish's actual positive melee-damage modifier API, so Martial Prowess and ordinary melee investment matter. This is only the support snapshot; skill impact damage retains its current school. P excludes Divine Protection's own power bonus; armor excludes Divine Oath's own armor. Banner uses these same snapshots. No three-buff feedback loop.

| Benefit | Base | Upgrade / cap |
|---|---|---|
| Oath extra damage |15% S, capped 3 HP per proc |Decree:20% S, capped4; existing fixed0.5s interval |
| Oath armor |2 +20% caster armor, capped6 |Decree:2 +30%, capped8 |
| Oath absorption |1 +45% P, capped8 HP |Decree:1 +60% P, capped10 HP |
| Protection reduction |5% +1.5% per P, capped30% |Bulwark:8% +2% per P, capped40% |
| Protection attack blocks |1 +one per complete10 S, capped3 |Still before absorption; attacker eligibility unchanged |
| Branch knockback resistance |10% +3.5% per S, capped80% |Both existing branches |
| Merciful recovery |5% P per second, capped1 HP per pulse |Five pulses after actual qualifying damage; unchanged trigger |
| Banner attack speed per pulse |0.2% +0.04% per S |20%; Intense25% |
| Banner draw speed per pulse |0.4% +0.03% per S |20%; Intense25% |
| Banner spell haste per pulse |0.1% +0.025% per P |10%; Intense15% |
| Banner general / extra magic reduction per pulse |0.1% +0.02% per caster armor |Each10%; Intense15% |
| Banner knockback resistance per pulse |0.4% +0.055% per S |30%; Intense35% |
| Banner healing |1 +5% P HP every1s, capped2 HP |No faster healing or haste-scaled pulses |

At S20/P16/armor20, normal combat caps take10s and Intense takes up to15s. At lower stats, useful benefits develop more slowly. Higher attributes shorten ramp time, never raise the caps. The original range, duration, rescue limits and fixed0.5s combat pulses remain. Use only the stronger Protection/Banner general reduction; retain Banner's independent extra magic layer. Different Banner sources retain existing strongest-value leases. Overlapping Banner healing uses the strongest eligible caster and one shared1s recipient interval, so additional Paladins do not multiply healing. Oath absorption refresh does not replenish consumed HP. These ceilings are intended to keep temporary Paladin support below dedicated damage/healing roles, not to guarantee every unrelated build reaches every cap.

## Outstanding requested changes

- Purge Heal self/Divine Strength ally healing:20% P, Improved25%, Strength35%.
- Vanguard: basic melee hit readies/executes one stun proc with4s haste-affected cooldown,1s stun or2s with a recognized equipped shield; only that hit adds20% native base melee damage. Exclude spell/wave/ranged triggers. No unapproved cooldown floor.
- Guardian: unconditional2% armor and P; incoming hostile combat damage adds four2% stacks for5s, refreshed, existing0.25s haste-affected interval. Equipped shield makes earned stacks3%, not the baseline. Show actual aggregate amount. Clear old Vanguard/Guardian modifiers and effects on respec/death.
- Purgatory: middle-stage ally regenerationII; final remainsIII; caster excluded.
- Vanish: synchronized violet silhouette/body/armor/weapons, including first person, fades10% to0% over2s and back over2s. No observer-visible particles at zero. Preserve stealth targeting, mark cleanse, duration and break behavior. Existing full-hide hooks must be replaced rather than layered over.

## Execution and proof

1. Add failing native Paladin tests before changing the candidates. Check proc eligibility/cooldowns, Guardian shield changes, healing variants, snapshot feedback, support caps and early/endgame progression.
2. Recompile the focused helpers from pinned current archives. Preserve current M-based Oath impacts and finite attack blocks when rebuilding historical source helpers. Repack current archives under their current names.
3. Vanish renderer work is isolated to its own patch directory/candidates; merge only its audited entries. Native client views must establish real translucent geometry and held-item visibility, not just an alpha calculation.
4. Run fresh/stale-config isolated server checks and focused client rendering checks. Keep personal Minecraft and separate desktop server untouched. Inspect changed archive entries and installed/fixed hashes.
5. Update brief English descriptions using the existing description-rewrite guidance; use native gold cooldown/trigger rows, percentages and real received status amounts. Synchronize raw references, handbook and reviewed PDF. Report any runtime/visual limits honestly.


## Completion evidence

Installed same-name Paladins, Skill Tree, Spell Engine and Rogue repacks. Fresh and stale-config isolated servers each pass225 checks with the final Rogue candidate present. Vanish passes74 native client checks with six reviewed GPU captures. A focused review found and corrected Merciful Recovery tooltip receiving-modifier scaling and Banner healing at the exact20s/25s endpoint; both have regression checks. Exact hashes and entry diffs are in the two patch folders. The79-page handbook was rebuilt and visually reviewed; all51 contents page links/numbers match. No GitHub synchronization requested.

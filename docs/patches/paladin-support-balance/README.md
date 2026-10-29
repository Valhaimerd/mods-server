# Paladin support progression and pending skill adjustments

Minecraft26.2 / NeoForge26.2.0.90 / Java25. The approved specialized design and every formula are in [the implementation plan](../../superpowers/plans/2026-10-29-paladin-support-and-vanish.md).

## Gameplay

- Purge healing is20% P, Improved25%, Divine Strength35% (including eligible nearby allies).
- Vanguard uses only the native basic-melee-impact trigger: one extra20% native M hit and1s stun, extended to2s with a recognized shield, on a4s haste-affected cooldown. Spell/wave/ranged triggers and old persistent Vanguard healing bonuses are removed. The extra hit does not recursively trigger Vanguard. No additional cooldown floor was introduced.
- Guardian grants2% base armor and Healing Power. Hostile combat damage adds four earned2% stacks for5s, or3% per earned stack while a shield is equipped. The baseline remains2%; totals are10% or14%. Shield changes update existing stack amounts. The existing0.25s haste-affected interval remains. Stale configured effects are cleared, and death/respec remove runtime modifiers.
- Oath separately caps extra damage at3/4, armor at6/8, and base absorption at8/10 HP (normal/Decree). It retains its0.5s damage interval, seven-second buff, non-replenishing shield refresh and70%/100% native-M hammer damage. The projectile stores native impact M separately from support M, preserving the launch snapshot even if equipment changes in flight.
- Protection scales to30% normally or40% with Bulwark. Up to three attack blocks still occur before absorption; old saved block budgets normalize to the new cap. Both branches can reach80% knockback resistance through realistic melee investment. Merciful Recovery remains five actual-damage-triggered pulses, with base healing capped at1 HP per pulse.
- Banner has smooth proportional combat gains, fixed0.5s pulses and per-second healing of1 +5% P, capped2 base HP. Overlapping Banners use the strongest eligible healer with one shared1s recipient interval. General reduction overlaps Protection using the stronger amount; Banner's separate magic layer remains. At S20/P16/armor20, normal combat caps take10s and Intense takes up to15s. Existing radius6/8, duration20/25, large worn model and per-cast unique rescue behavior remain.
- Purgatory grants allies RegenerationII in its middle stage andIII in its final stage. It still excludes the caster from this healing.

Support S uses Puffish's native `DynamicModification.withPositive(MELEE_DAMAGE).applyTo(M)` API; reading the dynamic attribute as a conventional scalar would return NaN. Only support snapshots use S. Native damage routing, skill schools and every other class remain unchanged. Snapshots exclude Protection's power and Oath's armor; Banner cannot feed those benefits back into itself. Status details show actual stack/percentage/charge amounts; Banner healing details include healing-received modifiers. Caps on base healing/shield grants precede the recipient's normal healing/absorption modifiers.

## Endgame comparison

Calculated from the installed Lightbringer/Hammer definitions: full Lightbringer, Hammer of Destiny with the user's stated25% generic Spell Power, three Netherite Citrine accessories, all ordinary Paladin power nodes, Colossus and three healing nodes. Martial additionally uses three ordinary melee nodes and Martial Prowess; Magistrate uses four magic nodes and Magistrate. These figures exclude race, Guardian/Reinforced, other enchantments, and temporary buffs. They are calculated loadout comparisons, not a claim that an entire public multiplayer build was playtested.

| Build | Support S | P | Oath / Decree damage | Protection / Bulwark | Normal Banner attack / haste cap time |
|---|---|---|---|---|---|
| Martial Prowess |21.42 |15.18 |3 /4 |27.76% /38.35% |9.5s /10.5s |
| Magistrate |19.04 |18.25 |2.86 /3.81 |30% /40% |10.5s /9s |

Both have useful off-path benefits, while their main investment reaches its relevant ceiling. Stronger stats shorten Banner buildup without raising caps. These are balance targets, not a guarantee that every enemy encounter or PvP matchup is equally difficult.

## Rebuild and evidence

Pin the three current input archives from `artifacts.json` in ignored `tmp/paladin-support-balance/before/`, then:

```powershell
python docs/patches/paladin-support-balance/build.py
python docs/patches/paladin-support-balance/run_server.py
python docs/patches/paladin-support-balance/run_server.py --legacy
python docs/patches/paladin-support-balance/verify.py
```

The original native baseline reproduced21 failures among29 initial checks. The expanded suite exercises new behavior plus the prior ultimate/control/visual regressions: real melee hits, native cooldowns, stack refresh/equipment changes, actual party healing, shields, separate M snapshots, finite attack cancellation, reduction overlap, source leases, resurrection/cleanup boundaries, full60-pulse channel cost, fixed pulses and existing class visual cues. Final results and exact tested hashes are recorded alongside this file; add `--installed` to verify matching active/fixed archives after installation.

Recompiling the historical helper requires restoring its existing ultimate init/configuration, roll restrictions and presentation hooks. `build.py` applies the count-checked original `PatchTierThree` transformations and preserves the existing `ClassStatusVisuals.configure` hook. Same-version archive filenames are retained; rollbacks remain ignored. No personal Minecraft/server installation, world or GitHub remote is modified.

Vanish is independently implemented and client-tested in [vanish-breathing](../vanish-breathing/README.md). Its Rogue archive does not require server helper changes. This support pass has isolated dedicated-server coverage; it is not a new full-pack multiplayer or shader compatibility certification.

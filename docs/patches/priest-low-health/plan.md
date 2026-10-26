# Priest tier-III passive rework

Requested behavior: Pain Suppression checks current/max health every five server ticks, applying 6/12/18/24% reduction below 50/40/30/20%, without a cooldown. Reuse its effect icon, particles and sound. Celestial Orbs retain their critical-active-spell activation and 15s readiness, now five charges at 30% Healing Power, also protecting party allies and owned pets within eight blocks of the Priest. Own-hit retaliation remains native. Ally-hit handling consumes only Celestial charges, never unrelated stashes. Qualifying damage includes damage absorbed by shields; environmental damage without a hostile attacker cannot retaliate.

Implementation plan:
- [x] Pin installed Skill Tree/Spell Engine inputs; test requested resources against baseline.
- [x] Add one narrow configure hook and a helper reusing effect, impact and party APIs; adjust only the two passive resources and English descriptions.
- [x] Run isolated regression probes covering threshold changes/removal, scaled maximum HP, actual reduction, five charges, friendly-fire/radius/absorption and no duplicate self retaliation.
- [x] Install same-name replacements with ignored backups and matching fixed copies; regenerate Priest reference and handbook/PDF; verify archives and layout. No push requested.

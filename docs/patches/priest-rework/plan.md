# Priest rework - 26 October 2026

Proceeding after the user's request to implement the settled discussion. Preserve current active archive bytes except explicit changes; never rebuild historical combat sources. No push requested.

1. Snapshot current Paladins, Skill Tree, Spell Engine and Arsenal inputs. Change spell resources and whole Priest branch placement, retaining IDs and learned-book compatibility.
2. Add one Priest helper for exact Holy Light coefficients, Penance radial impacts/one-HP absorption increments, Ascension aura and Serenity, scaled regeneration, smooth cooldown interpolation and end-of-interval channel timing.
3. Replace total-refill initialization for custom absorption effects with incremental grants. Refreshes/capped stack applications cannot restore consumed absorption; preserve distinct fresh grants after expiry.
4. Build candidates from pinned snapshots. Test native projectile collisions against friend/enemy, party/pets, buffs and pulse timing, cooldown interpolation, stack damage examples, upgrade values and preservation of unrelated resources.
5. Install matching same-name replacements only after checks pass. Synchronize Priest raw reference, concise names/descriptions and handbook, rebuild/render PDF. Leave all unrelated dirty files intact; no commit/push.

Settled numbers are the user's 26 October request plus confirmations: Penance ten bolts at 25% (30% Improved); Holy Light 12-24s and Penance 8-14s smooth cooldowns; Ascension buffs 6s/8s Improved. Absorption only adds newly earned shield HP: 6 -> damage 4 -> 2 -> two Penance bolts -> 4. No refill from total stack count.

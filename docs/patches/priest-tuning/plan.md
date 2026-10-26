# Confirmed Priest tuning - 26 October 2026

The user confirmed lowest **raw HP**, five-second Penance with ten bolts, two simultaneous Lightwell motes per existing cycle, and existing useful full-health support fallback. Proceed with the existing spell resources and narrow current-bytecode hooks. Preserve existing IDs, absorption limits, party checks and unrelated patches. No push requested.

1. Snapshot active Paladins, Skill Tree and Spell Engine; update explicit resources and concise descriptions.
2. Apply final per-delivery Holy Light/Penance coefficient overrides and smooth Penance cooldown 8-12s. Remote Penance caster buffs occur once on living ally/enemy contact, never terrain alone or twice inside radius.
3. Lightwell reevaluates lowest raw HP every firing cycle: two distinct injured allies when available, both motes on a sole injured ally, support fallback only when no injuries. Keep cooldown, power, upgrades and projectile attribution.
4. Isolated tests cover native launch/collision, coefficient branches, Holy Staff damage-only modification, radius boundary, remote caster grants/no duplication, raw-HP ranking, retargeting and injured/full-health split scenarios. Preserve prior Priest/party regressions.
5. Install same-name candidates only after checks pass, retain ignored backups, synchronize raw reference and handbook, rebuild/render PDF. No personal worlds or installations change.

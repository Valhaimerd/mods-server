# Divine Protection: finite attack blocks

Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25. This follow-up corrects the owner's intended mitigation: **one qualifying attack block, plus one per complete 10 caster Melee Power**, rather than flat HP subtracted from every hit. At 17 M, two attacks are blocked.

Hostile living-attacker melee, attributed projectiles and magic consume one block each. Cancellation happens in `LivingIncomingDamageEvent`, before armor/absorption damage processing. Percentage mitigation remains after charges are spent, with the approved **40% cap**. Environmental damage and self-damage do not consume blocks; invulnerability-bypassing damage is excluded. Recasting grants a new block budget. Dispel/expiry/death cleanup prevents stale blocks. Existing saved `flat` snapshots are migrated when read. Status details show remaining attacks instead of HP per hit.

Only two active archives change, retaining their filenames: Spell Engine contains the focused runtime repair; Paladins contains the corrected English description. Skill Tree is pinned as a regression input and remains byte-identical. Existing duration, caster Healing Power bonus, party targeting, knockback resistance and actual-damage Merciful Recovery behavior remain. Normal magic damage already uses the general reduction; no duplicate magic reduction is added. No Vanish or tier-I passive redesign is installed by this patch.

## Verification and rebuild

The installed baseline failed 16 of the first 17 native checks, including HP/absorption loss on the supposedly blocked attacks. The final candidate passes **27 checks on fresh configs and 27 on stale configs** under `-Xverify:all`: scaling at M0/17/40, melee/magic/projectile cancellation, shield preservation, exhausted budgets, environmental/self hits, cap, recast, dispel, saved-state migration, expiry and cleanup. These are isolated dedicated-server checks, not a new multiplayer/client visual test. Existing icon art is unchanged; actual status detail text is exercised on the server.

1. Pin the current Paladins, Skill Tree and Spell Engine archives in ignored `tmp/protection-hit-charges/before/`. Match the input hashes in `artifacts.json`.
2. `python docs/patches/protection-hit-charges/build.py`
3. `python docs/patches/protection-hit-charges/run_server.py`
4. `python docs/patches/protection-hit-charges/run_server.py --legacy`
5. `python docs/patches/protection-hit-charges/verify.py` (add `--installed` after installation).

The runner uses existing ignored local NeoForge libraries and isolated worlds, never the personal Minecraft or desktop server installations. Exact inputs/outputs and native evidence are recorded alongside this README. Rebuild the handbook using `docs/build-handbook.ps1` after synchronizing `docs/MODLIST.md`.

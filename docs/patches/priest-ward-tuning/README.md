# Priest coefficients and close-range Arcane Ward

4 November 2026. Focused update over the installed Holy Wand and Adventurer layout repairs.

| Skill | Current behavior |
| --- | --- |
| Holy Light | **12s flat base cooldown**, haste affected, independent of channel completion. Existing 10s/25-delivery channel, damage/healing and branches remain. |
| Penance | **40% P** per explosion, **50% P** with Improved Penance. Retains its proportional 8-12s cooldown, ten bolts, 4-block radius and existing support. |
| Circle of Healing | **65% P** healing, **90% P** with Improved. |
| Celestial Orbs | **40% P** per orb, **1.0 knockback** (previously 0.1). Five orbs, readiness and party retaliation remain. |
| Arcane Ward | Teleports only when the attributed attacker is **within four blocks**, including exactly four. More distant attackers still trigger the existing absorption shield and cooldown, without teleporting. Existing safety and party checks remain. |

Both serialized resources and actual runtime overrides are updated. Holy Light's runtime completion multiplier is removed; Penance's independent cooldown formula is retained. Arcane Ward adds its distance check only to the post-shield teleport branch. No new event listeners, cooldowns or global combat changes are introduced.

The same-name Paladins, Skill Tree and Spell Engine JARs are tested together. Other repairs, including Holy Wand's enemy damage, Purelight glow/cooldown, Hydra detection and the Adventurer layout, remain. Exact hashes/changed members are recorded in `artifacts.json`; runtime and installation evidence is recorded beside this file.

```powershell
python docs/patches/priest-ward-tuning/build.py
python docs/patches/priest-ward-tuning/verify.py
python docs/patches/priest-ward-tuning/run_client.py
```

Testing uses isolated Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25. Personal worlds and the separate desktop server are untouched. No handbook rebuild or Git push.

Final result: **35 connected-client/integrated-server checks and 38 artifact/install checks passed**. Native Holy Light releases at 4%, 50%, 96% and full completion each apply 240 ticks (12s); doubled haste gives 6s. Base/improved Penance explosions and Circle healing match the new coefficients. Native Orb retaliation consumes one charge, deals 40% P under existing PvE scaling and applies strong knockback. Arcane Ward retreats at 3/4 blocks but not 4.01/8 blocks; its shield remains in every case.

Installed exact tested replacements in repository active/fixed packs. Originals are retained in `retired packs/priest-ward-tuning-2026-11-04/{active,fixed}`. Restart clients and server with matching updated JARs.

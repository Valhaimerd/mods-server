# Holy Wand damage/healing and Defense reward audit

4 November 2026. Holy Wand's native right-click spell `paladins:heal` now supports enemies as well as friendly targets. It uses the existing Holy Staff damage impact: **80% Healing Power damage**, with the same hit effects and target-relation rules. The existing **50% Healing Power heal** remains for self/eligible allies. This is target-dependent damage or healing, not a new heal-on-damage mechanic.

All four existing wand bindings retain the same spell ID. Range stays 24 blocks, casting stays 1s and base cooldown stays 4s with existing haste behavior. Healing-stone cost, class restrictions, Staff specialization and Holy Staff itself are unchanged. Damage passes through the pack's existing armor/resistance and PvE/PvP scaling.

Only two Paladins JAR resources change: the wand spell and its concise English description. No new Java hooks or dependencies are introduced.

The [Adventurer layout repair](../adventurer-layout-repair/README.md) is already installed. Its rewards are independently summed and exercised one node at a time in the native runtime, including refund removal and no outgoing Healing Power from healing-received rewards.

| Complete defensive path | HP | Armor | General Resistance | Healing received | Knockback resistance |
| --- | ---: | ---: | ---: | ---: | ---: |
| Aegis | +4 | +2 | 4% | 4% | 0 |
| Colossus | +6 | +2 | 2% | 8% | 0 |
| Bulwark | +2 | +4 | 8% | 0 | 20 percentage points |

General Resistance applies to physical and ordinary magic damage. Aegis's temporary 15% reduction and Colossus's triggered recovery are separate from these permanent totals. Ordinary and specialty rewards are counted once.

```powershell
python docs/patches/holy-wand-dual-purpose/build.py
python docs/patches/holy-wand-dual-purpose/verify.py
python docs/patches/holy-wand-dual-purpose/run_client.py
```

Results and exact tested/installed hashes are recorded beside this file. Tests run in an isolated Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25 client with its integrated server; personal installations/worlds are untouched. Routine tuning does not rebuild the handbook or push to GitHub.

Final result: **152 connected-client/integrated-server checks passed**. All four item bindings resolve to the changed right-click spell; native AIM finds enemies, damage matches Holy Staff under the current 80% PvE scaling, and self/ally healing remains 50% P without friendly damage. Every defensive node was checked against cumulative HP, armor, physical/magic Resistance, healing received and knockback totals, followed by complete refund removal.

The exact tested same-name Paladins replacement is installed in `mods/` and `mod fixing/1 fixed packs/`; originals are retained in `retired packs/holy-wand-dual-purpose-2026-11-04/{active,fixed}`. The installed Adventurer layout hash also matches the tested client. Restart both sides with matching JARs.

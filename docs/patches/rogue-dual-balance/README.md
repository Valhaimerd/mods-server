# Rogue dual-wield power balance — 25 October 2026

All Rogue class spell/modifier/passive resources previously using physical-melee power now use **D**, the engine's existing `spell_power:physical_melee_dual` school. D combines main-hand damage and the engine's offhand contribution. This also makes Shock Powder's mob-health threshold and Coated Blades' poison cap use D. Warrior resources and shared weapon trees are not converted.

| Effect | Installed behavior |
| --- | --- |
| Explosive Powder | 40% D; existing guaranteed proc, 4-block radius and no falloff |
| Slice & Dice | +8% D per empowered hit; charge count, pull and evasion unchanged |
| Fracture | 2 flat armor + 5% D removed per stack, up to three |
| Ambush | 30% D; existing two-second debuffs unchanged |
| Crimson Strikes | Heal 15% D once per successful Mutilate cast |
| Bloody Strikes | Ten-second, single-level Bleed |
| Prepared Counter | Next qualifying basic hit adds 15% D; skills cannot consume it |
| Bleed | 1 raw HP stationary or 4 raw HP with horizontal movement, every 25 ticks |

Bleed retains its original ownerless magic source, defenses and fixed single level; vertical movement alone does not increase damage. Serrated Traps keeps its 2.5-second duration. Fracture retains expiry/removal cleanup. Crimson Strikes still rejects empty/failed/protected hits and cannot heal repeatedly from multiple enemies.

Adventurer Skills ordinary attack speed changes from 3% to **2%**. RPG Rogue root/boost nodes change from 2% to **1%** in both class and weapon categories. Martial Prowess's 5% specialization bonus and all other nodes remain unchanged. Existing IDs and unlocks are preserved. The preceding Archer node balance remains installed.

Four same-name archives are overlaid: Adventurer Skills, Rogues & Warriors, Skill Tree and Spell Engine. Data resources set the schools, coefficients, durations, nodes and concise English descriptions. `PatchDual.java` changes only exact matched fields/constants in Fracture, Counter, Crimson healing and Bleed; match-count assertions stop unexpected inputs. No runtime maps, tick listeners, mixins or production classes are added.

`build.py` pins inputs under ignored `tmp/rogue-dual-balance/before`, builds candidates and records exact changed entries/hashes in `artifacts.json`. `verify.py --installed` checks active/fixed hashes, ZIP inventories/CRC, unrelated-entry preservation, schools and node data. The baseline reproduced 4 HP healing rather than 6 HP at D=40. Candidates passed **971 isolated Warrior/shield/party checks**, including native spell impacts, actual basic Counter attack, multi-target Crimson healing, Fracture stacks/cleanup, Bleed amplifier normalization/NBT and both node durations. The global damage-retention mod is excluded from this raw-coefficient fixture. No fresh client visual test is claimed.

```powershell
python docs/patches/rogue-dual-balance/build.py
python docs/patches/rogue-dual-balance/run_probe.py
python docs/patches/rogue-dual-balance/verify.py --installed
```

Matching active/fixed replacements are installed. Originals remain in ignored `retired packs/rogue-dual-balance-20261025`. Personal installations, the separate server and existing worlds were not changed. Sync/restart both sides; no commit or push requested.

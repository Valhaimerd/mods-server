# CrossArms Frenzy balance

Minecraft 26.2 / NeoForge 26.2.0.90, CrossArms 1.4. Same active JAR filename.

Capacity is removed entirely: its enchantment definition, functions, predicates, item modifiers and acquisition/exclusion references no longer ship. Capacity can no longer be obtained or applied in this pack. CrossArms now supplies seven enchantments. The Multishot/Piercing integration remains.

## Frenzy rules

| Heat display | Heat score | Shot damage retained | Magic self-damage per 40 ticks |
| --- | --- | --- | --- |
| Low / first two bars | 0-300 | 50% | None |
| Third / red bar | 301-450 | 60% | 1 HP |
| Fourth / dark red bar | 451-610 | 75% | 2 HP |
| Fifth / black bar | 611+ | 100% | 4 HP |

The timer begins on entering damaging heat and resets on cooling below it; sustained heat deals damage every 40 ticks (two seconds at 20 TPS). The amount uses the current bar at the damage tick. Damage uses `minecraft:magic`, so Fire Resistance does not cancel it. Heat cools by two units per world tick and remains attached to the player when switching weapons; death clears heat and its timer.

Shot damage is a multiplier on the existing projectile damage, preserving weapon bonuses and Ranged Weapon API scaling. The projectile reads its actual owner's heat, rather than a nearby player's. The requested 95% accuracy is implemented as **5% of ordinary crossbow spread**, not a guaranteed hit chance. Ordinary crossbows retain their spread. A narrow mixin changes only the Frenzy crossbow firing argument.

Overheating no longer forces the crossbow down to two durability. Normal one-durability firing wear remains, and auto-feeding still stops at one remaining durability. Frenzy consumes normal arrows and auto-feeds on vanilla and modded crossbows in either hand. The earlier firing fix uses `#minecraft:enchantable/crossbow`, rather than hardcoding the vanilla item. The former Capacity magazine repair is superseded by full Capacity removal.

## Rebuild

```powershell
python docs/patches/bows-balance/build.py --classpath-file tmp/bows-balance/classpath.txt
python docs/patches/bows-balance/verify.py tmp/bows-balance
```

The canonical build applies `overrides/`, removes Capacity data/references, and compiles only `src/` into production. It registers `crossarms-pack.mixins.json` for the accuracy hook. Provide a Java 25+ compiler and the NeoForge-patched 26.2 server/FML/Mixin dependency classpath. Original inputs are hash-pinned in the combined builder's `upstream.json`; final active hashes are in [artifacts.json](../bows-balance/artifacts.json). Tests never enter production archives. Personal worlds and configs are untouched.

## Verification

The final isolated server passed **145 current Frenzy checks**, testing real firing on vanilla and Arsenal 1.5.1 crossbows, in both hands, with Ranged Weapon API 4.0.0 and Expanded Crossbow Enchantings 1.11. Coverage includes Capacity absent from the enchantment registry; 50/60/75/100% damage relative to ordinary shots; repeated auto-feeding and one-arrow consumption; ordinary firing wear with no forced overheat break; exact magic self-damage after 40 ticks with Fire Resistance active; and the loaded accuracy hook returning 0.05 versus the unchanged ordinary spread of 1.0. Logs remain under `tmp/crossarms-firing/server`.

The first regression run failed because Capacity still existed. Review also caught an orphaned Capacity function and malformed actionbar continuations; both are removed/fixed, and the current archive has no function parse errors. The historical modded-crossbow repair passed 260 checks before Capacity was removed; those magazine checks describe the previous build, not this one.

Sources and attribution: [CrossArms](https://modrinth.com/datapack/crossarms), AlmazmanElytra, Din37r and IceBan; upstream content CC-BY-4.0. Original metadata and credits are retained.

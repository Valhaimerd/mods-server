# CrossArms Capacity and Frenzy on modded crossbows

Minecraft 26.2 / NeoForge 26.2.0.90, CrossArms 1.4. Same active JAR filename.

CrossArms allows its enchantments on `#minecraft:enchantable/crossbow`, but its firing functions and predicates checked only `minecraft:crossbow`. An Arsenal crossbow could receive Capacity and fire its first loaded arrow, then never refill. Frenzy's enchantment blocked ordinary loading while its vanilla-only checks prevented auto-feed. Vanilla firing worked in the isolated baseline.

The data-only overrides use the supported crossbow tag for magazine and auto-feed checks. Frenzy's broken-item predicate now tests remaining durability instead of the vanilla damage value 464. Overheating derives the effective maximum durability through the native item modifier and leaves two durability, preserving the crossbow's own maximum. No Java classes or new mod are required. The existing Multishot/Piercing merge remains in place.

Capacity fires **one arrow per trigger**, with **4/8/12/16 shots before reloading**, rather than firing the whole magazine at once. It only magazines normal arrows; tipped arrows retain a single loaded shot. Frenzy automatically feeds normal arrows, consumes one per feed, increases heat and loses normal firing durability. It stops feeding at one remaining durability.

## Rebuild

The canonical combined build automatically applies every file in `overrides/` after merging the CrossArms enchantment definitions:

```powershell
python docs/patches/bows-balance/build.py
```

For a focused candidate over an already integrated archive:

```powershell
python docs/patches/crossarms-firing/build.py --input mods/crossarms-1.4.jar --output tmp/crossarms-firing/crossarms-1.4.jar
```

Original and final hashes are recorded by the combined [bow build manifest](../bows-balance/artifacts.json). Upstream attribution and license: [CrossArms](https://modrinth.com/datapack/crossarms), AlmazmanElytra, Din37r and IceBan; CC-BY-4.0.

## Verification

The baseline reproduced Arsenal `unique_heavy_crossbow_1` failing Capacity's second shot. The repaired isolated server passed **260 checks** with Ranged Weapon API 4.0.0, Arsenal 1.5.1 and Expanded Crossbow Enchantings 1.11. Tests drive real `CrossbowItem` loading/firing and enchantment ticks, counting spawned arrow entities, for vanilla and Arsenal crossbows in both hands.

Coverage: all four magazine sizes, empty magazine after the final shot, six consecutive Frenzy shots, exact ammunition consumption, durability loss, heat increase, overheat leaving two durability for maximums 465 and 4062, feeding stopping at one durability, tipped ammunition staying single-shot and ordinary crossbows still requiring a reload. Test startup explicitly invokes the normal CrossArms load function because the fake-player probe runs before the first world tick. Test sources are excluded from production archives. This is focused firing verification, not a claim about every modded crossbow's custom abilities.

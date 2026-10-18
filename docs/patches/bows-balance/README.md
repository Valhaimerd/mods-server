# Too Many Bows accessories and crossbow integration

Minecraft 26.2, NeoForge 26.2.0.90, Java 25+; 18 October 2026.

Too Many Bows **4.0.0-26.2** replaces **1.0.1-26.2**. The supplied archive’s metadata declares both-side Architectury and Curios dependencies. The old active JAR is retired to the ignored backup folder. This is a local repack of the supplied 4.0.0 file, not a claim of an unmodified upstream release.

Removed accessories:

| Item | Removed bonus |
| --- | --- |
| Stormbound Signet | +30% bow damage |
| Sharpshot Ring | +15% bow damage |
| Dead Eye’s Pendant | Bow critical chance |

IDs stay registered for existing saves. Their Curios providers grant no modifiers and reject equipping; existing equipped copies also grant no bonuses. Slot tags, acquisition recipes/advancements where present, creative entries and JEI entries omit them. Legacy tooltips explain that they are inert. The shared programmatic loot builder filters these IDs even when an old generated config lists them. Other accessories and bow content are retained. Personal configs and worlds are not edited.

CrossArms **1.4** adds eight crossbow enchantments, Multishot III, projectile mechanics and cosmetic skins. Expanded Crossbow Enchantings **1.11** adds crossbow Looting and permits Multishot/Piercing together. Both supplied files override Multishot/Piercing; vanilla-style Expanded definitions would erase CrossArms effects depending on resource order. Both archives now contain identical merged definitions, retaining CrossArms effects and removing only the mutual vanilla Multishot/Piercing exclusion. Other CrossArms exclusions remain, including Capacity versus Multishot. Expanded’s Looting predicate is updated from `type` to 26.2’s `minecraft:entity_type`; the original causes a fatal enchantment-registry loading error. Looting still supports melee weapons.

No speculative crossbow damage multiplier is added. CrossArms supplies distinct weapon roles; equal DPS against every bow has not been established.

## Rebuild

Preserve the exact three supplied JARs as `tmp/bows-balance/original-<filename>`; SHA-256 values are in [upstream.json](upstream.json). Supply a classpath containing the NeoForge-patched 26.2 server JAR, NeoForge/FML/event libraries, the original Too Many Bows archive, Architectury, Curios and JEI API. The development environment caches it in `tmp/bows-balance/classpath.txt`.

```powershell
python docs/patches/bows-balance/build.py --classpath-file tmp/bows-balance/classpath.txt
python docs/patches/bows-balance/verify.py tmp/bows-balance
```

Outputs stay under `tmp/bows-balance`. Compiled production classes come only from `src`; `tests` never enters the pack. Added ZIP entries use fixed timestamps and stale compiled classes are cleared before each build. After verification, replace the three active files under their current filenames; retain originals and the previous Too Many Bows version in ignored retired packs. [artifacts.json](artifacts.json) records final production hashes.

## Verification

The original Expanded archive reproduced the registry startup failure. The integrated candidate passed **28 isolated dedicated-server checks**: registered legacy IDs, empty accessory modifiers, equipment rejection, removed slot tags, blocked stale-config loot, unrelated loot retained, all eight CrossArms enchantments loaded, Multishot III, Multishot/Piercing compatibility, Capacity incompatibility, Looting support for crossbows and swords, and creative removal. Test source: [BalanceProbe.java](tests/BalanceProbe.java).

The isolated client **passed** JEI removal, retention of Wind Glove and inert tooltips for all three removed items. Test source: [ClientProbe.java](tests/ClientProbe.java). Logs/results remain under ignored `tmp/bows-balance` and `tmp/skill-overhaul/client`. These are focused compatibility checks, not a complete crossbow balance playthrough.

Sources and attribution: [Too Many Bows](https://www.curseforge.com/minecraft/mc-mods/too-many-bows), [CrossArms](https://modrinth.com/datapack/crossarms) (AlmazmanElytra, Din37r, IceBan; CC-BY-4.0), [Expanded Crossbow Enchantings](https://modrinth.com/datapack/expanded-crossbow-enchantings) (ALDgamer, CDGG; archive declares ARR). Original metadata and license notices are retained.

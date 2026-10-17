# Server bugs — NeoForge 26.2 audit

## 17 October 2026 — current distribution note

The current repository distributes Advanced Loot Info 2.4.0, ALICompat 1.3.0, and required Advanced Core Info 1.5.0 in `mods`. Advanced Worldgen Info 1.2.0 is in `mod store` to preserve the prior server-stall isolation. Older statements below that all four JARs are excluded describe the historical audit set. Emotecraft is now in `mods`; its borrowing add-on, MossyLib, Universal Enchantment Info, and the local Vectors NeoForge alpha are client extras. The new combination has been inspected for metadata/dependencies, not rerun through the historical full server audit.

## 14 October 2026 — client/server split check

An isolated NeoForge **26.2.0.90** server loaded all **293 repository `mods` JARs** with a copy of the live world and config on port 25942. It reached `Done`, answered `list`, and stopped cleanly with exit code 0. The live server folder was not changed. Evidence: [`latest.log`](../tmp/server-client-split-test/logs/latest.log). This is a startup and existing-world check; no player joined or new terrain was deliberately explored.

The first run also included the four Advanced Core/Loot/Worldgen Info and ALICompat JARs. It reached `Done`, but Advanced Worldgen Info kept the server thread in `FeatureConfigurationCollectorUtils.collectCompositeFeatureConfigurationBlocks` for more than three minutes; queued commands did not run. This matches the earlier watchdog investigation. Those four JARs now live in `mod store`, so server installs exclude them. `underground_village` also required Better Library 2.1.0 or newer; the verified 2.1.2 NeoForge JAR was added to `mods` before the successful run.

The successful run still logged nonfatal data errors. In addition to the older AddonsLib, Guard Villagers, Animal Pen, Penchant, Better Nether, and Starcatcher issues below, these new entries need review:

- **Stoneholm:** four loot tables fail to load. Three reference Create items absent from this pack; the cleric table uses the removed `minecraft:set_nbt` function. Gate the Create loot behind its dependency and port the cleric loot function.
- **When Dungeons Arise:** five structure advancements use an outdated item-predicate shape, and two dependent advancements do not load. Its `ignores_ensnaring` entity tag also has missing references, so the associated predicate is rejected. Repair its data resources for 26.2.

These data errors did not prevent server startup. They do mean that affected loot, advancements, and recipes should not be considered working until repaired and checked in game.

Audited **13 October 2026, 01:47–01:50 (Taiwan time)** against `C:\Users\Valhaimerd\Desktop\Minecraft-NeoForge-Server`. I copied its world and configuration into an isolated instance, used the same NeoForge **26.2.0.88** launch files, and loaded all **287 current server JARs**. The test instance used a random port; the original world and server configuration were not changed. The server reached `Done`, answered `list`, remained running until the normal empty-server pause, and stopped cleanly with `stop`. There was **no crash report** in this run. Its log recorded **10 ERROR** and **330 WARN** lines; counts include repeated messages.

Evidence: [current test log](../tmp/server-audit-current-2026-10-13/logs/latest.log). This was a startup and copied-world test. No player joined and no new terrain was deliberately generated, so it does not prove every mod interaction or world-generation path is safe.

## Watchdog timeout with the earlier mod set

**Advanced Worldgen Info (AWI): exceeded the watchdog during information collection.** A prior isolated run with the previous **291-JAR** set loaded the world, then hit the configured **180-second watchdog** while AWI was collecting worldgen information. The [crash report](../tmp/server-audit-2026-10-13/crash-reports/crash-2026-10-13_01.45.45-server.txt) repeatedly shows `awi@1.2.0` in `FeatureConfigurationCollectorUtils` and `AwiServerRegistry`. This proves the tick exceeded 180 seconds; it does **not** establish whether the collection would eventually finish. Earlier server crash reports show the same AWI pattern. The current folder excludes `AdvancedWorldgenInfo`, `AdvancedCoreInfo`, `AdvancedLootInfo`, and `ALICompat`; the 287-JAR run did not time out. **Recommendation:** if restoring that group, first increase `max-tick-time` in an isolated server copy and measure whether collection completes. Do not assume a higher limit fixes a permanent stall. [AWI's project page](https://modrinth.com/mod/advanced-worldgen-info) describes it as a recipe-viewer information plugin.

## Current errors to fix

| Priority | Source | Observed problem | Recommendation |
| --- | --- | --- | --- |
| High | `addonslib-neoforge-26.2-1.0.jar` | Its `data/neoforge/loot_modifiers/global_loot_modifiers.json` uses the older `{"replace":false,"entries":["addonslib:add_loot_table"]}` shape. NeoForge rejects the file with `No key type`. | Port the loot modifier declaration to the 26.2 format, or replace the JAR with a verified compatible build. Check its intended drops afterward. See [NeoForge's global loot modifier documentation](https://docs.neoforged.net/docs/resources/server/loottables/glm/). |
| Medium | `guardvillagers-4.0.4-26.2.0.jar` | `recruit_guard.json` refers to an unrecognized `minecraft:type` entity sub-predicate. The advancement is rejected. | Update or patch that advancement JSON; verify recruiting a guard awards it. |
| Medium | `animal_pen-neoforge-26.2-2.5.0.jar` | The guide-book crafting recipe is rejected: an ingredient list is empty under this version's parser. | Update or patch `data/animal_pen/recipe/guide_book.json`, then check the guide can be crafted. |
| Medium | `penchant-neoforge-26.2.0-1.6-fixed.jar` | Its tome recipe cannot resolve `penchant:tome_of_penchant`, and the result's object form also fails parsing. | Check that the tome item is registered in this build, then repair the 26.2 recipe result and verify crafting in-game. |
| Medium | `BetterNether-26.2.2.jar` | Patchouli skips `betternether:betternether_book`: its book content still uses the removed server-side layout. Two optional built-in datapacks also lack `pack.mcmeta`. | Port the Patchouli book resources and optional datapack metadata, or use a corrected build; verify the book and those integrations. |
| Medium | Starcatcher's `selling_bin_fishes` compatibility data | Five `selling_bin:quality_foods_processor` errors. Starcatcher supplies references to that processor, but it is not registered; Selling Bin substitutes an empty processor. | Disable or repair that compatibility data if Quality Food is not in this pack. The [Selling Bin author's wiki](https://github.com/wdiscute/Selling-Bin/wiki/Customizing-%26-creating-a-datapack) says this processor requires Quality Food. Verify fish sale values afterward. |

## Warnings that can affect gameplay

- **World/mod mismatch:** the copied world reports twelve removed mod IDs plus a Display Delight version change (`fix1` to `fix2`). The missing IDs are `easymagic`, `eatinganimationid`, `enchantinginfuser`, `lockedinslots`, `refurbished_furniture`, `rugmod`, `seamlessapi`, `seamlessdeconstructor`, `sensible_stackables`, `skillperks`, `the_darkness_will_find_you`, and `travelertoolbelt`. Keep a backup of the original world before further pack changes; check whether any removed mod's blocks or items matter to existing builds. The test saved only its copied world.
- **Recipe placement (21 warnings):** Simple Hats, Arrow+, and Fletching Recipe entries have empty ingredients and are ignored for recipe placement. Check these crafts if players need them; repair the relevant recipe or ingredient tags.
- **Loot tables (2):** Alex's Mobs seal reward uses a loot parameter absent from that context; Invasion Mod's wildfire loot table references missing `friendsandfoes:entities/wildfire`. The affected drops may differ from intent.
- **Mob spawn categories (21):** several biome entries use a different category from the mob's registration, including Astral's `fiery_birch_forest` magma cubes, Stellarity illusioners, and Ender Zoology dire wolves. NeoForge warns this can distort mob caps. Review the biome spawn entries if spawn rates seem wrong.
- **Structure salts (16):** Structure Essentials finds duplicate salts among structure sets from Alex's Caves, Nova Structures, Towns and Towers, and others. Overlap is possible; give conflicting sets distinct salts if world generation shows collisions. Changing salts can alter placement in unexplored chunks.
- **Networking mixins (3):** These are skipped patch warnings, not startup failures. The server started normally, and the mods' other patches are not disabled wholesale.
  - Two warnings target the 1 MiB constants in `ClientboundCustomPayloadPacket`. Packet Fixer's priority 1000 patch wins over Connectivity's priority 1 patch. `packetfixer.properties` has `allSizesUnlimited=true`, so Packet Fixer's `getPacketSize()` returns `2,147,483,647` at these sites despite `packetSize=1048576` in the same file. Other packet limits may still apply. Connectivity's `disableLoginLimits=true` patch is skipped at these two sites.
  - One warning targets the 15-second constant in `ServerCommonPacketListenerImpl.keepConnectionAlive`. Connectivity's priority 1001 patch wins over Packet Fixer's priority 1000 patch. With `disconnectTimeout=60`, Connectivity returns about 43 seconds when a keepalive response is pending; its intent is roughly a 60-second total window. Packet Fixer's `keepAliveTimeout=120` does not control this site. No conflict was reported for Packet Fixer's separate `checkIfClosed` patch.
  - **Recommendation:** keep both mods if their other functions are needed, but choose the intended size and timeout policy in their configs and test client login and large modded packets. Removing a mod solely to silence these warnings is unnecessary.
- **JEI Trades (90):** thirty nonconstant trade counts default to zero and sixty zero counts are changed to one. Inspect trade displays if they look wrong; these warnings did not block server startup.
- **Online authentication:** `server.properties` has `online-mode=false`, and NeoForge warns the server is insecure. If it will be reachable by others, enable authentication before public access; keep offline mode only if that is an intentional private setup.

## Lower-priority startup noise

- **Mixin warnings (151):** 47 unreadable reference maps, 53 absent client-side targets, 47 client class-load warnings, the 3 networking conflicts above, and 1 constructor selection. Most are packaging or dedicated-server-side warnings rather than observed crashes; investigate a specific mod only if its feature misbehaves.
- **Resource metadata (5):** `ivp`, `mns`, `kingdomcats`, `quickskin`, and `stronger_skill_tree` declare pack formats that fail 26.2 metadata parsing; NeoForge falls back. Refresh their `pack.mcmeta` when repacking.
- **Deprecated `lowcodefml` declarations (11):** metadata cleanup for several data mods; no load failure.
- **Waystones config (1):** NeoForge corrected `config/waystones-common.toml` in the isolated copy. Check the original config's intended values before its next live start.
- **Other isolated warnings:** duplicate JarJar coroutines dependency selection; Better Nether's duplicate pig-statue point of interest; optional built-in packs missing metadata; update notices. They did not stop this test.

## Historical reports, not reproduced in the current run

- The old server log contains thousands of unsafe terrain-read errors while generating Wyther's dark-jungle trees. The current 287-JAR folder does not contain that mod, and this test generated no new terrain; keep the historical issue separate from current startup results.
- September crash reports show a `threateningly_mobs:ferox_death_worm` entity hitting `Mod fluids must override getFluidType` under NeoForge 26.2.0.87. That mod is absent now, and the current 26.2.0.88 startup did not reproduce the crash.

**Suggested order:** retain the stable 287-JAR baseline, back up the world, fix the rejected data files and Starcatcher compatibility data, then test player login, crafting/trading, and new chunk generation on a copied instance. Keep each mod change isolated so a new failure has a clear source.

# End's Delight 26.2 source port

This is **End's Delight** (`ends_delight`), by FoggyHillside, not Ender's Delight.

Upstream: https://github.com/FoggyHillside/End-s-Delight, branch `1.21.1`, commit `5778ed081dfb18656f04cb1ba91220bf87ea2bba`. Original input: `mod fixing/delight extras (optional)/ends_delight-2.6.1+neoforge.1.21.1.jar`; SHA-256 `030593aa0957520b114c7ab36532cbf3c63c02d7d1bff86331b8bd9fc4145c4e`. MIT, copyright 2022 FoggyHillside; original LICENSE is retained and embedded in the production JAR.

Production candidate: `tmp/delight-batch/ends_delight/ends_delight-2.6.1+neoforge.26.2-port1.jar`, SHA-256 `800a650a1e203dbaf998d3a867d643dec23e7c13721eedde986e97e148cc56bf`. Root workflow owns promotion/install and combined/client verification.

## Preservation and API changes

All 63 item IDs and 8 block IDs remain. All 75 original recipe IDs, outputs/counts, chances, patterns, cooking times and experience remain: 14 FD cooking, 10 cutting, 12 shapeless, 11 shaped, 9 each smelting/smoking/campfire and 1 smithing. Original textures/models/lang/worldgen/loot tables remain. Added 63 native item definitions matching the registered item IDs exactly. The original unregistered liquid-egg-in-shell model is retained for resource preservation but has no generated item definition, preventing its nonexistent unused texture from being baked. Four repair tags preserve all knife materials and numerical stats.

Food nutrition, saturation, always-edible flags, 63 effect applications and 7 fast foods are migrated to native FoodProperties/Consumable components without balance changes. Native use remainders preserve bowls, bottles and shulker shells; crafting remainders remain. Drinks retain drink animation/sounds. Custom teleport, shift requirements, wine cleansing, popsicle cooling and knife event implementations remain; NeoForge teleport events receive the consumed stack, copied before the gristle item empties it.

End stove remains an AbstractStoveBlock/AbstractStoveBlockEntity with six original offsets and native campfire cooking. Its renderer extends the installed FD DefaultStoveRenderer, which extracts/submits native item render states for all inventory slots using the overridden offsets. This avoids deprecated renderStatic paths without replacing the stove with a stub. An Ends-only getUpdateTag override serializes the inherited inventory/timers for update packets and newly tracking chunks; the installed FD base otherwise inherits vanilla's empty update tag.

Migrated registry keys, ToolMaterial, block entity construction, block update signatures, player messages, identifiers, entity namespaces and loot-modifier priorities. Recipes use modern item/tag/custom-ingredient codecs. Entity loot predicates use entity_type; the legacy modifier index is removed because NeoForge 26.2 discovers each modifier JSON directly. All nine modifier definitions remain.

## Rebuild and checks

Source directory: `mod fixing/source/delight-batch-20261027/upstream/ends_delight`. Compile/runtime Farmer's Delight authority is the repository's `mods/FarmersDelight-26.2-1.3.3-fix5.jar`; it is referenced without embedding. ModDev 2.0.148, Gradle 9.5.1, Minecraft 26.2, NeoForge 26.2.0.90, Java 25 toolchain (classfile 69). A Java 26 launcher is usable with Gradle 9.5.1; Gradle resolves the installed Java 25 toolchain.

From repository root, set JAVA_HOME to an appropriate JDK and run:

```powershell
& 'mod fixing/source/delight-batch-20261027/upstream/ends_delight/gradlew.bat' -p 'mod fixing/source/delight-batch-20261027/upstream/ends_delight' build --console plain
python docs/patches/delight-batch/ends_delight/check_resources.py
& 'mod fixing/source/delight-batch-20261027/upstream/ends_delight/gradlew.bat' -p 'mod fixing/source/delight-batch-20261027/upstream/ends_delight' runServer -Pprobe --console plain
```

The native probe requires `run/eula.txt` acceptance and an isolated `run/server.properties`; this session used offline localhost port 25697, view/simulation distance 2, a fresh flat world and only FD/Ends plus the test fixture. Probe sources live in `src/probe`, are enabled only by `-Pprobe`, and are excluded from the production JAR. Require `ENDS_CHECK ALL PASS` and absence of Minecraft ERROR messages: the fixture catches exceptions and stops the server, so Gradle exit status alone is insufficient.

Observed checks:

- Baseline RED: 151 old-source compilation errors on target APIs; first startup exposed old loot predicates, legacy index and nested compound ingredients. Intermediate preservation/native tests caught result wrapper loss before it could be promoted; restored exact original results.
- GREEN: final production Gradle build. All 40 compiled classes are Java 25 bytecode, metadata requires Minecraft 26.2/NeoForge 26.2.0.90+, MIT included and probe classes absent.
- GREEN: resource check compares all 75 original recipe input semantics/results/times/XP/patterns, PNG bytes, food nutrition/effect IDs/durations/amplifiers, fast foods, item definitions and modifier count.
- GREEN: isolated native `probe3.log`, MC 26.2 / NeoForge 26.2.0.90 / Java 25 / FD 1.3.3-fix5: 63 item IDs, 8 block IDs, all 75 decoded recipes; six stove insertions and full capacity; 610 native cooking ticks empty all slots and produce six real dried-endermite outputs; consuming shulker soup returns its bowl and applies levitation. No Minecraft ERROR lines. A final consumed-stack-copy correction was then compile-verified; the probe does not exercise gristle teleport randomness.

Evidence: `tmp/delight-batch/ends_delight/{compile-red.log,server.log,probe2.log,probe3.log,build-final.log,sync-red.log,sync-green3.log,build-sync.log,build-resources-final.log}`. These are development/server checks, not client-render or combined-pack evidence. Root workflow tests combined mods and the client renderer. Personal installations/worlds were untouched.

## Stove client synchronization follow-up

The native client fixture with six insertions showed server inventory 6 but client inventory/render items 0 on the earlier candidate. Inspection confirmed FD AbstractStoveBlockEntity.getUpdatePacket calls create(this) while neither it nor EndStoveBlockEntity supplied getUpdateTag, so vanilla serialized an empty compound. A focused server regression failed on missing Inventory before the fix (`sync-red.log`). The fix is confined to EndStoveBlockEntity and sends saveCustomOnly(provider), preserving inventory and both cooking timer arrays. Client integration with the sync-fix `0108fb5f...` candidate then passed 322 checks, including actual client inventory 6, six nonempty renderer item states, six original offsets and six nonempty food models. A subsequent resource-only candidate `800a650a...` removes the unused item definition; no Java classes or gameplay behavior changed. Final client visual confirmation belongs to the client integration workflow. The client agent retains full evidence under `docs/patches/delight-batch/client/` and `tmp/delight-batch/client-all/`. The final native server probe (`sync-green3.log`) passed every check: inventory plus cooking timers in the actual update packet, six filled mirrored slots after NBT round trip, all six real cooked outputs and soup bowl/effect behavior. The fixture resets its block and local drops for repeat runs; an intermediate run inherited saved RED inventory and another reused old block state, so these fixture failures are retained in sync-green/sync-green2 logs without being treated as product regressions.

## Reproducible patch

`port.patch` contains the complete upstream-relative source/build/resource diff and test fixture, including new files; checked successfully with `git apply --reverse --check` against the ported source and `git apply --check` against a separate clean checkout of the pinned commit. Apply it to a clean checkout of the pinned upstream commit from inside that checkout:

```powershell
git apply '/path/to/docs/patches/delight-batch/ends_delight/port.patch'
```

The build.gradle dependency path assumes the recorded repository layout; update that local path when rebuilding outside this pack. No tests or external dependencies are embedded in the production mod.

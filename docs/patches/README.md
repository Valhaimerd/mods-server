# Alex's Caves Continued: Emotecraft preview repair

The pack's `mods/alexscaves-1.0.11-neoforge+26.2.jar` is a same-version local repair of [Alex's Caves Continued](https://modrinth.com/mod/alexs-caves-continued), whose [source repository](https://github.com/Codx-org/AlexsCavesContinued) supplies the original LGPL-3.0 code. Only `com/github/alexmodguy/alexscaves/server/misc/ACFluids.class` changes; metadata, assets, fluid algorithms and all other classes remain unchanged.

Emotecraft's preview mannequin uses a dummy level with no chunk source. The original `fluidHeight(Entity, TagKey, TagKey)` dereferences that source during `LivingEntityRenderer.extractRenderState`, causing `Failed to render entity preview!` and blank wheel/list models. `isEyeInFluid(Entity, TagKey)` makes the same assumption. The patch adds an early zero/false return when the entity has no level or its level has no chunk source. Real world levels continue through the original method bodies.

## Rebuild

Use Java 25+ and ASM 9.10.1. `ACFluidsPreviewGuard.java` contains the complete transformation; keep the original mod's license and notices in the resulting archive.

1. Start with the original 1.0.11 NeoForge 26.2 JAR, SHA-256 `e81fb86f6de31194cb407ea0144cb56945ab886c957040840375dd329094fc3c`.
2. Extract `com/github/alexmodguy/alexscaves/server/misc/ACFluids.class` as `ACFluids.class` in a temporary directory.
3. Compile and run, replacing the ASM path as needed:

   ```powershell
   javac --release 25 -cp asm-9.10.1.jar ACFluidsPreviewGuard.java
   java -cp '.;asm-9.10.1.jar' ACFluidsPreviewGuard ACFluids.class ACFluids-patched.class
   ```

4. Rebuild the original ZIP/JAR, replacing only that entry with `ACFluids-patched.class`. Retain the active JAR filename. Verify every other uncompressed entry is byte-identical and the archive passes its CRC check.

The current repaired archive has SHA-256 `feb97d1ecf6b6dacf18aaef8c8ae478828b60d08d42a73076916ac1daef6f610` (ZIP compression settings can change the archive hash).

## Observed verification — 2026-10-17

An isolated client used Minecraft 26.2, NeoForge 26.2.0.90, Java 26.0.2, Alex's Caves Continued 1.0.11 with CodxLib 1.6.0, Emotecraft 3.4.0-b.build.170, PAL 1.2.6 and Vectors 1.5.0-alpha.4 at Maximum Performance. With the original JAR, the wheel was blank and its fluid-height exception repeated 4,864 times. With the repaired JAR, all eight animated wheel models and the list's large left preview were visible; neither preview rendering nor preview ticking logged a failure. No personal world was used. Offline library authentication warnings were unrelated to these local built-in previews.

This is a focused preview regression check, not a full cave/fluid gameplay or multiplayer test. The original archive is retained locally in ignored `mod fixing/retired packs/`; final production copies are in `mods/` and ignored `mod fixing/1 fixed packs/`. Test instances, probe JARs, screenshots and intermediate classes remain under ignored `tmp/emotes-vectors-fix/`.

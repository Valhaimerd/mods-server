# Alex's Caves Continued: Emotecraft preview repair

The pack's `mods/alexscaves-1.1.2-neoforge+26.2.jar` carries forward our repair of [Alex's Caves Continued](https://modrinth.com/mod/alexs-caves-continued), whose [source repository](https://github.com/Codx-org/AlexsCavesContinued) supplies the original LGPL-3.0 code. Only `com/github/alexmodguy/alexscaves/server/misc/ACFluids.class` changes; metadata, assets, fluid algorithms and all other classes remain unchanged. The obsolete repaired 1.0.11 is no longer in the active or fixed-pack folders.

Emotecraft's preview mannequin uses a dummy level with no chunk source. The original `fluidHeight(Entity, TagKey, TagKey)` dereferences that source during `LivingEntityRenderer.extractRenderState`, causing `Failed to render entity preview!` and blank wheel/list models. `isEyeInFluid(Entity, TagKey)` makes the same assumption. The patch adds an early zero/false return when the entity has no level or its level has no chunk source. Real world levels continue through the original method bodies.

## Rebuild

Use Java 25+ and ASM 9.10.1. `ACFluidsPreviewGuard.java` contains the complete transformation; keep the original mod's license and notices in the resulting archive.

1. Start with the supplied original 1.1.2 NeoForge 26.2 JAR, SHA-256 `5a9ca503775705e574c42d1c7d8f4b65f96b8bc6b862676202245d8efba07d3c`. The older original 1.0.11 SHA-256 was `e81fb86f6de31194cb407ea0144cb56945ab886c957040840375dd329094fc3c`.
2. Extract `com/github/alexmodguy/alexscaves/server/misc/ACFluids.class` as `ACFluids.class` in a temporary directory.
3. Compile and run, replacing the ASM path as needed:

   ```powershell
   javac --release 25 -cp asm-9.10.1.jar ACFluidsPreviewGuard.java
   java -cp '.;asm-9.10.1.jar' ACFluidsPreviewGuard ACFluids.class ACFluids-patched.class
   ```

4. Rebuild the original ZIP/JAR, replacing only that entry with `ACFluids-patched.class`. Retain the active JAR filename. Verify every other uncompressed entry is byte-identical and the archive passes its CRC check.

The current repaired 1.1.2 archive has SHA-256 `70976ac92ec6abdc9e778b8f23ef2e73a3a67e8b869a81ea9d69144fd6fa3a2a` (ZIP compression settings can change the archive hash). The historical repaired 1.0.11 hash was `feb97d1ecf6b6dacf18aaef8c8ae478828b60d08d42a73076916ac1daef6f610`.

## Update verification — 2026-10-20

Git history and the archived original confirm that 1.0.11 had this custom patch. The supplied 1.1.2 still dereferences the level/chunk source without those checks. The existing transformer matched both exact method signatures and applied cleanly. Every other uncompressed archive entry is identical, including metadata, licenses and assets; the archive CRC check passed.

`test-fluid-preview.py` executes the actual helper bytecode with minimal Minecraft API fixtures and JVM verification enabled. The unpatched 1.1.2 fails with a null-level exception; the repaired build passes ten checks for missing levels, missing chunk sources, unloaded chunks, live fluid and dry paths:

```powershell
python docs/patches/test-fluid-preview.py mods/alexscaves-1.1.2-neoforge+26.2.jar --java-bin 'C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin'
```

These fixture checks do not replace an in-game preview or full cave gameplay test on 1.1.2. Its required CodxLib 1.3.6+ is satisfied by the installed 1.6.0. Both upstream and replaced archives remain in ignored retired packs; the current repair is also in fixed packs.

The accompanying building replacement uses the supplied **Sophisticated Building 5.1.0**, mod ID `sophisticatedbuilding`, in place of Effortless Building 4.3. Its embedded metadata requires Minecraft 26.2 / NeoForge 26.2.0.88+; installed Sophisticated Backpacks 3.26.2 and Core 1.5.0 satisfy its optional integration ranges. All installed metadata was scanned: no other mod declares an Effortless Building dependency. The [official project](https://www.curseforge.com/minecraft/mc-mods/effortlessbuilding-sophisticated) and [source](https://github.com/navrelis/EffortlessBuildingSophisticated) confirm this is a standalone client/server fork. Public listings inspected showed 5.0.2; this pack uses the user's supplied 5.1.0 metadata and does not claim it was downloaded from that listing.

The installer source profile uses the verified new key IDs: radial menu on Alt, the other six actions unbound. The next installer build will embed this profile. No released installer was rebuilt or published for this task. No automatic conversion of old Effortless Building items/settings is asserted. No new building gameplay test, commit or push was performed.

## Observed verification — 2026-10-17

An isolated client used Minecraft 26.2, NeoForge 26.2.0.90, Java 26.0.2, Alex's Caves Continued 1.0.11 with CodxLib 1.6.0, Emotecraft 3.4.0-b.build.170, PAL 1.2.6 and Vectors 1.5.0-alpha.4 at Maximum Performance. With the original JAR, the wheel was blank and its fluid-height exception repeated 4,864 times. With the repaired JAR, all eight animated wheel models and the list's large left preview were visible; neither preview rendering nor preview ticking logged a failure. No personal world was used. Offline library authentication warnings were unrelated to these local built-in previews.

This is a focused preview regression check, not a full cave/fluid gameplay or multiplayer test. The original archive is retained locally in ignored `mod fixing/retired packs/`; final production copies are in `mods/` and ignored `mod fixing/1 fixed packs/`. Test instances, probe JARs, screenshots and intermediate classes remain under ignored `tmp/emotes-vectors-fix/`.

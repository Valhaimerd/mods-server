# Nameless Trinkets: pack balance and server repair

This same-version repair targets `mods/Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar` (Curios variant, Java 25 bytecode). `balance.json` records the requested settings, 12 removed relics and Titan's effect durations/amplifiers. It retains all other upstream classes and resources except the explicitly changed classes, affected item tags, 36 removed recipes/advancements and the English tooltip updates.

## Resulting pack behavior

- 42 trinkets remain available. Broken Magnet, Cracked Crown, Dying Star, Electric Paddle, Ethereal Wings, God's Crown, Lunar Crest, Pocket Lightning Rod, Reforger, Speed Force, Super Magnet and What...? Magnet are disabled. Registry IDs remain for existing saves; old stacks are inert and removable, refuse equipping, and are excluded from crafting, random rewards, loot injection, the creative tab and JEI.
- `PackBalance` applies at `TrinketItem.setTrinketConfig`, both during construction and when old disk/network configuration is assigned. The changed fields cannot silently revert through stale generated JSON. Unlisted configuration fields retain their original behavior. Constructor defaults also match the policy. The existing JSON files themselves are not rewritten by this patch.
- The percentages are interpreted according to each handler: Rage Mind is **+25% / 1.25x** total damage, Scarab is **1.5x**, Shrinking Veil is **1.25x**, Vampire Blood is **+20% / 1.2x**, and XP is **+50% / 1.5x**. Reverse Card's random check is corrected from `<=` to a strict threshold for exactly 25%, rather than 26%.
- Ghast Eye gives **+6 maximum HP / +3 hearts**, Regeneration I initially for 5 seconds, and 2.5 seconds per extension. It does not give a flat six-HP heal on every kill.
- Titan's Mark retains +50% scale. It removes its attack modifier, including a previously applied legacy modifier. While worn it refreshes **Slowness I for 1,200 ticks**, **Health Boost V for 100 ticks (+20 maximum HP / +10 hearts, confirmed by the user)**, and **Resistance I for 100 ticks**. Health Boost adds capacity without filling the added hearts. After removal, effects expire at their remaining durations; other sources of those effects are not forcibly cleared.
- Legacy crown, Dying Star and Speed Force attribute modifiers are removed when the corresponding old stack ticks or is unequipped. Ethereal Wings' old flight ability is cleared for survival/adventure wearers on those same paths. Personal worlds are not edited.

## Why removing Lunar Crest alone was insufficient

The original `LunarCrestEvents` listener executes on damage events, and the shared `LunarCrestHandler` contains a client-only Minecraft helper. The dedicated server resolves `ClientLevel` when that shared class is reached, causing the supplied `NoClassDefFoundError`. This is independent of whether new Lunar Crests can still be obtained. The repair removes the listener entirely for the removed relic and replaces the obsolete client-bonus method with a zero-returning compatibility method, leaving no client-class references in the shared handler.

## Rebuild

1. Preserve the original JAR as `tmp/nameless-balance/upstream.jar`. `repack.py` verifies its pinned SHA-256 before changing anything. The ignored retired-packs backup contains the same original bytes.
2. Provide a compilation classpath containing the **NeoForge-patched** Minecraft 26.2 client/common JAR, NeoForge/FML and their dependencies, the original Nameless Trinkets JAR, Curios 16.0.0+26.2, and JEI 30.32.0.215. Put the NeoForge-patched Minecraft JAR first; a vanilla-only JAR lacks the added creative/loot methods. This pass used the cached 26.2.0.88 development artifact and exercised the result on 26.2.0.90.
3. Run `build.ps1 -ClassPath '<semicolon-separated dependency paths>'` from the repository root with Java 25+ and Python available. It compiles only `src/` and repacks the original archive to `tmp/nameless-balance/Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar`. Test-only probe source under `tests/` is never included in that compile or production archive.
4. Inspect `repack.json`, run archive/static checks, and repeat the isolated tests before replacing the active same-filename JAR. Keep upstream license notices. Neither this patch nor its build script publishes artifacts.

## Verification — 17 October 2026

- A minimal dedicated server with Minecraft 26.2, NeoForge 26.2.0.90, Java 25, Curios 16.0.0+26.2, Nameless Trinkets and a test-only probe reproduced the original `ClientLevel` crash on player damage with a legacy Lunar Crest equipped.
- The repaired JAR passed **108 dedicated-server checks**, including that exact damage path, all requested numeric overrides after deliberately stale config assignment, all 12 removed relics' disabled/equip/tag/recipe paths, Titan's health/effect levels/durations/refresh and attack cleanup, guaranteed Poison I for three seconds on a susceptible creature, XP and magic-damage calculations, Vampire Blood's actual 1.2x attribute, Scarab/Veil movement ratios and legacy modifier cleanup.
- A separate isolated client with Minecraft 26.2, NeoForge 26.2.0.90, Java 26.0.2, Curios and JEI confirmed the 12 removed relics are absent from JEI while Titan's Mark remains available. The client and dedicated server both stopped normally.
- Static checks verify 42 entries remain in the acquisition tag, no removed relic survives the relevant item tags/recipes, and the archive passes CRC validation. Unrelated ZIP entries are preserved.

These are focused tests, not a full multiplayer playthrough of every surviving trinket and interaction. Test worlds, logs, probe JARs and intermediate classes remain under ignored `tmp/nameless-balance/`. The active pack and ignored fixed-packs folder hold the final JAR; the personal `.minecraft` installation and separate desktop server were not changed. The owner's detailed working reference remains `mod fixing/Nameless Trinkets - effects.md`.

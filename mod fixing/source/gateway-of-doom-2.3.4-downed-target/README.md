# Gateway of Doom 2.3.4 downed-player guard

The upstream 2.3.4 release adds weighted multiplayer targeting for wave mobs and separate threat systems for bosses. Its target checks treat a downed PlayerRevive or Better Revive player as alive, allowing combat to remain focused on someone who cannot fight.

`DownedTargeting.isActive` preserves the normal living-entity check and additionally rejects players in either installed downed state. `PatchDownedTargeting` substitutes this predicate only at the existing `isAlive` checks in `GatewayTargeting.isValid`, `BossFocus.valid`, the four boss entity target validators, and the Rogue, Stone Golem, Cinder Wraith, and Kobold enemy validators. The Kobold check resides in `isEnemy`; its `valid` method delegates there. The upstream scoring, reassignment timing, attack behavior, and assets are unchanged.

Compile both Java files with JDK 26 using `--release 25`. The helper needs the Minecraft/NeoForge, PlayerRevive, and Better Revive JARs; the patcher needs ASM and ASM Tree. Extract the original 2.3.4 classes to `original/`, run the patcher into `patched/`, then run `repack.py` against the original upstream JAR. The final production artifact keeps the 2.3.4 filename and belongs in `1 fixed packs/` and `mods/`. Leave the older fixed 2.2.1 JAR in `retired packs/`, not active `mods/`.

Static compilation and archive inspection are packaging evidence, not a multiplayer runtime check. An isolated encounter with two players, including a downed and revived player, is still needed to confirm every custom attack path in play.

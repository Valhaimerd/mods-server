# Better Revive grave guard

The original block already has bedrock strength, explosion resistance, a no-op explosion callback, a detonation-list filter, and canceled player breaking. A direct world block replacement can still remove its block entity and contents. `PatchGraveGuard.java` adds a `setRemoved` hook to the existing `GraveBlockEntity`; `GraveGuard.java` restores that same entity after one tick when its grave ID is still registered, its deadline has not passed, and the block is absent. Normal collection and expiry unregister the ID and therefore are not reversed.

Compile `GraveGuard.java` for Java 25 against the matching Minecraft 26.2 server and NeoForge libraries plus the source Better Revive JAR. Compile `PatchGraveGuard.java` against ASM and ASM Tree, run it on the original grave entity class, then use `repack.py` to insert both classes into the source JAR. No changes to the separate desktop server are part of this patch.

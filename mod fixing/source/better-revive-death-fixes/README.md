# Better Revive death and compass fixes

The prior JAR already contains the grave guard, XP fix, retained-gear rule, and five-minute expiry. The active repack modifies three original classes and adds `DeathFix`.

Root causes:
- PlayerRevive leaves a downed player alive. On a second lethal hit its death handler returns, and Better Revive could enter its own downed state; the player could therefore die twice. After creating the Better Revive grave, the patch immediately calls its existing final-death path when PlayerRevive reports bleeding. Respawn also clears persisted downed state and the PlayerRevive bleeding flag.
- Better Revive's final-damage defaults include `minecraft:lava`, so lava death bypassed grave creation. Effective damage classification now treats lava as a grave death, while `minecraft:out_of_world` and the below-world rule remain final. The safe-placement search reaches farther to find a shore and can place a grave in air immediately above lava when no nearby land exists.
- A missing grave could leave a registered location and an active Grave Compass. Online compasses are checked each second against registration and the stored deadline; expired or unregistered compasses are removed. Old graves retain their stored deadlines.

Compile `PatchDeathFix.java` for Java 25 against ASM and ASM Tree, run it on the three original classes, compile `DeathFix.java` for Java 25 against the patched classes plus the Better Revive, PlayerRevive, Minecraft 26.2, and NeoForge libraries, then run `repack.py` against the previous grave-guard JAR. The separate desktop server was not changed. Archive and class-version checks passed; multiplayer death and lava gameplay have not yet been exercised.

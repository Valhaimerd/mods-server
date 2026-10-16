# Gateway of Doom downed-player target fix

`GatewayForceTargetGoal` and `GatewayAggressionGoal` each chose only the nearest player, then rejected creative and spectator players. PlayerRevive keeps a downed player alive, so both goals kept targeting that player and never tried another. `GatewayPortalEntity.findNearestPlayer` also accepted a downed player when first assigning targets.

`GatewayTargeting.java` iterates players and chooses the nearest active one. It excludes PlayerRevive's bleeding state and Better Revive's downed state when those mods are loaded. `PatchGatewayTargeting.java` routes both goal searches and the portal search through this helper, and replaces each goal's active-target check. The current goal stops on a downed player; its next acquisition chooses an active teammate. `repack.py` inserts the four Java 25 class files into the original JAR.

The original goals kept a valid target until death or range loss, even when another active player moved closer. The 2026-10-16 revision refreshes each mob goal's target every 60 ticks (about three seconds) from that mob's position. Force targeting uses its original range; aggression uses its follow range and preserves the daytime/land restriction. Both searches still skip creative, spectator, and downed players. This changes the two goal classes and `GatewayTargeting.class`; the portal's initial target search is unchanged.

Compile the helper with the Gateway of Doom, PlayerRevive, Better Revive, Minecraft 26.2, and NeoForge libraries. Compile the patcher with ASM and ASM Tree. The JAR builds for Java 25 and passes archive inspection; no isolated multiplayer runtime check has been run for this revision.

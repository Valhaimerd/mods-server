# Retained gear after disconnecting while downed

1 November 2026. Same-filename replacement: `mods/Better Revive - 2.2.0 - NeoForge - 26.2-MultiGraveFix-Java25.jar`.

## Cause and fix

PlayerRevive's installed `ReviveEventServer.playerLeave` calls `PlayerReviveServer.kill` when a bleeding player disconnects. That produces a real death. Better Revive keeps ordinary hotbar, armor and offhand items in the player's `NeoForgeData.better_revive_keep_gear` record, separately from the grave. Backpacks and Curios use the grave path.

The existing `KeepGear.onLoggedIn` hook could restore that record into a player who reconnects still dead at the death screen. Restoration then deleted the record. The subsequent death clone/respawn discarded the restored inventory with no retained-gear record left to copy. This explains why the grave could contain backpacks/Curios while the retained equipment was absent.

`KeepGear.restore` now returns immediately while the player is not alive, before removing either the pending snapshot or persistent record. The existing death-clone hook carries the record forward and the existing respawn/deferred restoration returns it to the living player. Living-player login recovery remains enabled. No acquisition, expiry, grave-order, backpack or Curios policy changes were made.

## Verification

An isolated Minecraft 26.2 / NeoForge 26.2.0.90 dedicated server loaded Better Revive, both integration bridges, PlayerRevive 2.1.2, CreativeCore, Curios, Sophisticated Core and Sophisticated Backpacks, plus the test-only probe. Java 26 ran Java-25-targeted production classes.

The original build failed: **dead login preserves retained-gear record for respawn**. The patched build passed nine checks covering the retained/grave split, dead login, record preservation through a death clone, restoration of sword/chestplate/offhand shield, record consumption, no duplicate restoration and living-player recovery. The probe clears the in-memory snapshot before login to exercise persistence-based recovery, as after a server restart.

Scope: these are engine-loaded event/state tests using unlisted fake players, not an actual network disconnect/reconnect. The harness posts login, clone and respawn events and calls the same restoration operation used by the next-tick handler because fake players are not in the online-player list. It does not exercise a physical grave interaction or equipped Curios retrieval. Earlier grave-order tests remain separate evidence; this change does not modify their classes.

`before-results.txt`, `after-results.txt`, `ReconnectProbe.java` and `archive-verification.json` retain the evidence. Only the three recompiled `KeepGear` helper classes differ; all other archive entries are byte-identical and the changed classes target Java 25 (69). The test mod is not in the production pack.

Source: ignored `mod fixing/source/better-revive-keep-gear/KeepGear.java`. Isolated lab: ignored `tmp/better-revive-reconnect/`. Original JAR: ignored `mod backup/better-revive-reconnect-2026-11-01/`. Final fixed-pack copy: ignored `mod fixing/1 fixed packs/`.

## Deployment and already-missing items

The replacement is installed in this repository only. Update the multiplayer server and clients to this build, then restart. The active filename is unchanged; the installer detects changed content hashes after distribution. No personal profile, desktop server or world was edited.

Reconnect to the death screen, then press Respawn: retained gear must wait for the living player. A fix cannot reconstruct a snapshot already consumed and discarded by the old build. Existing player-data/world backups may contain it; preserve those before attempting recovery. A grave's emptiness alone does not prove deletion, since retained gear is intentionally stored separately.

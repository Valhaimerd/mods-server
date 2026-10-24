# Spread Spawnpoints — Spring default and isolated test

24 October 2026. The requested “Random Unique Spawn” is installed as **Spread Spawnpoints 1.2.0-26.2**, mod ID `spreadspawnpoints`. The owner clarified **Spring** rather than pool mode. This JAR has four registered generators: vanilla, random, grid and spring; there is no pool generator.

## Installed setting

The same-filename repack changes the default generator from **Vanilla** to **Spring** for worlds without existing Spread Spawnpoints SavedData. Only the `SpawnPointManager` static default reference changes; original metadata, commands, bundled Common Permissions API, other classes and stored player assignments are preserved.

Spring's original settings remain: 128-block minimum player-coordinate separation, a 256-block overlap neighborhood that keeps the distribution connected, a 256-block world-spawn reserve and a 384-block world-spawn neighborhood. These are generator-coordinate rules; actual safe arrivals can move nearby through native spawn adjustment. Ground safety uses Minecraft's spawn finder. Invalid terrain may require retries.

**Existing worlds keep their saved generator.** An operator can switch the active world without erasing assignments:

```text
/spawnpoints generator query
/spawnpoints generator set spreadspawnpoints:spring false
```

Restart after changing an existing world's generator so its existing assignments are fully registered with Spring's reservation grid. Do not use `true` unless deliberately resetting saved player assignments. This task does not edit the separate desktop server/world or personal Minecraft. The installed pack default will apply when a fresh world is created; the saved setting in an existing world is authoritative.

## What was actually tested

The original JAR passed 29 checks for Random generation and another 29 after an actual isolated restart, confirming its native command, per-player stable assignment, saved generator/assignment, spawn helper and default-respawn Mixin work.

The Spring candidate passed **65 isolated server runtime checks**, including default selection, command availability, distinct/stable assignments, 128-block separation, world-spawn exclusion, native default respawn, and persisted mode/assignments after restart. The test server runs **only the mod, its bundled Common Permissions API and the disposable probe**, on NeoForge 26.2.0.90 / Java 26 with Java-25 bytecode.

A real isolated client then joined a clean server-created flat world with only the same mod/bundled dependency and a disposable probe. The initial-spawn preparation selected Spring coordinates **(-462, 65)** and the connected player arrived at **(-471.5, -60, 68.5)**, within native adjustment distance. This exercises the actual login path, not only a generator method call. No full-pack respawn conflict test is claimed; Better Respawn, beds, anchors and revive mods can govern other respawn paths.

The first client fixture copied a world with embedded single-player data and loaded that saved position. That was a fixture mistake, not evidence of a mod failure. The final first-join fixture uses a server-created world without an embedded local player.

## Rebuild and verification

```powershell
python docs/patches/spawn-audit/build.py
python docs/patches/spawn-audit/run_server.py --spring
python docs/patches/spawn-audit/run_server.py --spring
python docs/patches/spawn-audit/run_client.py --spring
# Wait for tmp/spawn-spring/client-first-join/spawn-client-result.txt.
python docs/patches/spawn-audit/verify.py tmp/spawn-spring/built
```

`run_server.py` without `--spring` retains the original Random audit under `tmp/spawn-pool/`; that directory name records the original request and is not an installed mode. `artifacts.json` pins original/candidate hashes and the single changed class. Runtime reports are copied here. Previous active/fixed copies are retained under ignored `mod fixing/retired packs/spawn-spring-2026-10-24/`. No commit or push requested.

Handbook review: all 77 pages rendered; changed pages 16, 28, 29 and 30 were visually reviewed against the prior edition. All 51 contents destinations remain correct and both PDF copies match. Concurrent unrelated handbook edits were preserved.

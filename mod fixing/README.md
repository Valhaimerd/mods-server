# Mod Fixing Workspace

Read this file first, then read the note in any task-specific folder. For optional Farmer's Delight add-ons, read [`delight extras (optional)/README.md`](delight%20extras%20(optional)/README.md) before working there. The user's latest specific request defines which mod to fix and the expected behavior; use these notes as workflow context.

## Folders

| Folder | Purpose |
| --- | --- |
| `1 pending packs` | Input JARs awaiting work. Work only on the mod or issue named by the user. |
| `1 fixed packs` | Final production JARs for completed fixes. Keep test builds and intermediate compilations out. |
| `retired packs` | Superseded or removed JARs kept locally for recovery; not part of the active pack. |
| `delight extras (optional)` | Non-urgent add-ons for Farmer's Delight that may need a port to Minecraft 26.2. See its README. |
| `source`, `patches`, and mod-specific port folders | Local source and working files used to make fixes; these are not final mod-pack outputs. |

Treat JARs already in `1 fixed packs` as completed. Reopen one only when the user asks for another change or reports a follow-up issue.

## Death and grave system

`Better Revive - 2.2.0 - NeoForge - 26.2-KeepGearFix-Java25.jar`, Better Revive X PlayerRevive 1.0.1, and `Better Revive X Curios - 1.0.0 - NeoForge - 26.2-KeepGearFix.jar` are installed in the active Minecraft 26.2 pack and server. PlayerRevive 2.1.2 and Curios 16.0.0 remain installed. The PlayerRevive integration gives the first rescue attempt to PlayerRevive, then hands a bleed-out or surrender to Better Revive for its grave and longer revival window. The repacked Curios add-on sends Sophisticated Backpacks to the grave and leaves other equipped Curios for Better Revive to retain.

The Better Revive repack saves XP from the player's current level and progress in both normal and explosion deaths. The original saved `totalExperience`, which includes XP earned before spending levels and could inflate a level-20 recovery to roughly level 90. Built-in defaults are now `graveDurationSeconds = 1200` and `collectionDurationSeconds = 0`: the grave stays owner-protected for 20 minutes, then expires without a public collection phase. The admin menu accepts zero for that setting. The server's `config/better_revive.json` and existing `world_config.dat` were updated; their previous copies and the original JAR are in `modpack-backups/better-revive-xp-timers-2026-10-15/`. The production repack is in `1 fixed packs`; local patch source is in `source/better-revive-xp-timers/`. Existing graves keep the XP amount and deadline already stored in them. The current repack keeps the XP and 20-minute timer changes.

The death rule retains hotbar slots 0–8, armor slots 36–39, offhand slot 40, and equipped Curios through revival or normal respawn. It always sends Sophisticated Backpacks to the grave, including backpacks in those retained positions. The rule applies to immediate and delayed explosion graves. Retained gear is saved to player data so a restart while downed does not discard it. The 2026-10-15 repack fixes normal respawn, which bypasses Minecraft's respawn event, and restores previously stranded retained gear when a living player next logs in. Existing players who already respawned with missing retained gear should reconnect after installing the new JAR; items can be recovered only if their `NeoForgeData.better_revive_keep_gear` record still exists. The prior server JAR is backed up in `modpack-backups/better-revive-normal-respawn-2026-10-15/`; implementation source is in `source/better-revive-keep-gear/`.

The fix was exercised in an isolated NeoForge 26.2.0.90 client and dedicated flat-world server. The five core mods loaded; a scripted death returned the sword and chestplate on normal respawn while placing Sophisticated Backpacks and ordinary inventory in the grave. A second test used Artifacts only as a Curios accessory fixture and confirmed that an equipped Flame Pendant was retained. A saved retained-gear record was also recovered on login. A separate test invoked the grave's `restoreInventory` and `restoreXp` methods and returned `complete=true`. The server-side tests did not drive the grave block's mouse interaction or the full 300-mod pack.

The Java 25 repack recompiles only the three `KeepGear` helper classes with `javac --release 25`. The previous repack accidentally used Java 26 class version 70, while Minecraft 26.2 and the rest of Better Revive use Java 25 class version 69. The updated archive contains 187 version-69 classes, and only those three helper entries differ from the previous repack. The previous server copy is backed up in `modpack-backups/better-revive-java25-2026-10-15/`.

Tool Belt's `Shift + Q` belt slot is a separate NeoForge attachment, outside both vanilla inventory and Curios. Its original death handler dropped the equipped belt and cleared that attachment. `ToolBelt-26.2-2.10.0-CuriosBetterReviveFix.jar` retains the dedicated slot on player deaths when Better Revive is loaded, allowing Tool Belt's clone handler to copy it on respawn. Its ordinary death drop behavior remains for packs without Better Revive, and Curse of Vanishing still removes a cursed belt. The existing Curios compatibility fix is retained. In the isolated six-mod flat-world client/server, a scripted final death emptied the slot with the old JAR; the repack kept `toolbelt:belt` in the attachment after final death and immediate respawn. The test-only belt equip hook was removed from the production JAR. The prior server JAR is backed up in `modpack-backups/toolbelt-better-revive-slot-2026-10-15/`, and the local source is in `source/toolbelt/`.

Graveless 1.3.2 was removed from the pack and server after the client crashed while rendering its 3D grave preview. Its JAR is kept in `retired packs`. Existing Graveless death records may not be accessible through Better Revive; recover any items from an old save before relying on this replacement.

## End and Nether world generation

Better End 26.2.6 replaces Stellarity 5.5.5 in the pack and server. `lootintegrations_stellarity-1.2.jar` was removed with Stellarity; no other active JAR referenced it when inspected. Better Nether 26.2.3 replaces 26.2.2, leaving one active version. The removed pack JARs are in `retired packs`; the server copies are in `modpack-backups/better-end-switch-2026-10-15/`. Existing End chunks retain their generated terrain, so explore new chunks for Better End's generation.

## Penchant progression

`penchant-neoforge-26.2.0-1.6-2.5x-progress.jar` replaces the previous 26.2 fixed repack in the pack and server. It changes only `EnchantmentProgress.getMaxProgress`: the target for each eligible enchantment is `ceil(original target × 0.4)`. This keeps the normal use triggers, level caps, and existing saved progress while making leveling approximately 2.5 times faster. The prior JAR is in `retired packs` and the server backup is in `modpack-backups/penchant-2.5x-2026-10-15/`. Repack source is in `source/penchant-fast-progression/`.

## Fix workflow

1. Read the current user request and identify its exact target and requested behavior. Inspect the pending JAR's embedded mod metadata, Minecraft/NeoForge compatibility, and relevant source before changing it. Do not update unrelated pending mods just because they are present.
2. Make and package the requested fix. A test compilation, development build, or intermediate JAR is not a finished fix.
3. Once the final production JAR is built and its archive and metadata have been checked, put that JAR in `1 fixed packs`.
4. For every completed fix, remove the superseded JAR for that mod from `mods/` and replace it with the final compiled JAR. Do this only with the final production artifact, never with a test or intermediate compilation. Check that `mods/` contains the intended replacement and not a duplicate old entry.
5. Remove the completed input from `1 pending packs` so that folder continues to represent unresolved work.

Keep a fix in the existing mod entry when that is what the user requested. Do not create an extra compatibility mod or pack unless the user asks for one. If the final build or compatibility check cannot be completed, leave the mod pending and report what remains unresolved.

## Skill tree and Orb of Oblivion compatibility

Adventurer Skills is the pack's only general stat tree. RPG Series class and weapon trees remain available. Default Skill Trees and the older Stronger Skill Tree are retired; the removed Default JAR is in `retired packs` for recovery. The server's previous copies are in `modpack-backups/adventurer-skills-only-2026-10-15/`. `orb_of_oblivion_puffish_compat-1.0.2.jar` replaces 1.0.1 in `1 fixed packs`, `mods/`, and the server's `mods/` folder.

The add-on registers `adventurer_skills:adventurer_skills` with the RPG Series Orb's reset categories. The Orb now refunds spent Adventurer points even when no RPG class or weapon points were spent, and still resets the RPG categories under its original use and durability rules. Earned category experience is preserved. The local source and build notes are in `source/orb-oblivion-puffish-compat/`.

## Withered Lands Slime Rain removal

`Withered-Lands-26.2-1.6.9-no-slime-rain.jar` replaces the original in the pack, client, and server `mods/` folders. Its only changed archive entry is `SlimeRainEvent.class`: natural and command starts are blocked, and a previously saved active event ends on its next tick. Other Withered Lands content remains in the JAR. The original is in `retired packs`; the finished repack is in `1 fixed packs`. The installed client and server `withered_lands.toml` files also set `"Slime Rain event enable" = false`. Rebuild instructions are in `source/withered-lands-no-slime-rain/README.md`.

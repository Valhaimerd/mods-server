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

## Fix workflow

1. Read the current user request and identify its exact target and requested behavior. Inspect the pending JAR's embedded mod metadata, Minecraft/NeoForge compatibility, and relevant source before changing it. Do not update unrelated pending mods just because they are present.
2. Make and package the requested fix. A test compilation, development build, or intermediate JAR is not a finished fix.
3. Once the final production JAR is built and its archive and metadata have been checked, put that JAR in `1 fixed packs`.
4. For every completed fix, remove the superseded JAR for that mod from `mods/` and replace it with the final compiled JAR. Do this only with the final production artifact, never with a test or intermediate compilation. Check that `mods/` contains the intended replacement and not a duplicate old entry.
5. Remove the completed input from `1 pending packs` so that folder continues to represent unresolved work.

Keep a fix in the existing mod entry when that is what the user requested. Do not create an extra compatibility mod or pack unless the user asks for one. If the final build or compatibility check cannot be completed, leave the mod pending and report what remains unresolved.

## Skill tree and Orb of Oblivion compatibility

Default Skill Trees replaces Stronger Skill Tree in the active client and server packs. The retired Stronger JAR is kept in `retired packs` for recovery. `orb_of_oblivion_puffish_compat-1.0.1.jar` replaces version 1.0.0 in `1 fixed packs`, `mods/`, and the server's `mods/` folder.

The add-on now refunds spent points in `adventurer_skills:adventurer_skills`, `puffish_skills:combat`, and `puffish_skills:mining` when the RPG Series Orb of Oblivion successfully resets spent RPG skill points. Earned category experience is preserved. The original orb requires spent RPG skill points to trigger, so points spent only in Adventurer or Default Skill Trees cannot activate this reset. The local source and build notes are in `source/orb-oblivion-puffish-compat/`.

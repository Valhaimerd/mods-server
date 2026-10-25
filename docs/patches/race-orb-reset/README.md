# Orb of Oblivion race reset and join synchronization

25 October 2026. Same-filename overlay for `iourusraces-1.1.0.jar`, retaining the prior race balance, slowfall, solid leaves and Elf/Goblin leaf climbing changes. Sync matching client/server JARs and restart.

## Player behavior

Using the **Orb of Oblivion** now resets the user's Iourus race as well as its existing RPG/Adventurer skill allocations, then opens the race selection panel. It also works when the player has a race but no spent skill points. The native Orb durability/use cost, particles, stat and Creative behavior remain unchanged. If there is neither a race nor any skill allocation to reset, the Orb still fails without spending a use. Other players' races are unchanged.

Existing-race players no longer receive a selection prompt while their join packet is pending. The client waits for server confirmation, with no arbitrary loading delay. A late packet confirming a selected race also closes a stale race-selection panel. Only confirmed `NONE` can open the chooser automatically or with the race-menu key. The panel waits for another open screen to close, and dismissing it does not reopen it every tick. Later Orb/admin resets can prompt again within the same connection.

## Root cause and implementation

`ClientRaceData` initialized to `Race.NONE`, and `ClientInputHandler` treated that default as an authoritative missing race before `RaceSyncPayload` arrived. Heavy loading/network delays could expose this interval. The old JAR reproduced the unwanted panel in the connected delayed-packet fixture; server SavedData itself was not shown to lose the race.

The client now tracks **unknown versus synchronized** separately. The existing payload's `setRace` confirms readiness, including an explicit `NONE`. Existing logout resets clear readiness, so a prior connection's identity cannot authorize another connection's UI. Selection controls wait until the server has replied; known races clear stale selection screens.

An optional `@Pseudo` Mixin redirects only the actual RPG `RespecItem.use` call to `SkillHelper.respec`, combines its result with the user's race reset, and lets the original item success/cost path run. It uses the existing server `PlayerRaceData.resetRace` setter, which already clears racial attributes, updates persisted identity and syncs the client. SkillHelper itself and administrative skill resets are untouched. Without RPG Skill Tree, the optional integration is skipped; the dedicated race-only fixture proves server startup without those classes.

Exactly two existing classes and the mixin manifest change, and one Mixin class is added. Previous race/leaf/slowfall/attribute/server-save/network entries remain byte-identical. No new timers, entity scans, threads, packets or per-player maps are added. No race IDs, saved-data schema or existing choices are migrated/reset by installation.

## Verification

```powershell
python docs/patches/race-orb-reset/build.py
python docs/patches/race-orb-reset/run_client.py --baseline
python docs/patches/race-orb-reset/run_client.py
python docs/patches/race-orb-reset/run_optional_server.py
python docs/patches/race-orb-reset/verify.py --installed
```

- Baseline reproduces the unwanted race panel before delayed confirmation.
- **125 connected client/integrated-server checks** cover delayed packets, late known-race closure, actual native Orb use, normal Survival use cost, zero-spent-point race reset, immediate racial attribute cleanup, unrelated-player preservation, live selection/resync, combined Adventurer skill/race reset, repeated resets, failed no-op preservation and logout readiness.
- **5 pure client-state checks** distinguish pending sync from confirmed NONE and clear state on reconnect.
- A dedicated server containing only Iourus Races plus the diagnostic probe loads without RPG Skill Tree, proving optional integration and client-side isolation.

The first Orb cost fixture inherited Creative mode; setting its test player to Survival exercises the native durability path. No production cost logic was changed. Heavy full-pack loading is represented by a controlled delayed acknowledgment, not a claim that every possible network delay or multiplayer reconnect was reproduced. Test worlds and diagnostic files are confined to ignored repository `tmp/`.

The tested production JAR is installed in `mods` and ignored `mod fixing/1 fixed packs`; predecessors are retained under `mod fixing/retired packs/race-orb-reset-2026-10-25`. Build baselines/hashes and archive deltas are in `artifacts.json`. The handbook source and PDF are synchronized. Personal installations and the separate desktop server are untouched. No commit or push requested.

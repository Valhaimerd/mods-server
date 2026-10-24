# Bifrost creative and JEI discovery repair

25 October 2026. Minecraft 26.2, NeoForge 26.2.0.90, JEI 30.32.0.215.

The four registered Bifrost items had no creative-tab entries. This also excluded them from JEI's item list. Their crafting recipes and recipe-book unlocks already worked.

`ModItems.creativeContents` now adds Bifrost Marker to Functional Blocks and Hofund, Mjolnir and Stormbreaker to Combat, including creative search. The replacement changes only `com/emandev/bifrost/ModItems.class`. Recipes, ownership, combat, skill integration, models and textures are preserved.

## Verification

The disposable connected client/integrated server reproduced the missing entries with the previous archive. The replacement passes 26 checks: all four parent-tab and creative-search entries, actual marker text search, all four recipe-book unlocks, native crafting recipes, JEI crafting lookup and JEI item listing. Creative search and JEI recipe screenshots were visually checked. The test opens the native creative screen to populate both contents and its search indexes; directly rebuilding contents in a fixture bypasses that screen's index refresh.

```powershell
python docs/patches/bifrost-creative-discovery/build.py
python docs/patches/bifrost-creative-discovery/run_client.py
python docs/patches/bifrost-creative-discovery/verify.py --candidate --runtime
# After installing the exact tested candidate:
python docs/patches/bifrost-creative-discovery/verify.py --runtime
```

The same filename, `bifrost-teleport-0.4.3-alpha-neoforge-mc26.2.jar`, is installed in `mods/` and mirrored in `mod fixing/1 fixed packs/`. Predecessors are archived under ignored `mod fixing/retired packs/bifrost-creative-discovery-2026-10-25/`. Hashes are recorded in `artifacts.json`; test results are alongside this file. Temporary clients and screenshots stay under ignored `tmp/`. Personal installations and worlds are untouched. Nothing was pushed.

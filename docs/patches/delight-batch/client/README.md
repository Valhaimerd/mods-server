# Isolated client integration verification

Minecraft26.2 / NeoForge26.2.0.90 / Eclipse Adoptium Java25.0.4.1, with a3GiB maximum heap. Client game directories are `tmp/delight-batch/client-base` and `tmp/delight-batch/client-all`. The installed launcher libraries/assets were read-only. A copy of the existing isolated `tmp/archer-tier-one/client/saves/Punchy Flat` template supplies the world; no personal installation/world was changed. Old template mod-list warnings are expected because these runs intentionally load only the selected integration core.

Core: installed Farmer's Delight26.2-1.3.3-fix5, Expanded Delight26.2-0.1.4-fix1, installed JEI26.2-30.32.0.215, and Display Delight candidate1.7.0-26.2-fix2. No JEI upgrade or optimizer is involved. All-addons adds the three production candidates. Exact hashes are in `client-runtime-manifest.json`.

`ClientDelightProbe.java` is an isolated client-only mod, compiled against exact runtime libraries using `build-client-probe.py`. It waits for a connected integrated-server player and the actual JEI runtime. It queries native creative tabs/search, JEI visible ingredients/information/recipe categories, all edible addon items, native item-model render states and Display Delight association maps. It also calls the actual client `gameMode.useItemOn` path and verifies server-authoritative returned meals/empty plates, rather than directly invoking the mod event handler. Screenshots come from Minecraft's own `Screenshot.grab` after stable UI frames.

The base completed135 assertions with zero failures. All unavailable displays are absent from the native creative tab/search, JEI ingredient browser and JEI information recipes; missing Brewin' and Chewin' entries are absent. Base foods and empty plates remain visible.106 GUI render models resolve; Display Delight tab has106 entries and bowl/plate/small-plate category maps have56/32/16 entries. Actual empty-hand beef-stew retrieval returns the original FD food; retrieving one plated cookie retains its original empty plate. `base-verification.txt` and six images under `screenshots/base` record the final135-pass run at1280x800 GUI2.

## Discovered regression

Initial DD client tag reload failed before world join because constructing an ItemStack queried components before they were bound. Root replaced that tag check with a direct registry-holder check, then base tests passed. The failure log is `tmp/delight-batch/client-base/launcher-baseline-components-error.log`.

The first all-addons run had zero food render states for End Stove. A corrected fixture used six successful native `placeFood` calls with the stove unlit, proving six server inventory entries but zero client entries and zero extracted renderer items/offsets. This ruled out the original fixture's zero cooking timers and established missing update-tag sync. Baseline files are `tmp/delight-batch/client-all/native-stove-sync-red-*`. End's Delight owns a local override of `getUpdateTag(provider)` returning `saveCustomOnly(provider)`; no shared Farmer's Delight edit was required. Final all-addons:322 assertions passed with zero failures using the final End's Delight SHA256 `800a650a1e203dbaf998d3a867d643dec23e7c13721eedde986e97e148cc56bf`. Both server and client inventory counts are six; the native EndStoveRenderer extracts six nonempty item render states and six original offsets. All three addons' edible foods are visible in JEI with populated recipe categories and resolved display information.260 item models resolve; bowl/plate/small-plate maps have91/42/19 entries. No UI atlas-limit warning occurs at GUI3. `all-verification.txt` records the final run, with eight screenshots under `screenshots/all`. A final all-addons pass repeats the322-check suite only after the three resource repairs, then adds an overhead view of all six stove items, feasts and displays.

## Reproduce

Copy the archived launcher to the isolated target folder, prepare the same core/addon JARs and a private copy of the template world, then from repository root:

```powershell
python docs/patches/delight-batch/client/build-client-probe.py
python tmp/delight-batch/client-base/launch.py --world
python tmp/delight-batch/client-all/launch.py --world
```

Run clients sequentially. They hide their own window, capture screenshots, write `client-results.txt`/`client-result.txt` (the322-pass all-addons result is also retained as `final-green-client-*`), and exit automatically. Launcher uses Java25 and a3GiB heap. The all-addons run uses GUI3 to reduce simultaneously visible icon count; the GUI2 exploratory run logged the vanilla UI atlas limit warning. The copied template also contains an unknown retired Puffish gamerule, logged once during world-data load; it does not prevent either integration run or any assertion. Offline user-properties/Realms authentication401 errors are expected from the deliberate offline identity; they are unrelated to mod/resource loading. This is an automated native client integration test, not manual multiplayer or a full-pack performance test.


Final native texture audit: all13 baseline MaterialBaker warnings are resolved. The final all-addons run at20:35 passed322 checks and produced zero missing-texture, unbound model-face or UI atlas-limit warnings. Large Meals uses current FD tray/bone textures and binds its previously missing cut pie face; Ocean's Delight uses current FD leftover tray; End's Delight no longer generates a native item definition for an unregistered upstream model. The overhead image visibly shows all six End Stove food items, food feasts and addon displays without magenta missing materials. See `material-baseline.txt`, `material-final.txt` and the eighth all-addons image. Final JAR hashes are pinned in the manifest.

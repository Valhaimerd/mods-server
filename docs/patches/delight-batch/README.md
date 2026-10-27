# Farmer's Delight additions and Display Delight compatibility

Installed 27 October 2026 for Minecraft26.2, NeoForge26.2.0.90+ and Java25+. Both clients and servers need the matching pack. No personal instance or separate desktop server was changed; no GitHub push was requested.

## Installed changes

| Mod | Installed JAR | Content |
|---|---|---|
| Ocean's Delight | `oceansdelight-26.2-1.0.4-port1.jar` | 25 items,23 edible foods,one shareable soup block,31 recipes |
| Large Meals | `large_meals-26.2-1.0.1-port1.jar` | 18 items,15 edible foods,three four-serving feasts,18 recipes |
| End's Delight | `ends_delight-2.6.1+neoforge.26.2-port1.jar` | 63 items,eight blocks,75 recipes and six-slot End Stove |
| Display Delight | `displaydelight-26.2-1.7.0-fix2.jar` | Same-filename repack with source-food availability filtering |

These are pack-local source ports of the supplied1.21.1 versions, not upstream26.2 releases. The installed Farmer's Delight1.3.3-fix5 and Expanded Delight0.1.4-fix1 remain byte-identical. [installed.json](installed.json) records exact final hashes. Matching artifacts are in the ignored fixed-packs folder; the original DD is backed up under `mod fixing/retired packs/delight-batch-20261027/`.

Upstream repositories, pinned commits, licenses, source patches and reproduction details are in [Ocean's Delight](oceansdelight/README.md), [Large Meals](large_meals/README.md) and [End's Delight](ends_delight/README.md). Each port retains original food/recipe IDs, effects and containers. Modern ingredient/loot/consumable codecs and item definitions are migrated; End Stove additionally synchronizes inventory/timers so all six foods render. Original texture pixels remain; model references/UVs are adapted where the current FD renamed textures.

## Display availability

Only displays whose source mod and exact food are present, with a matching display-category association, appear in JEI ingredients/information, creative/search lists and new wandering-trader offers. Missing add-ons do not generate dummy bowls or unrelated-food fallback trades. Valid vanilla/FD/Expanded meals and empty plates remain. No player recipe-book unlock is required.

Unavailable Display Delight results are removed when saved traders load; ordinary and valid display trades remain. Empty categories omit extra offers. All display registry IDs remain, including hidden items and old placed decorations. Existing food retrieval and empty-plate behavior is preserved. The original Festive Delight spelling alias remains. Map initialization is repeatable and refreshes on tag updates, without relying on server-only caches for client visibility. See the [implementation patch and regression records](displaydelight/README.md).

## Verification

| Check | Result |
|---|---|
| Production builds | All four compile/build on Java25/NeoForge26.2.0.90; no probes or FD classes embedded |
| Original content preservation | Ocean31,Large18,End75 recipe semantics verified; original textures/effects retained |
| Isolated Ocean gameplay | 455 assertions, including recipes/cutting/pot matching,23 foods,effects,bowls,soup servings and80 loot outcomes |
| Isolated Large Meals gameplay | 124 assertions, including all recipes/foods/effects,three feasts and knife/fire sheep drops |
| Isolated End gameplay | All75 recipes,six-slot stove cooking/outputs,packet inventory+timers and soup bowl/effect pass |
| Display baseline | Nine failures demonstrated before the repair |
| Base/individual/all-three server matrix | All cases pass; individual/all-three394 checks each |
| Missing-food fixture | Base/all-three397 checks each; installed-mod/nonexistent-food case is hidden |
| Exact installed final artifacts | Combined server397/397 pass, including saved trades and repeated initialization |
| Actual client with installed JEI30.32.0.215 | Base135/135 and final all-three322/322 pass |
| Client visuals | Six stove inventory/render entries,260 item models,zero missing-texture or UI atlas-limit warnings;14 screenshots |
| Handbook |307 main JARs,23 client extras,184 gameplay entries;78-page PDF rebuilt,all pages reviewed,TOC links and output hashes checked |

The [client record](client/README.md) includes real network bowl retrieval and last-cookie retrieval leaving an empty plate. Its final overhead screenshot shows feasts, six stove foods and supported displays. Client runtime manifests pin exact final JARs. These are isolated integration/gameplay checks, not a full-pack performance or live multiplayer test.

The handbook builder now waits for the hidden headless browser process to finish and checks its exit code. Previously PowerShell could report failure before the browser wrote its successful PDF. Root/output PDFs match; the brief new mod entries, icons, indexes and counts were synchronized.

## Reproduction and rollback

Use each port's patch against its pinned source commit and the existing FDfix5 dependency. `VisibilityProbe.java`/`run_probe.py` provide the pack-local isolated server regression; client fixtures and launcher are under `client/`. Temporary worlds, caches, baseline JARs and raw runtime logs stay under ignored `tmp/delight-batch/`. No tests belong in production JARs.

To undo this batch, restore only the original Display Delight JAR from the recorded backup, remove the three added active/fixed JARs, and restore the corresponding handbook edits. Existing worlds can retain registered add-on blocks/items, so back up a world before removing its content mods. Do not use a broad repository reset or change personal installations.

# Three Delight ports and Display Delight availability

Date: 27 October 2026. Status: approved by the user on 27 October 2026; implementation authorized.

## Intent and agreed scope

Port and install three add-ons selected from `mod fixing/delight extras (optional)/`, compatible with the installed Minecraft 26.2 / NeoForge 26.2.0.90+ / Java 25 pack, Farmer's Delight and Display Delight. The user authorized picking three and confirmed unavailable Display Delight foods must be hidden from **JEI, creative lists and wandering-trader offers**. Existing display registry IDs remain for world compatibility. No GitHub push requested.

Selected inputs:

| Add-on | Local supplied input | Reason for this batch |
|---|---|---|
| Ocean's Delight | oceansdelight-neoforge-1.0.4-1.21.1.jar | Seafood content; existing Display Delight `od_` integration; no additional required library identified beyond the established loader/FD stack |
| Large Meals | large_meals-1.21.1-1.0.1.jar | More meal recipes; existing `lm_` integration; small independent food expansion |
| End's Delight | ends_delight-2.6.1+neoforge.1.21.1.jar | End-themed food and stove; existing `edd_` integration; complements ordinary and ocean cooking |

End's Delight is `ends_delight`, not the separate `endersdelight` input. Expanded Delight is already installed and does not count as one of the three additions. All three selected inputs target 1.21.1 and need real 26.2 compatibility work, not metadata-only relabeling. Exact source commits/input hashes must be pinned during implementation; retain their MIT notices and attribution.

## Current installed baseline

- `mods/FarmersDelight-26.2-1.3.3-fix5.jar`
- `mods/displaydelight-26.2-1.7.0-fix2.jar`
- `mods/expandeddelight-26.2-0.1.4-fix1.jar`
- No selected add-on is currently active. The optional folder holds inputs, not installable 26.2 releases.
- Display Delight has only two serialized crafting recipes: empty normal/small plates. The unwanted food listings largely come from registered food-display items and their JEI ingredient information.
- The existing creative tab accepts the whole display-item list. JEI registers information for every FoodBlockItem. Missing-mod display items are deliberately placed in wandering-trader pools; trader joining currently skips offers loaded from disk. Those are discovery/acquisition paths to correct together.
- BlockAssociations already contains prefix-to-mod mappings and actual food/block associations. Preserve existing ID aliases, display categories, plate tags and the current food-return/empty-plate repair.

## Approaches

1. **Shared dynamic availability (recommended).** Resolve each display's actual source food and apply one policy across JEI, creative/search lists and trader pools. It automatically follows installed add-ons and handles renamed/missing items even when a mod is present. It requires auditing initialization/reload timing and three integration points.
2. **Fixed blacklist of absent add-ons.** Smaller immediate filtering, but becomes stale when this or later batches add mods, and cannot reliably handle missing individual foods. It works against the user's request that unavailable future additions stay hidden until installed.

The proposed design uses approach 1. The builder can choose a small helper or extend BlockAssociations after checking actual call order; do not introduce a new generic compatibility framework.

## Availability rule

- Empty plates and valid vanilla/Farmer's Delight/Expanded Delight displays remain available.
- An add-on display is available when its source mod is loaded and its exact real source item resolves to a non-AIR registry entry with a supported display association/category. Use the established alias table where an official food has a different ID; add aliases only for the same actual dish.
- Do not create substitute food, bowl recipes or generic bowl fallback inputs to make an absent dish appear functional. A loaded mod alone is insufficient when that specific food is missing.
- This is source-content availability, **not player recipe-book unlock progression**. Legitimate source foods obtained by harvesting, loot or cutting can remain visible without a direct crafting-table recipe. The ports must preserve their real acquisition recipes/loot; no speculative universal recipe-reachability system is requested.
- Visibility must not depend on server-only maps that have not initialized on the client. Resolve stable registry facts safely and rebuild/invalidate any derived association state on the relevant world/tag/recipe reload paths. Reinitialization must not duplicate maps or trader pools.
- Keep hidden blocks/items registered and leave existing inventory stacks/placed blocks intact. Admin `/give` remains possible. Do not convert, delete or substitute saved objects.

## Player-visible outcomes

### Newly ported add-ons

Preserve each selected mod's intended recipes, ingredients, food properties/effects, containers, blocks, models, drops and normal acquisition. Port the native APIs and resource formats needed for those systems. End's Delight's stove, block entity and renderer must work. Avoid new progression/balance reworks or unrelated dependency updates.

A food with a valid recipe can be prepared using its actual FD crafting/cooking/cutting or add-on workstation, placed as its matching Display Delight display, and retrieved/served as the correct edible item. Normal, small and large plates, drink/container behavior, stack counts and the last-serving empty plate must remain correct where applicable. Missing mappings are fixed or hidden, never presented as bowl-only dummy recipes.

### Discovery and trades

- JEI: hide unavailable display ingredients as well as their ingredient-info pages; preserve valid foods and both empty-plate recipes.
- Creative: hide unavailable display items in the mod tab and searchable creative contents; honor current creative/config options for valid items.
- Wandering traders: do not generate unavailable-display offers. Filter existing saved unavailable-display offers when a trader loads, without removing unrelated vanilla/mod trades. Build/fallback pools contain only eligible display items. If an eligible category is empty, omit that extra offer rather than inserting AIR, an arbitrary bowl or an unrelated edible item.
- Adding a supported mod on restart reveals its valid displays without editing a blacklist. Removing it hides those displays again without unregistering their IDs.

## Delivery constraints

Use official matching 26.2 builds if an actual supported release is verified; otherwise produce properly tested local source ports from the selected licensed inputs. Preserve item/mod IDs. Choose descriptive new filenames/version metadata for genuinely new 26.2 ports; keep the existing Display Delight filename for a same-version repack. Farmer's Delight and Expanded Delight stay on their current tested builds unless a demonstrated dependency defect requires a separately explained fix.

Only final tested outputs go to `mods/` and matching `mod fixing/1 fixed packs/`. Preserve original inputs and ignored backups. Do not install original 1.21.1 JARs as 26.2 builds. Personal .minecraft and the separate desktop server/worlds stay untouched.

## Acceptance and tests

1. Inspect/pin upstream metadata, source, dependencies and exact active baseline. Verify port licenses/source versions and classify all affected displays from actual registries rather than names alone.
2. Compile against the pack's target API/Java level. Start an isolated dedicated server with core FD + Display Delight + Expanded Delight and required libraries. Test each selected port individually, then all three together. Require successful recipe/tag/registry load, with no broken optional integration references.
3. Exercise actual preparation paths for each new food family, End stove/container behavior, consumption/container returns and native display placement/serving/removal. Audit every selected-addon display-to-food mapping; report actual test counts and any unsupported upstream dishes.
4. In an isolated client with JEI, compare base-only and all-three configurations. Assert unavailable ingredients/info and creative/search entries are absent, valid entries and plate recipes remain, models render, and actual display interactions work. Take focused screenshots for inspection; do not call server compilation a rendering test.
5. Exercise new and saved trader offers, empty-category fallback, repeated world/tag/recipe initialization, installed-mod/missing-food cases, and stable hidden registry IDs. Preserve existing invalid decorations as saved objects while preventing renewed discovery/trade offers.
6. Inspect final JAR contents/metadata and dependency sides, install final artifacts only, confirm no duplicate mod IDs, then update pack notes and the player handbook: three new canonical entries/index links/sources/icons as appropriate, concise Display Delight visibility guidance, counts and PDF. Build and visually review the PDF; verify its contents links.

## Boundaries and unresolved implementation details

The builder chooses the exact source acquisition, port layout, native visibility hooks and smallest isolated fixtures after API inspection. Reuse existing FD/Display Delight port infrastructure when compatible. Do not silently replace one of the three selected mods if a blocking port issue appears: explain the demonstrated blocker and propose a substitute. Do not promise every food has a display if upstream Display Delight never supplied one; preserve legitimate foods and state that coverage accurately.

The user approved this written design, the selected three and dynamic availability policy on 27 October 2026.

## Builder review

A fresh read-only mod-port reviewer checked this design on 27 October 2026 and reported: "nothing important left". User approval was received.

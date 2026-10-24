# Hammer claim and recipe discovery repair

24 October 2026. Includes the earlier beam-visibility and marker-occlusion fixes. The active Bifrost filename and registry IDs stay unchanged.

## Ground and command-created weapons

`/give @s bifrost:thors_hammer` creates an unbound hammer. An eligible Asgardian can hold it and run `/asgard claim`. After dropping it, the same command can claim the nearby ground item. Crafting still binds Mjolnir and Stormbreaker automatically.

The old ground selector compared the player's look vector to the dropped entity's tiny floor-level origin. Close to the item, looking at its visible model instead of its origin failed the narrow aiming cone. The native regression reproduces this with a valid stored Asgardian identity and an unclaimed ground hammer.

The explicit claim command now accepts the closest unobstructed unclaimed ground weapon within 1.5 blocks without exact floor aiming. From farther away, its existing four-block query and aiming rule remain. Solid terrain, another owner's data, waiting/in-flight authority and the Asgardian eligibility gates are preserved. A main-inventory slot is required; a full inventory leaves the ground item intact. Full stack components survive the move into the owner's inventory.

Failure messages now distinguish the actual race/downed gate, existing ownership data, no reachable unclaimed item and a full inventory. Item tooltips no longer label a weapon unclaimed merely because its stored owner name is missing. Incomplete owner/weapon identity is reported for operator investigation rather than being rebound. Displayed items must be taken down into an item/stack before claiming.

## Recipes and unlocks

All four shaped crafting recipes already existed. This repair preserves their ingredients and outputs and adds the missing native inventory-based recipe-book advancement entries. JEI uses the ordinary recipes; no custom JEI plugin or dependency is added.

| Item | Grid rows, top to bottom | Ingredients | Recipe-book discovery |
|---|---|---|---|
| Bifrost Marker | ` E ` / `GCG` / `OOO` | E: end rod; G: gold ingot; C: crying obsidian; O: obsidian | Obtain crying obsidian |
| Hofund | ` O ` / `GOG` / ` H ` | O: obsidian; G: gold ingot; H: the existing Bifrost handle tag | Obtain obsidian |
| Mjolnir | `NNN` / `ISI` / ` R ` | N: netherite ingot; I: iron block; S: Nether star; R: lightning rod | Obtain netherite ingot |
| Stormbreaker | `OC ` / `OB ` / ` B ` | O: obsidian; C: crying obsidian; B: blaze rod | Obtain crying obsidian |

Spaces are empty grid cells. Craft the weapons on a 3×3 crafting table. Their existing Asgardian crafting/use policy remains. Obtaining the relevant ingredient unlocks the recipe guide; JEI can show the recipe independently of that discovery.

## Verification

The dedicated suite checks native loading of all four recipes and unlock advancements, close ground selection, original entity consumption, component preservation, authoritative usability, terrain rejection, Human rejection, full-inventory recovery and aimed four-block selection. Existing ownership, death, party, combat and transit checks remain in the same run. The failed prior ground-selection and missing-unlock checks were observed before the fixes.

Connected client modes exercise the actual command and JEI runtime:

```powershell
python docs/patches/asgardian-expansion/run_client.py --claim-only --renderers
python docs/patches/asgardian-expansion/run_client.py --recipes-only
```

Wait for each asynchronous result before starting the next client. The claim mode verifies the command packet, original ground entity removal, authoritative owner and synchronized inventory. The recipe mode triggers the native inventory advancement path, verifies all four entries in the server recipe book, queries all four visible recipes in JEI and opens JEI's crafting page for a screenshot. No personal profile or world is changed.

The same-filename tested build is installed in repository `mods/` and mirrored in `mod fixing/1 fixed packs/`. Its predecessor is retained under ignored `mod fixing/retired packs/bifrost-claim-recipes-2026-10-24/`. Final hashes and results: [runtime-results.txt](runtime-results.txt). No commit or push was requested.

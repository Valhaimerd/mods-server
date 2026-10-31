# RPG netherite repairs and Unbreaking VIII audit

31 October 2026. Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25+.

## Repair policy

All **53 explicitly netherite-tier RPG Series items** now repair with **Netherite Scrap**, replacing Netherite Ingot: 18 Wizard, 14 Paladin/Priest, 12 Rogue/Warrior and 9 Archer items. This includes 32 armor pieces and 21 weapons/shields. See `target-items.json` for the exact IDs. Netherite can occur at the start or middle of an ID, covering Wizard staves and wands.

One native `ModifyDefaultComponentsEvent` subscriber in the existing Spell Engine JAR changes only `minecraft:repairable` defaults on damageable netherite items in the four class namespaces. Armory has no explicitly netherite item IDs; its legendary equipment and every other tier retain their materials. Recipes, upgrade costs, durability, attributes and skills are unchanged. Saved ordinary stacks inherit the new default and retain their damage. Explicit administrator-authored component overrides remain native overrides.

The same active filename is retained. All **2,200 pre-existing archive entries remain byte-identical**; only `NetheriteRepairs.class` is added. The hook applies after item/config registration on both sides, using native component synchronization and anvil repair logic.

## Reported Unbreaking VIII stall

The current installed Penchant hash `20a0274f92f20a3777b1a370d3df0943ce0908daea854b423bf0f6c0c3e34015` already contains both level-X definitions and `PendingUnbreakingMixin`. The personal client's copy matches it. No additional cap increase or free enchantment levels are applied in this repair.

The current server test confirms actual worn full VIII bars advance to IX and then X on five RPG armor pieces, including Priest leggings matching the report's equipment type. A connected real-player client test also observes both transitions through ordinary equipment ticks and checks the native Penchant tooltip reports MAX at X. These tests cover complete earned bars; partial progress is not granted levels for free.

The separate desktop server's Penchant archive was inspected read-only and differs: SHA `7b08656bffcc78a93809b24eebe4f014b55d4aa86b266ad1a886a3eadbedb4b7`, a Durability Rework definition capped at V, no base Unbreaking override, and no pending-progress reconciliation. The profile log shows a multiplayer connection, but that does not establish which server files the remote endpoint runs. Client and server must both install the current Penchant build and restart. A client-only replacement cannot repair server-authoritative progression. The reported live VIII stall is not reproduced with matching current builds; its specific live server version remains unconfirmed.

## Verification

```powershell
python docs/patches/rpg-netherite-repairs/build.py
python docs/patches/rpg-netherite-repairs/run_server.py --baseline
python docs/patches/rpg-netherite-repairs/run_server.py
python docs/patches/rpg-netherite-repairs/run_server.py --module-off
python docs/patches/rpg-netherite-repairs/run_client.py
python docs/patches/rpg-netherite-repairs/verify.py
```

`baseline-items.json` records prior repair ingredients for all 240 RPG items; `baseline-stacks.json` contains generated worn-item fixtures, not personal player data. Native checks cover all 53 material changes, actual anvil results with scraps and rejection of ingots, saved damage retention, all 187 non-target repair definitions and five worn-armor VIII/IX transitions. Connected-client checks verify progression synchronization, the level-X tooltip and the new repair component. These are isolated native tests, not a live multiplayer-server repair or a full-pack soak test.

Prior active/fixed Spell Engine bytes are retained in ignored `mod fixing/retired packs/rpg-netherite-repairs-2026-10-31/`. The final replacement is installed in repository `mods/` and `mod fixing/1 fixed packs/`. Personal installations, server directories and worlds are unchanged. Install matching updated client/server pack files and restart. No push was requested.

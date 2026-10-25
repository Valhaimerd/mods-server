# Pet Vault Keeper's Locket in Trinket slots

25 October 2026. Same-filename resource-only overlay for `pet_vault-mc26.2-v1.5.0-neoforge.jar`.

The Keeper's Locket (`pet_vault:pet_necklace`) can be equipped in the **Trinket** slots supplied by Nameless Trinkets, as well as its original **Necklace** slot. Its Pet Vault menu remains on the pack's recommended **R** key. Charm and other accessory slots are not added.

The only archive addition is `data/curios/tags/item/trinket.json`, containing the Locket with `replace: false`. Every previous archive entry—including the Necklace tag, Java classes, recipes, pet data and networking—is byte-identical. Pet Vault already checks all equipped Curios slots; no class change is needed for Trinket support. Nameless Trinkets is unchanged.

## Verification

```powershell
python docs/patches/pet-vault-trinket/build.py
python docs/patches/pet-vault-trinket/run_server.py --baseline
python docs/patches/pet-vault-trinket/run_server.py
python docs/patches/pet-vault-trinket/verify.py --installed
```

The unpatched baseline fails the expected Trinket tag assertion. The final isolated Minecraft 26.2 / NeoForge 26.2.0.90 dedicated server checks native Curios slot validation for both Trinket and Necklace, Pet Vault's actual equipped predicate in each slot, retained Nameless tag members, empty/removed slots and rejection of unrelated items. Curios caches item queries for a game tick, so fresh test wearers are used for native equipped queries; live inventory predicates check removal. No production cache behavior is changed.

This is native slot/equipped-state testing, not a full-pack manual radial-menu or pet capture/summoning test. The existing equipped check is shared by those paths. The three-mod fixture and diagnostics remain under ignored `tmp/`; hashes and exact test results are adjacent to this file. The final JAR is in repository `mods` and ignored fixed packs, with its predecessor retained in `mod fixing/retired packs/pet-vault-trinket-2026-10-25`. Handbook/PDF are updated; personal installations and the separate server are untouched. Sync matching client/server copies and restart. No commit or push requested.

# Animal Pen guidebook crash; Auras removal

25 October 2026. Auras is removed from the client pack at the owner's request. Animal Pen 2.5.0 remains unchanged; Patchouli 26.2-95 receives a same-filename Java-25 repair. Install the updated Patchouli on client/server, remove Auras from the client and restart.

## Root causes

The owner's latest crash reported `Tried to access entity ID before ID assignment` while Animal Pen's cow page rendered. Its first failing hook was Auras' `EntityRendererMixin.auras$stampEntityId`, reached from Patchouli `PageEntity.renderEntity`.

A minimal test without Auras reproduced the same exception in vanilla `ItemModelResolver.updateForLiving`. Patchouli's shared `EntityUtil.loadEntity` preview factory created detached entities without assigning the IDs required by Minecraft 26.2 rendering. Removing Auras alone therefore did not fix the underlying book preview.

The repaired factory gives an unassigned detached preview a distinct, stable negative ID before returning it. Already assigned IDs are preserved. The preview is not added to a world, registered, ticked or saved; live entity allocation and Animal Pen gameplay remain untouched. An atomic counter supplies IDs without a retained entity map or background task. One existing class changes and one small common helper is added. All other Patchouli entries remain byte-identical.

## Verification

An isolated Minecraft 26.2 / NeoForge 26.2.0.90 connected client and integrated server, with only Animal Pen, Patchouli and a disposable probe, passed **48 checks**. Coverage includes detached/stable/distinct preview IDs, repeated rendering, normal world/player rendering, twelve vanilla entity preview types, and eight actual Animal Pen guidebook pages: cow, pig, sheep, wolf, bee, tropical fish, turtle and goat. The cow page screenshot was visually checked. This is not a full-pack multiplayer or every-book test.

```powershell
python docs/patches/auras-preview-id/build.py
python docs/patches/auras-preview-id/run_client.py
python docs/patches/auras-preview-id/verify.py --installed
```

The baseline runner uses the pinned pre-repair Patchouli without Auras and reproduces the native renderer exception. `artifacts.json` records exact production hashes/entry changes; `runtime-tested-mods.json` pins the tested Animal Pen and Patchouli archives. The active/fixed Patchouli copies match. Its predecessor is archived under ignored `mod fixing/retired packs/animal-pen-guidebook-2026-10-25/`.

The original Auras archive and retired icon are retained under ignored `mod fixing/retired packs/auras-removal-2026-10-25/`. Installed JAR metadata declared no dependents requiring Auras. Its catalog entry, indexes, source link and icon were removed; the pack now has 304 main JARs and 23 client extras. Existing verified artwork for other mods was preserved after the catalog refresh. The handbook was rebuilt. Personal Minecraft files and worlds are untouched; the report was read only. No push requested.

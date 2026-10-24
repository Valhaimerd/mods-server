# Bifrost marker terrain-occlusion repair

24 October 2026. The marker's three inset model elements leave visible gaps around its base, stem and cap. Its registration copied crying obsidian's solid-cube occlusion setting, however. Minecraft therefore omitted adjacent terrain faces behind those gaps, producing the reported X-ray view. The model itself was not using a through-walls render layer.

`ModBlocks` now adds `noOcclusion()` to the existing crying-obsidian property copy. This is the only production class changed from the preceding first-person beam repair. The block/item IDs, marker data, recipes, textures, decorative model, full collision shape, strength, sounds, race permissions and teleport behavior are retained. The first-person beam repair is included in this build.

The native regression fails on the previous build's `canOcclude()` result. With the fix, it verifies non-occlusion and the visibility of all six adjacent stone faces. The combined dedicated suite passes 133 checks both with and without the revive integrations. A connected client repeats the six face checks and captures two stacked markers in a stone pit, with the pack's Sodium, More Culling and Vectors enabled:

```powershell
python docs/patches/asgardian-expansion/build.py
python docs/patches/asgardian-expansion/run_server.py --candidate --revive
python docs/patches/asgardian-expansion/run_server.py --candidate --no-revive
python docs/patches/asgardian-expansion/run_client.py --marker-only --renderers
# Wait for tmp/asgardian-expansion/client/expansion-client-result.txt; inspect its screenshot.
python docs/patches/asgardian-expansion/verify.py --candidate --runtime
```

The isolated client's marker mode bypasses unrelated combat/input/transit fixtures. The prior beam proof remains applicable because `TransitVisuals` is byte-identical to its tested repair. The archive verifier continues requiring byte-identical marker SavedData, service and commands; `ModBlocks` is now an intentional exception to the earlier unchanged-registration-class check. Registry identifiers themselves are preserved.

The same active JAR filename is retained. The tested replacement goes to repository `mods/` and `mod fixing/1 fixed packs/`; the previous beam-repaired JAR is archived under ignored `mod fixing/retired packs/bifrost-marker-occlusion-2026-10-24/`. Final hashes and checks are in [runtime-results.txt](runtime-results.txt). Existing chunks may need a client chunk reload after installing the replacement so their neighbouring faces are rebuilt.

No personal Minecraft profile, separate desktop server, or existing world is modified. No commit or push is part of this fix. The handbook's marker mechanics and controls remain accurate; this rendering correction requires no PDF revision. Other shader/resource-pack combinations are outside this focused visual check.

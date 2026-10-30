# Utilities food / Alex's Caves compatibility

31 October 2026. Minecraft 26.2, NeoForge 26.2.0.90, Java 25+.

The profile crash at `2026-10-31 00:28:43` was caused by two required redirects of `FoodData.eat(FoodProperties)` inside `FoodProperties.onConsume`. The Utilities hook claimed the invocation, leaving Alex's Caves' `ac_eat` redirect with zero targets. The profile's installed compatibility and Alex's Caves archives matched the repository hashes.

`utilities-paths/src/FoodMixin.java` now injects at normal return instead. Native consumption, including Alex's Caves' Primordial armor branch, completes first; Utilities then adds only its fractional nutrition bonus and saturation bonus from the original food payload. Hunger/saturation caps, the fractional remainder and the existing 14% path benefit remain. No redirect requirements are weakened and Alex's Caves is unchanged.

`build.py` overlays only `FoodMixin.class` onto the captured current compatibility JAR. All 31 other entries, including the existing Orb bridge and other Utilities hooks, remain byte-identical. The active filename remains `orb_of_oblivion_puffish_compat-1.0.2.jar`.

## Reproduction and proof

```powershell
python docs/patches/utilities-food-compat/build.py
python docs/patches/utilities-food-compat/run_server.py --baseline
python docs/patches/utilities-food-compat/run_server.py
python docs/patches/utilities-food-compat/run_client.py
```

The baseline reproduces the same fatal redirect conflict in an isolated server. The candidate passes **75 native checks**: the existing 68 Utilities checks plus seven assertions covering the raw-meat branch, Primordial nutrition/saturation, coexistence with Utilities, and hunger/saturation caps. Exact tested archives are recorded in the adjacent manifests. The client probe copies the complete distributed `mods` and `mod store` collection into a disposable instance and requires reaching the title screen after resource loading; it dismisses only vanilla accessibility onboarding.

Alex's Caves currently reads `forge:raw_meats` while supplying `c:raw_meats`. A **test-only** beef tag activates that otherwise dormant armor branch for coverage; this repair does not alter those production tags. The first candidate run exposed that mismatch, then the corrected fixture passed. It is separate from the fatal loading conflict.

The replacement is installed in repository `mods/` and ignored `mod fixing/1 fixed packs/`. Previous bytes are backed up under ignored `mod fixing/retired packs/utilities-food-compat-2026-10-31/`. Personal profile files and worlds are untouched. Resync the client and server with the updated archive. Gameplay documentation, counts, controls and the handbook PDF need no changes because the documented benefits are unchanged.

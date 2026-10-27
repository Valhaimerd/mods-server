# Large Meals 26.2 source port

Port of KreloX/Large-Meals branch `1.21.1`, commit `772a66c461c611974df61a022dec82bfe3589587`. Original input: `mod fixing/delight extras (optional)/large_meals-1.21.1-1.0.1.jar`. The source and input metadata declare MIT License; this upstream checkout/input has no separate LICENSE file. Exact hashes are in `provenance.json`.

Candidate: `tmp/delight-batch/large_meals/large_meals-26.2-1.0.1-port1.jar` (root agent handles integration and installation). Compiled with Java25 against Minecraft26.2, NeoForge26.2.0.90, moddev2.0.148, and the exact installed `FarmersDelight-26.2-1.3.3-fix5.jar`, without embedding FD.

All18 original item IDs,15 edible foods,3 feast blocks,18 recipes and2 sheep knife-loot modifiers remain. Nutrition, saturation, always-edible flags, effect durations/amplifiers/chances, stack sizes and bowl/bottle remainders are preserved. All27 original textures are byte-identical. Omurice, mushroom pot pie and roasted mutton rack retain four servings and leftover trays.

Changes: native deferred item/block registration supplies registry keys; old FoodProperties effects become native Consumable status effects; consumption returns bowl/bottle with usingConvertsTo; recipe ingredients adopt native strings/tags; old data directories become singular;18 modern item definitions point at original models; sheep entity predicates use `minecraft:entity_type`; obsolete global loot index is removed because NeoForge26.2 auto-loads modifiers. Vietnamese UTF16 language data is normalized to UTF8. Old developer datagen classes remain in source but are excluded: checked-in generated resources are the gameplay authority. JEI was an upstream runtime-only development dependency, with no source integration removed. Java25 Gradle bootstrap is required; installed Java26 cannot run this Gradle9.2.1 Groovy version.

## Rebuild and checks

From repository root in PowerShell:

```powershell
$env:JAVA_HOME='C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2'
$project='mod fixing/source/delight-batch-20261027/upstream/large_meals'
python "$project/verify_resources.py"
& "$project/gradlew.bat" -p $project clean build --no-configuration-cache --console=plain
& "$project/gradlew.bat" -p $project runServer -Pprobe --no-configuration-cache --console=plain
# Following a probe run, use clean build without -Pprobe for production.
```

The isolated project `run/` uses its own accepted EULA, offline test world and port25587. Regression sources are included only with `-Pprobe`; the final production candidate was built without that property and archive inspection confirms no probe classes. The probe halts the isolated server automatically.

Baseline: original runtime source produced29 compiler errors against native26.2 APIs. Resource check failed for missing item definitions. The first runtime passed94 checks but exposed old loot index and entity predicate decode failures, corrected before final candidate.

Final: `clean build` GREEN; resource checks GREEN for18 native recipes and exact semantic equality to pinned upstream after ingredient migration; production ZIP/CRC/metadata/texture checks GREEN. Isolated Java25/NeoForge26.2.0.90 server completed 124 assertions with zero ERROR records: all18 recipes decode with usable ingredient sets;18 item registrations; all3 feasts serve four bowls and leave trays; all15 edible foods have native consumables and feed original amounts; applicable13 foods apply14 effects with original durations/amplifiers; all13 container meals return bowls/bottle; both sheep modifiers load and obey all8 raw/cooked x knife/no-knife x burning/not-burning combinations. `verification.txt` records every assertion. Full logs remain in `tmp/delight-batch/large_meals/`.

Scope: scripted isolated dedicated-server interactions, not visual client or full-pack multiplayer QA. The root agent handles combined runtime/client testing. Personal installations and worlds were untouched.

## Reproduce source

Clone the source repository at the pinned commit, then apply `source-port.patch` from that checkout root. The patch includes the build setup, native resource migrations, resource verifier and optional regression source. `provenance.json` pins original/candidate bytes. The exported patch passes reverse `git apply --check` against the finished source.

## Native client texture follow-up

Actual client MaterialBaker initially reported retired FD `tray_top` and `honey_glazed_ham_bone` references plus an upstream unbound `#missing` mushroom-pie face. Modernize tray references to installed FD `tray`; bones use installed FD `honey_glazed_ham_details` with the native bone UV region; the missing cut pie face uses the existing pie-inner texture. All27 original PNGs remain byte-identical. The resource verifier now rejects absent FD texture paths and unbound model-face references. Production clean build and resource verifier pass; gameplay unchanged. Final client material/render validation belongs to the integration report.

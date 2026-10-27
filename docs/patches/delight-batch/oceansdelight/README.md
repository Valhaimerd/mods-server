# Ocean's Delight 26.2 source port

Candidate: `tmp/delight-batch/oceansdelight/oceansdelight-26.2-1.0.4-port1.jar`.
SHA-256: `606cc757cd6e525c9b7f605c6fafad6556deaaa13d21c7894be49c7475b827c3`.

Source: https://github.com/Scouter456/Oceans_Delight/tree/70fe769a71b0ff584b8a0164af62e4cb9fd8699a (`oceans_delight_1.21_neoforge`). Author Scouter456. Original source gradle.properties and input JAR metadata declare MIT; the branch also carries NeoForged's separate MIT TEMPLATE_LICENSE.txt, retained in the archive. No upstream license claim is changed.

Input: `mod fixing/delight extras (optional)/oceansdelight-neoforge-1.0.4-1.21.1.jar`, SHA-256 `30fec2263f77bcd4002995d9fc127a911c964317dbd0e3c54e348954d1722d38`.

## Port

Build uses Gradle 9.2.1, moddev 2.0.148, Minecraft 26.2, NeoForge 26.2.0.90, Java 25, and the installed `FarmersDelight-26.2-1.3.3-fix5.jar` as its non-embedded dependency. Native DeferredRegister factories set item/block IDs. Mod event registration uses the injected bus. Identifier and the native creative-tab builder replace obsolete APIs.

The 23 foods keep all nutrition/saturation and always-edible settings. Seven active effect entries retain effect, duration, amplifier and probability through Consumable components: tentacles/stuffed squid Blindness30s; cooked stuffed squid Comfort60s; guardian soup Comfort180s; raw elder guardian slice Mining Fatigue150s at50%; cabbage-wrapped elder guardian/cooked stuffed cod Comfort30s. Three fast foods retain0.8s consumption. Four bowl foods retain bowls when eaten and crafted. Guardian soup retains its original four servings, serving item, leftover bowl, shape and translucent models; modern model render_type replaces obsolete client render-layer registration.

All25 original item IDs/item models, one block,31 recipes,30 textures and four loot modifiers remain. Recipes migrate ingredient objects to native strings/tags without changing quantities, time, experience, results or requirements. Loot uses native minecraft:entity_type predicates and NeoForge's automatic loading instead of the retired global index. Diet compatibility tags use singular item directories and boolean replace fields; source contained invalid string booleans.

The old datagen sources remain available but are excluded from production compilation; the complete checked-in generated data is ported and packaged. Running old datagen is not supported by this patch.

## Rebuild and verify

Apply `oceansdelight-26.2.patch` to a clean checkout of the pinned commit at `mod fixing/source/delight-batch-20261027/upstream/oceansdelight`, relative to this repository, so its installed FD dependency path resolves. Set JAVA_HOME to a Java25 JDK, then run `gradlew.bat build`. The copied wrapper does not run on Java26; use25. The produced JAR is `build/libs/oceansdelight-1.0.4-26.2-port1.jar`.

Run `python docs/patches/delight-batch/oceansdelight/verify.py` from the repository root after copying the JAR to the candidate path. It compares all31 recipes semantically with the original, every original item model and texture, and all four loot modifiers after the required schema change; it also verifies Java class major69 and that FD is not embedded.

Original runtime sources fail native compilation (original-source-red.log). Production build passes (build.log). An isolated minimal FD+Ocean native26.2.0.90 / Java25 server reaches Done, loads1920recipes, and logs zeroERROR on the corrected data (server-green.log). The initial server test exposed the old loot schema/global index and malformed Diet tags; these were corrected before the final build. No personal worlds or installation was touched.

## Extended native regression probe

Run `gradlew.bat -Pprobe runServer --offline --console=plain` in the source checkout after writing `eula=true` in its isolated `run/eula.txt`. The test-only `regression/com/scouter/oceansdelight/probe/OceanProbe.java` is included only with `-Pprobe`; ordinary production builds exclude it. Do not distribute a JAR built with the probe flag. The probe automatically stops the isolated server and logs `OCEAN_PROBE COMPLETE checks=455`; a failed assertion logs FAIL with a stack trace, so inspect the marker as well as Gradle's exit status.

455 native assertions passed (`probe-green.log`) on Minecraft26.2 / NeoForge26.2.0.90 / Java25 with only FDfix5 and Ocean. These cover:

- All31 recipes decoded, every actual ingredient/tag nonempty, native assemble result item/count matching packaged JSON. All5 FD cooking-pot and4 cutting-board recipes also match real representative input stacks; all4 cutting recipes accept the installed iron knife and have nonempty outputs.
- All23 edible foods retain nutrition, saturation, fast/normal consumption duration and always-edible flag against values extracted from the original pinned source. Actual fake-player consumption restores the expected hunger/saturation and returns the appropriate bowl for all4 bowl foods; other foods leave no remainder.
- All7 active status-effect components retain exact effect IDs/durations/amplifier/probability against original source values. Guaranteed effects actually apply. The50% raw elder guardian slice Mining Fatigue component is also exercised128times with both applied and unapplied outcomes observed.
- Guardian soup starts with4 servings. Four bowl interactions return the correct bowl-of-soup item and decrement servings4?0. Its empty decorative container remains, then clears on another interaction. The original mod has no soup block loot table; no new empty-block drop behavior is introduced. Edible bowl-of-soup consumption returns its bowl as tested above.
- Four loaded loot modifiers are applied to squid, glow squid, elder guardian, guardian and pufferfish with both knife/stick and burning/not-burning combinations.80 outcome assertions verify exact added items: squid/glow squid tentacles and elder guardian slab require a knife; guardian item does not; fire does not change those original rules; pufferfish receives none of these unrelated additions. Its actual cutting recipe is covered separately.

A first probe stopped because the test loot context omitted ATTACKING_ENTITY; adding the parameter used by real native entity loot resolved that harness error. No production behavior was changed during this extended validation. The gameplay-tested candidate originally had SHA-256 `48cbd94a8c157686b8b3f913c09623c2fa1f28d1be5e0924f8965dc6eaa02f80`; the subsequent asset-only rebuild has SHA-256 `606cc757cd6e525c9b7f605c6fafad6556deaaa13d21c7894be49c7475b827c3`.

Remaining scope limits: client appearance/animation, whole-pack integrations and Display Delight behavior are handled by the integrating task. The probe exercises native food completion, recipe decoding/assembly, FD recipe matching, FeastBlock interaction and modifier application directly; it does not simulate player timed-click animation or cook full workstation ticks. No personal worlds/installations were touched.

## Client asset reference repair

Client validation found the upstream seven guardian soup models refer to the removed FD texture `farmersdelight:block/tray_pie_leftover`. The installed FDfix5's `shepherds_pie_block_leftovers` model uses `farmersdelight:block/shepherds_pie_leftovers` for the same14?14 tray top UV region. All seven models now reference that texture; geometry, UVs, original Ocean textures and translucent rendering remain unchanged. The normal production build passes (`asset-build.log`). The preservation verifier now compares all seven original block models allowing only this texture rename and the native render_type, and resolves all27direct texture references against installed FD/Ocean archives. All31recipes/25item models/30original texture bytes/4loot rules remain preserved, with no embedded probe classes. Native gameplay was not rerun for this asset-only repair; the455-check evidence covers unchanged gameplay classes/data.

# Demoralizing Shout red ground indicator

30 October 2026. A visual-only same-filename Rogues overlay. The existing central Shout effect is fixed at 3 blocks and does not communicate its 12-block radius, or 16 with Improved Shout.

Add an emissive red ground perimeter and soft red light pulse to the native release visuals. `scale_with: RANGE` resolves the current native spell range through `ReleaseFx`, so Improved Shout automatically enlarges both. The footprint stays at the casting position, fades naturally, and appears once per cast even with no enemies. Existing animation, sound and target-hit smoke remain. Battle Shout and Challenging Shout inherit the same parent cast indicator.

No new effects, entities, timers or Java runtime hooks. Only `data/rogues/spell/shout.json` release particles change; damage, buffs, cooldown, party rules and other spells stay byte-identical.

Build/test in the ignored `tmp/warrior-shout-area` instance. Native tests check release particle emission at 12/16 blocks with both upgrade choices and with no targets, preserving base spell data. Client captures check the contrasting red footprint and expiry. The test capture Mixin stays in the diagnostic JAR only.

Commands: `python docs/patches/warrior-shout-area/build.py`, then `run_server.py --baseline`, `run_server.py` and `run_client.py` in the same directory (from the repository root, use their full script paths). The builder stages a replacement; it does not modify personal Minecraft/server installations. The standard recipe/cast data schema needs no new dependency.

## Installed result

The old archive reproduces the missing readable range indicator. The replacement passes **51 native checks**, including base 12 and Improved 16 radius resolution, both branches, exact red/emissive appearance, ground anchoring, no follow behavior and single range resolution. Native sound, cooldown and impact count are retained. Isolated client captures show the red 12/16-block footprints and their expiry. The initial test camera was outside Minecraft?s normal particle distance; the final camera is inside that distance. Production code did not require a distance workaround.

[Normal Shout](screenshots/shout-radius12.png) ? [Improved Shout](screenshots/improved-shout-radius16.png)

Installed in `mods/` and ignored fixed packs under the existing filename. The prior archive remains in ignored `mod fixing/retired packs/warrior-shout-area-2026-10-30/`. Only the Shout spell?s release particle list changes; all 823 other archive entries and every other field of that spell are unchanged. Handbook source/PDF are synchronized. No commit or push requested. This is isolated native and visual coverage, not a full-pack shader/multiplayer certification.

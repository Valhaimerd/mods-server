# Punchy Combat Animation Compatibility Implementation Plan

> **For agentic workers:** Use `superpowers:executing-plans` to work through this plan. Keep the supplied screenshots and the user's confirmed comparison as evidence; do not assume that the retired Modded Swords overlay caused these symptoms.

**Goal:** Preserve Punchy's single set of first-person hands while making claymores, bows, crossbows, and RPG skill animations display correctly with Better Combat and Spell Engine on Minecraft 26.2.

**Architecture:** Find which renderer owns the hands and item in each state before changing code. First try Punchy's built-in per-item fallback on representative weapons. If it cannot preserve combat and skill animations without duplicates, patch the narrow ownership transition in Punchy and retain the same active JAR filename.

**Tech Stack:** Minecraft 26.2, NeoForge 26.2.0.90+, Java 25 class format, Punchy 2.8f, Better Combat 3.2.2, Spell Engine 1.10.5, RPG Series equipment, Player Animation Library 1.2.6.

**Spec:** The user's 2026-10-16 screenshots and follow-up: duplicate hands disappeared after enabling `bettercombatCompat`; claymore and custom skill animations, bow idle, and crossbow visibility remained wrong after removing the Modded Swords overlay. The same view must be checked in gameplay and behind the pause menu.

## Known evidence and limits

- The converted Modded Swords x Punchy! JAR was removed from the active pack, and the user confirmed the reported animation defects remained. The original ZIP and converted JAR are retained under `shaders/` as references. Do not reinstall them as a speculative fix.
- The user's saved Punchy config has `bettercombatCompat: true` and an empty `itemBlacklist`. Saved Better Combat and Spell Engine settings both say `firstPersonAnimations: AUTO`.
- Punchy 2.8f's `PunchyClient` calls `BetterCombatCompat.applyVanillaItemDisplay(true)` and `SpellEngineCompat.applyFirstPersonAnimations(true)` while in first person with compatible, non-blacklisted hands. The first method sets Better Combat's runtime first-person mode to `NO` and hides its arms; the same tick separately stops/suppresses Better Combat attack poses. The second method sets Spell Engine's runtime first-person mode to `NO`. These are bytecode observations from the installed JAR, not yet proof of every visual defect.
- The screenshot's **Black Bow of the Betrayer** resolves to `arsenal:unique_longbow_2` in the installed Arsenal language file. Candidate crossbows include `arsenal:unique_heavy_crossbow_1` and `archers:heavy_crossbow`; candidate claymores include `paladins:diamond_claymore` and Arsenal's unique claymores. Confirm exact item IDs during reproduction.
- Punchy's `itemBlacklist` supports exact item IDs and patterns; `blacklistApplyDualHanded` can extend a match to the other hand. Its first-person renderer skips a blacklisted hand, and the tick compatibility code restores Better Combat and Spell Engine modes when a hand is blacklisted. Verify the actual hand-off in game before adopting a pack default.
- Punchy's [official page](https://modrinth.com/mod/punchy-fpa) describes client-side first-person rendering and recommends compatibility resources for Better Combat add-ons. Better Combat's [26.2 source](https://github.com/ZsoltMolnarrr/BetterCombat/blob/26.2/common/src/main/java/net/bettercombat/mixin/client/AbstractClientPlayerEntityMixin.java) documents separate first-person item and arm visibility. The archived 1.21.1 compatibility mappings are not evidence of 26.2 correctness.

## Isolated runtime findings, 2026-10-16

- The copied flat world loaded with Punchy 2.8f, Better Combat 3.2.2, Spell Engine 1.10.5, Arsenal 1.5.1, Paladins 3.1.2, and their libraries on NeoForge 26.2.0.90. Runes embeds `bundle-api-4.0.0+26.2-neoforge.jar`; `bundleapi` is present despite having no standalone repository JAR. Paladins also needed the installed Structure Pool API JAR in this reduced set.
- With `bettercombatCompat: true` and no item blacklist, `arsenal:unique_heavy_crossbow_1` was selected but absent from first person. Adding only that exact ID to `itemBlacklist` restored its idle model. Captures are `tmp/punchy-26-2-client/crossbow-punchy-true.png` and `crossbow-blacklist.png`.
- A held `paladins:netherite_claymore` attack with the crossbow-only blacklist showed the sword vanishing during the swing while Punchy's arm moved. Capture: `tmp/punchy-26-2-client/claymore-attack-punchy.png`. The claymore's own blacklist and the Spell Engine cast case still require comparison before distribution.
- A no-Punchy launch displayed a vanilla crossbow in the selected slot; that capture does not compare the exact Arsenal item and should not be cited as an exact-item A/B result.
- Exact-item fallback captures: `claymore-attack-blacklist.png` keeps the claymore visible through a Better Combat swing; `bow-draw-blacklist.png` shows a visible idle bow and continued draw; `staff-cast-wildcard.png` shows a visible staff during a Spell Engine cast. The matching no-blacklist staff capture, `staff-cast-punchy.png`, shows the staff disappearing for most of the cast.
- Namespace wildcards (`arsenal:*`, `paladins:*`, `archers:*`, `rogues:*`, `wizards:*`) were selected so one configured rule covers every installed RPG Series weapon and class item; `rpg-wildcard-matrix.png` confirms the Arsenal bow/crossbow visual fallback. The same blacklist helper restores Better Combat's and Spell Engine's saved first-person mode whenever either hand matches. The isolated config also sets `blacklistApplyDualHanded` for every pattern so Punchy does not keep drawing the other hand.
- Ruling: the built-in item fallback passed the representative failures, so no renderer or attack-state bytecode patch is warranted. It yields Punchy's hand physics on RPG Series items to the RPG combat renderers; Punchy remains active for other items. This trades Punchy's RPG-weapon idle motion for stable weapon and cast presentation.
- The final same-name Punchy JAR changes only `PunchyConfig$Data.class`, retains all 551 archive entries and Java 25 class major version 69, and passed an isolated world launch with a newly generated config. `repack-staff-cast.png` shows the packaged default during a live staff cast. The client installer merge preserved custom entries/settings, created a backup, and was idempotent in `tmp/punchy-installer-probe/`.
- No-shader right-handed first-person gameplay was exercised. Shader-enabled, left-handed, third-person, and multiplayer presentation were not exercised; do not claim those matrix cells passed.

## Global constraints

- Use a separate client game directory and flat test world; do not change the user's personal world or active game for development.
- Keep `bettercombatCompat: true` during the baseline. Change only one config value, item, or mod at a time in each comparison.
- Keep Punchy client-only in `mod store/`; do not add the test probe or retired overlay to the server pack.
- A repack of Punchy 2.8f must keep `punchy-2.8f-neoforge-26.2.jar` as the active filename and retain Java 25-compatible class files.
- Document observed behavior separately from likely cause. Do not call compilation or a title-screen load a visual fix.

## Review focus

1. Main-hand claymore plus offhand shield: one set of hands, correct two-handed attack, and no offhand duplication.
2. Bow idle, draw, release, and switching away: the bow remains visible at rest and still draws correctly.
3. Crossbow idle, load, charged hold, fire, and pause menu: the same item stays visible in each required state.
4. RPG skill cast during and after a Better Combat attack: Spell Engine's cast pose appears and normal hand rendering resumes afterward.
5. Shader off and on, right and left-handed player, first and third person: no frozen duplicate, missing item, or lingering transform.

## File map

| Path | Role |
| --- | --- |
| `shaders/Modded Swords x Punchy! 1.21.1 v1.3.2.zip` | Original reference archive, never installed for the baseline. |
| `shaders/modded-swords-x-punchy-compat-26.2-1.3.2.jar` | Converted, inactive reference JAR. |
| `tmp/punchy-26-2-client/` | Ignored isolated client, copied configs, flat world, screenshots, and logs. |
| `mod fixing/source/punchy-combat-compat/` | Ignored patch source and reproducible build notes if a code fix is necessary. |
| `mod store/punchy-2.8f-neoforge-26.2.jar` | Replace only with a proven client repack. |
| `mod fixing/README.md` and `docs/MODLIST.md` | Record actual pack behavior after validation. |

## Task 1: Reproduce and classify each defect

- [x] In the isolated 26.2.0.90 flat world, load Punchy, Better Combat, Player Animation Library, Cloth Config, Spell Engine and its required library, plus the smallest installed RPG module that supplies each representative item. Keep the retired overlay absent.
- [ ] Capture idle, hold-attack, single attack, skill cast, item use, switch, and pause-screen frames for the exact claymore, `arsenal:unique_longbow_2`, and one crossbow. Record hand, offhand item, first/third person, shader state, and each exact item ID.
- [ ] Compare: Better Combat/Spell Engine without Punchy; Punchy with compatibility `false`; Punchy with compatibility `true`. Use copies of the same world and options. Mark which state first loses the item or animation. This separates Punchy's suppression from an RPG item/model defect.
- [ ] Compare shader off/on only after the no-shader result is known. The pause-screen model may be a retained rendered frame; measure whether its visibility differs from live gameplay before treating it as a separate bug.

## Task 2: Test the built-in per-item fallback

- [x] Back up the isolated Punchy config. Add one exact affected item ID at a time to `itemBlacklist`, then use dual-handed fallback for the claymore and bow.
- [ ] Repeat Task 1's states with that one item and inspect whether Punchy yields its hand/item render while Better Combat or Spell Engine resumes its first-person animation. Check that the earlier duplicate hands do not return.
- [x] Repeat independently for a claymore, longbow, and crossbow. A working blacklist is the preferred smallest fix; record the exact IDs and whether the fallback must cover both hands.
- [ ] If an item fallback works but a spell cast with another weapon still breaks, test a cast-state hand-off separately; do not blacklist whole RPG namespaces to hide the symptom.

## Task 3: Patch only a proven ownership conflict, if needed

- [ ] Identify the failing transition in `PunchyClient`, `BetterCombatCompat`, `SpellEngineCompat`, or `PunchyArmRenderer` from Task 1/2 evidence. Add short-lived instrumentation in the isolated copy to log effective first-person mode, active attack/cast state, blacklist match, and whether Punchy submitted an arm and item.
- [ ] Write a narrow guard so the correct renderer owns the first-person item during the failing state, then restore Punchy after it ends. Preserve vanilla use, third-person animations, offhand shields, and ordinary Punchy idle behavior.
- [ ] Rebuild only the changed Punchy class or classes to Java 25 bytecode, preserve other JAR entries, and compare archive contents against the current active JAR. Keep trial builds outside `mod store/`.

## Task 4: Validate and integrate

- [ ] Repeat the Review focus matrix in the isolated client, first without shaders, then with the pack's shader and resource settings. Check exact transitions as well as still frames.
- [x] Only after the affected items pass, replace the active Punchy JAR or package the verified config fallback. Keep the same JAR name for a same-version repack. Do not restore the retired overlay unless separately proven necessary.
- [x] Update `mod fixing/README.md`, Punchy's handbook wording if behavior changed, and the installer only if a recommended per-item config must be distributed. Rebuild the PDF when its source changes. Push only when requested.

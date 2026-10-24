# Asgardian race for Iourus

Current balance follow-up: [Iourus race balance](../race-balance/README.md) changes Asgardian size to 1.10x and removes its mining-speed bonus. Earlier values below record this original addition.

Local same-filename replacement of `iourusraces-1.1.0.jar` for Minecraft 26.2, NeoForge 26.2.0.90 and Java 25. Embedded upstream metadata declares mod version 1.0.0; it is preserved. No new mod or runtime dependency is added.

## Implemented

Asgardian is the eighth visible race, appended to the original enum after Draconic. It has 22 maximum HP before other bonuses, 1.05x uniform scale, 0.95x movement speed, +2% melee/ranged/magic hit damage, +2% block-breaking speed, direct lightning-strike immunity, and 10% more incoming magic damage. The selector includes plain traits and Ragnarok lore describing its broken ancestral wards. The preview covers all eight races and the layout keeps eight rows above the footer at the tested GUI sizes.

The 2% offensive bonus applies once to attributed player melee, projectile or magic hits. Magic projectiles do not receive two bonuses, and healing is unchanged. Magic uses the installed common `c:is_magic` tag plus vanilla magic, indirect magic, dragon breath and Wither damage types. Direct lightning is canceled before weakness applies; the lightning-strike event also prevents the strike's player ignition. Ordinary environmental fire remains harmful. Racial damage adjustment runs at the highest incoming-event priority so downstream fatal-hit logic sees the adjusted amount. It does not cancel party protection or repeat the pack's PvP scaling.

Native Iourus health/movement/scale modifiers are reused. The 2% mining modifier has a stable identifier and is removed/reapplied with native race changes. The race bonus is a per-hit damage adjustment, rather than an extra spell-power modifier, preserving healing and avoiding duplicated bonuses on hybrid attacks. The other seven races' ability handler is unchanged byte-for-byte.

## Selection, saves and compatibility

Native `/race asgardian`, first-login selection, administrator `/race reset`, respawn and clone paths work with the new enum. Ordinary players still cannot replace an already selected race. Existing saves and sync payloads use race names; original enum positions are also preserved. The native payload registrar now uses `asgardian-v1`, requiring matching updated Iourus client/server builds rather than silently decoding Asgardian as NONE on an older client.

This change implements the race and its listed traits only. Bifrost, Hofund, Mjolnir and Stormbreaker expansion/gating remain separate proposed work in `mod fixing/ASGARDIAN.md`; those JARs are not changed by this patch.

## Verification

The baseline dedicated server failed with `No enum constant ...Race.ASGARDIAN`. The final candidate passed **96 server measurements**, including original enum positions, all old race health/scale/speed values, repeated modifier application/removal, gear HP retention, named save codecs, network codecs, native selection and reselection denial, respawn/clone/reset, actual NPC damage and once-only magic-projectile bonuses, magic weakness, lightning and fire event behavior, unchanged healing, and persistence across an actual isolated server restart.

The actual NeoForge client passed first-login eight-option selection, every race preview, three GUI layout sizes (426x240, 640x360, 854x480), native Enter confirmation, server/client sync, visible attributes and lore, and reselection denial. A real connected Asgardian took 11 magic / 10 ordinary fire / 0 lightning damage from ten-point hits. Its Cheat Death triggered when racial weakness turned a ten-point magic hit into lethal damage at 11 HP. The final selector screenshot was visually reviewed. These are focused isolated tests, not a full-pack multiplayer balance playthrough.

```
python docs/patches/asgardian-race/build.py
python docs/patches/asgardian-race/run_server.py --candidate
python docs/patches/asgardian-race/run_server.py --candidate
python docs/patches/asgardian-race/run_client.py
python docs/patches/asgardian-race/verify.py
```

Client launch is asynchronous; wait for `tmp/asgardian-implementation/client/asgard-client-result.txt`. It uses a unique offline test profile and a copy of a disposable isolated world. Native server evidence and client summary are in `runtime-results.txt`. Source replacements are in `src/`; `artifacts.json` pins exact archive changes and hashes. Build inputs remain ignored in `tmp/asgardian-implementation/`. Previous active contents are retained under ignored `mod fixing/retired packs/asgardian-race-2026-10-23/`; final replacements reside in `mods/` and `mod fixing/1 fixed packs/`. Personal installations and worlds were untouched. No commit or push was requested.

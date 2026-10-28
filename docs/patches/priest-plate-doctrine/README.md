# Priest plate doctrine and Paladin armor progression

Update: [Paladin tier-I rework](../paladin-tier-one/README.md) supersedes Justicar to1.5 per piece, totaling8 including its retained +2 set bonus. The original hashes and evidence below describe this earlier revision.

The later [class armor balance](../class-armor-survivability/README.md) changes robe defenses and durability in Paladins/Armory while preserving this doctrine and flat plate Healing Power policy. Its artifact hashes supersede the affected JAR hashes below.

Installed 2026-10-28 for Minecraft26.2 / NeoForge26.2.0.90 / Java25. This extends the [Paladin hand restriction](../paladin-doctrine/README.md) using its existing server guards and saved overflow. No new mixins or dependencies.

## Player behavior

Priest class allocation forbids all20 Paladin, Crusader, Netherite Crusader, Justicar and Lightbringer armor pieces. Right-click equipment is denied and synchronized back to the client. Armor equipped through inventory, commands or before choosing Priest is returned to storage before native equipment modifiers are collected. If inventory is full, it is saved until space becomes available, with existing death/keepInventory persistence retained. The warning is: ?Your Priest doctrine forbids Paladin plate armor.? Priest robes, ordinary armor and healing weapons remain usable. The Priest root still grants +3% Healing Power and now explains the restriction. Paladins still cannot wield healing staves or wands in either hand.

| Set | Healing Power per piece | Four pieces |
| --- | ---: | ---: |
| Paladin | 1.00 | 4 |
| Crusader | 1.25 | 5 |
| Netherite Crusader | 1.50 | 6 |
| Justicar | 1.75 | 7, plus retained +2 set bonus |
| Lightbringer | 1.75 | 7 |

Existing generated equipment configurations receive these values. Other item modifiers and set bonuses are preserved. Paladin root and ordinary path nodes remain +0.25 Healing Power each.

## Rebuild and evidence

`python docs/patches/priest-plate-doctrine/build.py` compiles the two existing helper sources and repacks the three immutable input snapshots under ignored `tmp/priest-plate-doctrine/before`. See artifacts.json for exact hashes and entry differences. Existing active filenames are preserved. Previous JARs are retained under ignored retired packs; final copies also reside in fixed packs.

`python docs/patches/priest-plate-doctrine/run_server.py` runs the native207-check fixture; add `--legacy` to exercise stale generated configs, or `--baseline` for the failing original. The baseline failed26 checks. Both final fresh and legacy runs passed207/207. The original Paladin hand, role-switch, saved overflow and death checks remain included. All20 plate pieces and all non-healing armor modifiers are checked.

The isolated Minecraft client passed70 checks, including real right-click prediction, Priest warning/root tooltip, full-inventory recovery, allowed robes/weapons and earlier Paladin restrictions. See client/client-checks.txt, client/runtime-manifest.json and client/screenshots. The fixture preserves worn armor when switching roots. Visual review confirmed the warning and +3%/forbidden node text. Personal profiles, worlds and the separate desktop server were untouched. These focused tests do not claim every modded auto-equip interface was manually exercised; the common server equipment guard covers them.

Run `python docs/patches/priest-plate-doctrine/verify.py --installed` for exact artifact/content and evidence verification. Restart matching client and server installations after syncing all three JARs. No Git commit or push was requested.

# Paladin and legendary plate Healing Power balance

Historical27October2026 balance. Superseded by the [28October Paladin doctrine and per-set values](../paladin-doctrine/README.md); the evidence below describes the earlier +0.25-per-piece revision.

27 October 2026. Same-filename Paladins & Priests 3.1.2 and Armory 1.5.2 overlays. Use matching updated client/server JARs and restart. No push requested.

| Set | Previous flat Healing Power per piece | Current per piece | Full set |
|---|---:|---:|---:|
| Paladin | +0.5 | **+0.25** | **+1** |
| Crusader | +1 | **+0.25** | **+1** |
| Netherite Crusader | +1 | **+0.25** | **+1** |
| Justicar (Armory) | +1.5 | **+0.25** | **+1** |
| Lightbringer (Armory) | +1.25 | **+0.25** | **+1** |

All twenty pieces retain their other native attributes. Priest/Prior robes and Armory Avatar/Absolution percentage Healing Power, haste and critical bonuses remain. Armor, toughness, Lightbringer melee damage, legendary set bonuses, durability, recipes and item IDs are preserved. Explicit custom ItemStack attribute overrides are outside the default-item balance.

Each mod's armor-registration method receives one narrow helper call. The helper changes only flat `spell_power:healing` ADD_VALUE entries in armor defaults and corresponding loaded equipment configs, immediately before native attribute construction. The native config save records the new values. Both fresh installs and old `paladins/equipment_v2.json` / `armory_rpgs/equipment_v3.json` configurations work without deleting configs; other loaded stats remain. No Spell Engine repack, new dependency or class restriction is added.

## Reproduce and proof

```powershell
python docs/patches/healing-armor-balance/build.py
python docs/patches/healing-armor-balance/run_server.py --baseline
python docs/patches/healing-armor-balance/run_server.py
python docs/patches/healing-armor-balance/run_server.py --legacy
python docs/patches/healing-armor-balance/verify.py --installed
```

Baseline native item attributes fail the requested 0.25 check (Justicar chestplate registers 1.5). Candidates pass **29 native armor checks with fresh configs and 29 with the original configs restored**. Both runs check all twenty actual item attribute components and all five complete-set totals. Every other native item attribute modifier across both mods is compared with the baseline, including non-target items and percentage robes. Avatar/Absolution percentage healing is explicitly checked. Java-25 classes, NeoForge 26.2.0.90 and `-Xverify:all` run in fresh disposable worlds with the installed RPG dependencies. This is isolated registration/config verification, not a fresh connected-client or full-pack playthrough.

**1,556 unchanged archive entries** are verified byte-for-byte. Each JAR changes exactly its armor-registration class and adds one helper in its own package; resources, spells and set-bonus data remain unchanged. `artifacts.json` pins input/output hashes. Baseline item/component snapshots and old generated equipment configs are adjacent; these are disposable test fixtures, not private user configs. Native results and tested dependency hashes are adjacent. Ignored inputs/candidates/worlds live under `tmp/healing-armor-balance/`; previous active/fixed JARs are under `mod fixing/retired packs/healing-armor-balance-20261027/`. Active/fixed copies match. Personal installations/worlds are untouched.

The Priest reference, handbook/PDF and pack notes are synchronized. Earlier uncommitted gameplay work remains intact.

PDF review retained 77 pages and all 51 correct contents destinations. Both output copies match. All pages were rendered for overview review, with changed pages 17, 18 and 22 inspected at reading size without new clipping or altered pagination.

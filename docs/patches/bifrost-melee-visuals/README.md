# Bifrost weapon visual repair - 25 October 2026

This pass changes resources only. Damage and ownership behavior are unchanged: the user confirmed the reported zero-damage weapons were unclaimed and asked to skip the damage change. Explicit `/asgard claim`, crafting ownership, protected deaths and all existing native RPG/Bifrost skills remain intact.

## Punchy root cause

The RPG compatibility resource mapped 139 RPG items but none of the three Bifrost items. In the native client, all three therefore resolved to a generic six-step sword combo (`attack_1`, `attack_1`, `attack_2`, ...). Stormbreaker's server-side Better Combat attack was already correctly `bettercombat:two_handed_slam`; only the first-person Punchy mapping was wrong. The baseline reproduced a Punchy `attack_1` clip.

Three exact aliases are added to `assets/minecraft/punchy/compat/zz_rpg_26_2.json`:

| Bifrost item | Existing RPG resource reference |
| --- | --- |
| Stormbreaker | `paladins:netherite_great_hammer` |
| Mjolnir | `paladins:netherite_mace` |
| Hofund | `paladins:diamond_claymore` |

No prior mapping is modified. The original modded-swords resource explicitly gives the Paladins great hammer `attack_mace` for every attack variation; the adapted RPG resource gives all three Better Combat combo slots that downward clip. Stormbreaker now follows that same mapping. Mjolnir uses the mace reference; Hofund uses the claymore reference. No new attack animation, damage coefficient or third-person combo is invented.

The ignored local full compatibility builder under `mod fixing/source/punchy-rpg-26.2/` also carries the aliases, so a later full resource rebuild will retain them. Do not rerun that historical Punchy JAR builder over newer renderer fixes without reviewing its input.

## Model and icon

Stormbreaker's held display scales are multiplied by 1.5 in both hands, for first-person and third-person contexts (0.6 to 0.9). Punchy's first-person renderer can use third-person item transforms, so both are covered. Held translation is adjusted to frame the larger head and shaft. The model geometry, longer shaft, textures and combat reach are unchanged.

The inventory GUI display is independently tilted (`25, 215, -30` degrees), shifted slightly and reduced from 0.78 to 0.46 scale. It fits inside its slot instead of overlapping its neighbors. Ground and fixed display transforms are unchanged; the requested enlargement applies to the held weapon, not the inventory thumbnail.

Bifrost uses the supplied **Pixel Rainbow Portal Vortex.png**, copied without image editing to `assets/bifrost/textures/spell/travel.png`. The source copy is `resources/travel.png` in this directory. It replaces the more detailed Rainbow Rune Portal Gateway icon. Hotbar placement, casting and cooldowns are untouched.

## Build and verification

```powershell
python docs/patches/bifrost-melee-visuals/build.py
python docs/patches/bifrost-melee-visuals/run_client.py --baseline
python docs/patches/bifrost-melee-visuals/run_client.py
python docs/patches/bifrost-melee-visuals/verify.py --candidate --runtime
python docs/patches/bifrost-melee-visuals/verify.py --runtime
```

The overlay uses the pinned current repaired Bifrost and compatibility resource archives under ignored `tmp/bifrost-melee-visuals/before-*`. Do not rebuild from those historical inputs over future changes without reviewing them. Build produces candidates only. `artifacts.json` records exact original/final hashes and changed entries. Only Bifrost's model/icon and the compatibility JAR's JSON mapping change; every class, gameplay resource, recipe, other animation mapping and metadata entry is byte-identical.

Connected Minecraft 26.2 / NeoForge 26.2.0.90 checks use Better Combat 3.2.2, Punchy 2.8f, Player Animation Library 1.2.8 and the installed RPG modules. An unclaimed weapon is rejected; after claiming, native and Better Combat melee damage the iron golem for both weapons. The baseline lacks all three aliases and plays `attack_1`; the candidate resolves all three reference mappings and plays `attack_mace` for Stormbreaker. Checks also confirm its native third-person slam profile. Frame captures cover first-person idle/attack, third-person held size and a vanilla inventory showing Stormbreaker next to Mjolnir, Hofund and the reference hammer. The new skill icon is visible on the HUD. This is not a full combo, shader or multiplayer animation matrix.

Same-filename final archives are installed in `mods/` (Bifrost) and `mod store/` (Punchy compatibility), with matching `mod fixing/1 fixed packs/` copies. Predecessors are retained in ignored `mod fixing/retired packs/bifrost-melee-visuals-2026-10-25/`. Current Bifrost source model/icon resources are synchronized. The existing handbook's gameplay and controls remain accurate; its icon is not embedded there, so no PDF or catalog rebuild is needed for this visual-only pass. No personal installation, external server or world was touched. Nothing was committed or pushed.

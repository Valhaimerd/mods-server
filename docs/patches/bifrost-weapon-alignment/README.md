# Bifrost weapon alignment - 24 October 2026

This pass changes weapon attributes, native RPG weapon skills, combat classification and Stormbreaker's model only. The future custom skills and input revisions are recorded in [Bifrost Skills.md](Bifrost%20Skills.md), also copied to `mod fixing/Bifrost Skills.md`. Right-click, throw/return, custom ability coefficients and ownership rules are not rewritten in this pass.

## Weapon changes

| Weapon | Reference | Neutral base damage / attack speed | Native RPG skill |
| --- | --- | --- | --- |
| Stormbreaker | Paladins Netherite Great Hammer | 15.1 / 0.8 | Ground Slam |
| Mjolnir | Paladins Netherite Mace | 10.6 / 1.2 | Smash |
| Hofund | Minecraft Diamond Sword | 7 / 1.6 | Swift Strikes |

Displayed values depend on the wielder's base attributes, race, skill nodes and gear. The provided screenshots include build bonuses; those totals are not hardcoded onto the weapon a second time. The numbers above are from the isolated default RPG configuration. Mjolnir and Stormbreaker use the installed Paladins equipment config and its native attribute factory, so shared reference balancing remains synchronized. Hofund uses Diamond sword properties.

The three Better Combat profiles are `bettercombat:hammer`, `bettercombat:mace` and `bettercombat:sword`, with matching RPG weapon-type tags. Paladins great-hammer/mace tags are extended without replacing other entries. Stormbreaker is two-handed; its native Ground Slam is separate from the existing custom Wakanda Ground Slam selector ability. Explicit native Spell Engine weapon containers supply all three weapon skills.

Stormbreaker's handle grows from 12 to 20 model units; the head moves upward by eight units without changing its texture coordinates. Both left/right held yaw transforms are reversed. Held scale and position are adjusted for both first-person and third-person contexts because Punchy can use the third-person held model during its first-person pass. Inventory, ground and fixed display contexts keep their previous transforms.

## Implementation

The same-filename Bifrost replacement preserves the previous claim fix, recipes and recipe-book discovery, marker occlusion, transit visuals/sounds, ownership and all combat actions. Only `ModItems.class`, the model and dependency metadata are replaced; small weapon-profile/tag resources are added. Paladins 3.1.2+ is now an explicit required dependency because it supplies the referenced equipment config and weapons. The pack already includes it.

NeoForge binds item components during resource reload. Reading another item's components at this initialization stage is invalid, so the repair uses the same `Weapon.attributesFrom` factory and equipment config as the reference items. No new tick handler, process or entity is added.

## Build and checks

```powershell
python docs/patches/bifrost-weapon-alignment/build.py
python docs/patches/bifrost-weapon-alignment/run_client.py --baseline
python docs/patches/bifrost-weapon-alignment/run_client.py
python docs/patches/bifrost-weapon-alignment/verify.py --candidate --runtime
python docs/patches/bifrost-weapon-alignment/verify.py --runtime
```

The overlay uses the pinned current repaired archive under `tmp/bifrost-weapon-alignment/before-*`, not the historical expansion baseline. Do not rerun it over future changes without reviewing the input archive. The full expansion's source `ModItems.java` and dependency build list are synchronized; the approved final model is mirrored into its source resources.

The original archive fails the expected identity, profile, skill and speed checks. The final connected Minecraft 26.2 / NeoForge 26.2.0.90 client passes twelve checks: exact reference attribute components, deep combat-profile equality for each weapon, each expected skill, 0.8 Stormbreaker speed, two-handed behavior and retained unbreakability. It includes Better Combat 3.2.2, Punchy 2.8f, Player Animation Library 1.2.8 and the RPG modules 3.1.2. `candidate-results.txt` and `runtime-tested-sha256.txt` pin the tested build. The screenshot review covers the main-hand first-person model; it is not a multiplayer visual or every-animation comparison.

Previous active/fixed copies are archived under ignored `mod fixing/retired packs/bifrost-weapon-alignment-2026-10-24/`. The final archive is installed in `mods/` and `mod fixing/1 fixed packs/`. No personal Minecraft installation, external server or world was modified. No commit or push was requested. The handbook weapon paragraph and PDF are rebuilt; the Bifrost page was visually checked.

# Mutilate and Prepared Counter - 24 October 2026

Same-filename Rogues 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5 overlays for Minecraft26.2 / NeoForge26.2.0.90 / Java25 bytecode. Builds pin the latest active pack, retaining the previous tier I/II revision and entity lifecycle repairs.

- **Mutilate:** 100% combined dual-melee power per enemy instead of 200%. The native melee attack's additional damage bonus becomes zero. Its 12s base cooldown, Improved 10s, 160-degree sweep, native attack timing, Crimson healing and Bloody Strikes remain unchanged.
- **Prepared Counter:** evading an eligible enemy attack readies one basic melee hit. That hit adds 25% of physical-melee power before defenses/PvP retention. With 20 melee power and an otherwise 20-damage basic hit, it deals 25; the following hit returns to 20. No immediate retaliation remains. Base cooldown is 2s, reduced by haste using the physical-melee school.

Readiness is a separate beneficial native status with the Prepared Counter skill icon. It has no damage attribute modifier, never stacks, and remains until used or dispelled. The negative native duration sentinel is encoded as -0.05 seconds to produce exactly -1 tick (infinite); the native regression verifies `isInfiniteDuration`, avoiding the engine's ordinary seconds-to-ticks conversion turning -1 seconds into -20 ticks.

A low-priority incoming-damage handler consumes readiness only for a permitted, positive direct player basic-melee hit. Active melee skills and indirect spell sources are excluded. Party/owned-pet protection and canceled incoming hits remain intact. Spell attributes do not increase, so Mutilate and other spell impacts cannot borrow or consume the charge. The added damage is a flat quarter of melee power, rather than multiplying enchantment/critical portions of an existing hit. A blocked basic hit may spend the charge; misses and protected hits do not. The normal defenses and 30% PvP retention still apply afterward.

No tick listener, delayed task or static entity map is introduced. A native saved effect owns readiness and cleanup. The effect is added through the existing configuration registration before normal registration completes, with an empty attribute configuration so stale generated settings cannot add a global damage boost. Matching client/server builds are required for the new effect registry entry. Existing effect, spell and weapon IDs remain valid.

## Verification and rebuild

```powershell
python docs/patches/rogue-counter/build.py
python docs/patches/rogue-counter/run_probe.py
python docs/patches/rogue-counter/run_client.py
# Wait for tmp/rogue-counter/client/tooltip-result.txt.
python docs/patches/rogue-counter/verify.py --candidate
```

The prior active build failed the new Mutilate damage assertion: 80 instead of 40 with 40 dual power. Final candidate: **1311 dedicated Warrior/shield/Rogue/party checks** and **43 native client tooltip checks** passed. Explicit counter checks confirm no retaliation, 2s cooldown and haste, infinite one-charge readiness, unchanged power attributes, Mutilate exclusion, actual `Player.attack` 20-to-25 damage, charge consumption, normal second hit, cooldown rearming, nonstacking readiness, and pet protection. Existing Vanish, Cheat Death, Crimson and Bleed regressions remain included. Archive checks preserve every original class file and prior behavior patch; only exact JSON resources, one copied effect icon, two new classes and a mixin registration change.

Repository active and fixed-pack JARs match tested hashes. Previous archives are retained under ignored `retired packs/rogue-counter-2026-10-24/`. Tooltips, root README, Rogue reference and handbook are synchronized. The 77-page PDF was rebuilt: only page17 differs from the preceding PDF, all pages were rendered/contact-sheet reviewed, and page17 was reviewed at full size with unchanged pagination. No personal installation, external server or world is changed. No commit or push requested.

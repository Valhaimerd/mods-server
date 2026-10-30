# Capped accessory RPG balance - 30 October 2026

Same-filename replacements for Nameless Trinkets 1.2.4+26.2.1 and Artifacts 15.1.3+26.2.1, Minecraft 26.2 / NeoForge 26.2.0.90. Both active JARs and fixed-pack copies are updated. Previous installed bytes remain in ignored retired storage. Personal Minecraft profiles, worlds and the separate desktop server were not modified.

## Player changes

[All 87 wearable decisions](inventory.md) cover 42 available Nameless Trinkets and 45 wearable Artifacts; 32 have balance adjustments. The 12 removed relics remain inert and unobtainable. Six Nameless supporting items, three Artifacts handheld/food items and the Mimic egg retain their roles.

Moon Stone's gravity reduction is now **-0.02625**, exactly **3/8 of the previous -0.07**. Ordinary gravity becomes **0.05375** instead of 0.01. It retains fall protection, and already-equipped legacy modifiers migrate without requiring removal. Fragile Cloud remains a descent-only utility.

Missing Page no longer damages a fraction of a boss's maximum HP. Retaliation uses modest wearer-power scaling, explicit caps and fixed intervals. Vampire Blood kill healing uses wearer HP instead of victim HP; its equipped-time removal lock and sunlight drawback remain. Vampiric Glove heals 5% of actual melee health damage, capped at 2 base HP once per second. Healing-received modifiers remain native; caps are before those modifiers. Size penalties and Weakness II remain.

## Character-scaling follow-up

| Curio | Current scaling |
| --- | --- |
| Titan's Mark | +15% baseline maximum HP, capped +10 HP |
| Crystal Heart | +10% baseline maximum HP, capped +8 HP |
| Ghast Eye | +7.5% baseline maximum HP, capped +6 HP; existing kill regeneration retained |
| Power Glove | +5% baseline melee damage, capped +2 damage |
| Woundbearer | Stores 1% damage taken; capacity10% baseline melee damage, capped +4 damage |
| Chorus Totem | Restores25% maximum HP, capped16 HP; native consumption/teleport retained |
| Thorn Pendant | Retaliates for1 damage plus10% armor, capped4; native50% chance/4s interval |
| Tick | Once per second in8 blocks:1 damage plus10% offensive power, capped6; fixed wearer cost once per successful pulse |

Health scaling excludes all three accessory health modifiers and Health Boost, so these items cannot amplify each other or inherit the old Titan Health Boost. Melee scaling excludes Power Glove and Woundbearer. Outside attributes are reconstructed without temporarily removing modifiers; effective caps hold under base and total multipliers. Repeated equip/refresh never heals. Unequip/disable removes owned bonuses and clamps current HP only if capacity falls. Old short Health Boost expires naturally; unrelated effects are not stripped.

Tick retains the original non-player, maximum HP over50 and current HP above half eligibility. Damage uses wearer power instead of victim HP, remains attributed to the wearer, protects party/pets and respects the pack's normal damage event. Its5%-max-HP health cost or low-health hunger exhaustion applies once per successful pulse rather than once per target. Its burning-only removal restriction remains. Wooden Stick's hit-negation identity remains unchanged.

Exact mechanics metadata ships as `accessory-character-scaling.json` in both JARs. Old flat config fields remain for native component/save compatibility; the relevant callbacks/tickers replace their effective amounts.

## Mechanics

- Offensive proc power is the highest current melee, ranged, Arcane, Fire, Frost or Lightning attribute, never the sum. Healing Power is excluded. Optional attributes are registry lookups; Better Party is optional and queried through its server API when present.
- Proc damage is attributed to the wearer and uses normal defenses and the existing pack player-damage event. The accessory patch does not change the 35% PvP / 80% PvE policy or apply another blanket multiplier.
- A shared `modpack:accessory_damage` tag prevents cross-mod retaliation and life-steal chains. Party players, owned pets and scoreboard allies are protected. Creeper Sense intentionally retains self-risk and uses an explosion-tagged source with no terrain destruction.
- Fixed wearer/effect timers prevent swapping/relogging to refresh procs. They supplement native slot cooldowns. Haste does not reduce them. Do not confuse this with class skill cooldowns.
- Eclipse wolves deal capped damage even when their attack attribute is buffed. Sessions expire after 5s and clear on unequip, death, logout or dimension change. No trailing summoned wolf persists beyond expiry.
- Legacy Moon Stone/Ghast Eye/Vampire modifiers update to policy values. Legacy Woundbearer and Sigil stored bonuses are clamped. Health capacity is not automatically healed on equip/refresh.
- Fractured Nullstone recognizes `c:is_magic` as well as vanilla magic sources, including RPG school damage. Steadfast Spikes retains the restored Feet slot.

## Sources and rebuild

The existing [Nameless pipeline](../nameless-balance/README.md) and [Artifacts pipeline](../artifacts-balance/README.md) retain upstream resources and metadata, with focused replacements, English tooltip overrides and added damage resources. Numeric policies are embedded in the JARs and enforced against stale settings. `targets.json` records concise intended details; `inventory.json`/`inventory.md` include retained items. Local owner references mirror the tracked effect references.

Nameless build now uses a javac argument file to avoid Windows command-length limits. Artifacts compilation of native `ArtifactHooks` also needs the embedded ExpandAbility JAR and JetBrains annotations in its development classpath; these are existing development/runtime components, not new pack dependencies.

## Verification

The original installed build failed the new Moon Stone gravity assertion. Regression failures also reproduced old equipped gravity, legacy Woundbearer storage and a buffed phantom wolf exceeding its cap before the corresponding fixes.

- **171 checks passed** in an isolated server with Nameless Trinkets, Artifacts, Curios 16.0.0+26.2 and a test-only probe.
- **181 checks passed** with Better Party 1.1.7, Spell Power 1.6.1 and Ranged Weapon API 4.0.0 added. This repeats the base checks and adds integrations; it is not 352 unique tests.
- Checks additionally cover character scaling without self/cross-item feedback, effective caps under mixed outside multipliers, no refresh healing, disable cleanup, scaled Chorus/Thorn and Tick boss-independent damage/cost/attribution. The baseline fixed Titan health assertion failed before the fix. Checks cover policy after stale config, gravity/removal, bounded boss-independent damage, exact cooldown boundaries, fresh-stack bypass, serialization of wearer timers, damage attribution and shared tags, pet/team/real-party protection, life steal with overkill/allies/procs, regeneration refresh, legacy stored caps, phantom damage/unequip and persistent Creeper cooldown.
- A focused independent source/archive review identified the Eclipse lifecycle/cap and Creeper persistence gaps; they were fixed and included in the passing regression suite.
- `python docs/patches/accessory-rpg-balance/verify.py` verifies active/fixed hashes, ZIP CRC/duplicate paths, shipped policies/tooltips, 87 wearable decisions, 12 retired relics, Feet slot and result records. Probe classes are not shipped.

The rebuilt handbook has 79 pages with all 51 contents links/page numbers verified. All pages were reviewed in rendered contact sheets, with detailed inspection of changed page 24. Root and output copies match.

No new client visual or comparative class playtest is claimed. Tests exercise the modified mechanics and selected retained contracts, not every utility item in every environment. The existing overall PvP/PvE multiplier is preserved through player attribution; the minimal accessory server does not load that separate mod.

## Installed hashes

- `Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar`: `294856632041f44dfd1b851a4e04717601afcb5e44e5b6086d1b7f9da3b1db31`.
- `artifacts-neoforge-15.1.3+26.2.1.jar`: `4288b5b14ce810e38e89fc5016cf4b792c8be376c2e33cce22028944f6227f62`.

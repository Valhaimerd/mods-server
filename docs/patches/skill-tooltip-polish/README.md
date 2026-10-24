# Skill names and concise tooltips

24 October 2026. Replaces the earlier description passes with shorter effect text and correct tree titles. The active JAR filenames and saved skill IDs remain unchanged.

## Cause and changes

The skill tree uses `skill.skill_tree_rpgs.<node>.title`, independently of the `spell.<namespace>.<spell>.name` used by scrolls. Earlier renames changed only the latter. The build synchronizes titles in both class and weapon categories from their corresponding current spell names. Nineteen title strings change, including Endured Vitality, Warm Up, Momentum, Tenacity, Strong Throw, Head Throw, Bigger Net, Multiple Nets, Improved Wild Charge, Improved Berserker, Brutality, Lethality, Sentinel and renamed Rogue/Archer nodes.

Supercharge already has a **1.25 normal-arrow damage multiplier**, guaranteed activation and an eight-second haste-affected cooldown. Its description incorrectly said 100%. It now says **125%**; combat behavior is preserved.

Descriptions use `%`, short effect durations and direct descriptions of benefits. Calculation/rounding instructions, redundant base cooldown/haste explanations and repeated activation clauses are removed. Cooldown-reduction upgrades still explain their upgrade benefit. Buff timers, risks and meaningful exceptions remain. The exact source wording is in `wording.json`; all 196 Archer/Rogue/Warrior/Arcane/Fire/Frost spell and node descriptions pass the formatting scan.

Thirty-six class passive activations receive precise yellow labels, such as **On direct damage taken**, **On combat roll**, **At 1–30% HP**, and school-specific hits with their actual chance. A client-only Spell Engine tooltip injection replaces the generic trigger line or supplies the missing health trigger line for polled passives. Native range, current haste-adjusted cooldown, casting and cost lines remain in their original section. Tooltip visibility is enabled for these passives; no synthetic gameplay trigger is added. Other spells and untranslated tooltip paths retain native behavior.

## Packaging and verification

The build snapshots current Archers, Wizards, Rogues, Skill Tree and Spell Engine archives under ignored `tmp/skill-tooltip-polish/before/`, preserving earlier behavior patches. Its only nonlanguage changes are `tooltip.show_activation` visibility flags and one client-only Mixin plus its registration. Every combat field and pre-existing class file remains byte-for-byte unchanged. All five outputs use the existing active filenames and have matching fixed copies. Prior active/fixed archives are backed up under ignored `mod fixing/retired packs/skill-tooltip-polish-2026-10-24/`.

```powershell
python docs/patches/skill-tooltip-polish/build.py
python docs/patches/skill-tooltip-polish/run_client.py
# Wait for tmp/skill-tooltip-polish/client/tooltip-result.txt.
python docs/patches/skill-tooltip-polish/run_server.py
python docs/patches/skill-tooltip-polish/verify.py tmp/skill-tooltip-polish/built
# Install matching verified outputs, then:
python docs/patches/skill-tooltip-polish/verify.py
```

The isolated Minecraft client passed **43 native tooltip checks**: separate tree titles, formatted 125% Supercharge and native 1.25 multiplier, precise health/direct-hit/roll/school triggers, exactly one activation line and at most one native cooldown line. The dedicated NeoForge fixture passed **946 Warrior/shield/party checks**, confirming the client-only addition does not break server loading. Static archive checks prove unchanged combat resources and synchronization across both tree categories. No manual skill-tree hover layout review is claimed.

Raw class references are refreshed from installed archives. Existing handbook mechanics are unchanged, so its PDF does not require rebuilding for this display-only fix. Personal Minecraft, external servers and worlds remain untouched. No GitHub commit or push requested.

Latest Warrior overlay: [minimum-charge throws, health pulses, death cleanup and icon fixes](../warrior-followup/README.md). The current hashes are recorded there; earlier build outputs remain historical.

# Priest tier I/II rework - 26 October 2026

Same-version overlays for Paladins & Priests 3.1.2, Skill Tree 1.6.1, Spell Engine 1.10.5 and Arsenal 1.5.1. Both sides need matching updated JARs and a restart. Existing spell IDs, books, scroll tags and saved node IDs remain valid. Current input/output hashes and exact archive differences are recorded in `artifacts.json`.

## Skills

| Skill | Installed behavior |
|---|---|
| Holy Light | Tier I. 25 deliveries over 5s, 15% Healing Power healing / 30% damage per delivery, knockback 1.0. Smooth cooldown 12-24s by completion before haste. Improved sets healing/damage to 20%/35%; Graceful Light sets healing to 30% and knockback 2.0; Searing Light sets damage to 50% and ignites. No extra-delivery or cooldown-reduction upgrade remains. |
| Penance | Moved to tier I with its complete upgrade branch. Ten bolts over 2.5s, at 0.25s intervals before haste, 26-block flight. One five-block explosion on ally, enemy or block contact, 25% Healing Power damage per enemy, no extra direct hit or distance falloff, knockback 0.5. Smooth 8-14s cooldown. Each blast adds 1 absorption HP to each eligible ally, ten-stack / 10 HP capacity, lasting 3s refreshed. Improved makes damage 30% and shield duration 4s. Hysteria gives 1% attack speed/ranged haste/spell haste per stack, ten stacks, 4s refreshed. Chastise adds 1% damage taken per stack, ten stacks, 4s refreshed. |
| Circle of Healing | Tier II. Existing 0.5s cast and eight-block radius; 16s cooldown, 50% Healing Power healing, 60% with Improved. Mass Dispel removes up to two harmful effects while preserving Trial Omen. Sanctuary grants Regeneration for 5s, level 1 plus rounded 6% Healing Power (P27 gives III). |
| Ascension | Renames Levitate and moves its complete branch to tier II. Stationary 2s channel; four pulses at 0.5/1/1.5/2s before haste. Fixed 16s cooldown. Levitation 6s; eight-block hostile knockback at 2.0 on pulses one-three, 5.0 on pulse four. Allies in eight blocks gain 2% melee/ranged/offensive magic power per pulse, four stacks / 8%, 6s refreshed. Improved extends levitation and associated buffs to 8s. Uplift lifts allies within three blocks. Serenity grants absorption per pulse equal to 1 plus rounded 10% Healing Power, up to four stacks; 6s/8s duration. Damage-buff icon reuses Levitate artwork. |

The two offensive channels normalize the native channel multiplier so requested coefficients apply **per delivery**, rather than being multiplied again by seconds between deliveries. All other channels preserve their current behavior. Circle's Improved node changes only its healing coefficient; Sanctuary's regeneration scales from caster Healing Power rather than an unrelated upgraded spell-power multiplier.

Both class and weapon tree definition branches swap as complete units, including icons, title references, descriptions and spell grants. Resource tiers determine the new binding requirements; Priest scroll membership tags remain unchanged. Internal Penance upgrade IDs retain `tier_3`, and Ascension upgrade IDs retain `tier_2`; the exporter maps these saved IDs to their current placement. Tier-III skills and all six Priest passives retain their prior values.

## Absorption and party safety

Penance and Serenity add only HP earned by new stacks. They do not initialize the shield again from total stacks. At the cap, further applications refresh duration without restoring damage consumed. The user's example is covered: 6 HP minus 4 damage plus two bolts gives 4 HP.

The original Priest absorption, Wizard wards, Arsenal shield and Tenacity handlers now add only the positive difference in effect levels. Duration refreshes do not refill consumed HP; removal and expiry clear tracking for genuinely new grants. Gains preserve existing absorption from other sources, within the native summed maximum-absorption capacity. No vanilla golden-apple or potion behavior is modified by this custom-effect patch.

Penance's shared radial recipient path uses the installed Better Party owner/principal policy for each target: allies receive shields/support without damage; enemies receive damage/debuffs without shields. Caster, own pets and party pets remain protected. Ascension uses that same policy for its support and hostile knockback. Native spell damage attribution and the separate pack PvP/PvE multiplier remain in place.

## Packaging and checks

```powershell
python docs/patches/priest-rework/build.py
python docs/patches/priest-rework/run_probe.py
python docs/patches/priest-rework/verify.py --installed
python docs/patches/priest-reference/export.py
```

Pinned local snapshots: ignored `tmp/priest-rework/before/`; candidates: `built/`; isolated server: `server/`. Predecessors are retained under ignored `mod fixing/retired packs/priest-rework-20261026/`. Narrow ASM call-site changes preserve unrelated current patches instead of recompiling historical classes. `PriestRules` supplies the new mechanics. Matching active/fixed archives are installed without renaming them.

**481 isolated Priest/party checks passed, along with 5,235 archive/scope checks.** Runtime evidence is in `runtime-results.txt` and `runtime-checks.txt`; the isolated NeoForge 26.2.0.90 server uses Java-25 classes with `-Xverify:all`. Checks cover smooth cooldown samples, end-of-interval pulse timing, all Holy Light branches, native projectile impact, Penance shields and caps, Hysteria/Chastise, Ascension and Serenity, audited shield refresh/removal, Circle healing, rounded regeneration, exact two-effect dispel, and real Better Party players/pets with friendly fire on/off. The fixture excludes the separate global damage multiplier to verify raw coefficients. `verify.py` also checks archive inventory, unchanged entries, branch swaps, resource tiers and description scope. No connected-client animation/visual playtest is claimed.

Descriptions follow the existing brief-effect guide: percentages, useful effect timing, no unresolved tokens/formulas or repeated base cooldown/haste prose. The complete owner reference and handbook/PDF are synchronized. No commit or GitHub push is requested.

Latest overlay: [Priest passive tiers I/II](../priest-passives/README.md). Its current hashes supersede this revision for Skill Tree and Spell Engine; do not install this historical builder output over the newer passives.

Latest installed follow-up: [Holy Staff and Priest tuning](../priest-tuning/README.md). Historical source/output hashes above are superseded for Paladins, Skill Tree and Spell Engine; do not reinstall earlier builder outputs over the current overlay.

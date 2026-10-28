# Vanguard and Guardian

Current passive balance is superseded by the [Paladin revamp](../paladin-revamp/README.md). The results below document the earlier armor/healing arrangement.

Installed28 October2026: Minecraft26.2, NeoForge26.2.0.90, Java25. Only the same-name Skill Tree JAR changes; the previous Paladin tier-I active rework and all tier-II data remain.

| Detail | Vanguard | Guardian |
| --- | --- | --- |
| Basic equipment condition |Shield equipped |Empty offhand |
| Basic bonus |+5% base armor |+5% Healing Power |
| Stack trigger |Successful block |Successful melee/arrow/damaging spell impact |
| Stack bonus |+5% base armor |+5% Healing Power |
| Maximum |4 stacks, +20%; +25% with basic condition |4 stacks, +20%; +25% with basic condition |
| Duration |5s, refreshed |5s, refreshed |
| Cooldown |1s, affected by haste |None |

Basic and stacked bonuses add to the same base attribute. Equipment conditions govern the basic5% only; earned stacks expire normally after equipment changes. Removing the passive or dying clears its bonuses. Existing passive/effect IDs are retained, while English skill names, node titles, effect names and descriptions are updated. Guardian no longer grants attack damage or requires critical hits.

The small server helper detects Minecraft's BLOCKS_ATTACKS component or the legacy shield_block item ability across native equipment slots. It recognizes installed Paladin and Arsenal shields and non-shield items advertising these capabilities. This does not promise compatibility with a third-party Curios-only parry mechanism that advertises neither capability. Existing successful-block and impact events drive stacks; no new attack/damage listener or cooldown system is introduced. Purge Heal and Blessed waves qualify for Guardian. Old generated effect config attribute lists are replaced, and old longer status durations/higher stack counts are capped.

## Build and verification

`python docs/patches/paladin-passives/build.py` compiles the helper and one ASM callsite patch against the pinned ignored tmp/paladin-passives/before input, preserving every other archive entry. artifacts.json lists exact hashes and changes. Config helper runs before SkillEffects.register; it preserves all other effect configurations and prior hooks.

Native isolated fresh and legacy-effect-config runs each passed35 checks. Coverage includes vanilla/main-hand/modded shields, conditional removal, native blocks and cooldown,4-stack cap/5s refresh, noncritical melee/Purge/wave triggers, no old attack damage, haste, removal/death and exact healing/armor values. Actual client passed15 checks: real allocated passive sources, native/modded shields, equipment changes,25% total Guardian power, synchronized renamed status, respec cleanup and native tree display. Evidence and screenshots are in client/. Personal saves/profiles and the separate desktop server were untouched. Initial build verification caught an ASM argument-load omission before installation; the final build passes native server loading.

`python docs/patches/paladin-passives/verify.py --installed` checks tested hashes, preserved tier-II resources and installed/fixed copies. Final JAR is in mods and fixed packs, with the prior version retained in ignored retired packs. Matching client/server restart is required. No push requested; no workers used.

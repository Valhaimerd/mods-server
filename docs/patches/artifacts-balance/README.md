# Artifacts pack balance — Minecraft 26.2

Same-filename replacement for `mods/artifacts-neoforge-15.1.3+26.2.1.jar`.
Upstream: [Artifacts](https://github.com/ochotonida/artifacts), MIT, commit
`803fd1aea1cece1362d40ba626c64cc1c3cdabb7` (26.2 / 15.1.3).
The upstream license is preserved in `LICENSE.upstream` and in the archive.

## Character-scaling follow-up - 30 October 2026

[Character scaling](../accessory-rpg-balance/character-scaling.json) now supersedes earlier flat HP/attack values for Titan, Ghast Eye, Crystal Heart, Power Glove and Woundbearer. Chorus Totem healing, Thorn retaliation and Tick use the revised capped behavior in the [complete inventory](../accessory-rpg-balance/inventory.md). Legacy flat config fields remain for component/save compatibility; native item callbacks/tickers compute the effective scaled values. No extra dependencies are added.

## Effective pack values

The 30 October capped RPG revision replaces the earlier values below. [All 45 wearable decisions](../accessory-rpg-balance/inventory.md) and [the combined verification record](../accessory-rpg-balance/README.md) cover the complete pass. Pack-enforced numeric values remain in `balance.json`; stale config reads cannot override them.

| Artifact | Current effect |
| --- | --- |
| Vampiric Glove | Basic melee hits heal 5% of actual health damage, capped at 2 HP, once per 1s. Overkill and absorption do not increase healing; allies and accessory retaliation cannot trigger it. Healing-received modifiers remain native. |
| Feral Claws | +10% base attack speed. |
| Power Glove | Adds 5% of melee power before Power Glove and Woundbearer, capped at +2 effective attack damage. Recalculates as outside equipment and buffs change. |
| Crystal Heart | Adds 10% of maximum HP before the three scaling health accessories and Health Boost, capped at +8 effective HP. No healing on equip or refresh; cannot amplify the other scaling health curios. |
| Cross Necklace | Adds 10 invulnerability ticks (0.5s) after taking damage; native cooldown 5s. |
| Running Shoes | +15% base movement speed while sprinting; retains +0.5 block step height. |
| Steadfast Spikes | +30% knockback resistance and +1 slip resistance. Fits the restored Feet slot, separate from Nameless Shoes. |
| Fire Gauntlet | +4 seconds attack-burning duration. |
| Flame Pendant | 40% chance to ignite an eligible attacker for 4s, granting the wearer Fire Resistance I for 4s. 4s cooldown; allies and accessory retaliation are excluded. |
| Shock Pendant | 25% chance to retaliate against an eligible attacker under open sky. Visual lightning accompanies 2 damage plus 10% of strongest offensive power, capped at 6. 6s cooldown. Retains lightning immunity; no collateral lightning damage or fire. |
| Withered Bracelet | Melee hits have a 30% chance to apply Wither I for 4s. 4s cooldown shared by the wearer; excludes allies and accessory retaliation. |
| Thorn Pendant | 50% chance to retaliate for 1 damage plus 10% wearer armor, capped at 4 damage. Fixed 4s interval; excludes allies and cannot trigger retaliation chains. |

The 75% shrinking charm, refreshing Weakness II, native toggle handling, restored Feet slot and other utility items remain. Proc damage uses player-attributed nonrecursive damage sources; friendly players and pets are protected. Native per-stack cooldowns are supplemented by wearer timers for retaliation and life steal. No extra dependency is introduced.

## Implementation and distribution

`ConfigValue` enforces the numeric policy on defaults and reads. Existing item definitions and effects use native data components. Replacements for retaliation, the attack-effect gate and `ArtifactHooks.absorbDamage` bound the new procs; unrelated hooks retain upstream behavior. `AccessoryCombat` accesses optional RPG attributes through registry lookup and Better Party through its server API when installed. Both mods contribute to a shared accessory-damage tag to prevent cross-mod recursion while remaining independently loadable.

## Rebuild

1. Preserve the original archive as `tmp/artifacts-balance/upstream.jar`, SHA-256
   `3be92116769cbba850acfc0e03bd0f3688c459e2b17144d97da36679fe35c104`.
2. Supply a UTF-8 file containing a semicolon-separated compilation classpath:
   NeoForge-patched Minecraft 26.2 first, upstream Artifacts, NeoForge/FML, Curios,
   and the libraries used by the upstream classes. This pass compiled against
   the cached NeoForge 26.2.0.88 development artifact, Java release 25.
3. Run `python docs/patches/artifacts-balance/build.py --classpath-file <file>`.
   Java 25+ and Python are required; `--java-bin` selects the JDK bin directory.
4. Verify the output under `tmp/artifacts-balance/`, then replace the active JAR
   under its existing filename. Do not ship test probes or intermediate classes.

The build verifies the upstream hash, replaces the compiled source classes and English tooltip entries, preserves other archive entries, and adds the numeric policy and damage/slot resources. Include the embedded ExpandAbility JAR and JetBrains annotations in the compilation classpath when compiling the native ArtifactHooks class.
Source edits must stay aligned with `balance.json`; the runtime probe checks each
effective/default value and each override after writing a stale value.

## Verification

Isolated dedicated server: **Minecraft 26.2 / NeoForge 26.2.0.90**, Artifacts
**15.1.3+26.2.1**, Curios **16.0.0+26.2**, Java **26**, local test mod only.
The unmodified archive failed on Cross Necklace's old cooldown. The replacement
passed **64 checks** covering all 18 values, stale-config writes, defaults,
unrelated configuration mutability, charm scale/speed component operation,
Weakness level/duration and active/disabled/cooldown ticking, and skull effect.

Probe source: `tests/ArtifactsProbe.java`. Test logs/results stay ignored under
`tmp/artifacts-balance/`. This is focused server verification, not a full client,
tooltip, combat-balance, or multiplayer playtest. Retained values use upstream
mechanics; no claim is made that melee/ranged/caster outcomes are now equal.

## Shrinking and missing-slot repair - 21 October

Charm of Shrinking uses a -0.25 total-scale modifier (**75% normal size**) and refreshing **Weakness II**. Its 1.2x movement speed and native 60-second refresh/cleanup remain. Stale config cannot restore half size.

The installed upstream archive omitted Artifacts player slot declarations. Steadfast Spikes belongs to Feet, but the pack assigned only Nameless Trinkets? separate Shoes slot. Restored resources assign Head, Necklace, Hands, Belt and Feet with one base slot each, using normal Curios merging. Shared slots remain shared; Feet is the missing type. This changes the current pack from 20 to 21 functional slots. Charm already exists through Nameless Trinkets, Starcatcher and Charm of Undying Reborn; Feet sorts before Charm, moving it into the bottom-right cell of the supplied three-column layout. No existing slots or items are removed. The build now also includes the six JSON files under `resources/`.

Latest isolated server verification: **67 checks passed**, including the player Feet slot, Steadfast Spikes accepted in Feet and rejected from Necklace, 75% scale modifier, Weakness II and existing balance/toggle behavior. Runtime uses NeoForge 26.2.0.90 and Curios 16.0.0+26.2. The handbook remains 74 pages; visual review covers all pages and detailed review of the changed equipment entries. No new client UI playtest was performed. Hashes and slot totals are in `shrinking-slots-verification.json`.

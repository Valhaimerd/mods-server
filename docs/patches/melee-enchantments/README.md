# Melee enchantment policy and Life Steal balance

Applied 23 October 2026 for Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25.
This overlays the preceding armor and ranged policies without replacing their
behavior. The owner's "frost and fire" instruction refers to Frost Aspect and
Fire Aspect in this melee pass.

## Availability and compatibility

Sweeping Edge, Penetrating Strike, Spiteful and Decay are added to
`penchant:disabled`. All eleven earlier entries remain. Disabled enchantments
are rejected by normal application, omitted from random acquisition and
creative books, and included in the existing recipe-viewer hiding tag.
Definitions and old stored items remain for save compatibility; legacy gear
and existing offers are not purged.

| Enchantment | Rule |
| --- | --- |
| Sharpness | Incompatible with Bane of Arthropods, Smite, Aerial's Bane and Illagers Bane |
| Those four specialist damage enchantments | Compatible with each other: all six pairs |
| Ensnaring | Incompatible with Sharpness; compatible with all four specialists |
| Discharge | Incompatible with Frost Aspect and Fire Aspect |

Registry IDs distinguish `nova_structures:aerials_bane` and
`nova_structures:illagers_bane` from the vanilla damage enchantments.
The rules use Universal Enchants' installed exclusive/inclusive tag hooks.
Explicit removals from inclusive tags keep the Sharpness exclusions effective
even when its optional Compatible Damage Enchantments pack is enabled.
Other existing restrictions remain.

## Life Steal

| Level | Damage dealt before healing | Damage-stat units | Healing |
| --- | ---: | ---: | ---: |
| I | 20 HP | 200 | 3 HP |
| II | 16 HP | 160 | 3 HP |
| III | 12 HP | 120 | 3 HP |

The counter still resets after healing, including excess damage, and clears
when the player's main hand does not satisfy the existing enchantment
predicate. Healing caps at the player's current maximum health.

The original function gave Instant Health I, which supplies 4 HP. A small
native entry point in the existing Life Steal JAR now registers an internal
operator-only healing command that calls `ServerPlayer.heal(3.0F)`. The
threshold function invokes it, then resets its existing counter. This uses
normal healing and its cap, without healing 4 HP then inflicting damage.
The same declared mod ID, metadata, filename and acquisition resources remain;
no extra mod JAR or required dependency is added.

## Rebuild and archive proof

From the repository root:

```powershell
python docs/patches/melee-enchantments/build.py
python docs/patches/melee-enchantments/verify.py
python docs/patches/ranged-enchantments/verify.py
python docs/patches/armor-enchantments/verify.py
```

The builder verifies the pre-change hashes in `upstream.json`, compiles the
single native helper with Java 25 using `tmp/bows-balance/classpath.txt`, and
writes candidates under `tmp/melee-enchantments/`. It never installs them
automatically. After native verification, final files were copied under their
existing names to `mods/` and `mod fixing/1 fixed packs/`.

- Penchant changes/adds 16 policy resources, retaining its earlier balance and
  progression changes.
- Life Steal changes/adds three entries: its threshold function, healing
  function and one native helper class. All other entries are unchanged.

Inputs remain in ignored
`mod fixing/retired packs/melee-enchantments-2026-10-23/`.
`artifacts.json` records final hashes. The prior policy verifiers follow the
hash-pinned cumulative build chain and check that earlier rules remain.

## Native verification

`tests/prepare.py` packages a temporary test mod and creates an isolated server
under `tmp/melee-enchantments/server/`. It extends the preceding ranged fixture
with Life Steal, Better Archeology and When Dungeons Arise plus their required
dependencies. Runtime logs record exact loaded versions. It disables the
optional No Anvil Books module so anvil rejection is not a trivial side effect.

```powershell
python docs/patches/melee-enchantments/tests/prepare.py --candidate tmp/melee-enchantments/penchant-neoforge-26.2.0-1.6-2.5x-progress.jar --candidate tmp/melee-enchantments/life-steal-enchantment-v1.1.0.jar
```

The fixture needs a `libraries` junction to the existing local NeoForge
26.2.0.90 libraries. Launch from its server directory with Java 25+:

```powershell
java -Xmx2G '@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt' nogui
```

For the optional-pack regression, the fixture world enables
`mod/universalenchants:data/universalenchants/datapacks/compatible_damage_enchantments`.
The relevant native policy checks failed before the inclusive-tag removals
and passed afterward.

| Check | Observed result |
| --- | --- |
| Original Life Steal at tier-I threshold | Failed the new expectation: health rose from 10 to 14, rather than 13 |
| Native policy and healing fixture | 984 checks passed |
| Optional Compatible Damage pack before guard | Failed Sharpness / Bane of Arthropods incompatibility |
| Final candidate with optional pack enabled | 984 checks passed |

Coverage includes all fifteen hidden IDs, explicit random-loot samples,
creative books, both native and actual-anvil orders for rejected combinations,
all six accepted Bane pairs and all four Ensnaring/specialist pairs in actual
anvils, plus the prior armor/ranged and Zero Drop critical regressions.

Healing checks use the real threshold/healing functions at each tier, verify
no heal just below the boundary, exactly 3 HP at the boundary, counter reset,
maximum-health clamping and no heal without the enchantment. Tests wait for
load functions and process effect ticks so the original delayed potion heal
cannot be mistaken for no healing. The internal heal command is also checked
as inaccessible to a source with no operator permissions.

Evidence: `tmp/melee-enchantments/baseline-heal.log`, `candidate.log`,
`compatible-damage-before.log`, `compatible-damage-final.log` and
`server/result.txt`. Test classes are excluded from production archives.
This is an isolated native fixture, not a full-pack gameplay session or a
manual test of every existing world and offer. Personal Minecraft folders,
the separate desktop server and player worlds were untouched.

The owner reference and handbook describe the applied values. PDF review
evidence is under `tmp/melee-enchantments/pdf-review/` and `pdf-report.json`.
The final handbook has 75 pages and 461 valid internal links. All page contact
sheets and the enchantment policy page were visually reviewed; table-of-contents
page numbers match their destinations.

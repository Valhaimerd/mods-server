# Ranged enchantment policy and Zero Drop critical repair

Applied 23 October 2026. Minecraft 26.2 / NeoForge 26.2.0.90 / Java 25.
The owner explicitly selected prevention of both vanilla and RPG critical hits
for Zero Drop. Tri-Shot cancelling Power is implemented as a same-bow
incompatibility, rather than silently changing Power's damage calculation.

## Availability and compatibility

Tense, Withering, Deflector, Bombard, Recycling and Ghasted are added to the
existing `penchant:disabled` tag. Its earlier five armor entries remain.
Penchant's installed hooks hide creative books, tag recipe-viewer hiding,
filter random acquisition and reject normal application. Existing stored
items and villager offers are not purged; direct administrator component
construction is outside this acquisition policy.

| Rule | Result |
| --- | --- |
| Flame / Frost / Spectral | Mutually exclusive on one weapon |
| Tri-Shot / Power | Mutually exclusive on one bow |
| Multishot / Might / Acceleration / Frenzy | All six pairs mutually exclusive |
| Piercing | No pairwise conflicts with other registered enchantments |
| Gravity / Frenzy | Incompatible |
| Gravity / Multishot, Might or Acceleration | Compatible |
| Zero Drop | No pairwise conflicts with other registered enchantments |

Might means `nova_structures:power`; ordinary Power means `minecraft:power`.
Other native restrictions remain, including unrelated Quick Charge conflicts.
As with vanilla compatibility rules, this does not sanitize pre-existing
forced combinations. The earlier held Might/Frenzy reconciliation remains.

The implementation uses Universal Enchants' existing per-enchantment
`exclusive_set/` and `inclusive_set/` tag hooks. Both sides of relevant
conflicts are considered, rather than only clearing Piercing's own definition.
The inclusions were derived from installed exclusion definitions/tags and
tested against every enchantment in the isolated registry. They do not promise
automatic compatibility with future mods that introduce additional exclusions.

## Zero Drop root cause and repair

CrossArms already sets the projectile's vanilla `crit` flag to false.
Critical Strike 1.0.5 independently rolls inside `CritLogic.modifyDamage` from
the owner's physical critical attributes, without consulting that flag.
Consequently, clearing the vanilla flag does not prevent RPG critical damage.

The regression fixture registers its player in the level so projectile NBT
updates can resolve the real owner; an unregistered fake player loses its
owner during `/data` reload and would produce a false negative.

The same-filename CrossArms replacement adds the optional
`ZeroDropCriticalMixin`, returning no RPG critical result only when the direct
damage-source entity is a Zero Drop arrow. A persistent
`CROSSARMS_NO_CRITICAL` entity tag retains suppression after the temporary
`ZERO_DROP` flight tag is removed. Legacy arrows still carrying `ZERO_DROP`
are also recognized. Ordinary ranged and melee criticals are unchanged.
`@Pseudo` keeps Critical Strike optional; no required dependency is added.

Source is maintained under
`../crossarms-firing/src/local/crossarms/mixin/ZeroDropCriticalMixin.java`,
and its function override and registration are included in the existing
CrossArms build sources. Frenzy damage, spread, heat, recoil, Infinity and
burst/reload implementations are preserved byte-for-byte by this overlay.

## Build and verification

From the repository root:

```powershell
python docs/patches/ranged-enchantments/build.py
python docs/patches/ranged-enchantments/verify.py
python docs/patches/armor-enchantments/verify.py
```

The builder compiles the single optional mixin with Java 25 using the existing
`tmp/bows-balance/classpath.txt`. It verifies the exact pre-change archive
hashes in `upstream.json` and writes candidates under `tmp/ranged-enchantments/`.
It does not install automatically. The tested candidates were installed under
their existing filenames in `mods/` and `mod fixing/1 fixed packs/`:

- Penchant: 21 policy resources changed/added; the previous armor policy and
  2.5x progression repair are retained.
- CrossArms: three entries changed/added: Zero Drop function, mixin registration
  and one compiled mixin class. All other entries are unchanged.

Inputs are backed up under ignored
`mod fixing/retired packs/ranged-enchantments-2026-10-23/`. `artifacts.json`
records final hashes. Test classes never enter production archives.

### Native dedicated-server results

`tests/prepare.py` builds an isolated test mod and copies only relevant installed
mods into `tmp/ranged-enchantments/server/`. The fixture uses Penchant,
Patchouli, Universal Enchants, Puzzles Lib, Dungeons and Taverns, Ender Zoology,
the individual enchantment providers, CrossArms, Expanded Crossbow Enchantings,
Ranged Weapon API, Arsenal and its spell/animation dependencies. Runtime logs
record exact versions. It uses fresh/isolated worlds and disables the optional
No Anvil Books module so anvil rejection cannot pass trivially.

```powershell
python docs/patches/ranged-enchantments/tests/prepare.py --candidate tmp/ranged-enchantments/penchant-neoforge-26.2.0-1.6-2.5x-progress.jar --candidate tmp/ranged-enchantments/crossarms-1.4.jar
```

The fixture needs a `libraries` junction to the local NeoForge 26.2.0.90
libraries, as used by `tmp/crossarms-firing/server/libraries`. Launch from
`tmp/ranged-enchantments/server/` with Java 25+:

```powershell
java -Xmx2G '@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt' nogui
```

The probe writes `result.txt` and stops the server.

| Evidence | Result |
| --- | --- |
| Baseline before policy | Failed: Tense not disabled |
| Critical regression before fix | Failed: Zero Drop dealt 18 HP instead of 6 HP with guaranteed RPG critical chance |
| Final with Critical Strike | **777 checks passed** |
| Final without optional Critical Strike | **741 checks passed** |
| Production archive checks | Hashes, exact changed-entry sets, prior armor policy and no test classes verified |

The final suite checks all eleven hidden IDs, forced-option loot samples,
creative books, all requested conflict pairs in both native/anvil orders,
the earlier armor rules, positive anvil controls, and Piercing/Zero Drop
compatibility in both orders against every other fixture registry entry.

For both a vanilla crossbow and Arsenal's heavy crossbow, controlled hits on a
zero-armor target measured 6 HP normally and 18 HP with guaranteed RPG criticals.
Zero Drop measured 6 HP in both cases after the fix. It also retained
suppression after removing the flight tag; ordinary ranged and melee RPG
critical controls passed. These controlled measurements isolate the repair,
not the final damage of a fully geared player.

Logs: `tmp/ranged-enchantments/baseline.log`, `zero-drop-red.log`, `final.log`
and `without-critical-strike.log`. No manual JEI-screen check, every loot-table
variant, full-pack gameplay session or every possible projectile combination
was tested. Personal Minecraft folders, the separate desktop server and player
worlds were untouched.

The handbook and owner balance reference describe the applied rules. PDF
rendering, contents numbers and internal-link results are recorded in
`tmp/ranged-enchantments/pdf-report.json` and `pdf-review/`.

Final PDF proof: 74 pages, 461 valid internal destinations, matching printed contents numbers and identical root/output copies. Only pages 23 and 26 differ from the preceding reviewed armor edition; both were visually checked without clipping.

## Registry retirement follow-up (2026-10-24)

The [enchantment retirement](../enchantment-retirement/README.md) supersedes the hidden modded enchantment and Life Steal policy recorded here. Its hashes identify the current active repacks; these original build checks remain historical. Vanilla Sweeping Edge stays hidden.

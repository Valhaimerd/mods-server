# Skill tree overhaul — 18 October 2026

Implements the approved [design](design.md) in Minecraft 26.2 / NeoForge 26.2.0.90. The working planning document remains under `mod fixing/Skill tree changes - planning.md`. These are same-version local repacks; install all three together on clients and servers:

- `adventurerskills-EN-1.3-26.2-neoforge-fixed.jar`: Adventurer tree data, descriptions, prerequisites, exclusions and XP calculations.
- `skill_tree-neoforge-1.6.1+26.2.jar`: RPG class/weapon power and Rogue speed nodes, plus XP calculations. Paladin definitions remain identical to the original.
- `orb_of_oblivion_puffish_compat-1.0.2.jar`: retains Orb reset support and adds the Companion attribute bridge, fixed spell critical percentage points and migration.

## Behavior and units

Offensive, Defensive and Companion alternatives exclude each other at their first path nodes. Utilities is unrestricted, loses its three specialties and replaces combat bonuses with utility stats. All Adventurer completion item gifts are removed. Magistrate boosts Arcane, Fire and Frost; Priest healing remains exclusive to the RPG changes requested, where ordinary healing-power nodes grant 3%. Ordinary Adventurer spell haste is 2% and spell critical chance adds two percentage points. The final Magistrate grants 5% power/haste and five critical points, without health or healing power.

The [data transformer](repack.py) records every ordinary node assignment. Author amounts remain where requested, with explicit exceptions for damage, attack speed, caster stats, pet stats and anvil reductions. Anvil nodes each multiply remaining cost by 0.8, so three leave 51.2% before integer rounding. Bow draw haste also accelerates crossbow charging through Ranged Weapon API. Projectile nodes modify both bow and crossbow attributes.

Companion applies to loaded living entities implementing Minecraft's `OwnableEntity` whose owner resolves to an online server player. It does not change pet AI or manufacture missing attributes on modded entities. Ordinary pet damage/resistance are flat +1; attack knockback and knockback resistance add 0.1; speed adds 3%; health adds four HP/two hearts. Packleader grants 20% damage, 15% speed and 10% resistance; Guardian Stead grants 20 HP/ten hearts, 25% resistance and 0.3 knockback resistance. Shared entrance and ordinary bonuses also apply. Health adds capacity, without filling it.

The bridge uses stable transient attribute modifiers, refreshes every 20 ticks and removes its own bonuses when allocations or ownership change. Speed bonuses sum within the bridge before applying one total multiplier. Incoming pet damage is reduced after armor by `(damage - flat pet resistance) * (1 - percentage pet resistance)`, clamped to zero; damage types bypassing effects remain untouched. Critical chance is corrected for existing base/total multipliers so the requested bonus remains fixed percentage points even with gear.

On the first login after installation, Adventurer allocations reset once, preserving category XP and refunding their point budget. This also clears deleted/conflicting skill IDs. The persistent migration marker survives player cloning; subsequent logins do not reset new choices. RPG skill IDs are retained and RPG allocations are not reset. Orb of Oblivion remains available for later respecs.

All configured skill XP calculations are multiplied by 1.35 exactly once. Level curves, point costs and caps are unchanged. Award rounding remains Pufferfish's normal integer rounding; Minecraft enchanting XP is not globally multiplied.

## Rebuild

Use Python and Java 25+ (tested with Java 26.0.2, `--release 25`). Preserve the three original archives under `tmp/skill-overhaul/original-<filename>`; scripts verify their SHA-256 hashes. They can be recovered from repository commit `d4a4ce5fba8ea4ebb68c8bc95ca7498be32bd40e` using Python's `subprocess.check_output(['git', 'show', '<commit>:mods/<filename>'])` and `Path.write_bytes`, avoiding binary redirection through Windows PowerShell.

`build.py` uses the cached isolated NeoForge 26.2.0.90 server libraries under `tmp/nameless-balance/server` and installed pack dependencies. For another workspace, prepare that server cache or adjust its classpath paths. The patched Minecraft server JAR must precede dependencies; do not include installer tool fat JARs, which shadow Gson classes. Set `SKILL_PATCH_JAVA` to another JDK's `bin` directory if needed.

```powershell
python docs/patches/skill-overhaul/build.py
python docs/patches/skill-overhaul/verify.py tmp/skill-overhaul
```

The build writes three candidates under ignored `tmp/skill-overhaul`; it does not replace installed JARs or publish anything. Only `OrbOblivionPuffishCompat.java` and `SkillOverhaul.java` are compiled into production. `OverhaulProbe.java` is test-only. Preserve original license notices. After testing, copy candidates over their existing filenames in `mods/` and the ignored fixed-packs folder; keep originals in ignored retired packs.

## Verification

- Structural regression checks cover removed nodes, valid connections, new nodes, Magistrate, removed completion gifts, RPG values and preserved Paladin defaults. The original archives fail the new-tree assertions; the replacements pass.
- An isolated dedicated server with Minecraft 26.2, NeoForge 26.2.0.90 and Java 26.0.2 passed **43 runtime checks** in `OverhaulProbe.java`: migration/refund/XP preservation and one-time behavior; entrance exclusions; exact critical points with and without gear; haste; three-school power and controlled school damage, with health/healing excluded; compounded anvil costs; owned-wolf attributes, outgoing attack damage and incoming damage; modifier removal/non-accumulation; both ranged weapons' actual use timing; projectile attributes; Colossus health; both RPG categories loading.
- Separate original/modified server worlds evaluated the real parsed kill-XP calculations against identical controlled inputs: Adventurer 70 → 94.5; both RPG categories 120 → 162; zero-valued conditional results stay zero. Each nonzero result is exactly 1.35 times the original. Fake players bypass ordinary XP event awards in Pufferfish, so this comparison evaluates the loaded calculations directly rather than claiming a live player's kill award.
- An isolated client loaded the repacked category, opened its skill screen and rendered the zoomed-out tree; the integrated server and client shut down normally. Test data/logs and screenshots remain under `tmp/skill-overhaul/`.

These are focused runtime checks, not a full multiplayer playthrough or a test of every modded pet's AI. The wolf knockback check confirms that a real attack moves its target; it does not quantify the incremental distance. Personal worlds, the personal `.minecraft` instance and the separate desktop server were not modified.

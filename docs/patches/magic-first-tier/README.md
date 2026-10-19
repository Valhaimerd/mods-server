# First-tier magic rework

20 October 2026, Minecraft 26.2 / NeoForge 26.2.0.90. Same-filename resource repacks of Wizards 3.1.2 and Skill Tree 1.6.1. No production classes or dependencies are added. All earlier physical, caster, companion and progression changes are retained.

## Effective values

Damage percentages multiply the matching school's effective damage value. Critical hits, node modifiers and mitigation apply normally.

| Spell | Damage | Cast / channel | Base cooldown | Reach |
| --- | --- | --- | --- | --- |
| Arcane Missiles | 26.67% per base missile, 320% over 12 | 4 s channel | 4 s, proportional | 64 blocks, existing homing |
| Arcane Explosion | 280% per target | 1.5 s | 10 s | Radius 7; improved radius 9 |
| Fire Breath | 28% per delivery, 700% over 25 | 5 s channel | 4 s, proportional | 8 blocks; no distance falloff |
| Flame Slash | 150% per impact | 0.1 s standard cast | 3 s | 16 blocks; velocity 0.9 |
| Frost Nova | 200% per target | 1 s | 10 s | Radius 10 |
| Frost Spikes | 240% per eligible spike impact | Instant | 8 s | Eight spikes, centers 1–8 blocks |

Fire Breath stores coefficient **1.4**, because Spell Engine normalizes each channel delivery by **5/25**. This yields **0.28 actual coefficient per delivery**, rather than 0.056. Arcane Missiles retains its existing normalization and total damage.

“80% faster” Flame Slash uses **0.5 × 1.8 = 0.9 velocity**. It releases automatically after 0.1 seconds; the old charge-dependent damage penalty and charge bonus are removed. Towering Slash adds 45% scale to this projectile.

Both Frost spells apply **2 seconds of Frozen without a maximum-health threshold**. Improved Frost Nova retains +1 second, for **3 seconds**. Frozen still breaks on eligible direct hits. Explicit immunity from another entity/mod, combat permissions, and stationary boss AI are not overridden.

## Nodes

- Improved Arcane Explosion: +2 radius, giving 9 total. Chain Detonation: 140% damage.
- Explosive Breath: 10% chance, 30% damage, 0.2 s proc cooldown. Its own splash falloff remains.
- Flame Throwing: +4 blocks, giving 12 total. Towering Slash: +45% projectile scale. Wave after Wave remains byte-identical.
- Frost Splinters: 80% damage.
- Glacial Ridge: eight additional spikes, centers 9–16 blocks, for 16 total.
- Frost Fan: two additional eight-spike rows at ±45 degrees, for 24 total.
- All other node mechanics, allocations, exclusion choices, later tiers, costs and unspecified cooldowns are unchanged.

The literal English Towering Slash and Glacial Ridge descriptions are updated; other affected descriptions use dynamic placeholders. Other language files are preserved.

## Rebuild and verify

Keep prior active archives in an input directory. Do not use pristine upstream Skill Tree: this pack's existing skill changes must remain in the input.

```powershell
python docs/patches/magic-first-tier/repack.py <input-directory> <output-directory>
python docs/patches/magic-first-tier/verify.py <output-directory> <input-directory>
```

Apply this resource transformation **after** any rebuild of the broader skill-overhaul patch. Output remains under the existing filenames. The scripts do not install, commit or push. Production replacements go to `mods/` and ignored `mod fixing/1 fixed packs/`; prior bytes are preserved in ignored `mod fixing/retired packs/magic-first-tier-2026-10-20/`.

## Observed verification

- The prior archives fail the new cooldown assertion (2 seconds versus requested 4).
- **44 structural checks pass**: normalized channel output, spell timings, proc settings, freeze limit removal, exact spike placements and changed-entry allowlists. All untouched archive entries are byte-identical, including classes, allocation/category files, later tiers and Wave after Wave. All JSON parses and archive CRCs pass.
- **51 isolated dedicated-server checks pass** on Java 26.0.2: loaded spell registry; actual impact damage at school damage values 10, 40 and 80; cooldown calculations; and both Frost effects applying for 40 ticks to a **1,000-HP target**, above all old tested health thresholds. Vanilla's attribute cap prevents a plain pig test fixture from actually reaching 20,000 HP, so the probe uses 1,000 HP explicitly.
- At effective school damage 40, observed unmodified impacts are: Missiles **10.667 per missile** (128 over 12), Explosion **112**, Breath **11.2 per delivery** (280 over 25), Slash **60**, Nova **80**, and Spikes **96 per eligible impact**. These are controlled unarmored, noncritical samples, not damage guarantees.
- The server reaches normal startup and shuts down normally. Its log contains native kqueue/debug-appender diagnostics on Windows; no spell registry/resource load failure was observed.
- The handbook was rebuilt and all 74 pages rendered for visual review. Only page 16 changes text; pagination is preserved, the changed page fits, and generated internal links resolve. The raw spell reference was regenerated from the installed JARs.

Probe source: [MagicReworkProbe.java](MagicReworkProbe.java). Checked-in measurements: [runtime-results.txt](runtime-results.txt). Local fixture/logs: `tmp/magic-first-tier/`. The probe JAR is test-only and is not in the pack. These checks do not simulate live channel aiming, every spike overlap, a complete skill build, multiplayer combat or every modded boss's immunity. Personal instances and the separate desktop server were not modified.

# Assassin / Rogue tier-II rework ? 22 October 2026

Current tier I/II values are superseded by the [24 October follow-up](../rogue-rebalance/README.md). The behavior and evidence below describe this earlier layer.

This extends the canonical Rogue builder in `../rogue-tier-one/`, retaining the
three active filenames. Inputs are rebased over the latest installed Archer
and first-tier Rogue changes, pinned by `../rogue-tier-one/revision-inputs.json`
in ignored `tmp/rogue-tier-one/revision-before/`. The older pinned inputs remain
untouched. Do not restore the old shared engine or rerun older layers over this
build without rebasing them.

## Behavior

All six Rogue active casts/cooldowns and all Rogue passive cooldowns explicitly
enable native haste scaling. Warrior spells in the shared Rogues JAR are not
changed. Status durations and trap arming/lifetime remain fixed. Fleet Footed
is forced to +3% base movement speed per level, cap twelve (+36%), even with
old generated effect configs.

Shadowstep remains instant / 12 seconds, now 14 blocks (18 Improved). After a
successful teleport, hostile mobs within four blocks of arrival receive 30
ticks of Tracking Disruption: their current native target clears and native
LivingChangeTargetEvent acquisition is suppressed until the effect expires.
Party players and owned pets are excluded. The old caster/global tracking
status is no longer applied. Cloak appends a native 20-tick all-damage immunity
window, replacing one-hit protection. Ambush immediately adds 50% melee-power
damage, Blindness I, Weakness I and Slowness I for two seconds, replacing the
next-hit attack-damage buff.

Bear Trap casts in 0.2 seconds on a four-second base cooldown. It places one
trap three blocks ahead, visible during its one-second arming phase, hidden
while armed, visible when sprung and hidden on unused natural expiry. The
native 20-second active lifetime and snapping/despawn animation are retained.
The armed entity is noncolliding, untargetable and does not intercept projectiles.

The base one-block activation cylinder detonates the trap once, applying 60%
dual-melee-power damage to every eligible enemy touching a 1.5-block-radius,
2.5-block-tall volume. Jumping entities intersect this volume. Improved changes
only the damage coefficient to 75%; it does not multiply Bleed power. Root lasts
two seconds without health/boss/type restrictions, followed by independently
applied Slowness I for four seconds. Root physically holds custom-moving bosses.
Serrated keeps its original Bleed power scaling with duration reduced to five
seconds. Extensive replaces extra placements with hit/activation radii of
2.25/1.5 blocks; there is still one trap. Entity bounding-box contact defines
these cylinder boundaries. Party members and owned pets neither activate nor
receive the trap's impacts.

Sidestep is now a direct nonstacking 20-point evasion buff for two seconds on
roll. Damage does not consume it; there is no separate passive cooldown.
Opportunist always readies its next-melee-hit critical buff, adding 20 percentage
points of critical damage, on a six-second base cooldown. Its five-second ready
window and native delayed next-hit consumption remain.

## Source and build

`TrapRules.java` supplies isolated trap geometry, single-detonation lookup and
tracking disruption. `PatchTierTwo.java` redirects only SpellCloud's dimensions
and area-impact call, preserving its normal arming/phase/animation/expiry logic.
The Rogue renderer gets a synchronized-phase-derived hidden flag and returns
before drawing the model while hidden. Client classes stay inside the Rogues
archive; dedicated servers do not load them. Existing magic/Archer projectile,
wall, party, revival and first-tier Rogue hooks are preserved.

```powershell
python docs/patches/rogue-tier-one/build.py
python docs/patches/rogue-tier-two/check_values.py tmp/rogue-tier-one/built
python docs/patches/rogue-tier-one/run_probe.py
python docs/patches/rogue-tier-one/verify.py
python docs/patches/archer-tier-one/verify.py
python docs/patches/rogue-reference/export.py
```

Back up current active JARs before installing the three candidates and matching
fixed-pack copies. Never ship probe/build-tool classes. Production bytecode
remains Java 25. Dedicated-server probes retain the newer Archer regression
fixture and the existing Rogue/party checks. The resource regression failed
first against the installed 15-second trap cooldown.

The fake player does not perform a real network teleport, so its fixture is
positioned at an arrival site to measure nearby targeting changes; teleport
placement itself retains the native implementation. Immunity expiry is tested
through a normal living-entity tick. This is a focused isolated fixture, not a
full-pack visual/balance playthrough. Personal Minecraft and worlds remain
untouched. The isolated client render probe verifies the real patched renderer
and state classes and exercises hidden and visible submit paths; it does not
claim a multiplayer animation recording.

Final dedicated run: **2,769 Archer/Rogue/party measurements passed**. This
includes retained Archer projectile regressions, native haste at every Rogue
tier, nearby tracking suppression/recovery, repeated-hit immunity plus expiry,
base/improved trap damage, jumping activation, separate trigger/hit bands,
single multi-target detonation, pet protection, physical rooting, five-second
Bleed without extra power scaling, Sidestep persistence and Opportunist damage.
The actual NeoForge client verified both renderer/state bytecode and hidden
versus visible submit paths; see client-results.txt. Prior active JARs are in
ignored `mod fixing/retired packs/rogue-tier-two-2026-10-22/`.

Handbook proof: all 74 pages rendered; only page 17 differs from the reviewed
previous edition and was visually checked without clipping. All 51 contents
numbers match their destinations, all 461 internal links remain and root/output
PDF bytes match. The Rogue reference was regenerated from the installed JARs.

The final expanded run also verifies an immune Ender Dragon receives Bear
Trap root and its custom movement is held, plus native doubled-haste cast
timing for every Rogue active. Total: 2,769 measurements.

Current numeric follow-up: [Bear Trap 40% damage / one-second root / three-second slow, Rain 60%, Endless seven-tick spacing](../trap-rain-balance/README.md).

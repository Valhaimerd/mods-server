# Second-tier magic rework

20 October 2026. Minecraft 26.2 / NeoForge 26.2.0.90, Java 25+. Applies after the [first-tier rework](../magic-first-tier/README.md), preserving its current replacements. Same active filenames for Wizards 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5. No extra compatibility mod.

## Effective values

Percentages scale the corresponding school damage before critical hits and mitigation.

| Spell | Damage | Cast/channel | Cooldown before haste | Other changes |
| --- | --- | --- | --- | --- |
| Arcane Beam | 20% per delivery, 500% across 25 | 5 s channel | 20 s minimum, 30 s full | Beam width 0.08 to 0.12, +50%; Exposure cap 25 levels at +2% each, +50% maximum |
| Arcane Barrage | Existing emitter bolt damage | Instant | 6 s, still not haste affected | Emitters last 7 s |
| Meteor | 150% each, three meteors | 1 s | 16 s | 10-block splash, no falloff, 5-block launch radius |
| Firestorm | 70% per delivery, 560% across eight | 5 s channel | 15 s minimum, 25 s full | 15-block radius |
| Frost Shield | No direct damage | Instant | 12 s | 3 s, -100% base movement |
| Ice Lance | 200% at full charge | 1 s full charge | 12 s | Projectile base velocity 1.5, knockback 2; Frozen for 4 s after 0.5 s delay |

The channel cooldown formulas are **20 + 10 × completion** for Beam and **15 + 10 × completion** for Firestorm, before haste. Existing engine behavior for every other spell remains unchanged. Haste scales both ends proportionally. The stored duration is the full-channel maximum, 30 or 25; it is not a zero-to-maximum channel cooldown anymore.

Firestorm stores coefficient **1.12** because channel normalization is 5/8, producing **0.70 per delivery**. Beam retains its original coefficient and normalization. Raging Firestorm retains two extra deliveries, producing 700% before its +10% improved root and other modifiers.

## Nodes and retained mechanics

- Beam Exposure retains its six-second duration and stacking increments. Amplifier cap 24 means 25 effect levels. Beam width changes the configured beam appearance; this is not a promise of a 50% wider enemy-selection hitbox.
- Arcane Battery and Attuned Emitters remain unchanged.
- Meteor Splash: starts after 5 ticks / 0.25 s; 5-block cloud radius, 6 s lifetime, 50% per impact, existing eight-tick impact interval. Meteor Shower still adds a fourth meteor.
- Flame Whirlpool: Slowness II for 1 s, refreshed on hit; inward velocity component increases from -0.3 to -0.45, retaining +0.1 upward component. It uses Firestorm's whole 15-block target area; its visual radius is updated. This strength choice is an implementation recommendation, not an additional user-specified number.
- Nimble Shield: +50% base movement for 3 s. With Shield's -100%, net movement is 50% of base before other modifiers. Durable Shield adds 1 s, producing 4 s. Base Shield uses amplifier 1 to double its default -50% movement modifier; pre-existing custom effect config overrides can change the underlying modifier.
- Improved Frost Shield still deducts 3 s: 9 s cooldown before haste. Improved Ice Lance still deducts 2 s: 10 s before haste. Improved Meteor still deducts 2 s: 14 s before haste. Their base cooldowns are 12/12/16 s respectively.
- Ice Lance replaces Frost Slowness with Frozen, with no spell-level health limit. Freeze still breaks on eligible direct damage; explicit entity/boss immunities, combat permissions and target acceptance remain. Delayed freeze follows the struck entity, not its old position, and cancels if caster/target dies or changes dimension before the callback.
- Ice Lance retains its charge curve and bonuses: full charge adds 16 range and 0.8 velocity, giving 32 range and 2.3 velocity. Colossal Lance remains unchanged.
- Shattering Lance: 4-block splash, no distance falloff. The delayed freeze is applied separately to each affected target through ordinary impact eligibility.

## Build and scope

```powershell
python docs/patches/magic-second-tier/build.py <directory-containing-current-pack-jars>
python docs/patches/magic-second-tier/verify.py tmp/magic-second-tier/built
```

The builder uses the cached Java/NeoForge classpath under `tmp/magic-balance-audit/compile.args` and the installed JDK path recorded in the script. Candidates are written under ignored `tmp/magic-second-tier/built`; they are not installed or pushed automatically. Keep the current first-tier-reworked archives as input. Production replacements go to `mods/` and ignored `mod fixing/1 fixed packs/`; prior bytes remain in ignored `mod fixing/retired packs/magic-second-tier-2026-10-20/`.

Spell Engine lacks a minimum proportional cooldown and an individual-impact delay field. The repack adds `SecondTierRules.class`, replaces only `SpellCooldowns.cooldownToSet`, and adds an entry guard to private `SpellImpacts.performImpact`. The cooldown hook reproduces vanilla behavior except for the two named spells. The freeze guard only schedules Ice Lance's Frozen status; it does not delay damage or modify another spell's effects. The scheduled callback invokes the original impact implementation with a scoped recursion guard, preserving eligibility and modifier handling. ASM tooling remains outside production; no test classes ship.

Apply rebuilds in order: broader skill-overhaul changes, first-tier transformation, then this second-tier transformation. A later first-tier rebuild alone will erase second-tier edits to the same language files if based on older inputs; always use the latest active inputs.

## Verification

- **38 structural checks pass**, including all modified spell values, normalized damage, node settings, status duration, no health limit, JSON parsing and archive CRCs.
- **18 isolated dedicated-server checks pass**: patched classes load; Beam 20/25/30 and Firestorm 15/20/25 cooldowns; first-tier Missile retains its half-channel two-second cooldown; controlled Beam/Meteor/Firestorm/Lance impacts at school damage 40 measure 8/60/28/80 HP; Shield lasts 60 ticks and reduces movement to zero; Nimble restores half base movement; Lance has no Frozen status immediately or at tick nine, then applies an 80-tick Frozen effect to a 1,000-HP target after tick ten.
- Measurements: [runtime-results.txt](runtime-results.txt). Probe: [SecondTierProbe.java](SecondTierProbe.java). Logs and worlds are isolated under `tmp/magic-second-tier/`.
- The first-tier runtime probe also passes **56 checks** with the second-tier engine replacement. Its **43 current-value structural checks** pass; original-entry allowlist comparisons belong to the historical first-tier candidates, since this revision intentionally changes additional resources. Engine archive comparison confirms exactly two existing class entries changed and one helper class was added; Wizards and Skill Tree changes are limited to seven JSON entries each.
- The handbook was rebuilt at 74 pages. Only page 16 changes text, its layout was visually checked, and all generated internal links resolve. The raw reference was regenerated from installed production archives, including notes for minimum channel cooldowns and delayed freezing.

These are controlled impact and timing checks, not a full client animation, multiplayer, boss-immunity or aim playtest. Personal instances and the separate desktop server are untouched. No commit or push is requested for this revision.

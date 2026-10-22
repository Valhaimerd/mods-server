# Archer tier-I and Frenzy revision - 22 October 2026

Minecraft 26.2 / NeoForge 26.2.0.90, Java 25+ production classes. This layers over
the current pack without replacing older magic, skill, targeting or revival fixes.
Four active filenames are retained; no mod was added or removed.

## Current values

| Skill/node | Installed change |
| --- | --- |
| Hunter's Mark | 7% extra incoming damage per stack; 21% at three stacks / 28% with Improved Hunter's Mark |
| Entangling Roots | 12-second base cooldown; six-block radius; six-second cloud; -60% base movement speed and jump strength |
| Improved Entangling Roots | Deduct two seconds, giving 10 seconds before haste |
| Nettle Sprouts | 100% chance per pulse; original five-second stacking poison and power-scaled cap retained |
| Nature's Grasp | 100% chance per pulse; one-second immobilization |
| Charged Shot | Five-block splash; 50% ranged coefficient retained; no distance falloff; direct target still excluded from the added splash |
| Concussive Shot | Guaranteed one-second stun; eight-second base cooldown |
| Rhythm | Any enemy arrow hit; no Mark requirement; 3% base ranged attack speed per stack, five-stack cap, four-second duration refreshed |
| Momentum | Applies the same revised four-second Rhythm duration |
| Frenzy | 30% at zero/two bars, 40% at third, 50% at fourth, 60% at fifth |

The base Roots effect slows; Nature's Grasp provides complete immobilization.
Pulse interval stays 15 ticks (0.75 seconds at 20 TPS). Neither Roots nor the
original Power Shot gains a new direct damage effect.

`ArcherRules` enforces the changed attribute effects during native effect
registration, including when old generated configs contain -50% Roots, 10% Hunter's Mark or 5%
Rhythm. Existing unrelated effects and registrations are retained. The ASM tool
injects one configuration call in each of ArcherEffects and SkillEffects;
earlier PassiveRules calls remain intact. Tool and probe classes never ship.

## Targeting

The existing PartyRules acquisition/final-impact checks remain in charge of
party players, the caster and player-owned pets. Roots, Nature's Grasp and
Spell Engine-applied Poison now join the universal living-target effect list.
This bypasses boss tags, application health limits and ordinary type-effect
immunity rejection through native effect callbacks. Stun already uses this
policy. Vanilla potion application outside Spell Engine is not modified.
Nonliving blocks, projectiles and decorative objects do not gain mob effects.

## Might and Frenzy

Frenzy's exclusivity tag includes optional `nova_structures:power` (Might),
alongside its prior conflicts. Native compatibility rejects the pair in either
application order. The provider can be absent without preventing registry load.
A global per-player function also reconciles legacy/command-created held
combinations: both hands are checked; Might is preserved and only Frenzy is
removed. Stored items are reconciled when held.

Each successful Frenzy firing action explicitly starts a native swing for its
actual hand and sends that animation to the shooter and observers. The client
branch of FrenzyRecoilMixin permits restarting in the first half of the prior
swing; vanilla otherwise suppresses that restart. Empty blocked attempts do
not produce a shot animation. Normal crossbows retain their animation path.

## Build and proof

```powershell
python docs/patches/bows-balance/build.py
python docs/patches/archer-tier-one/build.py
python docs/patches/archer-tier-one/run_probe.py
python docs/patches/bows-balance/verify.py mods
python docs/patches/crossarms-firing/export-archer-reference.py
```

Preserve the exact three inputs in ignored `tmp/archer-tier-one/before/`.
[artifacts.json](artifacts.json) records original/final fingerprints and changed
entries for all four final archives. CrossArms rebuilds from its existing pinned
upstream input and canonical overrides. The Archer build adds focused helper
classes to Spell Engine and replaces declared classes/resources. Previous active
JARs are preserved under ignored
`mod fixing/retired packs/archer-tier-one-2026-10-22/`.

Baseline regressions reproduced native Might/Frenzy compatibility and the old
18-second Roots cooldown. Final isolated dedicated-server checks passed:

- **1,387 Frenzy checks** with a registry-only copy of the actual Might definition:
  compatibility in both orders, reconciliation in both hands, damage/impact
  curves, per-shot recoil state, spread, heat, burst/crank, Infinity and slowdown.
- **1,382 Frenzy checks** without the optional Might provider, verifying the empty
  optional tag and ordinary Frenzy behavior.
- **531 Archer/party measurements**: loaded values, native -60% movement/jump,
  unmarked arrow triggers, 3% stack gains capped at five, four-second refresh,
  guaranteed stun and proc cooldown, real Better Party relations, self/party/pet
  safety in both focus modes and both friendly-fire settings, living boss stun,
  and effect-rejecting/high-health/boss-tagged target bypass for the new statuses.
  A regression also reproduced a Dragon moving through Nature's Grasp; the
  existing Entity.move lock now holds it physically and releases movement when
  the status is removed.

The isolated integrated client verified both hands restart during an existing
early swing, eight actual native server shots produced eight client recoil
restarts, and percentage descriptions localized without formatting errors.
See [runtime measurements](runtime-results.txt) and [client result](client-results.txt).
Logs/screenshots remain under ignored `tmp/archer-tier-one/` and
`tmp/crossarms-firing/`. These are focused fixtures, not a full-pack balance
playthrough. Personal Minecraft, the separate desktop server and player worlds
were not modified.

The editable reference is generated at
`mod fixing/Archer skills - raw attributes and nodes.md`, with all six active
skills, eighteen upgrade nodes and six passive definitions from installed JARs.
The player handbook summarizes the changed mechanics without the raw formulas.

Handbook proof: all 74 pages rendered; only pages 1, 2 and 18-26 differ
from the reviewed previous edition. Changed layouts were visually checked
without clipping. All 48 contents destinations match their printed numbers;
461 internal links remain, and root/output PDF bytes match.

## Hunter's Mark and Rhythm follow-up

Rhythm and Momentum now apply four seconds of Rhythm. Hunter's Mark is forced
to +7% incoming damage per stack, including old generated effect configs.
Three/four stacks therefore give +21%/+28%. Native generic 10-damage hits
measured 10.7/11.4/12.1/12.8 damage at one through four stacks. The original
three-second duration failed the new four-second assertion before rebuilding.
The Mark/Rhythm fixture passed 394 measurements before the tier-II revision, including six Spirit Wolf
attribute audits at R = 0/20/40 with/without its improved node. Fake owners
are not world-resolvable, so the audit invokes the exact native owner-scaling
method after summon initialization; it is not a complete summon AI playthrough.
Before the tier-II revision, wolves reached the native 100% knockback-resistance cap even at
R = 0; this observation did not trigger an unrequested balance change.

Mark/Rhythm handbook follow-up: all 74 pages rendered; only page 19 changed
from the reviewed tier-I edition and was visually checked without clipping.
All 48 contents destinations and 461 internal links remain valid.

## Tier-II revision and all-tier haste

Every Archer active and passive cooldown now explicitly enables haste; cast
times also enable haste. This retains each spell's native school haste sources.
Barrage is 0.1-second cast, eight-second cooldown (seven with Improved), three
arrows at 70% damage with zero divergence and three-tick spacing. Extensive
adds two arrows (five total); Blood heals for 6% R per impact.

Spirit Wolf Pack has 35-second haste-affected cooldown and 32-block spell/AI
range. The confirmed lifetime is 20 seconds base plus Enduring's ten = 30
seconds active. Each wolf has HP20+0.5R (Improved:20+0.7R), armor10+0.05R,
melee3+0.2R, attack knockback0.1R, and knockback resistance0.2. Improved adds
20 percentage points of R scaling and keeps the flat 20 HP.

The native owned-summon type already satisfies the Adventurer pet bridge;
no duplicate pet implementation was added. The fixture resolves the fake
owner through the world's player list and dispatches actual EntityTickEvent.Post
updates. It verifies +2 flat damage before Pack Leader's percentage, plus pet
HP, speed, knockback, knockback resistance and incoming-damage reduction;
repeated updates remain stable. Momentum gives two stacks on a one-second
base cooldown. Tactical Maneuver always procs and retains ten-second cooldown.

The old candidate failed the new Barrage cast assertion (0.5 versus0.1).
The replacement passed 531 measurements, including native doubled-haste
cooldown/cast calculations across all Archer tiers, upgraded Barrage cooldown,
actual Momentum/Tactical roll triggers and the owned-summon pet bridge.

The all-tier haste sweep also covers both bow and both crossbow weapon-tree
passives; their existing native haste behavior remains active. Total: 531
measurements. This revision retained all three filenames; prior local JARs
are backed up in `mod fixing/retired packs/archer-tier-two-2026-10-22/`.

Tier-II handbook proof: all 74 pages rendered; only page 19 differs from
the reviewed Mark/Rhythm edition, with no clipping on visual review. Contents
destinations and 461 internal links remain valid; root/output copies match.

## Tier-III revision

Rain of Arrows now has a 20-second base cooldown, 30 arrows at 40% R each,
five-tick spacing, 1.5-block scatter and 2.5-block impact splash. Torrential
keeps 30 arrows with two-tick spacing and two-block scatter. Endless Volley
is now a modifier adding ten arrows; the old hit-triggered cooldown reduction
is removed. Improved Rain retains its five percentage points of critical chance.

Magic Arrow deals 250% R at full charge, 270% with Improved, on a 14-second
base cooldown. Range is 32 plus up to 96 blocks from charge. Medium homing
turns at most ten degrees per tick. Conjured is now a modifier granting full
180-degree steering toward the selected target; its old cooldown reset is
removed. Magic Punch is retained. A single native followTarget field-read
hook resolves Conjured per projectile owner without mutating shared registry
data or changing collision, target eligibility, party guards or other spells.

Supercharge is guaranteed on an eligible arrow impact, readies 125% normal
arrow damage, and has an eight-second haste-affected cooldown. Its five-second
ready window and -25% waiting haste remain. Deflection retains three eligible
blocks over up to ten seconds, with a 40-second haste-affected cooldown.

The old candidate failed the new native 20-second Rain cooldown assertion
(15 seconds). Final candidates passed 564 isolated Archer/party measurements,
including effective meteor launch modifiers, charge output, improved power,
actual native ten-/180-degree steering and shared-data isolation. Existing
all-tier haste and party fixtures remain passing. This is not a full-pack
visual/balance playthrough. Prior active JARs are backed up in ignored
`mod fixing/retired packs/archer-tier-three-2026-10-22/`.

Tier-III handbook proof: all 74 pages rendered; only page 19 differs from
the reviewed tier-II edition. The changed page was visually checked without
clipping. All 461 internal links remain and root/output copies match.

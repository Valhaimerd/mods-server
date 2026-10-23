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
| Frenzy | 20% at zero/two bars, 30% at third, 40% at fourth, 50% at fifth |

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

The latest build uses the three Rogue-tier-II-preserving inputs in ignored `tmp/archer-tier-one/follow-before/`, pinned by follow-inputs.json. The prior revision-inputs.json still records the earlier layer. The original earlier inputs remain in `tmp/archer-tier-one/before/`.
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

## Previous Archer rebalance (before the Rain repair)

Power Shot is 12-second cooldown, eight-second ready duration, six-second
refreshed Mark. Improved extends only readiness to ten seconds, through two
narrow native stash-duration reads; its old Mark-duration modifier is zero.
Barrage arrows retain 60% damage; Extensive adds one for four total. Wolves
use HP20+0.4R (Improved20+0.5R), attack3+0.15R, zero innate knockback
resistance. Existing pet bonuses still apply normally.

Rain has 25-second cooldown, 32 arrows every five ticks, ten-block scatter
and 2.5-block splash. Torrential uses two-tick spacing and the same area;
Endless adds 44 arrows (76 total), radius11, five-tick spacing. Native scatter
was a fixed-radius ring; the Archer-only hook scales its rotated offset by
sqrt(U), making the area uniform and including the center. The first arrow
also scatters. Initial target position is snapshotted and the target reference
removed before native extra-shot closures, preventing a moving barrage.

Magic Arrows retains its registry ID, but charges for one second with linear charge
scaling and fires four projectiles five ticks apart on a 25-second cooldown.
Each deals 100% R, phases through blocks, searches 32 blocks around itself for
the nearest eligible enemy every tick, and expires after 200 ticks rather than
traveled range. Five-degree steering can gradually turn in any direction;
Conjured permits 180-degree steering. Native entity impacts, criticals, piercing,
hit history and party guards remain authoritative. Improved gives 110% R at full charge;
Magic Punch gives 125% with Improved and adds its old 150% base knockback.
The owner confirmed 32-block searching, Improved at 110% full-charge damage,
and Magic Punch at 125% full-charge damage. All Archer descriptions use concrete
percentages and timings, without unresolved impact placeholders.

This build layers over the current Rogue JARs; its shared delivery hook keeps
the prior Rogue and magic code intact. Only declared Archer resources and
projectile/stash hooks change. Do not rebuild the old Rogue layer over this
newer Archer layer without rebasing its inputs. Personal worlds untouched.

Final latest-revision checks: 2,316 isolated Archer/Rogue/party measurements
passed. Native launch scheduling verified 32/32/76 Rain arrows, 5/2/5-tick
spacing, fixed aim after target movement, random interior positions and all
four Magic Arrows with five-tick spacing. Actual projectile ticks dealt 40/48
damage at R = 40 through a stone wall, reacquired enemies behind the projectile,
and expired at 200 ticks. The fixture explicitly advances the age value, as
normal world entity ticking does; it is not a ten-second visual recording.
A 1,000-sample disk audit checked bounds, center occupancy and radial moment.
CrossArms independently passed 1,387 native firing checks. Earlier Rogue
runtime checks run in the same final Archer fixture, preserving that layer.

Latest handbook: all 74 pages rendered; only pages 19 and 23 changed from
the previous reviewed edition. Both were visually inspected without clipping.
All 461 internal links remain, with matching root/output PDF bytes.

Magic Arrows clarification: full charge takes one base second, with linear
partial-charge output. Base/Improved/Magic Punch deal 100%/110%/125% R per
arrow at full charge. The two native power modifiers add 10 and 15 percentage
points respectively; knockback is preserved. Automatic acquisition now searches
32 blocks around each projectile. The four-shot burst, ten-second lifetime,
wall passage and party/pet safety remain.

Clarification verification: 2,325 isolated Archer/Rogue/party measurements
passed, including base/Improved/Punch full- and half-charge coefficients,
125% native impact through a wall, and acquisition between 16 and 32 blocks.
Matching active/fixed JARs retain Java 25 bytecode and the current filenames.

Clarification handbook proof: all74 pages rendered, only page19 changed and
visually checked without clipping. All461 internal links and matching PDF
copies remain.

## Rain follow/scatter repair and Conjured fan (current behavior)

The player report exposed a mistake in the previous patch: native
launchSequenceEligible treats 0 as disabled, not all sequences. The prior
bounds-only checks accepted every arrow at the center; they did not prove
nonzero scatter. Rain now uses offset rule -2147483647, enabling all 32/76
arrow indices. The rotated offset still scales by sqrt(U), uniformly sampling
the disk. The earlier rainTarget/rainLocation hooks are now identities, keeping
the live native target reference in every scheduled launch. Each arrow starts
above the mob's current position with 10/11-block random scatter; the native
first-arrow follow rule is retained. Wizard meteors are unchanged.

The new regression reproduces the removed follow reference on the old JAR.
The strengthened fixture requires positive native radial spread, interior
positions, movement to the impact plane and preserved horizontal offsets.
It moves the target before executing the native scheduled shots and checks
that later shots center on the new position. This supersedes the earlier
fixed-center behavior and the previous inadequate scatter proof.

Magic Arrows now deals 80%/90%/100% R at full charge for base/Improved/Punch.
Both power modifiers add 12.5% of the 80% base coefficient. Conjured keeps
four arrows at five-tick spacing and launches above the caster's eyes in four
approximately 90-degree-separated directions, upward velocity 1.2 and lateral
speed 0.9 (total 1.5). Each waits 10 ticks before acquisition/steering, then uses
medium 10-degree-per-tick steering rather than 180. A native appended impact
applies Glowing for 200 ticks to enemies hit; shared party/pet guards remain.
Magic Punch and ordinary arrows retain their forward launch and five-degree
steering. The 32-block search, ten-second lifetime, wall passage, charge scaling
and cooldown stay unchanged.

This build is rebased over the newer Rogue tier-II layer in ignored
`tmp/archer-tier-one/follow-before/`, pinned by follow-inputs.json. It replaces
only Archer data/descriptions, the Archer helper and one native launch call.
Existing Rogue, magic, party and stash hooks are retained byte-for-byte.

Final native dedicated-server fixture: 2,805 measurements passed, including
both Rogue tiers and party guards. The actual native scheduler produces
32/32/76 falling arrows with nonzero radial variance and interior samples;
shots after target movement center on its new position. Arrows are ticked to
the impact plane with scatter retained. Conjured tests run all ten startup
ticks, require separated upward trajectories, verify medium steering after
the delay, and measure native 36 damage at R = 40 plus 200-tick Glowing on an
enemy while an owned pet remains unaffected. Base/Punch impacts measure 32/40.
These are native server simulations, not a visual full-pack playthrough.

Rain/Conjured handbook proof: all 74 pages rendered; only page 19 differs
from the previous reviewed Rogue-tier-II edition. The changed page was
visually checked without clipping. All 461 internal links remain, with
matching root/output PDF copies.

## Rain count/radius follow-up (current values)

Rain of Arrows: 40 arrows, 30-second haste-affected base cooldown and eight-
block random scatter. Torrential retains 40 arrows and the same eight-block
scatter, firing every two ticks (0.1s). Endless adds 20 arrows for 60 total,
with the same eight-block scatter, firing every five ticks (0.25s). Base Rain
also retains five-tick spacing. First-to-last launch spans are 9.75s/3.9s/14.75s
for base/Torrential/Endless. All retain 40% R per impact, 2.5-block splash,
uniform interior scatter, native target following and party/pet protection.
Magic Arrows and Conjured behavior are unchanged.

Count/radius verification: all 2,805 native Archer/Rogue/party measurements
passed with the revised 40/40/60 arrow totals, 30-second Rain cooldown,
eight-block radius for every branch, native launch spacing and moving-target
scatter. Magic Arrows, Conjured and both Rogue tiers remain passing.

Count/radius handbook proof: all 74 pages rendered; only page 19 changed
from the reviewed Rain/Conjured edition and was visually checked without
clipping. All 461 internal links and matching PDF copies remain.

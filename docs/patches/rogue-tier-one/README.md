# Rogue first-tier rework ? 22 October 2026

Minecraft 26.2 / NeoForge 26.2.0.90, Java 25 production classes. Three active
filenames are retained: Rogues 3.1.2, Skill Tree 1.6.1 and Spell Engine 1.10.5.
No mod is added or removed. Later Rogue tiers and passive pairs are unchanged.

## Implemented rules

- Shock Powder: instant, 12-second haste-affected cooldown, five-block radius.
  Mobs with maximum HP <= 50 + twice noncritical melee power receive three
  seconds of Shock; stronger mobs receive one second. Boss/type immunities
  no longer prevent Shock. Players below 24 current HP receive three seconds;
  players at exactly 24 or above receive one. Improved adds one second to both.
  Custom-moving bosses are physically held while Shock is present.
- Explosive Powder: guaranteed per qualifying Shock impact, 100% melee power,
  2.5-block splash, no distance falloff. The hidden companion remains registered
  but grants no extra stun. Smoke Screen stays five blocks wide, lasts eight
  seconds and gives 50 percentage points of evasion at the native base stat.
- Slice & Dice: 12-second haste-affected cooldown, retained ten-second ready
  window, ten successful melee-hit charges. Improved gives twelve; Blades of
  Fury gives fifteen with Improved. Each empowered hit adds 15% melee power
  damage with native critical/defense handling, plus a horizontal pull of up
  to 0.2 toward the caster, respecting knockback resistance. The old stacking
  attack-damage modifier is removed, including from stale generated configs.
- Empowered hits grant five percentage points of evasion per stack, cap ten,
  for four seconds refreshed. Fleet Footed gives 5% base movement speed per
  stack, cap twelve, for four seconds refreshed. It explicitly targets the
  caster after the new damage-targeted stash trigger. Misses have no native
  melee-impact event; self/party players and owned pets cannot spend charges.
- Coated Blades: 25% proc chance, one-second haste-affected cooldown; native
  stacking Poison duration/scaling retained.
- Fracture: retained 25% chance, one-second haste-affected cooldown, no extra
  damage. Each of up to three stacks removes 2 + 10% melee power armor points
  for six seconds refreshed. The latest applying caster's power is snapshotted
  for the whole stack set. A stable saved scaling modifier combines with the
  native -2 flat armor per level; the effect removal handler clears it on
  dispel/expiry. Armor reduction is flat rather than a percentage of armor.

The shared PartyRules acquisition/final-impact checks remain authoritative.
Only Rogue Shock joins the universal control bypass list; Bear Trap is not
silently changed. Native stashes handle charge consumption. The per-target
impact hook calls the prior PassiveRules/RevisionRules path before applying
Rogue adjustments, preserving earlier magic changes. Registry JSON is never
mutated during combat.

## Rebuild and verification

Pinned prior pack JARs live under ignored `tmp/rogue-tier-one/before/` and are
fingerprinted in inputs.json. Build from the repository root:

```powershell
python docs/patches/rogue-tier-one/build.py
python docs/patches/rogue-tier-one/check_values.py tmp/rogue-tier-one/built
python docs/patches/rogue-tier-one/run_probe.py
python docs/patches/rogue-tier-one/verify.py
python docs/patches/rogue-reference/export.py
```

Install only the three built JARs after passing checks, preserving their active
filenames and placing matching copies in `mod fixing/1 fixed packs/`. Prior
active bytes are backed up under ignored
`mod fixing/retired packs/rogue-tier-one-2026-10-22/`. Probe classes and patch
build tools must never ship. artifacts.json records every changed/added entry.
This is a layer over the earlier Archer rework; if rebuilding that earlier
layer, rebuild this Rogue layer afterward, retaining the pinned inputs.

The resource regression first failed against the installed 16-second Shock
cooldown. Native integration checks exposed Fleet Footed's obsolete self-target
assumption; the appended buff now explicitly applies to the caster. Tests use
an isolated dedicated instance, preserving personal Minecraft, the separate
desktop server and player worlds. The fixture dispatches native melee/impact
callbacks; the explosion's scheduled POST callback is dispatched explicitly,
and its target chunk is made entity-accessible for native splash lookup.
This is not a full-pack client visual or balance playthrough.

The working reference is generated at
`mod fixing/Rogue skills - raw attributes and nodes.md`. It contains all three
active pairs and every upgrade/passive, with current first-tier runtime rules
explained alongside raw serialized data. The handbook gives the short overview.

Final isolated run: **884 Rogue/Archer/party measurements passed**, including
mob and player health boundaries, boss effect/movement bypass, 10/12/15 charge
consumption, party-protected charge preservation, native evasion values, Fleet
stack caps, per-hit bonus/pull, full edge splash, and Fracture dispel/expiry.

Handbook proof: all 74 pages rendered, changed pages 2 and 16?18 visually
reviewed without clipping. All 51 printed contents destinations match, all
461 internal links remain, and root/output PDF copies match. Classes and
playstyles now starts on page 17; its contents number is updated accordingly.

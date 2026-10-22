"""Export installed Archer data and readable tier pairs; no gameplay changes."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json

ROOT = Path(__file__).resolve().parents[3]
catalog = {}
languages = {}
sources = []
for pattern in ['archers-*.jar', 'skill_tree-*.jar']:
    jar = next((ROOT / 'mods').glob(pattern))
    sources.append(f'- `{jar.name}`; SHA-256 `{hashlib.sha256(jar.read_bytes()).hexdigest()}`')
    with ZipFile(jar) as archive:
        for name in archive.namelist():
            if name.endswith('/lang/en_us.json'):
                languages.update(json.loads(archive.read(name)))
            if name.endswith('.json') and (name.startswith('data/archers/spell/') or name.startswith('data/skill_tree_rpgs/spell/archer_')):
                namespace = name.split('/')[1]
                catalog[f'{namespace}:{Path(name).stem}'] = json.loads(archive.read(name))

def title(identifier):
    return languages.get('spell.' + identifier.replace(':', '.') + '.name', identifier)

def plain(value):
    """Retain mechanical data; omit presentation assets from the raw appendix."""
    if isinstance(value, dict):
        skip = {'visuals', 'client_data', 'particles', 'animation', 'sound', 'sounds', 'spawn_fx', 'despawn_fx', 'launch_visuals', 'launch_sound', 'travel_particles', 'swing_sound', 'impact_sound'}
        return {k: plain(v) for k, v in value.items() if k not in skip}
    if isinstance(value, list):
        return [plain(v) for v in value]
    return value

text = '''# Archer skills - raw attributes and nodes

Audited 22 October 2026 against the active pack. This is an inspection reference, not proposed balance changes.

## Reading the values

- **R** means caster Ranged Damage. A spell coefficient of 50% means 0.5 times R before criticals, defenses and other combat modifiers. Arrow multipliers instead scale the weapon's fired arrow; these are different mechanisms.
- Cooldowns and cast times are base values, before haste or node changes. Cooldowns default to haste-affected unless explicitly disabled. Spell Engine's omitted standard cast duration defaults to zero; default casting movement factor is 0.2.
- Internal tiers 2/3/4 are the player's active tiers I/II/III. At each tier the installed class-tree connections make the two active roots exclusive; each root has two exclusive branch upgrades.
- A status amplifier is zero-based. The ADD mode starts at one level for increment 1 and increases on subsequent applications, refreshing duration. The readable tables use levels/percentages; exact serialized fields are in the appendix.
- Shared pack targeting protects party players and player-owned pets. Archer Roots, Nature's Grasp, Poison and Stun bypass health, boss and type-immunity rejection for living enemies. Nature's Grasp also physically holds custom-moving bosses.
- The pack enforces Roots at -60% movement/jump, Hunter's Mark at +7% damage taken per stack, and Rhythm at +3% per stack even with older generated effect configs; no personal server configuration was changed.

## Active tier pairs

| Active tier | Marksman choice | Nature choice |
| --- | --- | --- |
| I | Power Shot | Entangling Roots |
| II | Barrage | Spirit Wolf Pack |
| III | Rain of Arrows | Magic Arrows |

## Tier I - Power Shot OR Entangling Roots

| Attribute | Power Shot | Entangling Roots |
| --- | --- | --- |
| Role | Prepare arrows to apply **Hunter's Mark** | Ground cloud that **slows** enemies |
| Cast time | **Instant** | **Instant** |
| Base cooldown | **12 s**, haste-affected | **12 s**, haste-affected |
| Direct damage | No extra direct-damage impact in the base skill; fired arrows retain normal weapon damage | **None** in the base skill |
| Main effect | Next **3 arrows** apply Mark, one stack per application | **-60% base movement speed** and **-60% base jump strength** |
| Effect duration | Prepared arrows: **8 s**; Mark: **6 s**, refreshed | Cloud: **6 s**; slow: **1 s**, reapplied by pulses |
| Strength | **+7% damage taken from all sources per Mark stack**, maximum **3 stacks / +21%** | Pulse every **15 ticks / 0.75 s** at 20 TPS |
| Area / range | No separate aiming range; rides the actual arrows | **6-block radius** around placement; vertical range multiplier **0.3** |
| Base costs | No separate arrow cost on activation; ordinary shots use weapon ammunition | Exhaustion **0.2**; no item cost specified |
| Important limit | This is vulnerability, not +21% base bow damage | Applies to living enemies regardless of health, boss tag or ordinary status immunity; party allies/pets protected |

### Tier-I upgrade nodes, side by side

| Node position | Power Shot branch | Entangling Roots branch |
| --- | --- | --- |
| Improved/root node | **Improved Power Shot:** prepared arrows last **10 s** instead of **8 s**. Mark stays **6 s**, refreshed. | **Improved Entangling Roots:** cooldown **-2 s**, becoming **10 s** before haste. |
| Branch A | **Improved Hunter's Mark:** **+1 prepared arrow** and **+1 maximum Mark stack**: **4 arrows**, up to **+28% damage taken**. Still one stack applied per arrow. | **Nettle Sprouts:** each impact has **100% chance** to add one stacking **Poison** level for **5 s**. Maximum level is **2 + the integer part of 20% caster physical-melee power**. Starts at Poison I; duration refreshes. |
| Branch B | **Charged Shot:** adds **50% R** splash in a **5-block radius** around the hit. **Excludes the directly hit target** from this added splash; **no distance falloff**. | **Nature's Grasp:** each impact has **100% chance** to **immobilize** the target for **1 s**. Its movement/jump modifiers clamp movement down; this is stronger than the base slow. |

Choose branch A OR branch B for each skill; they are not both active together. Root effects and branch effects are listed separately so their contributions remain clear.

### Nearby passive pair - Concussive Shot OR Rhythm

| Attribute | Concussive Shot | Rhythm |
| --- | --- | --- |
| Trigger | Arrow impact | Arrow impact against an enemy; **Mark is not required** |
| Chance | **100%** | **100%** |
| Effect | **Stun for 1 s** | **+3% base ranged attack speed per stack** |
| Maximum | One stun application per successful proc | **5 stacks / +15%** |
| Duration | **1 s** | **4 s**, refreshed on application |
| Cooldown | **8 s**, haste-affected by engine default | No additional proc cooldown specified |

Roots, Poison, Nature's Grasp and Stun use the pack's universal living-target debuff policy, while party protection remains. Momentum also applies the revised four-second Rhythm duration. These are passive choices, not a third active tier-I spell.

## Tier II - Barrage OR Spirit Wolf Pack

All Archer active and passive cooldowns have haste scaling enabled across all three tiers. Cast times also use haste. Buff/cloud/summon durations and the requested arrow spacing retain their stated base values. Native school haste applies: ranged skills use ranged haste; Roots retains its physical-melee school and its existing spell/attack-speed haste sources.

| Attribute | Barrage | Spirit Wolf Pack |
| --- | --- | --- |
| Cast | **0.1 s** | **Instant** |
| Base cooldown | **8 s**, haste-affected | **35 s**, haste-affected |
| Core effect | Fires **3 arrows**, each at **60% normal arrow damage** | Summons **2 Spirit Wolves** for **20 s** active life |
| Projectile | Launch velocity **3.15**, knockback **0.5**, bypasses damage immunity frames | Melee-only pets; Adventurer Companion bonuses apply |
| Accuracy / critical arrow | **Zero divergence on every arrow**. Critical-arrow flag enabled; character critical chance is separate. | Melee summon, no ranged attack defined |
| Burst spacing | **3 ticks / 0.15 s** between arrows; three arrows span **0.3 s** after release | Wolves appear at 0 and 5 ticks; Leader adds a third at 10 ticks |
| Improved/root | **Improved Barrage:** cooldown **-1 s**, becoming **7 s** before haste | **Improved Spirit Wolf:** adds **10% R** to maximum-health scaling, giving **20 + 50% R** |
| Branch A | **Extensive Barrage:** **+1 arrow**, total **4**, spanning **0.45 s** after release | **Leader of the Pack:** **+1 wolf**, total **3** |
| Branch B | **Blood Barrage:** arrow hits heal the caster for **6% R** | **Enduring Bond:** **+10 s** active life, total **30 s** |

The confirmed lifetime choice is 20 seconds base plus 10 seconds with Enduring Bond. Improved Spirit Wolf keeps the flat 20 HP and increases the R coefficient from 40% to 50%; it does not multiply all health by 1.2.

### Nearby roll-node pair - Momentum OR Tactical Maneuver

| Attribute | Momentum | Tactical Maneuver |
| --- | --- | --- |
| Trigger | Combat roll | Combat roll |
| Chance | **100%** | **100%** |
| Effect | Adds **two Rhythm stacks** (+3% base ranged attack speed each); maximum five stacks | **+200% base roll recharge rate** |
| Duration | **4 s**, refreshed | **4 s** |
| Base cooldown | **1 s**, haste-affected | **10 s**, haste-affected |

With the installed default summon config, each wolf has these stats **before other pet bonuses**. Native owner resolution, attribute scaling and automatic Adventurer updates were measured at R = 0, 20 and 40.

| Wolf attribute | Formula / value | At R = 40 |
| --- | --- | --- |
| Maximum health | **20 + 40% R** | **36 HP / 18 hearts** |
| With Improved Spirit Wolf | **20 + 50% R** | **40 HP / 20 hearts** |
| Armor | **10 + 5% R**, subject to vanilla cap | **12 armor** |
| Melee attack damage | **3 + 15% R** | **9 damage before defenses** |
| Attack knockback | **10% R** | **4** |
| Knockback resistance | **0%**, before pet bonuses | **0%** |
| Base movement speed | **0.35** | **0.35** |
| Spell range / AI detection range | **32 blocks / 32 blocks** | **32 blocks** |
| Melee action | **14 ticks** action duration; **1.5** speed; **1.2x** chase movement multiplier | Same |

Wolves are owned summons recognized by the existing Adventurer pet bridge. Flat pet attack bonuses add once to the base damage: a +2 bonus gives **3 + 2 + 15% R**. Pack Leader's percentage then multiplies that result. Health, speed, attack knockback, knockback resistance and incoming-damage resistance pet bonuses also apply. Repeated updates do not accumulate duplicate bonuses.

The native entity dimensions are 0.8 blocks wide / 0.925 high. Wolves teleport-follow after 32 blocks. Data requests HOSTILE automatic mob targeting, and the pack's shared relationship policy protects party allies and owned pets. Spawn and despawn phases are 20 ticks each, in addition to 20 seconds of active life (30 seconds with Enduring Bond). Generated summon configs or other pet bonuses can change native bases. No regeneration action is specified.

## Tier III - Rain of Arrows OR Magic Arrows

| Attribute | Rain of Arrows | Magic Arrows |
| --- | --- | --- |
| Cast | **0.5 s** | **1 s** full charge; partial charges deal less damage |
| Base cooldown | **25 s**, haste-affected | **25 s**, haste-affected |
| Damage | **40% R per impact** | **100% R per arrow at full charge**, four arrows |
| Range / area | **32 blocks** aiming; fixed ground location, **10-block random scatter**, **2.5-block splash per impact** | Automatically seeks nearest eligible enemy within **32 blocks of each arrow**, without pretargeting |
| Delivery | **32 arrows**, every **0.25 s**; launch span **7.75 s** | **4 arrows**, every **0.25 s**; launch span **0.75 s**, velocity **1.5** |
| Behavior | Uniform random disk scatter includes the middle, without following a moving target | Slight **5-degree steering per tick**, can turn all the way around over time; passes through blocks; **10 s lifetime**; effectively unlimited enemy piercing, without hitting the same entity twice |
| Improved/root | **+5 percentage points critical chance** | Full-charge damage becomes **110% R per arrow** |
| Branch A | **Torrential Arrows:** 32 arrows every **0.1 s**, same **10-block scatter / 2.5-block splash**; launch span **3.1 s** | **Conjured Arrow:** absolute homing, **180-degree steering per tick** |
| Branch B | **Endless Volley:** **+44 arrows**, **76 total**, every **0.25 s**, **11-block scatter / 2.5-block splash**; launch span **18.75 s** | **Magic Punch:** full-charge damage becomes **125% R per arrow** with Improved, plus **150% extra base knockback**, giving **5.0** instead of **2.0** |

Magic Arrows keeps its registry ID for save compatibility. Party allies and owned pets are excluded from automatic acquisition and final damage. It reacquires the nearest eligible enemy every tick, excluding already-hit enemies. Solid blocks do not prevent its entity collision checks. Range is no longer a traveled-distance lifetime limit; the projectile expires after 200 ticks. The confirmed Improved node deals 110% R per arrow at full charge; Magic Punch adds a further 15 percentage points, giving 125% R. Charge output scales linearly; half charge gives half the full-charge damage. Each arrow searches within 32 blocks.

### Third passive pair - Supercharge OR Deflection

| Attribute | Supercharge | Deflection |
| --- | --- | --- |
| Trigger / chance | Arrow impact, **100%** | Damage taken below **50% HP** and above **1% HP**, **100%** |
| Effect | Next ordinary, non-skill arrow deals **125% normal arrow damage**; added knockback effect **1.5** | Blocks the next **3 eligible attacks** (including melee and projectiles) |
| Duration | Ready for **5 s**; **-25% ranged haste** while waiting | Up to **10 s** |
| Base cooldown | **8 s**, haste-affected | **40 s**, haste-affected |

The raw appendix contains every installed active and node definition, including all tier-II/III branches and later passive pairs. No missing field is inferred from a tooltip sentence.

## Installed sources

'''
text += '\n'.join(sources) + '\n'
text += '\nEffect code checked: `net.archers.effect.ArcherEffects`, `net.skill_tree_rpgs.effect.SkillEffects`; engine status stacking checked in `SpellImpacts`.\n'
text += '\n## Raw mechanical definitions - all active tiers and nodes\n\nPresentation-only particles, models, animation and sound are omitted. All remaining installed fields are reproduced, so coefficients, chances, cooldown exceptions, summon scaling and branch mechanics can be reviewed without artwork noise.\n'
for tier, pair in [(2, ['power_shot', 'entangling_roots']), (3, ['barrage', 'spirit_wolf']), (4, ['rain_of_arrows', 'magic_arrow'])]:
    text += f'\n### Active tier {tier-1}\n'
    for index, spell in enumerate(pair, 1):
        identifier = 'archers:' + spell
        text += f'\n#### {title(identifier)}\n\nID: `{identifier}`\n\n```json\n{json.dumps(plain(catalog[identifier]), indent=2)}\n```\n'
        for suffix in ['root', 'modifier_1', 'modifier_2']:
            identifier = f'skill_tree_rpgs:archer_tier_{tier}_spell_{index}_{suffix}'
            text += f'\n##### {title(identifier)}\n\nID: `{identifier}`\n\n```json\n{json.dumps(plain(catalog[identifier]), indent=2)}\n```\n'
text += '\n### Passive pairs - exact data\n'
for identifier, data in sorted(catalog.items()):
    if '_passive_' in identifier:
        text += f'\n#### {title(identifier)}\n\nID: `{identifier}`\n\n```json\n{json.dumps(plain(data), indent=2)}\n```\n'
output = ROOT / 'mod fixing/Archer skills - raw attributes and nodes.md'
output.write_text(text, encoding='utf-8')
assert len(catalog) == 30, len(catalog)
print(f'Wrote {output.name}: 6 actives, 18 upgrade nodes, 6 passives')

"""Validate requested behavior and byte preservation against the input archives."""
from pathlib import Path
import json
import math
import sys
import zipfile

root = Path(sys.argv[1])
checks = 0


def check(actual, expected, label):
    global checks
    assert actual == expected or (isinstance(actual, (int, float)) and isinstance(expected, (int, float)) and math.isclose(actual, expected, abs_tol=1e-7)), f'{label}: {actual!r} != {expected!r}'
    checks += 1


def load(namespace, id):
    name = 'wizards-neoforge-3.1.2+26.2.jar' if namespace == 'wizards' else 'skill_tree-neoforge-1.6.1+26.2.jar'
    with zipfile.ZipFile(root / name) as z:
        return json.loads(z.read(f'data/{namespace}/spell/{id}.json'))


def dmg(d):
    return next(i['action']['damage']['spell_power_coefficient'] for i in d['impacts'] if i['action']['type'] == 'DAMAGE')


missile = load('wizards', 'arcane_missile')
check(missile['cost']['cooldown']['duration'], 4, 'Missiles cooldown')
check(missile['cost']['cooldown']['proportional'], True, 'Missiles proportional')
check(missile['active']['cast']['duration'] * dmg(missile), 3.2, 'Missiles total channel coefficient')
explosion = load('wizards', 'arcane_explosion')
check(dmg(explosion), 4.0, 'Explosion damage')
check(explosion['area_impact']['radius'], 7, 'Explosion radius')
check(explosion['range'], 64, 'Explosion targeting reach')
check(explosion['target']['type'], 'AIM', 'Explosion targets enemy')
check(explosion['target']['aim']['required'], True, 'Explosion requires target')
check('visuals' in explosion['release'], False, 'No caster-centered release explosion')
check(load('skill_tree_rpgs', 'arcane_tier_2_spell_2_root')['modifiers'][0]['replacing_area_impact']['radius'], 9, 'Improved Explosion radius')
check(dmg(load('skill_tree_rpgs', 'arcane_tier_2_spell_2_modifier_2')), 1.4, 'Chain damage')
breath = load('wizards', 'fire_breath')
cast = breath['active']['cast']
check(dmg(breath) * cast['duration'] / cast['channel']['ticks'], .22, 'Actual Breath delivery coefficient')
check(dmg(breath) * cast['duration'], 5.5, 'Breath full channel')
check(breath['cost']['cooldown']['duration'], 4, 'Breath cooldown')
check(breath['cost']['cooldown']['proportional'], True, 'Breath proportional')
check(breath['range'], 8, 'Breath reach')
check(breath['target']['area']['distance_dropoff'], 'NONE', 'Breath falloff removed')
proc = load('skill_tree_rpgs', 'fire_tier_2_spell_1_modifier_1')
check(proc['passive']['triggers'][0]['chance'], .1, 'Explosive chance')
check(proc['cost']['cooldown']['duration'], .2, 'Explosive cooldown')
check(dmg(proc), .3, 'Explosive damage')
check(load('skill_tree_rpgs', 'fire_tier_2_spell_1_modifier_2')['modifiers'][0]['range_add'], 4, 'Flame Throwing')
slash = load('wizards', 'fire_slash')
check(dmg(slash), 1.3, 'Slash damage')
check(slash['active']['cast']['duration'], .1, 'Slash cast')
check(slash['active']['cast']['type'], 'STANDARD', 'Slash auto release')
check('charge' in slash['active']['cast'], False, 'No partial-charge penalty')
check(slash['cost']['cooldown']['duration'], 5, 'Slash cooldown')
check(slash['deliver']['projectile']['launch_properties']['velocity'], .9, 'Slash velocity')
check(load('skill_tree_rpgs', 'fire_tier_2_spell_2_modifier_1')['modifiers'][0]['projectile_scale_multiply'], .45, 'Towering scale')
nova = load('wizards', 'frost_nova')
check(dmg(nova), 3, 'Nova damage')
check(nova['range'], 10, 'Nova radius')
check(nova['active']['cast']['duration'], .3, 'Nova cast')
check(dmg(load('skill_tree_rpgs', 'frost_tier_2_spell_1_modifier_1')), .8, 'Splinters damage')
spikes = load('wizards', 'frost_spikes')
check(dmg(spikes), 1.4, 'Spike damage')
for id, d in [('Nova', nova), ('Spikes', spikes)]:
    status = next(i['action']['status_effect'] for i in d['impacts'] if i['action']['type'] == 'STATUS_EFFECT')
    check(status['duration'], 2, id + ' freeze duration')
    check('apply_limit' in status, False, id + ' no freeze health threshold')
cloud = spikes['deliver']['clouds'][0]
base = [cloud['placement']] + cloud['additional_placements']
check([p['location_offset_by_look'] for p in base], list(range(1, 9)), 'Base spike centers')
ridge = load('skill_tree_rpgs', 'frost_tier_2_spell_2_modifier_1')['modifiers'][0]['additional_placements']
check([p['location_offset_by_look'] for p in ridge], list(range(9, 21)), 'Ridge extension')
fan = load('skill_tree_rpgs', 'frost_tier_2_spell_2_modifier_2')['modifiers'][0]['additional_placements']
for yaw in (-10, 10):
    check([p['location_offset_by_look'] for p in fan if p['location_yaw_offset'] == yaw], list(range(1, 9)), f'Fan row {yaw}')
check(len(base) + len(fan), 24, 'Fan total spike count')
fan_mod = load('skill_tree_rpgs', 'frost_tier_2_spell_2_modifier_2')['modifiers'][0]
check(dmg(spikes) * (1.1 + fan_mod['power_modifier']['power_multiplier']), .5, 'Fan damage with mandatory improved root')

if len(sys.argv) > 2:
    original = Path(sys.argv[2])
    allowed = {
        'wizards-neoforge-3.1.2+26.2.jar': {f'data/wizards/spell/{id}.json' for id in ['arcane_missile', 'arcane_explosion', 'fire_breath', 'fire_slash', 'frost_nova', 'frost_spikes']},
        'skill_tree-neoforge-1.6.1+26.2.jar': {f'data/skill_tree_rpgs/spell/{id}.json' for id in ['arcane_tier_2_spell_2_root', 'arcane_tier_2_spell_2_modifier_2', 'fire_tier_2_spell_1_modifier_1', 'fire_tier_2_spell_1_modifier_2', 'fire_tier_2_spell_2_modifier_1', 'frost_tier_2_spell_1_modifier_1', 'frost_tier_2_spell_2_modifier_1', 'frost_tier_2_spell_2_modifier_2']} | {'assets/skill_tree_rpgs/lang/en_us.json'},
    }
    for name, paths in allowed.items():
        with zipfile.ZipFile(root / name) as after, zipfile.ZipFile(original / name) as before:
            check(after.testzip(), None, name + ' archive CRC')
            check(set(after.namelist()), set(before.namelist()), name + ' entries preserved')
            changed = {n for n in before.namelist() if before.read(n) != after.read(n)}
            check(changed, paths, name + ' exact changed-entry allowlist')
            for n in after.namelist():
                if n.endswith('.json'):
                    json.loads(after.read(n))
    print('All other entries, including classes, category allocations, later tiers and Wave after Wave, are byte-identical.')
print(f'PASS {checks} structural checks')

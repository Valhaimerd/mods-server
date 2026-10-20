from pathlib import Path
import json
import math
import sys
import zipfile

root = Path(sys.argv[1]); checks = 0
def load(ns, id):
    name = 'wizards-neoforge-3.1.2+26.2.jar' if ns == 'wizards' else 'skill_tree-neoforge-1.6.1+26.2.jar'
    with zipfile.ZipFile(root / name) as z: return json.loads(z.read(f'data/{ns}/spell/{id}.json'))
def check(a, b):
    global checks
    assert a == b or isinstance(a, (int, float)) and isinstance(b, (int, float)) and math.isclose(a, b, abs_tol=1e-6), (a, b)
    checks += 1
def damage(d): return next(i['action']['damage'].get('spell_power_coefficient', 1) for i in d['impacts'] if i['action']['type'] == 'DAMAGE')
beam = load('wizards', 'arcane_beam')
check(beam['cost']['cooldown']['duration'], 30)
check(damage(beam) * 5 / 25, .2)
check(beam['target']['beam']['width'], .12)
check(load('skill_tree_rpgs', 'arcane_tier_3_spell_1_modifier_1')['modifiers'][0]['impacts'][0]['action']['status_effect']['amplifier_cap'], 24)
barrage = load('wizards', 'arcane_barrage')
check(barrage['cost']['cooldown']['duration'], 6)
check(barrage['impacts'][0]['action']['summon']['behaviour']['lifespan']['active_seconds'], 7)
meteor = load('wizards', 'fire_meteor')
check(meteor['active']['cast']['duration'], 1)
check(damage(meteor), 1.5)
check(meteor['deliver']['meteor']['launch_properties']['extra_launch_count'], 2)
check(meteor['deliver']['meteor']['launch_radius'], 5)
check(meteor['area_impact']['radius'], 10)
check(meteor['area_impact']['area']['distance_dropoff'], 'NONE')
check(meteor['cost']['cooldown']['duration'], 16)
splash = load('skill_tree_rpgs', 'fire_tier_3_spell_1_modifier_2')
check(splash['deliver']['delay'], 5)
check(splash['deliver']['clouds'][0]['volume']['radius'], 5)
check(splash['deliver']['clouds'][0]['time_to_live_seconds'], 6)
check(damage(splash), .5)
storm = load('wizards', 'fire_storm')
check(damage(storm) * 5 / 8, .7)
check(storm['cost']['cooldown']['duration'], 25)
check(storm['range'], 15)
shield = load('wizards', 'frost_shield')
check(shield['cost']['cooldown']['duration'], 12)
check(shield['impacts'][0]['action']['status_effect']['duration'], 3)
check(shield['impacts'][0]['action']['status_effect']['amplifier'], 1)
check(load('skill_tree_rpgs', 'frost_tier_3_spell_1_modifier_1')['modifiers'][0]['impacts'][0]['action']['status_effect']['duration'], 3)
check(load('skill_tree_rpgs', 'frost_tier_3_spell_1_modifier_2')['modifiers'][0]['effect_duration_add'], 1)
lance = load('wizards', 'frost_lance')
check(lance['active']['cast']['duration'], 1)
check(damage(lance), 2)
check(lance['cost']['cooldown']['duration'], 12)
check(lance['deliver']['projectile']['launch_properties']['velocity'], 1.5)
check(lance['impacts'][0]['action']['damage']['knockback'], 2)
freeze = lance['impacts'][1]['action']['status_effect']
check(freeze['effect_id'], 'wizards:frozen')
check(freeze['duration'], 4)
check('apply_limit' in freeze, False)
area = load('skill_tree_rpgs', 'frost_tier_3_spell_2_modifier_2')['modifiers'][0]['replacing_area_impact']
check(area['radius'], 4)
check(area['area']['distance_dropoff'], 'NONE')
for name in ['wizards-neoforge-3.1.2+26.2.jar', 'skill_tree-neoforge-1.6.1+26.2.jar', 'spell_engine-neoforge-1.10.5+26.2.jar']:
    with zipfile.ZipFile(root / name) as z:
        check(z.testzip(), None)
        for n in z.namelist():
            if n.endswith('.json'): json.loads(z.read(n))
print(f'PASS {checks} structural checks')

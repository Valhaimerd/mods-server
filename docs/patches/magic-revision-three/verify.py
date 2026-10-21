from pathlib import Path
import json
import math
import sys
import zipfile

root = Path(sys.argv[1]); count = 0
def load(ns, id):
    name = 'wizards-neoforge-3.1.2+26.2.jar' if ns == 'wizards' else 'skill_tree-neoforge-1.6.1+26.2.jar'
    with zipfile.ZipFile(root / name) as z: return json.loads(z.read(f'data/{ns}/spell/{id}.json'))
def check(a, b):
    global count
    assert a == b or isinstance(a, (int, float)) and isinstance(b, (int, float)) and math.isclose(a, b, abs_tol=1e-6), (a, b)
    count += 1
def damage(d): return next(i['action']['damage'].get('spell_power_coefficient', 1) for i in d['impacts'] if i['action']['type'] == 'DAMAGE')
missiles = load('wizards', 'arcane_missile')
check(missiles['cost']['cooldown']['duration'], 16)
check(damage(missiles) * 4 / 12, .24)
check(missiles['active']['cast']['channel']['ticks'], 12)
explosion = load('wizards', 'arcane_explosion')
check(damage(explosion), 2.8); check(explosion['target']['type'], 'AIM'); check(explosion['area_impact']['radius'], 7)
chain = load('skill_tree_rpgs', 'arcane_tier_2_spell_2_modifier_2')
check(damage(chain), .3); check(chain['target']['type'], 'FROM_TRIGGER'); check(chain['deliver']['delay'], 10)
check(chain['passive']['triggers'][0]['chance'], 1); check(chain['cost']['cooldown']['duration'], 0)
echo = load('skill_tree_rpgs', 'arcane_tier_2_spell_2_modifier_1')
check(echo['passive']['triggers'][0]['chance'], 1); check(echo['cost']['cooldown']['duration'], 0)
check(damage(load('wizards', 'fire_breath')) * 5 / 25, .15)
check(damage(load('wizards', 'fire_storm')) * 5 / 8, .4)
meteor = load('wizards', 'fire_meteor')
check(meteor['active']['cast']['duration'], 2); check(meteor['area_impact']['radius'], 6)
check(meteor['area_impact']['area']['distance_dropoff'], 'NONE')
check(meteor['deliver']['meteor']['projectile']['client_data']['composite_model']['models'][0]['fx']['scale'], 1.5)
check([p['appearance']['scale'] for p in meteor['area_impact']['visuals']['particles'] if p['id'].startswith('spell_engine:area_effect_')], [6])
check(damage(load('skill_tree_rpgs', 'fire_tier_3_spell_1_modifier_2')), .1)
spikes = load('wizards', 'frost_spikes'); check(damage(spikes), .8)
fan = load('skill_tree_rpgs', 'frost_tier_2_spell_2_modifier_2')['modifiers'][0]
check(.8 * (1.1 + fan['power_modifier']['power_multiplier']), .25)
check(damage(load('wizards', 'frost_nova')), 1.8)
check(damage(load('skill_tree_rpgs', 'frost_tier_2_spell_1_modifier_1')), .2)
check(load('skill_tree_rpgs', 'frost_tier_2_spell_1_root')['modifiers'][0]['effect_duration_add'], .5)
shatter = load('skill_tree_rpgs', 'frost_tier_3_spell_2_modifier_2')['modifiers'][0]
check(shatter['replacing_area_impact']['radius'], 8); check(shatter['projectile_perks']['pierce'], -999)
check(shatter['replacing_area_impact']['area']['distance_dropoff'], 'NONE')
beam = load('wizards', 'arcane_beam'); check(beam['target']['beam']['width'], .104)
exposure = load('skill_tree_rpgs', 'arcane_tier_3_spell_1_modifier_1')['modifiers'][0]['impacts'][0]['action']['status_effect']
check(exposure['duration'], 3); check(exposure['amplifier_cap'], 24)
charged = load('skill_tree_rpgs', 'arcane_tier_3_spell_1_modifier_2')['modifiers'][0]
check(charged['channel_ticks_add'], 25); check('mutate_impacts' in charged, False)
for name in ['wizards-neoforge-3.1.2+26.2.jar', 'skill_tree-neoforge-1.6.1+26.2.jar', 'spell_engine-neoforge-1.10.5+26.2.jar']:
    with zipfile.ZipFile(root / name) as z:
        check(z.testzip(), None)
        for n in z.namelist():
            if n.endswith('.json'): json.loads(z.read(n))
print(f'PASS {count} structural checks')

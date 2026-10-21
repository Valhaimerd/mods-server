from pathlib import Path
import json
import math
import sys
import zipfile

root = Path(sys.argv[1]); count = 0
def check(a, b):
    global count
    assert a == b or isinstance(a, (int, float)) and isinstance(b, (int, float)) and math.isclose(a, b, abs_tol=1e-6), (a, b)
    count += 1
def spell(id):
    with zipfile.ZipFile(root / 'wizards-neoforge-3.1.2+26.2.jar') as z: return json.loads(z.read('data/wizards/spell/' + id + '.json'))
d = spell('arcane_missile')
check(d['range'], 32); check(d['impacts'][0]['action']['damage']['spell_power_coefficient'] * 4 / 12, .26)
check(d['cost']['cooldown']['duration'], 16); check(d['active']['cast']['duration'], 4)
check(spell('arcane_explosion')['active']['cast']['duration'], 2)
check(spell('arcane_explosion')['active']['cast'].get('type', 'STANDARD'), 'STANDARD')
check(spell('fire_breath')['cost']['cooldown']['duration'], 8)
check(spell('fire_breath')['cost']['cooldown']['proportional'], True)
check(spell('arcane_barrage')['impacts'][0]['action']['summon']['behaviour']['lifespan']['active_seconds'], 6)
check(spell('frost_shield')['impacts'][0]['action']['status_effect']['duration'], 3)
burst = spell('frost_shield_burst')
check(burst['range'], 5); check(burst['target']['type'], 'AREA')
check([i['action']['type'] for i in burst['impacts']], ['STATUS_EFFECT'])
check(burst['impacts'][0]['action']['status_effect']['effect_id'], 'wizards:frozen')
check(burst['impacts'][0]['action']['status_effect']['duration'], 2)
check('apply_limit' in burst['impacts'][0]['action']['status_effect'], False)
for id, expected in [('arcane_explosion', 2.6), ('fire_slash', 1), ('arcane_barrage_bolt', .4), ('arcane_bolt', .7)]:
    check(spell(id)['impacts'][0]['action']['damage']['spell_power_coefficient'], expected)
check(spell('fire_breath')['impacts'][0]['action']['damage']['spell_power_coefficient'] * 5 / 25, .13)
check(spell('frost_nova')['cost']['cooldown']['duration'], 12)
check(spell('fire_meteor')['cost']['cooldown']['duration'], 20)
check(spell('arcane_barrage')['impacts'][0]['action']['summon']['behaviour']['actions'][0]['spell_cast']['spell_id'], 'wizards:arcane_barrage_bolt')
with zipfile.ZipFile(root / 'adventurerskills-EN-1.3-26.2-neoforge-fixed.jar') as z:
    d = json.loads(z.read('data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json'))['magistrate']
    check('Critical' in d['description']['text'], False)
    check(d['title']['color'], 'blue')
    check([r['data']['attribute'] for r in d['rewards']], ['spell_power:arcane', 'spell_power:fire', 'spell_power:frost', 'spell_power:haste', 'puffish_attributes:magic_resistance'])
with zipfile.ZipFile(root / 'skill_tree-neoforge-1.6.1+26.2.jar') as z:
    d = json.loads(z.read('assets/skill_tree_rpgs/lang/en_us.json'))
    check('160%%' in d['spell.skill_tree_rpgs.arcane_tier_2_spell_2_modifier_1.description'], True)
for name in ['adventurerskills-EN-1.3-26.2-neoforge-fixed.jar', 'orb_of_oblivion_puffish_compat-1.0.2.jar', 'wizards-neoforge-3.1.2+26.2.jar', 'skill_tree-neoforge-1.6.1+26.2.jar', 'spell_engine-neoforge-1.10.5+26.2.jar']:
    with zipfile.ZipFile(root / name) as z:
        check(z.testzip(), None)
        for n in z.namelist():
            if n.endswith('.json'): json.loads(z.read(n))
print(f'PASS {count} structural checks')

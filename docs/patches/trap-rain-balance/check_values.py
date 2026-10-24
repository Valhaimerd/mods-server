from pathlib import Path
from zipfile import ZipFile
import json,sys
ROOT=Path(__file__).resolve().parents[3];folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods'
def read(pattern,key):
 with ZipFile(next(folder.glob(pattern))) as z:return json.loads(z.read(key))
t=read('rogues-*.jar','data/rogues/spell/bear_trap.json')
assert t['impacts'][0]['action']['damage']['spell_power_coefficient']==.4,'Bear Trap damage must be 40% D'
status={i['action']['status_effect']['effect_id']:i['action']['status_effect'] for i in t['impacts'] if i['action']['type']=='STATUS_EFFECT'}
assert status['rogues:bear_trap']['duration']==1
assert status['minecraft:slowness']['duration']==3
assert t['active']['cast']['animation']['id']=='spell_engine:one_handed_ground_charge'
r=read('archers-*.jar','data/archers/spell/rain_of_arrows.json')
assert r['impacts'][0]['action']['damage']['spell_power_coefficient']==.6
assert r['deliver']['meteor']['launch_radius']==6 and r['area_impact']['radius']==2.5
m=read('skill_tree-*.jar','data/skill_tree_rpgs/spell/archer_tier_4_spell_1_modifier_2.json')['modifiers'][0]
assert 5+m['projectile_launch']['extra_launch_delay']==7
assert m['projectile_launch']['extra_launch_count']==20
print('PASS Bear Trap 40%/1s root/3s slow, cast animation retained, Rain 60%, Endless 7 ticks, existing scatter/splash/counts')

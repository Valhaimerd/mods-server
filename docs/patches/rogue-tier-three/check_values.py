from pathlib import Path
from zipfile import ZipFile
import json,sys
root=Path(__file__).resolve().parents[3]
folder=Path(sys.argv[1]) if len(sys.argv)>1 else root/'mods'
def spell(jar,name):
 with ZipFile(next(folder.glob(jar+'-*.jar'))) as z:return json.loads(z.read(name))
v=spell('rogues','data/rogues/spell/vanish.json')
assert v['cost']['cooldown']['duration']==8,'Vanish must have 8s cooldown'
assert v['impacts'][0]['action']['status_effect']['duration']==7
m=spell('rogues','data/rogues/spell/mutilate.json')
assert m['deliver']['melee']['attacks'][0]['damage_bonus']==1
p=spell('skill_tree','data/skill_tree_rpgs/spell/rogue_tier_3_passive_2.json')
assert p['school']=='spell_power:physical_melee_dual'
assert p['passive']['triggers'][0]['chance']==1
assert 'target_override' not in p['passive']['triggers'][0]
assert p['impacts'][0]['action']['type']=='DAMAGE'
assert p['impacts'][0]['action']['damage']['spell_power_coefficient']==.25
assert p['cost']['cooldown']['duration']==1
print('PASS Rogue III configuration')

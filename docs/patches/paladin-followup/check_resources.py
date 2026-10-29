from pathlib import Path
from zipfile import ZipFile
import json,sys
root=Path(sys.argv[1]);z=ZipFile(next(root.glob('paladins-*.jar')))
for name,coefficient in [('oath_landing',.7),('oath_landing_judgement',1.)]:
 d=json.loads(z.read('data/paladins/spell/'+name+'.json'));assert d['school']=='spell_power:physical_melee',name;assert abs(d['impacts'][0]['action']['damage']['spell_power_coefficient']-coefficient)<.0001,name
z=ZipFile(next(root.glob('skill_tree-*.jar')))
d=json.loads(z.read('data/skill_tree_rpgs/spell/paladin_tier_4_spell_1_root.json'));assert d.get('range')==8,d.get('range');assert d['modifiers'][0].get('range_add')==2
print('PASS Oath melee coefficients and Improved Banner8-block serialized range')

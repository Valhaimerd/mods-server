from pathlib import Path
from zipfile import ZipFile
import json,sys
folder=Path(sys.argv[1]) if len(sys.argv)>1 else Path('mods')
def spell(pattern,id):
 with ZipFile(next(folder.glob(pattern))) as z:return json.loads(z.read('data/'+id.replace(':','/spell/')+'.json'))
s=spell('rogues-*.jar','rogues:shock_powder')
assert s['cost']['cooldown']['duration']==12, 'Shock Powder must have 12-second cooldown'
s=spell('rogues-*.jar','rogues:slice_and_dice')
assert s['cost']['cooldown']['duration']==12
assert s['deliver']['stash_effect']['amplifier']==9 and s['deliver']['stash_effect']['consume']==1
assert s['impacts'][0]['action']['damage']['spell_power_coefficient']==.15
s=spell('skill_tree-*.jar','skill_tree_rpgs:rogue_tier_2_spell_1_modifier_1')
assert s['passive']['triggers'][0]['chance']==1 and s['area_impact']['area']['distance_dropoff']=='NONE'
s=spell('skill_tree-*.jar','skill_tree_rpgs:rogue_tier_1_passive_2')
assert not any(i['action']['type']=='DAMAGE' for i in s['impacts'])
assert s['cost']['cooldown']['duration']==1
print('PASS requested Rogue resource values')

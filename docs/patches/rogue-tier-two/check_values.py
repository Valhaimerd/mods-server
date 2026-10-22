from pathlib import Path
from zipfile import ZipFile
import json,sys
folder=Path(sys.argv[1]) if len(sys.argv)>1 else Path('mods')
def spell(pattern,id):
 with ZipFile(next(folder.glob(pattern))) as z:return json.loads(z.read('data/'+id.replace(':','/spell/')+'.json'))
s=spell('rogues-*.jar','rogues:bear_trap');assert s['cost']['cooldown']['duration']==4,'Bear Trap cooldown must be 4 seconds'
assert s['active']['cast']['duration']==.2
assert s['deliver']['clouds'][0]['volume']['radius']==1.5
assert len(s['deliver']['clouds'][0].get('additional_placements',[]))==0
s=spell('rogues-*.jar','rogues:shadow_step');assert s['range']==14
s=spell('skill_tree-*.jar','skill_tree_rpgs:rogue_tier_2_passive_1');assert s['deliver']['type']=='DIRECT'
assert s['impacts'][0]['action']['status_effect']['duration']==2
print('PASS Rogue tier-two resource regression')

from pathlib import Path
from zipfile import ZipFile
import json,sys
R=Path(__file__).resolve().parents[3];W=R/'tmp/combat-followup-phase-one';base=W/('before' if '--baseline' in sys.argv else 'built');checks=[]
def read(pattern,path):
 with ZipFile(next(base.glob(pattern))) as z:return json.loads(z.read(path))
def check(ok,label):
 checks.append((bool(ok),label))
d=read('rogues-*','data/rogues/spell/mutilate.json');check(d['range']==8,'Mutilate eight-block range');check(d['active']['cast'].get('duration')==.25,'Mutilate quarter-second cast');check(d['target']['type']=='AIM' and d['target']['aim'].get('required'),'Mutilate requires target')
for skill in ['throw','throw_net']:
 d=read('rogues-*',f'data/rogues/spell/{skill}.json');check(d['deliver']['projectile'].get('launch_properties',{}).get('velocity',1)==1.6,skill+' speed')
for id,cd in [('warrior_tier_2_passive_1',1),('warrior_tier_3_passive_2',30)]:
 d=read('skill_tree-*',f'data/skill_tree_rpgs/spell/{id}.json');check(d['cost']['cooldown']['duration']==cd,id+' cooldown')
d=read('skill_tree-*','data/skill_tree_rpgs/spell/warrior_tier_2_passive_2.json');check(d['deliver']['stash_effect']['duration']==8,'Momentum eight-second readiness');check(d['impacts'][0]['action']['status_effect']['duration']==5,'Momentum five-second buff')
for ok,label in checks:print(('PASS ' if ok else 'FAIL ')+label)
if not all(x[0] for x in checks):sys.exit(1)

from pathlib import Path
from zipfile import ZipFile
import json,sys
folder=Path(sys.argv[1])
with ZipFile(next(folder.glob('skill_tree-*.jar'))) as z:
 for i in [1,2]:
  d=json.loads(z.read(f'data/skill_tree_rpgs/spell/paladin_tier_1_passive_{i}.json'))
  assert d['impacts'][0]['action']['status_effect']['duration']==5
  assert d['impacts'][0]['action']['status_effect']['amplifier_cap']==3
  assert d['cost']['cooldown']['duration']==(1 if i==1 else 0)
  if i==2:assert len(d['passive']['triggers'])==3 and 'critical' not in str(d['passive'])
print('PASS new passive resources')

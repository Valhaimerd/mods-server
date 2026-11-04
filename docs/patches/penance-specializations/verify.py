from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/penance-specializations'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
count=0
def check(ok):
 global count
 assert ok
 count+=1
for row in json.loads((H/'artifacts.json').read_text()):
 check(sha(W/'built'/row['jar'])==row['after'])
 with ZipFile(W/'built'/row['jar']) as new,ZipFile(W/'before'/row['jar']) as old:
  check(new.testzip() is None);check(len(new.namelist())==len(set(new.namelist())))
  check(set(new.namelist())==set(old.namelist()))
  check({n for n in old.namelist() if old.read(n)!=new.read(n)}==set(row['changed']))
  check(new.read('META-INF/neoforge.mods.toml')==old.read('META-INF/neoforge.mods.toml'))
  if row['jar'].startswith('skill_tree-'):
   key='assets/skill_tree_rpgs/lang/en_us.json';a=json.loads(old.read(key));b=json.loads(new.read(key))
   check({k for k in a if a[k]!=b[k]}=={'spell.skill_tree_rpgs.priest_tier_3_spell_2_modifier_1.description','spell.skill_tree_rpgs.priest_tier_3_spell_2_modifier_2.description'})
 if '--installed' in sys.argv:
  check(json.loads((H/'client-tested.json').read_text())[row['jar']]==row['after'])
  for folder in ['mods','mod fixing/1 fixed packs']:check(sha(R/folder/row['jar'])==row['after'])
(H/'verification.json').write_text(json.dumps({'checks':count,'installed':'--installed' in sys.argv},indent=2)+'\n')
print('PASS',count,'artifact/install checks')

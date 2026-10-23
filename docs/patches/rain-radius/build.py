from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import json,hashlib,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/rain-radius';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True)
records=[]
for pattern in ['archers-*.jar','skill_tree-*.jar']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 with ZipFile(source) as z:files={n:z.read(n) for n in z.namelist()}
 old=dict(files)
 if active.name.startswith('archers-'):
  n='data/archers/spell/rain_of_arrows.json';o=json.loads(files[n]);o['deliver']['meteor']['launch_radius']=6;files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 for n in ['assets/archers/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']:
  if n not in files:continue
  o=json.loads(files[n])
  for key,value in o.items():
   if key=='spell.archers.rain_of_arrows.description' or key in ['spell.skill_tree_rpgs.archer_tier_4_spell_1_modifier_1.description','spell.skill_tree_rpgs.archer_tier_4_spell_1_modifier_2.description']:o[key]=value.replace('8-block','6-block')
  files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 with ZipFile(OUT/active.name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({'jar':active.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':[]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n',encoding='utf-8')
print('Built two resource-only six-block Rain candidates')

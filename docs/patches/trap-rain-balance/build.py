"""Layer only the requested Bear Trap/Rain resource changes over current pack JARs."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/trap-rain-balance';BEFORE=WORK/'before';OUT=WORK/'built';BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True)
def update(n,o):
 if n=='data/rogues/spell/bear_trap.json':
  o['impacts'][0]['action']['damage']['spell_power_coefficient']=.4
  for i in o['impacts']:
   status=i['action'].get('status_effect')
   if status and status['effect_id']=='rogues:bear_trap':status['duration']=1
   if status and status['effect_id']=='minecraft:slowness':status['duration']=3
 if n=='data/archers/spell/rain_of_arrows.json':o['impacts'][0]['action']['damage']['spell_power_coefficient']=.6
 if n=='data/skill_tree_rpgs/spell/archer_tier_4_spell_1_modifier_2.json':o['modifiers'][0]['projectile_launch']['extra_launch_delay']=2
 if n=='assets/rogues/lang/en_us.json':o['spell.rogues.bear_trap.description']=o['spell.rogues.bear_trap.description'].replace('60%% dual-melee damage and 2 seconds of root','40%% dual-melee damage and 1 second of root').replace('Slowness I for 4 seconds','Slowness I for 3 seconds')
 if n=='assets/archers/lang/en_us.json':o['spell.archers.rain_of_arrows.description']=o['spell.archers.rain_of_arrows.description'].replace('40%%','60%%')
 if n=='assets/skill_tree_rpgs/lang/en_us.json':
  key='spell.skill_tree_rpgs.archer_tier_4_spell_1_modifier_2.description';o[key]=o[key].replace('every 0.25 seconds','every 0.35 seconds')
  key='spell.skill_tree_rpgs.rogue_tier_3_spell_2_root.description';o[key]=o[key].replace('instead of 60%%','instead of 40%%')
 return o
records=[]
for pattern in ['rogues-*.jar','archers-*.jar','skill_tree-*.jar']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 with ZipFile(source) as z:files={n:z.read(n) for n in z.namelist()}
 old=dict(files)
 for n,data in list(files.items()):
  if n.endswith('.json') and (n.startswith('data/rogues/spell/') or n.startswith('data/archers/spell/') or n.startswith('data/skill_tree_rpgs/spell/') or n in ['assets/rogues/lang/en_us.json','assets/archers/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']):
   original=json.loads(data);new=update(n,json.loads(data))
   if original!=new:files[n]=(json.dumps(new,indent=2,ensure_ascii=False)+'\n').encode()
 with ZipFile(OUT/active.name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({'jar':active.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':[]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n',encoding='utf-8');print('Built three resource-only candidates')

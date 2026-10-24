from pathlib import Path
from zipfile import ZipFile
import json,hashlib,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/rogue-dual-balance';BEFORE=WORK/'before';OUT=WORK/'built';BEFORE.mkdir(exist_ok=True);OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');ASM=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));classes=WORK/'classes';classes.mkdir(exist_ok=True)
subprocess.run([str(JAVA/'javac.exe'),'--release','25','-cp',str(ASM),'-d',str(classes),str(HERE/'PatchDual.java')],check=True)
def patch(data,mode):
 (WORK/'in.class').write_bytes(data);subprocess.run([str(JAVA/'java.exe'),'-cp',str(classes)+';'+str(ASM),'PatchDual',str(WORK/'in.class'),str(WORK/'out.class'),mode],check=True);return (WORK/'out.class').read_bytes()
def text(o):
 if isinstance(o,str):return o.replace('+2% Attack Speed','+1% Attack Speed')
 if isinstance(o,list):return [text(v) for v in o]
 if isinstance(o,dict):return {k:text(v) for k,v in o.items()}
 return o
records=[]
for pattern in ['adventurerskills-*','rogues-*','skill_tree-*','spell_engine-*']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 for n,data in old.items():
  if not n.endswith('.json'):continue
  if n.startswith('data/rogues/spell/') or n.startswith('data/skill_tree_rpgs/spell/rogue_'):
   o=json.loads(data)
   if n.startswith('data/skill_tree') or o.get('group','').startswith('rogue_'):
    if o.get('school')=='spell_power:physical_melee':o['school']='spell_power:physical_melee_dual'
   if n=='data/rogues/spell/slice_and_dice.json':o['impacts'][0]['action']['damage']['spell_power_coefficient']=.08
   if n=='data/skill_tree_rpgs/spell/rogue_tier_2_spell_1_modifier_1.json':o['impacts'][0]['action']['damage']['spell_power_coefficient']=.4
   if n=='data/skill_tree_rpgs/spell/rogue_tier_3_spell_1_modifier_2.json':o['modifiers'][0]['impacts'][0]['action']['damage']['spell_power_coefficient']=.3
   if n=='data/skill_tree_rpgs/spell/rogue_tier_4_spell_2_modifier_2.json':o['modifiers'][0]['impacts'][0]['action']['status_effect']['duration']=10
   if o!=json.loads(data):files[n]=(json.dumps(o,indent=2)+'\n').encode()
  if n.endswith('/definitions.json') and '/puffish_skills/categories/' in n:
   o=json.loads(data)
   if pattern.startswith('adventurer'):
    o['attack_speed+5%']['rewards'][0]['data']['value']=.02;o['attack_speed+5%']['title']='+2% Attack Speed'
   elif pattern.startswith('skill_tree'):
    for key in ['rogue_root','rogue_boost']:
     o[key]['rewards'][0]['data']['value']=.01;o[key]=text(o[key])
   if o!=json.loads(data):files[n]=(json.dumps(o,indent=2)+'\n').encode()
  if n=='assets/rogues/lang/en_us.json':
   o=json.loads(data);o['spell.rogues.slice_and_dice.description']='Your next 10 melee hits deal an additional 8%% dual-wield damage and pull enemies closer. Each hit grants 5%% evasion for 4s, stacking up to 50%%.';files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
  if n=='assets/skill_tree_rpgs/lang/en_us.json':
   o=json.loads(data)
   for key,value in {'rogue_tier_2_spell_1_modifier_1':'Shock Powder explodes for 40%% dual-wield damage within 4 blocks.','rogue_tier_1_passive_2':'Hits can reduce enemy armor by 2 armor plus 5%% of dual-wield power per stack. Stacks up to 3 times for 6s.','rogue_tier_3_spell_1_modifier_2':'Shadowstep deals 30%% dual-wield damage and applies Blindness I, Weakness I and Slowness I for 2s.','rogue_tier_4_spell_2_modifier_1':'A successful Mutilate heals 15%% of dual-wield power, once per cast.','rogue_tier_4_spell_2_modifier_2':'Mutilate applies Bleed for 10s.','rogue_tier_3_passive_2':'Evading readies a basic melee hit that deals an additional 15%% dual-wield damage.'}.items():o['spell.skill_tree_rpgs.'+key+'.description']=value
   files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 if pattern.startswith('spell_engine'):
  for n,mode in [('net/spell_engine/internals/RogueRules.class','fracture'),('net/spell_engine/internals/PreparedCounter.class','counter'),('net/spell_engine/internals/CrimsonHealing.class','crimson'),('net/spell_engine/api/effect/BleedStatusEffect.class','bleed')]:files[n]=patch(files[n],mode)
  n='crimson_heal.json';o=json.loads(files[n]);o['action']['heal']['spell_power_coefficient']=.15;files[n]=json.dumps(o).encode()
 with ZipFile(OUT/active.name,'w') as z:
  for i in infos:z.writestr(i,files[i.filename])
 records.append({'jar':active.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built four Rogue dual-power/attack-speed candidates')

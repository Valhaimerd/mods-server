"""Overlay the current pack, preserving every unrelated entry and saved ID."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/warrior-balance-20261025';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
inputs=[]
for pattern in ['rogues-*.jar','skill_tree-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 inputs.append(source)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text()
cp=';'.join(str(p) for p in inputs)+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm'
asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'))
classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'WarriorBalance.java'),str(HERE/'PatchBalance.java')]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
def patch(data,mode):
 (WORK/'in.class').write_bytes(data)
 subprocess.run([str(JAVA/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchBalance',str(WORK/'in.class'),str(WORK/'out.class'),mode],check=True)
 return (WORK/'out.class').read_bytes()
descriptions={
 'rogues':{
  'mortal_strike':'Each pulse fully heals you and builds attack speed, basic melee damage and knockback resistance, but increases damage taken. Grants rising Speed for 2s per pulse. Lasts 14s. Fully heals on expiry or fatal-hit rescue; rescue also puts early skills on cooldown and leaves you stunned for 5s and nauseated for 10s.',
  'shout':'Weaken and reveal nearby enemies for 6s, reducing melee, ranged and magic power by 12%%. Break free of movement impairments and resist them for 3s.'},
 'skill_tree_rpgs':{
  'warrior_tier_1_passive_1':'Hits build 2%% melee damage per stack, up to 10%%, for 8s. Kills restore 4%% of your maximum health.',
  'warrior_tier_3_spell_1_root':'Wild Charge and its protection against movement impairments last 1s longer.',
  'warrior_tier_3_spell_1_modifier_2':'Wild Charge readies your next melee hit to stun for 1s. Use within 4s.',
  'warrior_tier_4_spell_1_root':'Berserker lasts 3s longer.',
  'warrior_tier_3_passive_1':'As health falls, gain up to 18%% melee damage and 36%% size. Bonuses fade as you recover.',
  'warrior_tier_3_passive_2':'At 30%% health or lower, gain an absorption shield for 14s and stun nearby enemies for 2s. Melee power strengthens the shield.'}}
records=[]
for source in inputs:
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(n,fn):
  obj=json.loads(files[n]);fn(obj);files[n]=(json.dumps(obj,indent=2,ensure_ascii=False)+'\n').encode()
 if source.name.startswith('spell_engine-'):
  for name,mode in [('WarriorRules','rules'),('WarriorTierTwo','tierTwo'),('WarriorUltimates','ultimate')]:
   n='net/spell_engine/entity/'+name+'.class';files[n]=patch(files[n],mode)
  n='net/spell_engine/entity/WarriorBalance.class';files[n]=(classes/n).read_bytes()
 if source.name.startswith('rogues-'):
  edit('data/rogues/spell/mortal_strike.json',lambda o:o['impacts'][0]['action']['status_effect'].update(duration=14))
 if source.name.startswith('skill_tree-'):
  edit('data/skill_tree_rpgs/spell/warrior_tier_3_spell_1_modifier_2.json',lambda o:o['impacts'][0]['action']['status_effect'].update(duration=1))
  edit('data/skill_tree_rpgs/spell/warrior_tier_4_spell_1_modifier_1.json',lambda o:o['modifiers'][0]['impacts'][0]['action']['status_effect'].update(duration=14))
  edit('data/skill_tree_rpgs/spell/warrior_tier_4_spell_2_modifier_2.json',lambda o:o['cost']['cooldown'].update(duration=.25,haste_affected=True))
  edit('data/skill_tree_rpgs/spell/warrior_tier_4_spell_1_modifier_2.json',lambda o:o['cost']['cooldown'].update(duration=.2,haste_affected=True))
 namespace='rogues' if source.name.startswith('rogues-') else 'skill_tree_rpgs' if source.name.startswith('skill_tree-') else None
 if namespace:
  def language(o):
   for key,value in descriptions[namespace].items():
    k=f'spell.{namespace}.{key}.description';assert k in o,k;o[k]=value
   effects={'rogues':{'demoralize':'Melee, ranged and magic power reduced by 12%.'},'skill_tree_rpgs':{'killing_spree':'Increased melee damage. Kills restore health.','enrage':'Increased melee damage and size.'}}
   for key,value in effects[namespace].items():o[f'effect.{namespace}.{key}.description']=value
  edit(f'assets/{namespace}/lang/en_us.json',language)
 with ZipFile(OUT/source.name,'w') as z:
  for i in infos:z.writestr(i,files[i.filename])
  for n in files.keys()-old.keys():z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(files.keys()-old.keys())})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Built current-input Warrior candidates')

from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/rogue-tier-one';OUT=WORK/'built';OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in (WORK/'before').glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
sources=[HERE/'RogueRules.java',HERE/'PatchRogue.java',ROOT/'docs/patches/magic-third-tier/PartyRules.java',ROOT/'docs/patches/magic-third-tier/FrozenEntityMoveMixin.java']
args=['--release','25','-cp',cp,'-d',str(classes),*[str(p) for p in sources]];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))
def patch(data,mode):
 a=WORK/'input.class';b=WORK/'output.class';a.write_bytes(data);subprocess.run([str(JAVA/'java.exe'),'-cp',str(classes)+';'+str(asm),'PatchRogue',str(a),str(b),mode],check=True);return b.read_bytes()
def update(name,o):
 if name=='data/rogues/spell/shock_powder.json':
  o['cost']['cooldown']['duration']=12;o['impacts'][0]['action']['status_effect'].pop('apply_limit',None)
 if name=='data/rogues/spell/slice_and_dice.json':
  o['cost']['cooldown']['duration']=12
  stash=o['deliver']['stash_effect'];stash['amplifier']=9;stash['consume']=1;stash['duration']=10;stash['triggers'][0].pop('target_override',None)
  o['impacts']=[{'action':{'type':'DAMAGE','damage':{'spell_power_coefficient':.15,'knockback':0}}},{'action':{'type':'STATUS_EFFECT','apply_to_caster':True,'status_effect':{'effect_id':'skill_tree_rpgs:slice_evasion','duration':4,'amplifier':1,'amplifier_cap':9,'apply_mode':'ADD'}}}]
 if name.startswith('data/skill_tree_rpgs/spell/rogue_'):
  key=Path(name).stem
  if key=='rogue_tier_2_spell_1_modifier_1':
   o['passive']['triggers'][0]['chance']=1;o['impacts'][0]['action']['damage']['spell_power_coefficient']=1;o['area_impact']['area']['distance_dropoff']='NONE'
  if key=='rogue_tier_2_spell_1_modifier_1_bonus':o['modifiers']=[]
  if key=='rogue_tier_2_spell_1_modifier_2':o['deliver']['clouds'][0]['time_to_live_seconds']=8
  if key=='rogue_tier_2_spell_2_root':o['modifiers']=[{'spell_pattern':'rogues:slice_and_dice','stash_amplifier_add':2}]
  if key=='rogue_tier_2_spell_2_modifier_1':o['modifiers']=[{'spell_pattern':'rogues:slice_and_dice','stash_amplifier_add':3}]
  if key=='rogue_tier_2_spell_2_modifier_2':
   o['modifiers'][0]['impacts'][0]['action']['status_effect']['amplifier_cap']=11;o['modifiers'][0]['impacts'][0]['action']['apply_to_caster']=True
  if key=='rogue_tier_1_passive_1':o['cost']['cooldown']={'duration':1,'haste_affected':True};[t.update(chance=.25) for t in o['passive']['triggers']]
  if key=='rogue_tier_1_passive_2':
   o['impacts']=[i for i in o['impacts'] if i['action']['type']!='DAMAGE'];o['impacts'][0]['action']['status_effect']['amplifier_cap']=2;o['cost']['cooldown']['duration']=1
 if name=='assets/rogues/lang/en_us.json':
  o['spell.rogues.shock_powder.description']='Stuns enemies within 5 blocks for 3 seconds, or 1 second for high-health mobs and players at 24 HP or more. Improved adds 1 second. Mob threshold: 50 HP plus twice your melee power; players below 24 current HP receive the longer stun.'
  o['spell.rogues.slice_and_dice.description']='For 10 seconds, your next 10 successful melee hits deal 15%% extra melee damage and gently pull enemies toward you. Each hit grants 5 percentage points of evasion for 4 seconds, stacking up to 50 points.'
 if name=='assets/skill_tree_rpgs/lang/en_us.json':
  desc={
   'rogue_tier_2_spell_1_root':'Shock Powder stuns last 1 second longer: 2 or 4 seconds.',
   'rogue_tier_2_spell_1_modifier_1':'Successful Shock Powder impacts always explode for 100%% melee damage within 2.5 blocks, without distance falloff.',
   'rogue_tier_2_spell_1_modifier_1_bonus':'Companion marker; adds no extra stun duration.',
   'rogue_tier_2_spell_1_modifier_2':'Shock Powder leaves a smoke screen for 8 seconds, granting allies inside 50 percentage points of evasion.',
   'rogue_tier_2_spell_2_root':'Slice & Dice empowers 12 successful hits instead of 10.',
   'rogue_tier_2_spell_2_modifier_1':'Slice & Dice empowers 15 successful hits with Improved Slice and Dice.',
   'rogue_tier_2_spell_2_modifier_2':'Empowered Slice & Dice hits grant 5%% base movement speed per stack, up to 12 stacks, for 4 seconds. Further hits refresh the duration.',
   'rogue_tier_1_passive_1':'Melee hits have a 25%% chance to add stacking Poison for 8 seconds. Base cooldown: 1 second, reduced by haste.',
   'rogue_tier_1_passive_2':'Melee hits have a 25%% chance to reduce armor by 2 points plus 10%% of your melee power per stack, up to 3 stacks, for 6 seconds. Further procs refresh it. Base cooldown: 1 second, reduced by haste.'}
  for k,v in desc.items():o['spell.skill_tree_rpgs.'+k+'.description']=v
  o['spell.skill_tree_rpgs.rogue_tier_2_spell_2_modifier_1.name']='Blades of Fury'
  o['effect.skill_tree_rpgs.slice_evasion']='Slice Evasion'
 return o
records=[]
for record in json.loads((HERE/'inputs.json').read_text()):
 jar=WORK/'before'/record['jar'];assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['before_sha256']
 with ZipFile(jar) as z:files={n:z.read(n) for n in z.namelist()}
 old=dict(files)
 for n,data in list(files.items()):
  if n.endswith('.json') and (n.startswith('data/rogues/spell/') or n.startswith('data/skill_tree_rpgs/spell/rogue_') or n in ['assets/rogues/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']):
   original=json.loads(data);new=update(n,json.loads(data))
   if original!=new:files[n]=(json.dumps(new,indent=2,ensure_ascii=False)+'\n').encode()
 if jar.name.startswith('rogues-'):n='net/rogues/effect/RogueEffects.class';files[n]=patch(files[n],'rogue')
 if jar.name.startswith('skill_tree-'):n='net/skill_tree_rpgs/effect/SkillEffects.class';files[n]=patch(files[n],'skill')
 if jar.name.startswith('spell_engine-'):
  for n in ['net/spell_engine/internals/impact/SpellImpacts.class','net/spell_engine/internals/delivery/SpellStashHelper.class']:files[n]=patch(files[n],'')
  for n in ['net/spell_engine/internals/RogueRules.class','net/spell_engine/internals/PartyRules.class','net/spell_engine/internals/PartyRules$Client.class','net/spell_engine/internals/PartyRules$EffectCallbacks.class','net/spell_engine/mixin/thirdtier/FrozenEntityMoveMixin.class']:files[n]=(classes/n).read_bytes()
 with ZipFile(OUT/jar.name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({**record,'sha256':hashlib.sha256((OUT/jar.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built three same-filename Rogue candidates')

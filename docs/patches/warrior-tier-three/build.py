"""Apply the ultimate revision to the exact pinned Warrior tier-II inputs."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/warrior-tier-three';OUT=WORK/'built';OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in (WORK/'before').glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
sources=[p for p in HERE.glob('*.java') if 'Probe' not in p.name]
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in sources]];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))
def patch(data,mode):
 a=WORK/'in.class';b=WORK/'out.class';a.write_bytes(data);subprocess.run([str(JAVA/'java.exe'),'-cp',str(classes)+';'+str(asm),'PatchUltimates',str(a),str(b),mode],check=True);return b.read_bytes()
def status(id,duration,self=False):return {'action':{'type':'STATUS_EFFECT','apply_to_caster':self,'status_effect':{'effect_id':id,'duration':duration}}}
def stacked(id,duration,cap):
 o=status(id,duration,True);o['action']['status_effect'].update(amplifier=1,amplifier_cap=cap-1,apply_mode='ADD');return o
def update(n,o,last):
 if n=='data/rogues/spell/mortal_strike.json':
  o['range']=0;o['active']=last['active'];o['active']['cast'].update(duration=2.5,haste_affected=True);o['release']=last.get('release',{});o.pop('target',None);o['deliver']={'type':'DIRECT'}
  o['impacts']=[stacked('skill_tree_rpgs:berserker',12,5)];o['cost']['cooldown']={'duration':100,'haste_affected':True,'proportional':False}
 if n=='data/rogues/spell/last_stand.json':
  o['impacts']=[status('rogues:last_stand',15,True),{'action':{'type':'IMMUNITY','apply_to_caster':True,'immunity':{'duration_ticks':12}}},status('minecraft:regeneration',15,True)]
  o['cost']['cooldown']={'duration':100,'haste_affected':True,'proportional':False}
 if n.startswith('data/skill_tree_rpgs/spell/warrior_'):
  key=Path(n).stem
  if key=='warrior_tier_2_passive_2':
   stash=o['deliver']['stash_effect'];stash.update(duration=6,amplifier=4,consume=1,consumed_next_tick=True)
   o['impacts']=[stacked('skill_tree_rpgs:warrior_momentum',3,5)];o['cost']['cooldown']['duration']=12
  changes={
   'warrior_tier_4_spell_1_root':{'spell_pattern':'rogues:mortal_strike','effect_duration_add':3},
   'warrior_tier_4_spell_1_modifier_1':{'spell_pattern':'rogues:mortal_strike','mutate_impacts':'APPEND','impacts':[stacked('skill_tree_rpgs:berserker_crit',12,5)]},
   'warrior_tier_4_spell_1_modifier_2':{'spell_pattern':'rogues:mortal_strike'},
   'warrior_tier_4_spell_2_root':{'spell_pattern':'rogues:last_stand','effect_duration_add':5},
   'warrior_tier_4_spell_2_modifier_1':{'spell_pattern':'rogues:last_stand','mutate_impacts':'APPEND','impacts':[stacked('skill_tree_rpgs:juggernaut',15,5)]},
  }
  if key in changes:o['modifiers']=[changes[key]]
  if key=='warrior_tier_4_spell_2_modifier_2':
   o['passive']['triggers']=[{'type':'DAMAGE_TAKEN','target_override':'CASTER','chance':1,'caster_conditions':[{'entity_predicate_id':'spell_engine:has_effect','entity_predicate_param':'rogues:last_stand'}]}]
   o['impacts']=[stacked('skill_tree_rpgs:sentinel',5,10),status('minecraft:regeneration',5,True)];o['cost']['cooldown']={'duration':0,'haste_affected':True}
  if key in ['warrior_tier_3_passive_1','warrior_tier_3_passive_2']:
   o['passive']['triggers']=[];o['deliver']={'type':'DIRECT'}
   if key.endswith('_1'):o['impacts']=[];o['cost']['cooldown']={'duration':0,'haste_affected':True}
   else:o['range']=5;o['target']={'type':'AREA','area':{'include_caster':False}};o['impacts']=[status('spell_engine:stun',2)];o['cost']['cooldown']={'duration':25,'haste_affected':True}
 if n=='assets/rogues/lang/en_us.json':
  o['spell.rogues.mortal_strike.name']='Berserker'
  o['spell.rogues.mortal_strike.description']='Channel five pulses over 2.5 seconds while stationary. Each pulse grants 15%% attack speed, 10%% basic melee attack damage, 15 percentage points of knockback resistance and 20%% more damage taken for 12 seconds. The fifth pulse fully heals you and grants Speed IV for 2 seconds. Natural expiry fully heals you. A fatal hit ends Berserker and fully heals you, but puts both early Warrior tiers on cooldown and causes Nausea for 10 seconds and stun for 5 seconds. Cooldown: 60–100 seconds by completed pulses, reduced by haste. Stun or freeze interrupts the channel; roots do not.'
  o['spell.rogues.last_stand.description']='Channel five pulses over 2.5 seconds while stationary. Every pulse grants 0.6 seconds of damage immunity and refreshes Regeneration I and knockback immunity for 15 seconds. Cooldown: 60–100 seconds by completed pulses, reduced by haste. Stun or freeze interrupts the channel; roots do not.'
 if n=='assets/skill_tree_rpgs/lang/en_us.json':
  items={
   'warrior_tier_2_passive_2':('Momentum','Combat rolls ready five melee hits within 6 seconds. Each hit grants 2%% attack speed and 2%% movement speed, up to 10%% each, for 3 seconds. Further hits refresh the buffs. Base cooldown: 12 seconds, reduced by haste.'),
   'warrior_tier_4_spell_1_root':('Improved Berserker','Berserker lasts 15 seconds instead of 12 seconds.'),
   'warrior_tier_4_spell_1_modifier_1':('Brutality','Every Berserker pulse also adds 4 percentage points of physical critical chance, up to 20 points. Lasts as long as Berserker.'),
   'warrior_tier_4_spell_1_modifier_2':('Lethality','During Berserker, melee hits heal 1 heart. You lose 1 HP every half second, ignoring armor, Resistance, absorption and damage immunity.'),
   'warrior_tier_4_spell_2_root':('Improved Last Stand','Last Stand buffs last 20 seconds instead of 15 seconds. Pulse damage immunity stays at 0.6 seconds.'),
   'warrior_tier_4_spell_2_modifier_1':('Juggernaut','Each Last Stand pulse adds 10%% size and 3%% damage reduction, up to 50%% size and 15%% reduction. Lasts 20 seconds with Improved Last Stand; pulses refresh it.'),
   'warrior_tier_4_spell_2_modifier_2':('Sentinel','During Last Stand, direct damage taken grants 1%% melee damage per stack, up to 10 stacks. Lasts 5 seconds, refreshed by further hits. Also refreshes Regeneration I for 5 seconds.'),
   'warrior_tier_3_passive_1':('Enrage','Checked every quarter second. At 70%% health gain 4%% melee damage and 8%% size; each further 10%% health threshold adds the same bonuses, up to 24%% damage and 48%% size at 20%% health or lower. Buffs last 3 seconds and refresh while eligible. No cooldown.'),
   'warrior_tier_3_passive_2':('Tenacity','At 1–40%% health, gain an absorption shield for 5 seconds. Shield strength is 6 HP plus 30%% of melee power, rounded to the nearest whole HP. Stuns enemies within 5 blocks for 2 seconds. Base cooldown: 25 seconds, reduced by haste.'),
  }
  for key,(name,desc) in items.items():o['spell.skill_tree_rpgs.'+key+'.name']=name;o['spell.skill_tree_rpgs.'+key+'.description']=desc
  o['spell.skill_tree_rpgs.rogue_tier_4_spell_2_root.description']='Reduces Mutilate’s base cooldown by 2 seconds.'
  for key,name in [('berserker','Berserker'),('berserker_crit','Brutality'),('sentinel','Sentinel'),('tenacity_ward','Tenacity'),('enrage','Enrage')]:o['effect.skill_tree_rpgs.'+key]=name
 return o

records=[]
for r in json.loads((HERE/'inputs.json').read_text()):
 jar=WORK/'before'/r['jar'];assert hashlib.sha256(jar.read_bytes()).hexdigest()==r['before_sha256']
 with ZipFile(jar) as z:files={n:z.read(n) for n in z.namelist()}
 old=dict(files);last=json.loads(files['data/rogues/spell/last_stand.json']) if jar.name.startswith('rogues-') else None
 for n,data in list(files.items()):
  if n.endswith('.json') and (n.startswith('data/rogues/spell/') or n.startswith('data/skill_tree_rpgs/spell/warrior_') or n in ['assets/rogues/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']):
   original=json.loads(data);new=update(n,json.loads(data),last)
   if new!=original:files[n]=(json.dumps(new,indent=2,ensure_ascii=False)+'\n').encode()
 if jar.name.startswith('rogues-'):
  n='net/rogues/effect/RogueEffects.class';files[n]=patch(files[n],'rogue')
 if jar.name.startswith('spell_engine-'):
  for n,mode in [('net/spell_engine/internals/SpellParameters.class','cooldown'),('net/spell_engine/internals/SpellTriggers.class','melee')]:files[n]=patch(files[n],mode)
  for p in classes.rglob('*.class'):
   n=p.relative_to(classes).as_posix()
   if not n.startswith('PatchUltimates'):files[n]=p.read_bytes()
  n='spell_engine.mixins.json';o=json.loads(files[n]);o['mixins']+=['warrior.WarriorBasicAttackMixin','warrior.WarriorChannelMixin'];files[n]=(json.dumps(o,indent=2)+'\n').encode()
 with ZipFile(OUT/jar.name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({**r,'sha256':hashlib.sha256((OUT/jar.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Warrior ultimate candidates; active pack unchanged')

"""Apply the second Warrior revision to the exact currently installed first-revision inputs."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/warrior-tier-two';OUT=WORK/'built';OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in (WORK/'before').glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
sources=[HERE/n for n in ['WarriorTierTwo.java','WarriorFreedomMixin.java','PatchTierTwo.java']]+[HERE/'WarriorRules.java']
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in sources]];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))
def patch(data,mode):
 a=WORK/'in.class';b=WORK/'out.class';a.write_bytes(data);subprocess.run([str(JAVA/'java.exe'),'-cp',str(classes)+';'+str(asm),'PatchTierTwo',str(a),str(b),mode],check=True);return b.read_bytes()
def status(id,duration,self=False):return {'action':{'type':'STATUS_EFFECT','apply_to_caster':self,'status_effect':{'effect_id':id,'duration':duration}}}
def update(n,o):
 if n=='data/rogues/spell/throw_net.json':o['cost']['cooldown']['duration']=6
 if n=='data/rogues/spell/charge.json':o['cost']['cooldown']['duration']=16;o['impacts'][0]['action']['status_effect']['duration']=2
 if n=='data/rogues/spell/shout.json':
  o['cost']['cooldown']['duration']=16;o['range']=12;o['target']['area']['include_caster']=True
  original=o['impacts'][0];original['action']['status_effect']={'effect_id':'rogues:demoralize','duration':6}
  o['impacts']=[status('skill_tree_rpgs:warrior_freedom',3,True),original,status('minecraft:glowing',6)]
 if n.startswith('data/skill_tree_rpgs/spell/warrior_'):
  key=Path(n).stem
  if key=='warrior_tier_3_spell_1_root':
   o={'school':'spell_power:physical_melee','range':0,'tier':1,'type':'MODIFIER','modifiers':[{'spell_pattern':'rogues:charge','effect_duration_add':1}]}
  if key=='warrior_tier_3_spell_1_modifier_1':o['modifiers']=[{'spell_pattern':'rogues:charge'}]
  if key=='warrior_tier_3_spell_1_modifier_2':o['deliver']['stash_effect']['duration']=4;o['cost']['cooldown']['duration']=0
  if key=='warrior_tier_3_spell_2_root':o['modifiers']=[{'spell_pattern':'rogues:shout','range_add':4},{'spell_pattern':'skill_tree_rpgs:warrior_tier_3_spell_2_modifier_1','range_add':4}]
  if key=='warrior_tier_3_spell_2_modifier_2':
   o['modifiers'][0]['impacts']=[{'action':{'type':'DAMAGE','damage':{'spell_power_coefficient':.1,'knockback':0}}},{'action':{'type':'AGGRO','aggro':{'only_if_targeted':False,'mode':'SET'}}},status('spell_engine:stun',.2)]
  if key=='warrior_tier_2_passive_1':
   o['passive']['triggers']=[{'type':'ROLL','chance':1,'target_override':'CASTER'}];o['impacts']=[{'action':{'type':'HEAL','apply_to_caster':True,'heal':{'spell_power_coefficient':1}}}];o['cost']['cooldown']['duration']=0
  if key=='warrior_tier_2_passive_2':
   o['school']='spell_power:physical_melee';o['passive']['triggers']=[{'type':'ROLL','chance':1,'target_override':'CASTER'}]
   o['deliver']={'type':'STASH_EFFECT','stash_effect':{'id':'skill_tree_rpgs:warrior_momentum_ready','duration':5,'consumed_next_tick':True,'triggers':[{'type':'MELEE_IMPACT'}]}}
   o['impacts']=[status('skill_tree_rpgs:warrior_momentum',3,True)];o['cost']['cooldown']['duration']=10
 if n=='assets/rogues/lang/en_us.json':
  o['spell.rogues.throw_net.description']=o['spell.rogues.throw_net.description'].replace('Base cooldown: 5 seconds','Base cooldown: 6 seconds')
  o['spell.rogues.charge.name']='Wild Charge'
  o['spell.rogues.charge.description']='For 2 seconds gain 50%% base movement speed and 25 percentage points of knockback resistance. Can be cast while controlled. Removes movement-impairing effects and prevents new ones for 2 seconds. Base cooldown: 16 seconds, reduced by haste.'
  o['spell.rogues.shout.description']='Demoralize and reveal enemies within 12 blocks for 6 seconds, reducing their base melee, ranged and offensive magic power by 8%%. Does not stack or require a health limit. Can be cast while controlled; removes movement-impairing effects and prevents new ones for 3 seconds. Deals no base damage. Base cooldown: 16 seconds, reduced by haste.'
  o['effect.rogues.charge']='Wild Charge'
  o['effect.rogues.demoralize.description']='Melee, ranged and offensive magic power reduced by 8%.'
 if n=='assets/skill_tree_rpgs/lang/en_us.json':
  items={
   'warrior_tier_2_spell_2_modifier_2':('Multiple Nets','Throw Net has a 4-second base cooldown instead of 6 seconds. Its root lasts 1.5 seconds instead of 2.'),
   'warrior_tier_1_passive_2':('Endured Vitality','Direct hits grant 1 armor plus 6%% of your melee power per stack, with the scaling bonus rounded to the nearest half armor point. Also adds 1 maximum heart per stack, healed only when newly added. Up to 10 stacks for 8 seconds, refreshed. Base interval: 0.25 seconds, reduced by haste. Damage over time does not trigger this.'),
   'warrior_tier_3_spell_1_root':('Improved Wild Charge','Wild Charge lasts 1 second longer: 3 seconds total. Its control protection remains 2 seconds.'),
   'warrior_tier_3_spell_1_modifier_1':('Endurance','Wild Charge grants full knockback immunity for its entire duration.'),
   'warrior_tier_3_spell_1_modifier_2':('Concussion Blow','Wild Charge readies your next melee hit to stun for 2 seconds, including bosses. Use within 4 seconds. No additional cooldown.'),
   'warrior_tier_3_spell_2_root':('Improved Shout','Demoralizing Shout and Battle Shout reach 4 blocks farther: 16 blocks total.'),
   'warrior_tier_3_spell_2_modifier_1':('Battle Shout','Casting Demoralizing Shout grants you and party allies 8%% base melee, ranged and offensive magic power for 6 seconds. Uses the same 12-block radius, or 16 with Improved Shout.'),
   'warrior_tier_3_spell_2_modifier_2':('Challenging Shout','Demoralizing Shout also deals 10%% melee damage, makes hostile mobs target you and stuns enemies for 0.2 seconds, including bosses.'),
   'warrior_tier_2_passive_1':('Warm Up','Every combat roll heals 1 HP plus 6%% of your melee power, with the scaling bonus rounded to the nearest half HP. Guaranteed; no additional cooldown.'),
   'warrior_tier_2_passive_2':('Momentum','Every combat roll readies your next melee hit for 5 seconds. That hit grants 5%% base movement and attack speed, 2%% base melee damage and 2%% damage reduction for 3 seconds. Base cooldown: 10 seconds, reduced by haste.'),
  }
  for key,(name,desc) in items.items():o['spell.skill_tree_rpgs.'+key+'.name']=name;o['spell.skill_tree_rpgs.'+key+'.description']=desc
  for key,name in [('warrior_freedom','Unhindered'),('warrior_endurance','Endurance'),('warrior_momentum_ready','Momentum Ready'),('warrior_momentum','Momentum')]:o['effect.skill_tree_rpgs.'+key]=name
 return o
records=[]
for r in json.loads((HERE/'inputs.json').read_text()):
 jar=WORK/'before'/r['jar'];assert hashlib.sha256(jar.read_bytes()).hexdigest()==r['before_sha256']
 with ZipFile(jar) as z:files={n:z.read(n) for n in z.namelist()}
 old=dict(files)
 for n,data in list(files.items()):
  if n.endswith('.json') and (n.startswith('data/rogues/spell/') or n.startswith('data/skill_tree_rpgs/spell/warrior_') or n in ['assets/rogues/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']):
   original=json.loads(data);new=update(n,json.loads(data))
   if new!=original:files[n]=(json.dumps(new,indent=2,ensure_ascii=False)+'\n').encode()
 if jar.name.startswith('rogues-'):
  n='net/rogues/effect/RogueEffects.class';files[n]=patch(files[n],'rogue')
 if jar.name.startswith('spell_engine-'):
  for n,mode in [('net/spell_engine/client/casting/ClientCastController.class','client'),('net/spell_engine/internals/impact/SpellImpacts.class','impacts')]:files[n]=patch(files[n],mode)
  for p in classes.rglob('*.class'):
   n=p.relative_to(classes).as_posix()
   if not n.startswith('PatchTierTwo'):files[n]=p.read_bytes()
  n='spell_engine.mixins.json';o=json.loads(files[n]);o['mixins'].append('warrior.WarriorFreedomMixin');files[n]=(json.dumps(o,indent=2)+'\n').encode()
 with ZipFile(OUT/jar.name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({**r,'sha256':hashlib.sha256((OUT/jar.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Warrior tier-II candidates; active pack unchanged')

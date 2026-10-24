"""Build a narrow Warrior/shield overlay over the pinned current pack, preserving other revisions."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/warrior-tier-one';BEFORE=WORK/'before';OUT=WORK/'built';OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text()
cp=';'.join(p.as_posix() for p in BEFORE.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
sources=list(HERE.glob('*.java'));sources=[p for p in sources if 'Probe' not in p.name]
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in sources]]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))
def patch(data,mode):
 a=WORK/'in.class';b=WORK/'out.class';a.write_bytes(data)
 subprocess.run([str(JAVA/'java.exe'),'-cp',str(classes)+';'+str(asm),'PatchWarrior',str(a),str(b),mode],check=True);return b.read_bytes()
def update(n,o):
 if (n.startswith('data/rogues/spell/') and o.get('group','').startswith('warrior_')) or (n.startswith('data/skill_tree_rpgs/spell/warrior_') and o.get('type')!='MODIFIER'):
  o.setdefault('cost',{}).setdefault('cooldown',{})['haste_affected']=True
  if 'active' in o:o['active'].setdefault('cast',{})['haste_affected']=True
 if n=='data/rogues/spell/throw.json':
  o['cost']['cooldown']['duration']=5;o['deliver']['projectile']['launch_properties']['velocity']=1
  o['deliver']['projectile']['projectile']['perks']={'bounce':0,'ricochet':0,'pierce':0}
  o['impacts'][0]['action']['damage']['spell_power_coefficient']=.8
  o['impacts'][1]['action']['status_effect']={'effect_id':'minecraft:slowness','duration':1}
 if n=='data/rogues/spell/throw_net.json':
  o['range']=12;o['cost']['cooldown']['duration']=5;o['active']['cast']['duration']=.5
  o['active']['cast']['charge']['output_scaling']=1
  o['deliver']['projectile']['projectile']['homing_angle']=2
  o['impacts'][0]['action']['damage']['spell_power_coefficient']=.15
  o['impacts'][1]['action']['status_effect']['duration']=2
 if n.startswith('data/skill_tree_rpgs/spell/warrior_'):
  key=Path(n).stem
  changes={
   'warrior_tier_2_spell_1_root':{'spell_pattern':'rogues:throw'},
   'warrior_tier_2_spell_1_modifier_1':{'spell_pattern':'rogues:throw','projectile_perks':{'pierce':2,'ricochet':0,'ricochet_range':0}},
   'warrior_tier_2_spell_1_modifier_2':{'spell_pattern':'rogues:throw'},
   'warrior_tier_2_spell_2_root':{'spell_pattern':'rogues:throw_net','range_add':6},
   'warrior_tier_2_spell_2_modifier_1':{'spell_pattern':'rogues:throw_net','replacing_area_impact':{'radius':5,'area':{'distance_dropoff':'NONE','vertical_range_multiplier':1}}},
   'warrior_tier_2_spell_2_modifier_2':{'spell_pattern':'rogues:throw_net','cooldown_duration_deduct':2,'effect_duration_add':-.5},
  }
  if key in changes:o['modifiers']=[changes[key]]
  if key=='warrior_tier_1_passive_1':
   for t in o['passive']['triggers']:t.pop('target_conditions',None);t['chance']=1
   o['impacts'][0]['action']['status_effect']['amplifier_cap']=4;o['cost']['cooldown']['duration']=.25
  if key=='warrior_tier_1_passive_2':
   o['passive']['triggers']=[{'type':'DAMAGE_TAKEN','target_override':'CASTER','chance':1}]
   o['impacts'][0]['action']['status_effect']['amplifier_cap']=9;o['cost']['cooldown']['duration']=.25
 if n=='assets/rogues/lang/en_us.json':
  for k,v in {
   'spell.rogues.throw.name':'Throw Weapon',
   'spell.rogues.throw.description':'Throw your weapon for up to 80%% melee damage. Full charge takes 0.5 seconds; half charge deals 40%%. Travels 12–24 blocks with slight homing and slows enemies for 1 second. You gain Weakness IV for 2 seconds after release; the projectile keeps its original damage. Base cooldown: 5 seconds, reduced by haste.',
   'spell.rogues.throw_net.description':'Throw a slightly homing net for up to 15%% melee damage, rooting eligible enemies for 2 seconds and rapidly dragging them toward a point 2 blocks in front of you. Full charge: 0.5 seconds; range: 12–24 blocks. Strong enemies can resist the root and pull. Base cooldown: 5 seconds, reduced by haste.',
  }.items():o[k]=v
 if n=='assets/skill_tree_rpgs/lang/en_us.json':
  items={
   'warrior_tier_2_spell_1_root':('Improved Throw Weapon','Throw Weapon deals up to 90%% melee damage instead of 80%%.'),
   'warrior_tier_2_spell_1_modifier_1':('Strong Throw','Throw Weapon pierces 2 enemies, allowing 3 hits. After each hit, it seeks the next enemy with slight homing. No ricochet or block bounce.'),
   'warrior_tier_2_spell_1_modifier_2':('Head Throw','Throw Weapon deals up to 100%% melee damage and stuns enemies for 0.25 seconds, including bosses.'),
   'warrior_tier_2_spell_2_root':('Improved Throw Net','Throw Net gains 6 blocks of charged range, reaching 30 blocks at full charge.'),
   'warrior_tier_2_spell_2_modifier_1':('Bigger Net','Throw Net also damages and nets enemies within 5 blocks of impact. Each enemy must qualify for the root and pull; party allies and pets are protected.'),
   'warrior_tier_2_spell_2_modifier_2':('Multiple Nets','Throw Net has a 3-second base cooldown instead of 5 seconds. Its root lasts 1.5 seconds instead of 2.'),
   'warrior_tier_1_passive_1':('Killing Spree','Melee hits and active melee-skill hits grant 2%% base melee damage and 2%% damage reduction per stack, up to 5 stacks. Lasts 8 seconds; further hits refresh it. Base cooldown: 0.25 seconds, reduced by haste.'),
   'warrior_tier_1_passive_2':('Endured Vitality','Taking direct attack damage grants 1 armor and 1 maximum heart per stack, up to 10 stacks for 8 seconds. New hearts are healed when added. Refreshing at maximum stacks does not heal. Damage over time and environmental damage do not trigger it. Base cooldown: 0.25 seconds, reduced by haste.'),
  }
  for key,(name,desc) in items.items():o['spell.skill_tree_rpgs.'+key+'.name']=name;o['spell.skill_tree_rpgs.'+key+'.description']=desc
  o['effect.skill_tree_rpgs.vitality']='Endured Vitality'
  o['effect.skill_tree_rpgs.vitality.description']='Increased armor and maximum health from direct hits.'
  o['effect.skill_tree_rpgs.killing_spree.description']='Increased melee damage and reduced incoming damage.'
 return o
records=[]
for record in json.loads((HERE/'inputs.json').read_text()):
 jar=BEFORE/record['jar'];assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['before_sha256']
 with ZipFile(jar) as z:files={n:z.read(n) for n in z.namelist()}
 old=dict(files)
 for n,data in list(files.items()):
  if n.endswith('.json') and (n.startswith('data/rogues/spell/') or n.startswith('data/skill_tree_rpgs/spell/warrior_') or n in ['assets/rogues/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']):
   original=json.loads(data);new=update(n,json.loads(data))
   if original!=new:files[n]=(json.dumps(new,indent=2,ensure_ascii=False)+'\n').encode()
 if jar.name.startswith('skill_tree-'):
  n='net/skill_tree_rpgs/effect/SkillEffects.class';files[n]=patch(files[n],'skill')
 if jar.name.startswith('spell_engine-'):
  for n,mode in [('net/spell_engine/internals/impact/SpellImpacts.class','impacts'),('net/spell_engine/internals/SpellTriggers.class','triggers'),('net/spell_engine/internals/delivery/SpellStashHelper.class','stash'),('net/spell_engine/internals/delivery/ProjectileLauncher.class','launcher'),('net/spell_engine/entity/SpellProjectile.class','projectile'),('net/spell_engine/internals/SpellParameters.class','parameters')]:files[n]=patch(files[n],mode)
  for p in classes.rglob('*.class'):
   n=p.relative_to(classes).as_posix()
   if not n.startswith('PatchWarrior'):files[n]=p.read_bytes()
  n='spell_engine.mixins.json';o=json.loads(files[n]);o['mixins']+=['warrior.WarriorShieldMixin','warrior.ShieldDisableMixin','warrior.NetMovementMixin'];files[n]=(json.dumps(o,indent=2)+'\n').encode()
 with ZipFile(OUT/jar.name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({**record,'sha256':hashlib.sha256((OUT/jar.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built three Warrior/shield candidates; not installed')

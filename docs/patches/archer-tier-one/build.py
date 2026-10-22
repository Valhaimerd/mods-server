"""Layer the Archer revision over pinned current pack JARs, preserving older patches."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/archer-tier-one';SOURCE=WORK/'before';OUT=WORK/'built';OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text()
cp=';'.join(p.as_posix() for p in SOURCE.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
sources=[HERE/'ArcherRules.java',HERE/'PatchEffectsConfig.java',HERE/'PatchArcherProjectile.java',HERE/'ArcherProjectileRules.java',ROOT/'docs/patches/magic-third-tier/PartyRules.java',ROOT/'docs/patches/magic-third-tier/FrozenEntityMoveMixin.java']
args=['--release','25','-cp',cp,'-d',str(classes),*[str(p) for p in sources]]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))

def patch(data,mode):
 a=WORK/'effect-input.class';b=WORK/'effect-output.class';a.write_bytes(data)
 subprocess.run([str(JAVA/'java.exe'),'-cp',str(classes)+';'+str(asm),'PatchEffectsConfig',str(a),str(b),mode],check=True)
 return b.read_bytes()

def update(name,obj):
 if name.startswith('data/archers/spell/') or (name.startswith('data/skill_tree_rpgs/spell/archer_') and obj.get('type')!='MODIFIER'):
  obj.setdefault('cost',{}).setdefault('cooldown',{})['haste_affected']=True
  if 'active' in obj:obj['active'].setdefault('cast',{})['haste_affected']=True
 if name=='data/archers/spell/rain_of_arrows.json':
  obj['cost']['cooldown']['duration']=20
  meteor=obj['deliver']['meteor'];meteor['launch_radius']=1.5
  meteor['launch_properties']['extra_launch_count']=29;meteor['launch_properties']['extra_launch_delay']=5
  obj['area_impact']['radius']=2.5
 if name=='data/archers/spell/magic_arrow.json':
  obj['range']=32;obj['active']['cast']['charge']['bonus']['range_add']=96
  obj['cost']['cooldown']['duration']=14
  obj['impacts'][0]['action']['damage']['spell_power_coefficient']=2.5
  obj['deliver']['projectile']['projectile']['homing_angle']=10
 if name=='data/archers/spell/barrage.json':
  obj['active']['cast']['duration']=.1
  obj['cost']['cooldown']['duration']=8
  obj['arrow_perks']['damage_multiplier']=.7
  obj['deliver']['shoot_arrow']['launch_properties']['extra_launch_delay']=3
  obj['deliver']['shoot_arrow']['divergence']=0
 if name=='data/archers/spell/spirit_wolf.json':
  obj['cost']['cooldown']['duration']=35;obj['range']=32
  summon=obj['impacts'][0]['action']['summon']
  summon['behaviour']['lifespan']['active_seconds']=20
  summon['behaviour']['targeting']['detection_range']={'mode':'STATIC','value':32}
  for entry in summon['attribute_scaling']['entries']:
   mod=entry['modifiers'][0]
   if entry['attribute_id']=='minecraft:max_health':mod['coefficient']=.5
   if entry['attribute_id']=='minecraft:armor':mod['coefficient']=.05
   if entry['attribute_id']=='minecraft:attack_damage':mod['coefficient']=.2
   if entry['attribute_id']=='minecraft:knockback_resistance':mod['base']=.2;mod['coefficient']=0
 if name=='data/archers/spell/entangling_roots.json':
  obj['cost']['cooldown']['duration']=12
  obj['deliver']['clouds'][0]['volume']['radius']=6
  obj['deliver']['clouds'][0]['time_to_live_seconds']=6
  obj['impacts'][0]['action']['status_effect'].pop('apply_limit',None)
 if name.startswith('data/skill_tree_rpgs/spell/archer_'):
  key=Path(name).stem
  if key=='archer_tier_4_spell_1_modifier_1':
   obj['modifiers'][0]['projectile_launch']['extra_launch_count']=0
   obj['modifiers'][0]['projectile_launch']['extra_launch_delay']=-3
   obj['modifiers'][0]['meteor_launch_radius_add']=.5
  if key=='archer_tier_4_spell_1_modifier_2':
   obj={'school':'spell_power:physical_ranged','range':0,'tier':1,'type':'MODIFIER','modifiers':[{'spell_pattern':'archers:rain_of_arrows','projectile_launch':{'velocity':0,'extra_launch_count':10,'extra_launch_delay':0}}]}
  if key=='archer_tier_4_spell_2_root':obj['modifiers'][0]['power_modifier']['power_multiplier']=.08
  if key=='archer_tier_4_spell_2_modifier_1':
   obj={'school':'spell_power:physical_ranged','range':0,'tier':1,'type':'MODIFIER','modifiers':[{'spell_pattern':'archers:magic_arrow'}]}
  if key=='archer_tier_3_passive_1':
   obj['passive']['triggers'][0]['chance']=1;obj['arrow_perks']['damage_multiplier']=1.25;obj['cost']['cooldown']['duration']=8
  if key=='archer_tier_3_passive_2':obj['cost']['cooldown']['duration']=40
  if key=='archer_tier_3_spell_1_modifier_1':obj['modifiers'][0]['projectile_launch']['extra_launch_count']=2
  if key=='archer_tier_3_spell_1_modifier_2':obj['modifiers'][0]['impacts'][0]['action']['heal']['spell_power_coefficient']=.06
  if key=='archer_tier_3_spell_2_root':
   mod=obj['modifiers'][0]['summon_attribute_scaling']['entries'][0]['modifiers'][0];mod['base']=0;mod['coefficient']=.2
  if key=='archer_tier_2_passive_1':
   obj['cost']['cooldown']['duration']=1;obj['impacts'][0]['action']['status_effect']['amplifier']=2
  if key=='archer_tier_2_passive_2':obj['passive']['triggers'][0]['chance']=1;obj['cost']['cooldown']['duration']=10
  if key=='archer_tier_2_spell_2_root':obj['modifiers'][0]['cooldown_duration_deduct']=2
  if key=='archer_tier_2_spell_2_modifier_1':obj['modifiers'][0]['impacts'][0]['chance']=1
  if key=='archer_tier_2_spell_2_modifier_2':
   impact=obj['modifiers'][0]['impacts'][0];impact['chance']=1;impact['action']['status_effect']['duration']=1
  if key=='archer_tier_2_spell_1_modifier_2':
   area=obj['modifiers'][0]['replacing_area_impact'];area['radius']=5;area['area']['distance_dropoff']='NONE'
  if key=='archer_tier_1_passive_1':
   obj['passive']['triggers'][0]['chance']=1;obj['impacts'][0]['action']['status_effect']['duration']=1;obj['cost']['cooldown']['duration']=8
  if key=='archer_tier_1_passive_2':
   obj['passive']['triggers'][0].pop('target_conditions',None)
  if key in ['archer_tier_1_passive_2','archer_tier_2_passive_1']:
   obj['impacts'][0]['action']['status_effect']['duration']=4
 if name=='assets/skill_tree_rpgs/lang/en_us.json':
  for key,desc in {
   'archer_tier_4_spell_1_modifier_1':'Rain of Arrows fires 30 arrows every 0.1 seconds across a 2-block scatter radius. Each impact splashes within 2.5 blocks.',
   'archer_tier_4_spell_1_modifier_2':'Rain of Arrows fires 10 additional arrows, for 40 total, at one arrow every 0.25 seconds.',
   'archer_tier_4_spell_2_root':'Magic Arrow deals 270%% of your Ranged Damage at full charge.',
   'archer_tier_4_spell_2_modifier_1':'Magic Arrow fully homes toward its selected target. It no longer resets its cooldown.',
   'archer_tier_3_passive_1':'Arrow hits ready your next ordinary arrow to deal 125%% normal damage. Use it within 5 seconds; ranged haste is reduced by 25%% while ready. Base cooldown: 8 seconds, reduced by haste.',
   'archer_tier_3_passive_2':'Taking damage while below 50%% health protects you from the next 3 eligible physical attacks for up to 10 seconds. Base cooldown: 40 seconds, reduced by haste.'
  }.items():obj['spell.skill_tree_rpgs.'+key+'.description']=desc

  obj['spell.skill_tree_rpgs.archer_tier_3_spell_1_modifier_2.description']='Barrage arrow hits heal you for 6%% of your Ranged Damage.'
  obj['spell.skill_tree_rpgs.archer_tier_3_spell_2_root.description']='Spirit Wolves gain extra maximum health equal to 20%% of your Ranged Damage.'
  obj['spell.skill_tree_rpgs.archer_tier_2_passive_1.description']='Rolling grants two stacks of Rhythm for 4 seconds.'
  obj['spell.skill_tree_rpgs.archer_tier_1_passive_2.description']='Arrow hits increase ranged attack speed by 3%% per stack, up to 5 stacks, for 4 seconds. Further hits refresh the duration.'
  obj['spell.skill_tree_rpgs.archer_tier_2_spell_1_modifier_2.description']='Power Shot deals 50%% ranged damage to other enemies within 5 blocks of the target, with no distance falloff.'
 if name=='assets/archers/lang/en_us.json':
  obj['spell.archers.rain_of_arrows.description']='Rains 30 arrows on the target, dealing 40%% of your Ranged Damage per impact. Arrows fall every 0.25 seconds across a 1.5-block scatter radius, with 2.5-block splash per impact.'
  obj['spell.archers.magic_arrow.description']='Charge a piercing magic arrow for up to 250%% of your Ranged Damage. Range increases from 32 to 128 blocks with charge. Moderately homes toward the selected target.'

  obj['spell.archers.entangling_roots.description']='Creates roots in a 6-block radius for 6 seconds, reducing enemy movement speed and jump strength by 60%%. Party allies and owned pets are protected.'
 return obj

records=[]
pinned={r['jar']:r['before_sha256'] for r in json.loads((HERE/'artifacts.json').read_text())}
for jar in SOURCE.glob('*.jar'):
 assert hashlib.sha256(jar.read_bytes()).hexdigest()==pinned[jar.name],jar.name
 with ZipFile(jar) as z:infos=z.infolist();original={i.filename:z.read(i) for i in infos}
 files=dict(original)
 for name,data in list(files.items()):
  if name.endswith('.json') and (name.startswith('data/archers/spell/') or name.startswith('data/skill_tree_rpgs/spell/archer_') or name in ['assets/archers/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']):
   obj=json.loads(data);changed=update(name,json.loads(data))
   if changed!=obj:files[name]=(json.dumps(changed,indent=2,ensure_ascii=False)+'\n').encode()
 if jar.name.startswith('archers-'):
  key='net/archers/effect/ArcherEffects.class';files[key]=patch(files[key],'archer')
 if jar.name.startswith('skill_tree-'):
  key='net/skill_tree_rpgs/effect/SkillEffects.class';files[key]=patch(files[key],'skill')
 if jar.name.startswith('spell_engine-'):
  key='net/spell_engine/entity/SpellProjectile.class'
  a=WORK/'projectile-input.class';b=WORK/'projectile-output.class';a.write_bytes(files[key])
  subprocess.run([str(JAVA/'java.exe'),'-cp',str(classes)+';'+str(asm),'PatchArcherProjectile',str(a),str(b)],check=True)
  files[key]=b.read_bytes()
  for p in (classes/'net/spell_engine/internals').glob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
  p=classes/'net/spell_engine/mixin/thirdtier/FrozenEntityMoveMixin.class';files[p.relative_to(classes).as_posix()]=p.read_bytes()
 with ZipFile(OUT/jar.name,'w',ZIP_DEFLATED) as z:
  for name,data in files.items():z.writestr(name,data)
 with ZipFile(OUT/jar.name) as z:assert z.testzip() is None
 records.append({'jar':jar.name,'before_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/jar.name).read_bytes()).hexdigest(),'changed':[n for n in original if original[n]!=files[n]],'added':sorted(set(files)-set(original))})
records.extend(record for record in json.loads((HERE/'artifacts.json').read_text()) if record['jar'].startswith('crossarms-'))
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Built three same-filename Archer candidates; retained CrossArms artifact record')

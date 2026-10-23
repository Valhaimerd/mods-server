"""Layer the Archer revision over pinned current pack JARs, preserving older patches."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/archer-tier-one';SOURCE=WORK/'follow-before';OUT=WORK/'built';OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text()
cp=';'.join(p.as_posix() for p in SOURCE.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
sources=[HERE/'PatchArcherProjectile.java',HERE/'ArcherProjectileRules.java']
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
 if name=='data/archers/spell/power_shot.json':
  obj['cost']['cooldown']['duration']=12
  obj['deliver']['stash_effect']['duration']=8
  obj['impacts'][0]['action']['status_effect']['duration']=6
 if name=='data/archers/spell/rain_of_arrows.json':
  obj['cost']['cooldown']['duration']=30
  meteor=obj['deliver']['meteor'];meteor['launch_radius']=8;meteor['offset_requires_sequence']=-2147483647
  meteor['launch_properties']['extra_launch_count']=39;meteor['launch_properties']['extra_launch_delay']=5
  obj['area_impact']['radius']=2.5
 if name=='data/archers/spell/magic_arrow.json':
  obj['range']=32
  obj['active']['cast']['type']='CHARGE';obj['active']['cast']['charge']={'output_scaling':1}
  obj['target']={'type':'NONE'}
  obj['cost']['cooldown']['duration']=25
  obj['impacts'][0]['action']['damage']['spell_power_coefficient']=.8
  projectile=obj['deliver']['projectile'];projectile['launch_properties'].update(extra_launch_count=3,extra_launch_delay=5)
  projectile['projectile']['homing_angle']=5
 if name=='data/archers/spell/barrage.json':
  obj['active']['cast']['duration']=.1
  obj['cost']['cooldown']['duration']=8
  obj['arrow_perks']['damage_multiplier']=.6
  obj['deliver']['shoot_arrow']['launch_properties']['extra_launch_delay']=3
  obj['deliver']['shoot_arrow']['divergence']=0
 if name=='data/archers/spell/spirit_wolf.json':
  obj['cost']['cooldown']['duration']=35;obj['range']=32
  summon=obj['impacts'][0]['action']['summon']
  summon['behaviour']['lifespan']['active_seconds']=20
  summon['behaviour']['targeting']['detection_range']={'mode':'STATIC','value':32}
  for entry in summon['attribute_scaling']['entries']:
   mod=entry['modifiers'][0]
   if entry['attribute_id']=='minecraft:max_health':mod['coefficient']=.4
   if entry['attribute_id']=='minecraft:armor':mod['coefficient']=.05
   if entry['attribute_id']=='minecraft:attack_damage':mod['coefficient']=.15
   if entry['attribute_id']=='minecraft:knockback_resistance':mod['base']=0;mod['coefficient']=0
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
   obj['modifiers'][0]['meteor_launch_radius_add']=0
  if key=='archer_tier_4_spell_1_modifier_2':
   obj={'school':'spell_power:physical_ranged','range':0,'tier':1,'type':'MODIFIER','modifiers':[{'spell_pattern':'archers:rain_of_arrows','meteor_launch_radius_add':0,'projectile_launch':{'velocity':0,'extra_launch_count':20,'extra_launch_delay':0}}]}
  if key=='archer_tier_4_spell_2_root':obj['modifiers'][0]['power_modifier']['power_multiplier']=.125
  if key=='archer_tier_4_spell_2_modifier_2':obj['modifiers'][0]['power_modifier']={'power_multiplier':.125}
  if key=='archer_tier_4_spell_2_modifier_1':
   obj={'school':'spell_power:physical_ranged','range':0,'tier':1,'type':'MODIFIER','modifiers':[{'spell_pattern':'archers:magic_arrow','mutate_impacts':'APPEND','impacts':[{'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':'minecraft:glowing','duration':10}}}]}]}
  if key=='archer_tier_3_passive_1':
   obj['passive']['triggers'][0]['chance']=1;obj['arrow_perks']['damage_multiplier']=1.25;obj['cost']['cooldown']['duration']=8
  if key=='archer_tier_3_passive_2':obj['cost']['cooldown']['duration']=40
  if key=='archer_tier_3_spell_1_modifier_1':obj['modifiers'][0]['projectile_launch']['extra_launch_count']=1
  if key=='archer_tier_3_spell_1_modifier_2':obj['modifiers'][0]['impacts'][0]['action']['heal']['spell_power_coefficient']=.06
  if key=='archer_tier_3_spell_2_root':
   mod=obj['modifiers'][0]['summon_attribute_scaling']['entries'][0]['modifiers'][0];mod['base']=0;mod['coefficient']=.1
  if key=='archer_tier_2_passive_1':
   obj['cost']['cooldown']['duration']=1;obj['impacts'][0]['action']['status_effect']['amplifier']=2
  if key=='archer_tier_2_passive_2':obj['passive']['triggers'][0]['chance']=1;obj['cost']['cooldown']['duration']=10
  if key=='archer_tier_2_spell_1_root':obj['modifiers'][0]={'spell_pattern':'archers:power_shot','effect_duration_add':0}
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
  descriptions={
   'archer_tier_2_spell_1_root':'Power Shot prepares arrows for 10 seconds instead of 8 seconds. Hunter\'s Mark lasts 6 seconds, refreshed by hits.',
   'archer_tier_2_spell_1_modifier_1':'Prepares 4 arrows instead of 3. Hunter\'s Mark stacks up to 4 times, increasing damage taken by 7%% per stack, up to 28%%.',
   'archer_tier_2_spell_1_modifier_2':'Marked arrow hits deal 50%% of your Ranged Damage as splash to other enemies within 5 blocks, without distance falloff.',
   'archer_tier_2_spell_2_root':'Entangling Roots cooldown is reduced by 2 seconds, to 10 seconds before haste.',
   'archer_tier_2_spell_2_modifier_1':'Every root pulse adds stacking Poison for 5 seconds. The maximum Poison level scales with 20%% of your melee power.',
   'archer_tier_2_spell_2_modifier_2':'Every root pulse immobilizes enemies for 1 second.',
   'archer_tier_3_spell_1_root':'Barrage cooldown is reduced by 1 second, to 7 seconds before haste.',
   'archer_tier_3_spell_1_modifier_1':'Barrage fires 1 additional arrow, for 4 total. Each deals 60%% normal shot damage.',
   'archer_tier_3_spell_1_modifier_2':'Barrage arrow hits heal you for 6%% of your Ranged Damage.',
   'archer_tier_3_spell_2_root':'Spirit Wolf health is 20 HP plus 50%% of your Ranged Damage, instead of 40%%.',
   'archer_tier_3_spell_2_modifier_1':'Summons 1 additional Spirit Wolf, for 3 total.',
   'archer_tier_3_spell_2_modifier_2':'Spirit Wolves remain active 10 seconds longer, for 30 seconds total.',
   'archer_tier_4_spell_1_root':'Rain of Arrows gains 5 percentage points of critical chance.',
   'archer_tier_4_spell_1_modifier_1':'Rains 40 arrows every 0.1 seconds across a random 8-block radius. Each impact splashes within 2.5 blocks.',
   'archer_tier_4_spell_1_modifier_2':'Adds 20 falling arrows, for 60 total, every 0.25 seconds across an 8-block radius. Each impact splashes within 2.5 blocks.',
   'archer_tier_4_spell_2_root':'Magic Arrows deal 90%% of your Ranged Damage per arrow at full charge instead of 80%%.',
   'archer_tier_4_spell_2_modifier_1':'Fires Magic Arrows upward in a wide spread above you. After 0.5 seconds, they seek the nearest enemy within 32 blocks with medium homing. Hits reveal enemies for 10 seconds.',
   'archer_tier_4_spell_2_modifier_2':'Magic Arrows deal 100%% of your Ranged Damage per arrow at full charge with Improved Magic Arrows, and gain 150%% extra base knockback.',
   'archer_tier_1_passive_1':'Arrow hits always stun enemies for 1 second. Base cooldown: 8 seconds, reduced by haste.',
   'archer_tier_1_passive_2':'Arrow hits grant 3%% ranged attack speed per stack, up to 15%%, for 4 seconds. Further hits refresh the duration.',
   'archer_tier_2_passive_1':'Rolling grants 2 Rhythm stacks for 4 seconds. Base cooldown: 1 second, reduced by haste.',
   'archer_tier_2_passive_2':'Rolling grants 200%% base roll recharge rate for 4 seconds. Base cooldown: 10 seconds, reduced by haste.',
   'archer_tier_3_passive_1':'Arrow hits ready your next ordinary arrow to deal 100%% normal damage. Use it within 5 seconds; ranged haste is reduced by 25%% while ready. Base cooldown: 8 seconds, reduced by haste.',
   'archer_tier_3_passive_2':'Taking damage below 50%% health protects you from the next 3 eligible physical attacks for up to 10 seconds. Base cooldown: 40 seconds, reduced by haste.'
  }
  for key,desc in descriptions.items():obj['spell.skill_tree_rpgs.'+key+'.description']=desc
  obj['spell.skill_tree_rpgs.archer_tier_4_spell_2_root.name']='Improved Magic Arrows'
 if name=='assets/archers/lang/en_us.json':
  obj['spell.archers.power_shot.description']='Prepares your next 3 arrows for 8 seconds. Hits apply Hunter\'s Mark for 6 seconds, refreshed, increasing damage taken by 7%% per stack, up to 21%%.'
  obj['spell.archers.barrage.description']='Fires 3 accurate arrows every 0.15 seconds. Each deals 60%% normal shot damage.'
  obj['spell.archers.spirit_wolf.description']='Summons 2 Spirit Wolves for 20 seconds. Each has 20 HP plus 40%% of your Ranged Damage, and deals 3 damage plus 15%% of your Ranged Damage per melee hit. Pet bonuses apply.'
  obj['spell.archers.rain_of_arrows.description']='Rains 40 arrows at 40%% of your Ranged Damage per impact. Arrows fall every 0.25 seconds throughout a random 8-block radius that follows the target, with 2.5-block splash per impact.'
  obj['spell.archers.magic_arrow.name']='Magic Arrows'
  obj['spell.archers.magic_arrow.description']='Fires 4 seeking arrows every 0.25 seconds, each dealing up to 80%% of your Ranged Damage at full charge. Partial charges deal less damage. They find the nearest enemy within 32 blocks, turn gradually in any direction, pass through walls and last 10 seconds.'
  obj['spell.archers.entangling_roots.description']='Creates roots in a 6-block radius for 6 seconds, reducing enemy movement speed and jump strength by 60%%. Party allies and owned pets are protected.'
 return obj

records=[]
pinned=json.loads((HERE/'follow-inputs.json').read_text())
for jar in SOURCE.glob('*.jar'):
 assert hashlib.sha256(jar.read_bytes()).hexdigest()==pinned[jar.name],jar.name
 with ZipFile(jar) as z:infos=z.infolist();original={i.filename:z.read(i) for i in infos}
 files=dict(original)
 for name,data in list(files.items()):
  if name.endswith('.json') and (name.startswith('data/archers/spell/') or name.startswith('data/skill_tree_rpgs/spell/archer_') or name in ['assets/archers/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']):
   obj=json.loads(data);changed=update(name,json.loads(data))
   if changed!=obj:files[name]=(json.dumps(changed,indent=2,ensure_ascii=False)+'\n').encode()
 if jar.name.startswith('spell_engine-'):
  for p in (classes/'net/spell_engine/entity').glob('ArcherProjectileRules*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
  key='net/spell_engine/internals/delivery/ProjectileLauncher.class'
  a=WORK/'projectile-input.class';b=WORK/'projectile-output.class';a.write_bytes(files[key])
  subprocess.run([str(JAVA/'java.exe'),'-cp',str(classes)+';'+str(asm),'PatchArcherProjectile',str(a),str(b),'conjured-launch'],check=True);files[key]=b.read_bytes()
 with ZipFile(OUT/jar.name,'w',ZIP_DEFLATED) as z:
  for name,data in files.items():z.writestr(name,data)
 with ZipFile(OUT/jar.name) as z:assert z.testzip() is None
 records.append({'jar':jar.name,'before_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/jar.name).read_bytes()).hexdigest(),'changed':[n for n in original if original[n]!=files[n]],'added':sorted(set(files)-set(original))})
records.extend(record for record in json.loads((HERE/'artifacts.json').read_text()) if record['jar'].startswith('crossarms-'))
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Built three same-filename Archer candidates; retained CrossArms artifact record')

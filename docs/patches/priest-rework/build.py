from pathlib import Path
from zipfile import ZipFile
import copy,hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-rework';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');inputs=[]
for pattern in ['paladins-*.jar','skill_tree-*.jar','spell_engine-*.jar','arsenal-*.jar']:
 p=next((ROOT/'mods').glob(pattern));source=BEFORE/p.name
 if not source.exists():shutil.copy2(p,source)
 inputs.append(source)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(str(p) for p in inputs)+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'));classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'PriestRules.java'),str(HERE/'PatchPriest.java')]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
def patch(data,mode):
 (WORK/'in.class').write_bytes(data);subprocess.run([str(JAVA/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchPriest',str(WORK/'in.class'),str(WORK/'out.class'),mode],check=True);return (WORK/'out.class').read_bytes()
def status(key,duration,**extra):return {'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':key,'duration':duration,**extra}}}
records=[]
for source in inputs:
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(n,fn):
  o=json.loads(files[n]);fn(o);files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 if source.name.startswith('paladins-'):
  def holy(o):
   o['cost']['cooldown'].update(duration=24,proportional=True);o['impacts'][0]['action']['heal']['spell_power_coefficient']=.15;o['impacts'][1]['action']['damage'].update(spell_power_coefficient=.30,knockback=1)
  edit('data/paladins/spell/holy_beam.json',holy)
  def penance(o):
   o['tier']=2;o['range']=26;o['active']['cast']['duration']=2.5;o['active']['cast']['channel']['ticks']=10;o['cost']['cooldown'].update(duration=14,proportional=True)
   o['impacts'][0]['action']['damage'].update(spell_power_coefficient=.25,knockback=.5)
   o['impacts'][1]=status('skill_tree_rpgs:priest_penance_shield',3)
   o.pop('area_impact',None)
  edit('data/paladins/spell/penance.json',penance)
  def levitate(o):
   o['tier']=3;o['active']['cast']['duration']=2;o['active']['cast']['channel']['ticks']=4;o['cost']['cooldown'].update(duration=16,proportional=False)
   o['impacts'][1]['action']['status_effect']['duration']=6;o['deliver']={'type':'CUSTOM','custom':{'handler':'priest_pack:ascension'}}
  edit('data/paladins/spell/levitate.json',levitate)
  edit('data/paladins/spell/circle_of_healing.json',lambda o:(o['cost']['cooldown'].update(duration=16),o['impacts'][0]['action']['heal'].update(spell_power_coefficient=.5)))
  def lang(o):
   o['spell.paladins.levitate.name']='Ascension';o['effect.paladins.levitate']='Ascension'
   o['spell.paladins.holy_beam.description']='Channel holy light to heal allies for 15%% Healing Power and damage enemies for 30%% per hit.'
   o['spell.paladins.penance.description']='Fire 10 bolts that burst on impact. Each blast deals 25%% Healing Power to enemies and adds 1 absorption HP to nearby allies, up to 10 HP, for 3s.'
   o['spell.paladins.levitate.description']='Rise into the air and repel nearby enemies with four pulses, ending in a stronger push. Each pulse grants allies 2%% melee, ranged and magic power, up to 8%%, for 6s.'
   o['spell.paladins.circle_of_healing.description']='Heal nearby allies for 50%% Healing Power.'
  edit('assets/paladins/lang/en_us.json',lang)
  n='net/paladins/effect/PriestAbsorptionStatusEffect.class';files[n]=patch(files[n],'absorb')
 if source.name.startswith('skill_tree-'):
  with ZipFile(next(p for p in inputs if p.name.startswith('paladins-'))) as paladins:
   files['assets/skill_tree_rpgs/textures/mob_effect/priest_penance_shield.png']=paladins.read('assets/paladins/textures/mob_effect/priest_absorption.png')
   files['assets/skill_tree_rpgs/textures/mob_effect/ascension_power.png']=paladins.read('assets/paladins/textures/mob_effect/levitate.png')
  files['assets/skill_tree_rpgs/textures/mob_effect/priest_serenity_shield.png']=old['assets/skill_tree_rpgs/textures/mob_effect/serenity.png']
  prefix='data/skill_tree_rpgs/spell/'
  changes={
   'priest_tier_2_spell_1_root':{'spell_pattern':'paladins:holy_beam'},
   'priest_tier_2_spell_1_modifier_1':{'spell_pattern':'paladins:holy_beam'},
   'priest_tier_2_spell_1_modifier_2':{'spell_pattern':'paladins:holy_beam','mutate_impacts':'APPEND','impacts':[{'action':{'type':'FIRE','fire':{}}}]},
   'priest_tier_3_spell_2_root':{'spell_pattern':'paladins:penance'},
   'priest_tier_3_spell_2_modifier_1':{'spell_pattern':'paladins:penance'},
   'priest_tier_3_spell_2_modifier_2':{'spell_pattern':'paladins:penance'},
   'priest_tier_3_spell_1_root':{'spell_pattern':'paladins:circle_of_healing'},
   'priest_tier_3_spell_1_modifier_1':{'spell_pattern':'paladins:circle_of_healing','mutate_impacts':'APPEND','impacts':[{'action':{'type':'CUSTOM','custom':{'handler':'priest_pack:mass_dispel','intent':'HELPFUL'}}}]},
   'priest_tier_3_spell_1_modifier_2':{'spell_pattern':'paladins:circle_of_healing','mutate_impacts':'APPEND','impacts':[{'action':{'type':'CUSTOM','custom':{'handler':'priest_pack:sanctuary','intent':'HELPFUL'}}}]},
   'priest_tier_2_spell_2_root':{'spell_pattern':'paladins:levitate','effect_duration_add':2},
   'priest_tier_2_spell_2_modifier_1':{'spell_pattern':'paladins:levitate'},
   'priest_tier_2_spell_2_modifier_2':{'spell_pattern':'paladins:levitate'},
  }
  for key,value in changes.items():edit(prefix+key+'.json',lambda o,v=value:o.update(modifiers=[v]))
  # Move complete branch definitions into the other tier's existing slots; spell reward IDs stay stable.
  for category in ['class_skills','weapon_skills']:
   def swap(o):
    for suffix in ['root','modifier_1','modifier_2']:
     a=f'priest_tier_2_spell_2_{suffix}';b=f'priest_tier_3_spell_2_{suffix}';o[a],o[b]=o[b],o[a]
   edit(f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json',swap)
  def lang(o):
   for key,value in {
    'priest_tier_2_spell_1_root':'Holy Light heals for 20%% Healing Power and damages enemies for 35%% per hit.',
    'priest_tier_2_spell_1_modifier_1':'Holy Light heals for 30%% Healing Power per hit and doubles its knockback.',
    'priest_tier_2_spell_1_modifier_2':'Holy Light deals 50%% Healing Power per hit and burns enemies.',
    'priest_tier_3_spell_2_root':'Penance blasts deal 30%% Healing Power. Absorption lasts 1s longer.',
    'priest_tier_3_spell_2_modifier_1':'Penance blasts grant allies 1%% attack speed, draw speed and spell haste per stack, up to 10%%, for 4s.',
    'priest_tier_3_spell_2_modifier_2':'Penance blasts make enemies take 1%% more damage per stack, up to 10%%, for 4s.',
    'priest_tier_3_spell_1_root':'Circle of Healing restores 60%% Healing Power.',
    'priest_tier_3_spell_1_modifier_1':'Circle of Healing removes 2 harmful effects from allies.',
    'priest_tier_3_spell_1_modifier_2':'Circle of Healing grants Regeneration for 5s. Healing Power increases its level.',
    'priest_tier_2_spell_2_root':'Ascension and its protective buffs last 2s longer.',
    'priest_tier_2_spell_2_modifier_1':'Ascension also lifts allies within 3 blocks.',
    'priest_tier_2_spell_2_modifier_2':'Ascension pulses add absorption, up to 4 stacks. Healing Power strengthens each stack.'}.items():o['spell.skill_tree_rpgs.'+key+'.description']=value
   o['spell.skill_tree_rpgs.priest_tier_2_spell_1_modifier_1.name']='Graceful Light'
   o['spell.skill_tree_rpgs.priest_tier_2_spell_2_root.name']='Improved Ascension'
   o['effect.skill_tree_rpgs.priest_penance_shield']='Penance Shield'
   o['effect.skill_tree_rpgs.priest_penance_shield.description']='Absorbs incoming damage.'
   o['effect.skill_tree_rpgs.priest_serenity_shield']='Serenity'
   o['effect.skill_tree_rpgs.priest_serenity_shield.description']='Absorbs incoming damage.'
   o['effect.skill_tree_rpgs.ascension_power']='Ascension'
   o['effect.skill_tree_rpgs.ascension_power.description']='Increased melee, ranged and magic power.'
   for k in list(o):
    if k.startswith('skill.skill_tree_rpgs.priest_') and k.endswith('.title'):
     spell=k.removeprefix('skill.skill_tree_rpgs.').removesuffix('.title');o[k]=o.get('spell.skill_tree_rpgs.'+spell+'.name',o[k])
  edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
  n='net/skill_tree_rpgs/effect/SkillEffects.class';files[n]=patch(files[n],'effects')
  n='net/skill_tree_rpgs/effect/WizardAbsorbEffect.class';files[n]=patch(files[n],'absorb')
 if source.name.startswith('spell_engine-'):
  for cls,mode in [('internals/impact/SpellImpacts','impact'),('entity/SpellProjectile','projectile'),('internals/SpellExecution','delivery'),('internals/cost/SpellCooldowns','cooldown'),('internals/casting/SpellCast$Process','timing'),('entity/TenacityAbsorbEffect','absorb')]:
   n='net/spell_engine/'+cls+'.class';files[n]=patch(files[n],mode)
  for p in (classes/'net/spell_engine/internals').glob('PriestRules*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
 if source.name.startswith('arsenal-'):
  n='net/arsenal/spell/AbsorptionStatusEffect.class';files[n]=patch(files[n],'absorb')
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys())})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Priest rework candidates')

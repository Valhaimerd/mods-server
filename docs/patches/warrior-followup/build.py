"""Focused overlay on the currently installed Warrior JARs; build without installation."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import json,hashlib,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/warrior-followup';OUT=WORK/'built';BEFORE=WORK/'before'
OUT.mkdir(parents=True,exist_ok=True);BEFORE.mkdir(exist_ok=True)
names=[next((ROOT/'mods').glob(p)).name for p in ['rogues-*','skill_tree-*','spell_engine-*']]
for name in names:
 if not (BEFORE/name).exists():shutil.copy2(ROOT/'mods'/name,BEFORE/name)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in BEFORE.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'WarriorRules.java'),str(HERE/'WarriorUltimates.java')]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
with ZipFile(BEFORE/names[0]) as z:
 rage_icon=z.read('assets/rogues/textures/spell/mortal_strike.png');stand_icon=z.read('assets/rogues/textures/spell/last_stand.png');charge_icon=z.read('assets/rogues/textures/spell/charge.png')
records=[]
for name in names:
 with ZipFile(BEFORE/name) as z:old={n:z.read(n) for n in z.namelist()}
 files=dict(old)
 if name.startswith('rogues-'):
  for key in ['throw','throw_net']:
   n='data/rogues/spell/'+key+'.json';o=json.loads(files[n]);o['active']['cast']['charge']['output_scaling']=0;files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='data/rogues/spell/last_stand.json';o=json.loads(files[n]);o['impacts'][0]['action']['status_effect'].update(amplifier=1,amplifier_cap=4,apply_mode='ADD');files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='assets/rogues/lang/en_us.json';o=json.loads(files[n]);o['spell.rogues.throw.description']='Throw your weapon for 40%%-80%% melee damage, increasing with charge. Slows enemies for 1s and leaves you weakened for 2s.';o['spell.rogues.throw_net.description']='Throw a net for 7.5%%-15%% melee damage, increasing with charge. Roots eligible enemies for 2s and pulls them toward you.';o['spell.rogues.last_stand.description']='Each pulse adds 20%% maximum health and heals the added health, up to 100%%. Grants brief damage immunity, Regeneration I and knockback immunity.';files[n]=(json.dumps(o,indent=2)+'\n').encode()
 if name.startswith('skill_tree-'):
  n='data/skill_tree_rpgs/spell/warrior_tier_4_spell_1_modifier_2.json';o=json.loads(files[n]);o['type']='MODIFIER';o.pop('passive',None);o.pop('target',None);o.pop('deliver',None);o.pop('impacts',None);files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='assets/skill_tree_rpgs/lang/en_us.json';o=json.loads(files[n]);
  for key,label in [('modifier_1','Strong Throw'),('modifier_2','Head Throw')]:
   for prefix,suffix in [('skill.','.title'),('spell.','.name')]:o[prefix+'skill_tree_rpgs.warrior_tier_2_spell_1_'+key+suffix]=label
  o['spell.skill_tree_rpgs.warrior_tier_2_spell_1_root.description']='Throw Weapon deals 50%%-90%% melee damage, increasing with charge.'
  o['spell.skill_tree_rpgs.warrior_tier_2_spell_1_modifier_2.description']='Throw Weapon deals 60%%-100%% melee damage, increasing with charge, and stuns for 0.25s.'
  files[n]=(json.dumps(o,indent=2)+'\n').encode()
  for effect,icon in [('berserker',rage_icon),('berserker_crit',rage_icon),('sentinel',stand_icon),('juggernaut',stand_icon)]:files['assets/skill_tree_rpgs/textures/mob_effect/'+effect+'.png']=icon
  for effect,icon in [('warrior_freedom',charge_icon),('warrior_endurance',charge_icon),('warrior_momentum',files['assets/skill_tree_rpgs/textures/spell/warrior_tier_2_passive_2.png']),('warrior_momentum_ready',files['assets/skill_tree_rpgs/textures/spell/warrior_tier_2_passive_2.png']),('tenacity_ward',files['assets/skill_tree_rpgs/textures/spell/warrior_tier_3_passive_2.png'])]:files['assets/skill_tree_rpgs/textures/mob_effect/'+effect+'.png']=icon
 if name.startswith('spell_engine-'):
  for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
 with ZipFile(OUT/name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({'jar':name,'before_sha256':hashlib.sha256((BEFORE/name).read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/name).read_bytes()).hexdigest(),'changed':[n for n in files if old.get(n)!=files[n]]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Warrior follow-up candidates')

"""Focused overlay on the currently installed Warrior JARs; build without installation."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import json,hashlib,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/crimson-laststand';OUT=WORK/'built';BEFORE=WORK/'before'
OUT.mkdir(parents=True,exist_ok=True);BEFORE.mkdir(exist_ok=True)
names=[next((ROOT/'mods').glob(p)).name for p in ['rogues-*','skill_tree-*','spell_engine-*']]
for name in names:
 if not (BEFORE/name).exists():shutil.copy2(ROOT/'mods'/name,BEFORE/name)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in BEFORE.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in HERE.glob('*.java') if 'Probe' not in p.name]]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for name in names:
 with ZipFile(BEFORE/name) as z:old={n:z.read(n) for n in z.namelist()}
 files=dict(old)
 if name.startswith('rogues-'):
  n='data/rogues/spell/last_stand.json';o=json.loads(files[n]);o['impacts']=[i for i in o['impacts'] if i['action']['type']!='IMMUNITY'];files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='assets/rogues/lang/en_us.json';o=json.loads(files[n]);o['spell.rogues.last_stand.description']='Each pulse adds 20%% maximum health and heals the added health, up to 100%%. Grants Regeneration I and knockback immunity.';files[n]=(json.dumps(o,indent=2)+'\n').encode()
 if name.startswith('skill_tree-'):
  n='data/skill_tree_rpgs/spell/rogue_tier_4_spell_2_modifier_1.json';o=json.loads(files[n]);o['modifiers'][0].pop('impacts',None);o['modifiers'][0].pop('mutate_impacts',None);files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='assets/skill_tree_rpgs/lang/en_us.json';o=json.loads(files[n]);o['spell.skill_tree_rpgs.rogue_tier_4_spell_2_modifier_1.description']='A successful Mutilate heals 10%% of combined dual-wield power. Heals once per cast, regardless of enemies hit.';o['spell.skill_tree_rpgs.warrior_tier_4_spell_2_root.description']='Last Stand lasts 20s instead of 15s.';files[n]=(json.dumps(o,indent=2)+'\n').encode()
 if name.startswith('spell_engine-'):
  with ZipFile(next(BEFORE.glob('skill_tree-*'))) as source:files['crimson_heal.json']=json.dumps(json.loads(source.read('data/skill_tree_rpgs/spell/rogue_tier_4_spell_2_modifier_1.json'))['modifiers'][0]['impacts'][0]).encode()
  for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
  n='spell_engine.mixins.json';o=json.loads(files[n]);o['mixins']+=['crimson.CrimsonCastMixin','crimson.CrimsonHitMixin'];files[n]=(json.dumps(o,indent=2)+'\n').encode()
 with ZipFile(OUT/name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({'jar':name,'before_sha256':hashlib.sha256((BEFORE/name).read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/name).read_bytes()).hexdigest(),'changed':[n for n in files if old.get(n)!=files[n]]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Crimson/Last Stand candidates')

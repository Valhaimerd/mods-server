"""Focused overlay on the currently installed Warrior JARs; build without installation."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import json,hashlib,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/throw-weapon-hit';OUT=WORK/'built';BEFORE=WORK/'before'
OUT.mkdir(parents=True,exist_ok=True);BEFORE.mkdir(exist_ok=True)
names=[next((ROOT/'mods').glob(p)).name for p in ['skill_tree-*','spell_engine-*']]
for name in names:
 if not (BEFORE/name).exists():shutil.copy2(ROOT/'mods'/name,BEFORE/name)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in BEFORE.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'WarriorRules.java'),str(HERE/'ThrowContextAccess.java')]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for name in names:
 with ZipFile(BEFORE/name) as z:old={n:z.read(n) for n in z.namelist()}
 files=dict(old)
 if name.startswith('skill_tree-'):
  n='data/skill_tree_rpgs/spell/warrior_tier_2_spell_1_modifier_1.json';o=json.loads(files[n]);o['modifiers'][0]['projectile_perks']['pierce']=1;files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='assets/skill_tree_rpgs/lang/en_us.json';o=json.loads(files[n]);o['spell.skill_tree_rpgs.warrior_tier_2_spell_1_modifier_1.description']='Throw Weapon pierces the first enemy and seeks one more with slight homing, hitting up to 2 enemies.';files[n]=(json.dumps(o,indent=2)+'\n').encode()
 if name.startswith('spell_engine-'):
  for p in classes.rglob('WarriorRules*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
  n='net/spell_engine/mixin/warrior/ThrowContextAccess.class';files[n]=(classes/n).read_bytes()
  n='spell_engine.mixins.json';o=json.loads(files[n]);o['mixins'].append('warrior.ThrowContextAccess');files[n]=(json.dumps(o,indent=2)+'\n').encode()
 with ZipFile(OUT/name,'w',ZIP_DEFLATED) as z:
  for n,data in files.items():z.writestr(n,data)
 records.append({'jar':name,'before_sha256':hashlib.sha256((BEFORE/name).read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/name).read_bytes()).hexdigest(),'changed':[n for n in files if old.get(n)!=files[n]]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Throw Weapon candidates')

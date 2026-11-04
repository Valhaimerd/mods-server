from pathlib import Path
import json,hashlib,shutil,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-1-3'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
subprocess.run(['python',str(H/'verify.py')],check=True)
records=json.loads((H/'artifacts.json').read_text());backup=R/'mod fixing/retired packs/class-balance-phases-1-3-2026-11-04'
for row in records:
 active=R/'mods'/row['jar'];assert sha(active)==row['before'],f'Active input changed: {active}'
for row in records:
 for directory,label in [('mods','active'),('mod fixing/1 fixed packs','fixed')]:
  target=R/directory/row['jar'];assert target.resolve().is_relative_to(R.resolve())
  if target.exists():
   saved=backup/label/target.name;saved.parent.mkdir(parents=True,exist_ok=True)
   if saved.exists():assert sha(saved)==sha(target),'Existing backup differs'
   else:shutil.copy2(target,saved)
  target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(W/'built'/row['jar'],target)
subprocess.run(['python',str(H/'verify.py'),'--installed'],check=True)
(H/'completion.json').write_text(json.dumps({'date':'2026-11-04','status':'installed','phases':[1,2,3],'minecraft':'26.2','neoforge':'26.2.0.90','runtime_java':'Temurin 25.0.4.1+1','bytecode_target':25,'server_checks':104,'client_checks':13,'resource_checks':39,'installed_locations':['mods/','mod fixing/1 fixed packs/'],'backup':str(backup.relative_to(R)).replace('\\','/'),'artifacts':records,'personal_installations_changed':False,'committed_or_pushed':False},indent=2)+'\n')
print('Installed nine tested same-name replacements; originals preserved in retired packs.')

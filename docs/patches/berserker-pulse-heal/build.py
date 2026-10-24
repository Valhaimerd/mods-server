"""Move Berserker's existing heal outside the fifth-pulse condition."""
from pathlib import Path
from zipfile import ZipFile
import json,hashlib,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/berserker-pulse-heal';BEFORE=WORK/'before';OUT=WORK/'built';BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'));classes=WORK/'classes';classes.mkdir(exist_ok=True)
subprocess.run([str(java/'javac.exe'),'--release','25','-cp',str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'PatchPulse.java')],check=True)
records=[]
for pattern in ['rogues-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 if active.name.startswith('spell_engine-'):
  n='net/spell_engine/entity/WarriorUltimates.class';(WORK/'in.class').write_bytes(files[n]);subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchPulse',str(WORK/'in.class'),str(WORK/'out.class')],check=True);files[n]=(WORK/'out.class').read_bytes()
 else:
  n='assets/rogues/lang/en_us.json';o=json.loads(files[n]);o['spell.rogues.mortal_strike.description']='Each pulse fully heals you and stacks attack speed, basic melee damage and knockback resistance, but increases damage taken. The fifth pulse grants Speed IV for 2s. Fully heals again on normal expiry or fatal-hit rescue.';files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 with ZipFile(OUT/active.name,'w') as z:
  for i in infos:z.writestr(i,files[i.filename])
 records.append({'jar':active.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':[n for n in files if files[n]!=old[n]]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built every-pulse Berserker healing candidates')

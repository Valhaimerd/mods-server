from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/rpg-performance'
before=WORK/'before';before.mkdir(exist_ok=True);out=WORK/'built';out.mkdir(exist_ok=True)
active=next((ROOT/'mods').glob('spell_engine-*.jar'));saved=before/active.name
if not saved.exists():shutil.copy2(active,saved)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=str(saved)+';'+';'.join(p.as_posix() for p in (ROOT/'mods').glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
sources=[HERE/'TimedEntityAgeMixin.java',HERE/'CloudLifecycleMixin.java']
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in sources]]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8')
subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
with ZipFile(saved) as a:infos=a.infolist();old={i.filename:a.read(i) for i in infos}
files=dict(old);mix=json.loads(files['spell_engine.mixins.json'])
for key in ['performance.TimedEntityAgeMixin','performance.CloudLifecycleMixin']:assert key not in mix['mixins'];mix['mixins'].append(key)
files['spell_engine.mixins.json']=(json.dumps(mix,indent=2)+'\n').encode()
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
with ZipFile(out/active.name,'w') as z:
 for i in infos:z.writestr(i,files[i.filename])
 for n in sorted(set(files)-set(old)):z.writestr(n,files[n])
r={'jar':active.name,'before_sha256':hashlib.sha256(saved.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/active.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old)),'sources':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in sources}}
(HERE/'artifacts.json').write_text(json.dumps(r,indent=2)+'\n');print('Built RPG lifecycle candidate; active archive unchanged')

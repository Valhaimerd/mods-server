from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/lightwell-mote-spacing';BEFORE=WORK/'before';OUT=WORK/'built';BEFORE.mkdir(exist_ok=True);OUT.mkdir(exist_ok=True)
active=next((ROOT/'mods').glob('spell_engine-*.jar'));source=BEFORE/active.name
if not source.exists():shutil.copy2(active,source)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=str(source)+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"');classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'PriestTuning.java')];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old);name='net/spell_engine/internals/PriestTuning.class';files[name]=(classes/name).read_bytes()
with ZipFile(OUT/source.name,'w') as z:
 for info in infos:z.writestr(info,files[info.filename])
r={'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':[]};assert r['changed']==[name]
(HERE/'artifacts.json').write_text(json.dumps(r,indent=2)+'\n');print('Built same-name Spell Engine mote-spacing candidate')

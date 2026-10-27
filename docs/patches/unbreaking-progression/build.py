from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/unbreaking-progression';BEFORE=WORK/'before';OUT=WORK/'built';BEFORE.mkdir(exist_ok=True);OUT.mkdir(exist_ok=True)
active=next((ROOT/'mods').glob('penchant-*.jar'));source=BEFORE/active.name
if not source.exists():shutil.copy2(active,source)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=str(source)+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'PendingUnbreakingMixin.java')];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old);name='archives/tater/penchant/mixin/leveling/PendingUnbreakingMixin.class';files[name]=(classes/name).read_bytes()
n='penchant.mixins.json';o=json.loads(files[n]);assert 'leveling.PendingUnbreakingMixin' not in o['mixins'];o['mixins'].append('leveling.PendingUnbreakingMixin');files[n]=(json.dumps(o,indent=2)+'\n').encode()
with ZipFile(OUT/source.name,'w') as z:
 for info in infos:z.writestr(info,files[info.filename])
 for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
r={'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys())};assert r['changed']==['penchant.mixins.json']
(HERE/'artifacts.json').write_text(json.dumps(r,indent=2)+'\n');print('Built Penchant pending-progression candidate')

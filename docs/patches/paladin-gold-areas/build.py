from pathlib import Path
from zipfile import ZipFile
import subprocess,hashlib,json,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/paladin-gold';C=W/'classes';C.mkdir(exist_ok=True);out=W/'built';out.mkdir(exist_ok=True)
name='spell_engine-neoforge-1.10.5+26.2.jar';source=W/'before'/name
if not source.exists():source.parent.mkdir(exist_ok=True);shutil.copy2(R/'mods'/name,source)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(map(str,[source,*(R/'mods').glob('*.jar'),asm,tree]))+';'+(R/'tmp/utilities/classpath.txt').read_text().strip()
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),str(H/'PaladinVisuals.java'),str(H/'PatchGold.java')];a=W/'compile.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old);key='net/spell_engine/internals/PaladinVisuals.class';files[key]=(C/key).read_bytes()
key='net/spell_engine/internals/PaladinTierOne.class';p=W/'tier-one-before.class';p.write_bytes(files[key]);dest=W/'tier-one-after.class'
subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchGold',str(p),str(dest)],check=True);files[key]=dest.read_bytes()
with ZipFile(out/name,'w') as z:
 for info in infos:z.writestr(info,files[info.filename])
assert set(old)==set(files)
record={'jar':name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/name).read_bytes()).hexdigest(),'changed':[n for n in old if files[n]!=old[n]],'preserved_entries':sum(files[n]==old[n] for n in old)}
(H/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n');subprocess.run(['python',str(H/'verify.py'),str(out/name)],check=True);print(json.dumps(record,indent=2))

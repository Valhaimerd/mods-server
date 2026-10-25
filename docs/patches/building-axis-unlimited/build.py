from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/building-axis-unlimited';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('sophisticatedbuilding-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=str(before)+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'AxisPolicy.java'),str(HERE/'PatchAxis.java')];a=WORK/'compile.args';a.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args));subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(before) as z:infos=z.infolist();files={i.filename:z.read(i) for i in infos}
old=dict(files)
for name in ['attachment/AttachmentHandler','attachment/PowerLevel','utilities/BuildLimits','gui/buildmode/RadialMenu','gui/buildmodifier/ArrayEntry']:
 key='sophisticated/building/'+name+'.class';source=WORK/'in.class';target=WORK/'out.class';source.write_bytes(files[key]);subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchAxis',str(source),str(target)],check=True);files[key]=target.read_bytes()
key='sophisticated/building/pack/AxisPolicy.class';files[key]=(classes/key).read_bytes()
with ZipFile(OUT/active.name,'w') as out:
 for info in infos:out.writestr(info,files[info.filename])
 for name in sorted(set(files)-set(old)):out.writestr(name,files[name])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(HERE/'artifacts.json').write_text(json.dumps({'jar':active.name,'before_sha256':sha(before),'sha256':sha(OUT/active.name),'sources':{f.name:sha(f) for f in [HERE/'AxisPolicy.java',HERE/'PatchAxis.java']},'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))},indent=2)+'\n')
print('Built axis-only removal; batch/config/material/reach resources preserved')

from pathlib import Path
from zipfile import ZipFile
import subprocess,hashlib,json
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/building-reach';O=W/'built'
name='sophisticatedbuilding-neoforge-26.2-5.1.0.jar';before=W/'before.jar'
libs=R/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');classes=W/'classes';classes.mkdir(exist_ok=True)
subprocess.run([str(java/'javac.exe'),'--release','25','-cp',str(asm)+';'+str(tree),'-d',str(classes),str(H/'PatchReach.java')],check=True)
with ZipFile(before) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old)
for cls in ['AttachmentHandler','PowerLevel']:
 entry='sophisticated/building/attachment/'+cls+'.class';(W/'in.class').write_bytes(old[entry])
 subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchReach',str(W/'in.class'),str(W/'out.class')],check=True)
 files[entry]=(W/'out.class').read_bytes()
with ZipFile(O/name,'w') as z:
 for i in infos:z.writestr(i,files[i.filename])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(H/'artifacts.json').write_text(json.dumps({'jar':name,'before_sha256':sha(before),'sha256':sha(O/name),'changed':[n for n in old if old[n]!=files[n]],'preserved_entries':len(old)-2},indent=2)+'\n')
print('Built four-method reach overlay; previous axis and combat guards preserved')

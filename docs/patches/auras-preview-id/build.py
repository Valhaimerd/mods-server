from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/auras-preview-id';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('patchouli-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=str(before)+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'PreviewEntityIds.java'),str(HERE/'PatchPreview.java')];a=WORK/'compile.args';a.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args));subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(before) as z:infos=z.infolist();files={i.filename:z.read(i) for i in infos}
old=dict(files)
key='vazkii/patchouli/common/util/EntityUtil.class';source=WORK/'in.class';target=WORK/'out.class';source.write_bytes(files[key]);subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchPreview',str(source),str(target)],check=True);files[key]=target.read_bytes()
key='vazkii/patchouli/common/util/PreviewEntityIds.class';files[key]=(classes/key).read_bytes()
with ZipFile(OUT/active.name,'w') as out:
 for info in infos:out.writestr(info,files[info.filename])
 for name in sorted(set(files)-set(old)):out.writestr(name,files[name])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(HERE/'artifacts.json').write_text(json.dumps({'jar':active.name,'before_sha256':sha(before),'sha256':sha(OUT/active.name),'sources':{f.name:sha(f) for f in [HERE/'PreviewEntityIds.java',HERE/'PatchPreview.java']},'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))},indent=2)+'\n')
print('Built detached Patchouli preview ID assignment; world/gameplay resources preserved')

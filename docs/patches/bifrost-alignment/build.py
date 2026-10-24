from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import subprocess, shutil, hashlib, json

ROOT=Path(__file__).resolve().parents[3]; HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/bifrost-alignment';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
active=next((ROOT/'mods').glob('bifrost-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
classes=WORK/'classes';classes.mkdir(exist_ok=True)
with ZipFile(before) as z:original={n:z.read(n) for n in z.namelist()}
for n,data in original.items():
    if n.endswith('.class'):
        dest=classes/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(data)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8')
cp=';'.join(p.as_posix() for p in (ROOT/'mods').glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
def compile_java(sources,output):
    args=['--release','25','-cp',cp,'-d',str(output),*[str(p) for p in sources]]
    path=WORK/'compile.args';path.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8')
    subprocess.run([str(JAVA/'javac.exe'),'@'+str(path)],check=True)
tools=WORK/'tools';tools.mkdir(exist_ok=True)
compile_java([HERE/'PatchBifrost.java'],tools)
compile_java(list((HERE/'src').glob('*.java')),classes)
patch_args=WORK/'patch.args'
patch_args.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in ['-cp',str(tools)+';'+cp,'PatchBifrost',str(classes)]),encoding='utf-8')
subprocess.run([str(JAVA/'java.exe'),'@'+str(patch_args)],check=True)
files=dict(original)
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
meta=files['META-INF/neoforge.mods.toml'].decode().replace('displayName="Bifrost Teleport"','displayName="Asgardian_Mod"').replace("Named-marker teleportation with Heimdall's Sword, Mjolnir, and cinematic Bifrost beams.","Asgardian-only named-marker travel with Hofund, Mjolnir and cinematic Bifrost beams. Requires the pack's Asgardian-enabled Iourus Races build.")
meta+='\n[[dependencies.bifrost]]\nmodId="iourusraces"\ntype="required"\nversionRange="[1.0.0,)"\nordering="AFTER"\nside="BOTH"\n'
files['META-INF/neoforge.mods.toml']=meta.encode()
lang='assets/bifrost/lang/en_us.json';values=json.loads(files[lang]);values['item.bifrost.heimdalls_sword']='Hofund';files[lang]=(json.dumps(values,indent=2)+'\n').encode()
with ZipFile(OUT/active.name,'w',ZIP_DEFLATED) as z:
    for n,data in files.items():z.writestr(n,data)
record={'jar':active.name,'before_sha256':hashlib.sha256(before.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':sorted(n for n in original if original[n]!=files[n]),'added':sorted(set(files)-set(original))}
(HERE/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n',encoding='utf-8')
print('Built same-filename Bifrost alignment candidate')

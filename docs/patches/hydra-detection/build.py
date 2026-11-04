from pathlib import Path
from zipfile import ZipFile
import subprocess,json,hashlib,copy
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/hydra-detection';C=W/'classes';C.mkdir(exist_ok=True)
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(str(p) for p in (W/'before').glob('*.jar'))+';'+str(asm)+';'+str(tree)+';'+(R/'tmp/class-balance-phases-4-6/classpath.txt').read_text();(W/'classpath.txt').write_text(cp)
def run(tool,args):
 a=W/'build.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run([str(J/tool),'@'+str(a)],check=True)
run('javac.exe',['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),str(H/'MageSummons.java'),str(H/'PatchHydra.java')])
src=next((W/'before').glob('*.jar'))
with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
f=dict(old);key='net/spell_engine/internals/SummonRules.class';(W/'in.class').write_bytes(f[key]);run('java.exe',['-cp',str(C)+';'+cp,'PatchHydra',str(W/'in.class'),str(W/'out.class')]);f[key]=(W/'out.class').read_bytes()
for p in C.glob('net/spell_engine/internals/MageSummons*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
with ZipFile(W/'built'/src.name,'w') as z:
 for info in infos:z.writestr(copy.copy(info),f[info.filename])
assert set(f)==set(old),'Only existing classes should change'
record={'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256((W/'built'/src.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=f[n]]};assert set(record['changed'])=={'net/spell_engine/internals/SummonRules.class','net/spell_engine/internals/MageSummons.class'}
(H/'artifact.json').write_text(json.dumps(record,indent=2)+'\n');print('Built engine-only overlay; exactly two classes changed')

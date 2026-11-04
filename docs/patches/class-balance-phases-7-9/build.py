from pathlib import Path
from zipfile import ZipFile,ZipInfo,ZIP_DEFLATED
import subprocess,json,hashlib,copy
from data import apply
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-7-9';C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True)
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(map(str,[*(W/'before').glob('*.jar'),*(R/'mods').glob('*.jar'),asm,tree]))+';'+(R/'tmp/class-balance-phases-4-6/classpath.txt').read_text().strip();(W/'classpath.txt').write_text(cp)
def run(tool,args,label):
 a=W/(label+'.args');a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run([str(J/tool),'@'+str(a)],check=True)
run('javac.exe',['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),*[str(p) for p in H.glob('*.java') if not p.name.endswith('Probe.java')]],'compile')
def patch(f,key,mode):
 a=W/'in.class';b=W/'out.class';a.write_bytes(f[key]);run('java.exe',['-cp',str(C)+';'+cp,'PatchFinal',str(a),str(b),mode],'patch');f[key]=b.read_bytes()
records=[]
for src in sorted((W/'before').glob('*.jar')):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old);apply(src.name,f)
 if src.name.startswith('spell_engine-'):
  for key,mode in [('internals/PriestRules','priest'),('internals/PriestPassives','favor'),('internals/FollowUpRules','fade'),('internals/PaladinRevamp','revamp'),('internals/Vanguard','vanguard'),('internals/ProtectionHits','blocks'),('internals/PaladinUltimates','ultimate'),('internals/PurgatoryBoundary','boundary'),('entity/ArcherProjectileRules','seeker')]:patch(f,'net/spell_engine/'+key+'.class',mode)
  for p in C.rglob('*.class'):
   n=p.relative_to(C).as_posix()
   if n.startswith('net/spell_engine/'):f[n]=p.read_bytes()
  for n in list(f):
   if 'mixin' in n and n.endswith('.json'):
    d=json.loads(f[n])
    if d.get('package')=='net.spell_engine.mixin':d['mixins']+=['finale.FinalProtectionMixin','finale.FinalPurelightMixin','finale.FinalImpactMixin'];f[n]=(json.dumps(d,indent=2)+'\n').encode()
 if src.name.startswith('skill_tree-'):
  patch(f,'net/skill_tree_rpgs/effect/SkillEffects.class','register');patch(f,'net/skill_tree_rpgs/compat/PaladinPassives.class','guardian')
 with ZipFile(O/src.name,'w') as z:
  for i in infos:z.writestr(copy.copy(i),f[i.filename])
  for n in sorted(f.keys()-old.keys()):
   i=ZipInfo(n,(2026,11,4,0,0,0));i.compress_type=ZIP_DEFLATED;z.writestr(i,f[n])
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256((O/src.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=f[n]],'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built four current-input candidates; not installed')

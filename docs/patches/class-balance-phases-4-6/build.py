from pathlib import Path
from zipfile import ZipFile,ZipInfo,ZIP_DEFLATED
import subprocess,json,hashlib,copy
from data import apply
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-4-6';C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True)
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(map(str,[*(W/'before').glob('*.jar'),*(R/'mods').glob('*.jar'),asm,tree]))+';'+(R/'tmp/class-balance-phases-1-3/classpath.txt').read_text().strip();(W/'classpath.txt').write_text(cp)
def run(tool,args,label):
 a=W/(label+'.args');a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run([str(J/tool),'@'+str(a)],check=True)
run('javac.exe',['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),*[str(p) for p in H.glob('*.java') if not p.name.endswith('Probe.java')]],'compile')
def patch(f,key,mode):
 a=W/'in.class';b=W/'out.class';a.write_bytes(f[key]);run('java.exe',['-cp',str(C)+';'+cp,'PatchMages',str(a),str(b),mode],'patch');f[key]=b.read_bytes()
records=[]
for src in sorted((W/'before').glob('*.jar'),key=lambda p:p.name.lower()):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old);apply(src.name,f)
 if src.name.startswith('spell_engine-'):
  for key,mode in [('internals/ThirdTierRules','third'),('internals/RogueRules','rules'),('internals/PassiveRules','passive'),('internals/PriestRules','timing'),('internals/WardHitRules','ward'),('internals/SummonRules','summon'),('internals/SpatialRules','projectile'),('client/casting/ClientCastController','controller'),('internals/cost/SpellCooldowns','charges'),('internals/cost/SpellCooldownManager','charges'),('client/gui/EchoHud','hud'),('internals/SpellParameters','parameters')]:patch(f,'net/spell_engine/'+key+'.class',mode)
  for p in C.rglob('*.class'):
   n=p.relative_to(C).as_posix()
   if n.startswith('net/spell_engine/'):f[n]=p.read_bytes()
  for key in list(f):
   if key.endswith('.json') and 'mixin' in key:
    d=json.loads(f[key])
    if d.get('package')=='net.spell_engine.mixin':d['mixins']+=['mages.MageImpactScopeMixin','mages.MageArmorMixin'];f[key]=(json.dumps(d,indent=2)+'\n').encode()
 if src.name.startswith('skill_tree-'):
  patch(f,'net/skill_tree_rpgs/effect/SkillEffects.class','register')
 if f==old:continue
 with ZipFile(O/src.name,'w') as z:
  for i in infos:
   if i.filename in f:z.writestr(copy.copy(i),f[i.filename])
  for n in sorted(f.keys()-old.keys()):
   info=ZipInfo(n,(2026,11,4,0,0,0));info.compress_type=ZIP_DEFLATED;z.writestr(info,f[n])
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256((O/src.name).read_bytes()).hexdigest(),'changed':[n for n in old if n in f and f[n]!=old[n]],'removed':sorted(old.keys()-f.keys()),'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built',len(records),'current-input candidates; production unchanged')

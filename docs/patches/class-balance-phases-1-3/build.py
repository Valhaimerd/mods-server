from pathlib import Path
from zipfile import ZipFile,ZipInfo,ZIP_DEFLATED
import subprocess,json,hashlib,copy
from data import apply
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-1-3';C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True)
J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(map(str,[*(W/'before').glob('*.jar'),*(R/'mods').glob('*.jar'),asm,tree]))+';'+(R/'tmp/utilities/classpath.txt').read_text().strip();(W/'classpath.txt').write_text(cp)
def run(tool,args,label):
 a=W/(label+'.args');a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run([str(J/tool),'@'+str(a)],check=True)
run('javac.exe',['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),*[str(p) for p in H.glob('*.java') if not p.name.endswith('Probe.java')]],'compile')
def patch(f,key,mode):
 a=W/'in.class';b=W/'out.class';a.write_bytes(f[key]);run('java.exe',['-cp',str(C)+';'+cp,'PatchBatch',str(a),str(b),mode],'patch');f[key]=b.read_bytes()
records=[]
for src in sorted((W/'before').glob('*.jar'),key=lambda p:p.name.lower()):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old);apply(src.name,f)
 if src.name.startswith('spell_engine-'):
  for key,mode in [('internals/RevisionRules','revision'),('internals/SummonRules','summon'),('entity/WarriorUltimates','ultimate'),('internals/RogueRules','rules'),('entity/ClassRework','lastStand'),('internals/TrapRules','trap'),('internals/PriestRules','timing')]:patch(f,'net/spell_engine/'+key+'.class',mode)
  for package in ['net/spell_engine/internals/BatchRules','net/spell_engine/mixin/batch/']:
   for p in C.rglob('*.class'):
    n=p.relative_to(C).as_posix()
    if n.startswith(package):f[n]=p.read_bytes()
  for key in list(f):
   if key.endswith('.json') and 'mixin' in key:
    d=json.loads(f[key])
    if d.get('package')=='net.spell_engine.mixin':d['mixins']+=['batch.RegenerationMixin','batch.FoodRegenerationMixin'];f[key]=(json.dumps(d,indent=2)+'\n').encode()
 if src.name.startswith('skill_tree-'):
  patch(f,'net/skill_tree_rpgs/effect/SkillEffects.class','register')
  f['assets/skill_tree_rpgs/textures/mob_effect/aegis_guard.png']=f['assets/skill_tree_rpgs/textures/mob_effect/vitality.png']
 if src.name.startswith('orb_of_'):
  patch(f,'net/valhaimerd/orboblivioncompat/Utilities.class','food');patch(f,'net/valhaimerd/orboblivioncompat/SkillOverhaul.class','orbInit');patch(f,'net/valhaimerd/orboblivioncompat/mixin/CategoryGateMixin.class','gate')
  for p in C.glob('net/valhaimerd/orboblivioncompat/DefenseV2*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  # Runtime contract is shared with the actual tree, including retained IDs.
  contract=next(O.glob('adventurer*'))
  with ZipFile(contract) as z:f['defense-v2.json']=z.read('defense-v2.json')
 if src.name.startswith('Nameless'):
  patch(f,'com/cozary/nameless_trinkets/pack/PackBalance.class','retire');patch(f,'com/cozary/nameless_trinkets/events/FracturedNullstoneEvents.class','nullEffect')
 if f==old:continue
 with ZipFile(O/src.name,'w') as z:
  for i in infos:
   if i.filename in f:z.writestr(copy.copy(i),f[i.filename])
  for n in sorted(f.keys()-old.keys()):
   info=ZipInfo(n,(2026,11,4,0,0,0));info.compress_type=ZIP_DEFLATED;z.writestr(info,f[n])
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256((O/src.name).read_bytes()).hexdigest(),'changed':[n for n in old if n in f and f[n]!=old[n]],'removed':sorted(old.keys()-f.keys()),'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built',len(records),'current-input candidates; production unchanged')

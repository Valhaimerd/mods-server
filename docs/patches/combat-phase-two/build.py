from pathlib import Path
from zipfile import ZipFile
import subprocess,json,hashlib,copy,zlib,struct
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-phase-two';C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True);J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(str(p) for p in (W/'before').glob('*.jar'))+';'+';'.join(str(p) for p in (R/'mods').glob('*.jar'))+';'+str(asm)+';'+str(tree)+';'+(R/'tmp/utilities/classpath.txt').read_text()
(W/'classpath.txt').write_text(cp)
a=W/'compile.args';a.write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in ['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),*[str(p) for p in H.glob('*.java') if not p.name.endswith('Probe.java')]]));subprocess.run([str(J/'javac.exe'),'@'+str(a)],check=True)
def patch(f,key,mode):
 src=W/'in.class';dest=W/'out.class';src.write_bytes(f[key]);a=W/'patch.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in ['-cp',str(C)+';'+cp,'PatchPhaseTwo',str(src),str(dest),mode]));subprocess.run([str(J/'java.exe'),'@'+str(a)],check=True);f[key]=dest.read_bytes()
records=[]
for source in (W/'before').glob('*.jar'):
 if not source.name.startswith(('spell_engine-','bettercombat-','paladins-','wizards-','rogues-')):continue
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old)
 def edit(key,fn):
  d=json.loads(f[key]);fn(d);f[key]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 def addclasses(prefix):
  for p in C.glob(prefix+'*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
 if source.name.startswith('spell_engine-'):
  for key,mode in [('internals/target/SpellTarget','selection'),('internals/SpellExecution','execution'),('entity/SpellProjectile','projectile'),('internals/ArrowAssist','assist'),('entity/ArcherProjectileRules','magic'),('internals/PaladinUltimates','ultimate'),('internals/PriestUltimates','noPriestBuff')]:patch(f,'net/spell_engine/'+key+'.class',mode)
  for prefix in ['net/spell_engine/internals/SpatialRules','net/spell_engine/internals/SpatialState','net/spell_engine/internals/PurgatoryBoundary','net/spell_engine/mixin/entity/SpatialMovementMixin','net/spell_engine/mixin/entity/SpatialStateMixin']:addclasses(prefix)
  found=0
  for key in list(f):
   if key.endswith('.json') and 'mixin' in key:
    d=json.loads(f[key])
    if d.get('package')=='net.spell_engine.mixin' and 'mixins' in d:d['mixins']+=['entity.SpatialMovementMixin','entity.SpatialStateMixin'];f[key]=(json.dumps(d,indent=2)+'\n').encode();found+=1
  assert found==1,found
 if source.name.startswith('bettercombat-'):
  patch(f,'net/bettercombat/mixin/player/PlayerEntityRangeMixin.class','reach');addclasses('net/bettercombat/logic/ScaleReach')
 if source.name.startswith('paladins-'):
  def chunk(t,d):return struct.pack('>I',len(d))+t+d+struct.pack('>I',zlib.crc32(t+d)&0xffffffff)
  pixels=b''.join(b'\0'+b''.join(bytes((255,100,14,150 if x in (0,15) or y in (0,15) else 45)) for x in range(16)) for y in range(16))
  f['assets/paladins/textures/spell_effect/purgatory_boundary.png']=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(pixels))+chunk(b'IEND',b'')
  patch(f,'net/paladins/entity/BarrierEntity.class','barrier');patch(f,'net/paladins/client/entity/BarrierEntityRenderer.class','render');addclasses('net/paladins/client/entity/BoundaryRender')
  for id,radius in [('purgatory_boundary',12),('purgatory_boundary_improved',14)]:f[f'data/paladins/spell/{id}.json']=(json.dumps({'type':'PASSIVE','school':'spell_power:healing','range':radius,'target':{'type':'NONE'},'impacts':[]},indent=2)+'\n').encode()
  edit('assets/paladins/lang/en_us.json',lambda d:d.update({'spell.paladins.immolation.description':'Channel growing holy fire within an orange enclosure. The first pulse pulls enemies strongly inward. From 10s, control cannot interrupt you; later pulses consume absorption, then health. The final phase builds a powerful explosion.'}))
 if source.name.startswith('wizards-'):
  edit('data/wizards/spell/fire_slash.json',lambda d:d['deliver']['projectile']['projectile'].update(homing_angle=2))
  edit('assets/wizards/lang/en_us.json',lambda d:d.update({'spell.wizards.fire_slash.description':'Send out a wide wave of flame that gently seeks nearby enemies, dealing 80%% Fire damage.'}))
 if source.name.startswith('rogues-'):edit('assets/rogues/lang/en_us.json',lambda d:d.update({'spell.rogues.vanish.description':d['spell.rogues.vanish.description']+' Hostile skills cannot lock onto you; untargeted attacks can still hit.'}))
 dest=O/source.name
 with ZipFile(dest,'w') as z:
  for i in infos:z.writestr(copy.copy(i),f[i.filename])
  for key in sorted(f.keys()-old.keys()):z.writestr(key,f[key])
 with ZipFile(dest) as z:assert z.testzip() is None
 records.append({'jar':source.name,'before':hashlib.sha256(source.read_bytes()).hexdigest(),'after':hashlib.sha256(dest.read_bytes()).hexdigest(),'changed':[n for n in old if f[n]!=old[n]],'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built',len(records),'isolated candidates; production unchanged')

from pathlib import Path
from zipfile import ZipFile
import subprocess,copy,json,hashlib
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-followup-phase-two';C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True);J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
cp=';'.join(str(p) for p in (W/'before').glob('*.jar'))+';'+(R/'tmp/combat-followup-phase-one/build-classpath.txt').read_text();(W/'build-classpath.txt').write_text(cp)
def run(tool,args,label):
 a=W/(label+'.args');a.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args));subprocess.run([str(J/tool),'@'+str(a)],check=True)
run('javac.exe',['--release','25','-proc:none','-cp',cp,'-d',str(C),*[str(H/n) for n in ['MultiplayerBoundary.java','PlayerPacketBoundaryMixin.java','WeaponReachValidation.java','PlayerWeaponReachMixin.java','PatchReach.java']]],'compile')
records=[]
for src in (W/'before').glob('*.jar'):
 if not src.name.startswith(('spell_engine-','bettercombat-')):continue
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 data=dict(old)
 if src.name.startswith('spell_engine-'):prefixes=['net/spell_engine/internals/MultiplayerBoundary','net/spell_engine/mixin/entity/PlayerPacketBoundaryMixin'];package='net.spell_engine.mixin';mix='entity.PlayerPacketBoundaryMixin'
 else:
  prefixes=['net/bettercombat/logic/WeaponReachValidation','net/bettercombat/mixin/player/PlayerWeaponReachMixin'];package='net.bettercombat.mixin';mix='player.PlayerWeaponReachMixin'
  key='net/bettercombat/network/ServerNetwork.class';a=W/'in.class';b=W/'out.class';a.write_bytes(data[key]);run('java.exe',['-cp',str(C)+';'+cp,'PatchReach',str(a),str(b)],'patch');data[key]=b.read_bytes()
 for prefix in prefixes:
  for p in C.glob(prefix+'*.class'):data[p.relative_to(C).as_posix()]=p.read_bytes()
 found=0
 for key in list(data):
  if key.endswith('.json') and 'mixin' in key:
   d=json.loads(data[key])
   if d.get('package')==package and 'mixins' in d:d['mixins'].append(mix);data[key]=(json.dumps(d,indent=2)+'\n').encode();found+=1
 assert found==1,(package,found)
 with ZipFile(O/src.name,'w') as z:
  for i in infos:z.writestr(copy.copy(i),data[i.filename])
  for n in sorted(data.keys()-old.keys()):z.writestr(n,data[n])
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256((O/src.name).read_bytes()).hexdigest(),'changed':[n for n in old if data[n]!=old[n]],'added':sorted(data.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built two narrow multiplayer candidates')

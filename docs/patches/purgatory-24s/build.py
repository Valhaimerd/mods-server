from pathlib import Path
from zipfile import ZipFile
import subprocess,json,copy,hashlib
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/purgatory-24s';C=W/'classes';C.mkdir(parents=True,exist_ok=True);O=W/'built';O.mkdir(exist_ok=True);J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
cp=(R/'tmp/purgatory-continuous-pull/classpath.txt').read_text();(W/'classpath.txt').write_text(cp)
def run(binary,args,name):
 f=W/(name+'.args');f.write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in args));subprocess.run([str(J/binary),'@'+str(f)],check=True)
run('javac.exe',['--release','25','-proc:none','-cp',cp,'-d',str(C),str(H/'PatchTiming.java')],'compile')
records=[]
for src in (W/'before').glob('*.jar'):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 data=dict(old)
 if src.name.startswith('spell_engine-'):
  for name,mode in [('PaladinUltimates','ultimate'),('PaladinFollowup','focus'),('PurgatoryPull','pull'),('PurgatoryBoundary','boundary')]:
   key=f'net/spell_engine/internals/{name}.class';a=W/'in.class';b=W/'out.class';a.write_bytes(data[key]);run('java.exe',['-cp',str(C)+';'+cp,'PatchTiming',str(a),str(b),mode],'patch');data[key]=b.read_bytes()
 if src.name.startswith('paladins-'):
  key='data/paladins/spell/immolation.json';d=json.loads(data[key]);assert d['active']['cast']['duration']==30 and d['active']['cast']['channel']['ticks']==60;d['active']['cast']['duration']=24;d['active']['cast']['channel']['ticks']=48;data[key]=(json.dumps(d,indent=2)+'\n').encode()
  key='assets/paladins/lang/en_us.json';d=json.loads(data[key]);d['spell.paladins.immolation.description']=d['spell.paladins.immolation.description'].replace('From 10s,','From 8s,');data[key]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 out=O/src.name
 with ZipFile(out,'w') as z:
  for i in infos:z.writestr(copy.copy(i),data[i.filename])
 with ZipFile(out) as z:assert z.testzip() is None
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256(out.read_bytes()).hexdigest(),'changed':[n for n in old if data[n]!=old[n]]})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built two timing-only candidates')

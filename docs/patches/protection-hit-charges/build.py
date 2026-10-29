from compile import *
from zipfile import ZipFile
import hashlib
out=W/'built';out.mkdir(exist_ok=True);records=[]
for source in (W/'before').glob('*.jar'):
 with ZipFile(source) as z: infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 if source.name.startswith('spell_engine-'):
  name='net/spell_engine/internals/PaladinRevamp.class';(W/'in.class').write_bytes(files[name])
  subprocess.run([str(J/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchProtection',str(W/'in.class'),str(W/'out.class')],check=True)
  files[name]=(W/'out.class').read_bytes()
  name='net/spell_engine/internals/ProtectionHits.class';files[name]=(C/name).read_bytes()
 if source.name.startswith('paladins-'):
  name='assets/paladins/lang/en_us.json';data=json.loads(files[name]);data['spell.paladins.divine_protection.description']='Protect yourself and nearby allies, blocking a limited number of attacks and reducing damage taken by up to 40%%. Gain 10%% Healing Power.'
  data['effect.paladins.divine_protection.description']='Blocks incoming attacks while charges remain and reduces damage taken.'
  files[name]=(json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode()
 with ZipFile(out/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for name in sorted(files.keys()-old.keys()):z.writestr(name,files[name])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Candidates built; current archive contents preserved outside listed entries.')

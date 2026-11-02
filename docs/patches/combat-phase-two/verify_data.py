from pathlib import Path
from zipfile import ZipFile
import hashlib,json,struct
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-phase-two';checks=0
def check(ok,label):
 global checks
 assert ok,label
 checks+=1
for record in json.loads((H/'artifacts.json').read_text()):
 name=record['jar'];before=W/'before'/name;after=W/'built'/name
 check(hashlib.sha256(before.read_bytes()).hexdigest()==record['before'],name+' input')
 check(hashlib.sha256(after.read_bytes()).hexdigest()==record['after'],name+' output')
 with ZipFile(before) as a,ZipFile(after) as b:
  check(b.testzip() is None,name+' ZIP CRC');check(len(b.namelist())==len(set(b.namelist())),name+' duplicate entries')
  changed={n for n in a.namelist() if a.read(n)!=b.read(n)}
  check(changed==set(record['changed']),name+' exact changes')
  check(set(b.namelist())-set(a.namelist())==set(record['added']),name+' exact additions')
  for n in changed|set(record['added']):
   data=b.read(n)
   if n.endswith('.class'):check(struct.unpack('>H',data[6:8])[0]==69,n+' Java25')
   if n.endswith('.json'):json.loads(data);check(True,n+' JSON')
  check(not any(n.startswith(('local/','test/')) for n in b.namelist()),name+' no probes')
print('PASS',checks,'archive/resource checks')
(H/'archive-result.txt').write_text(f'PASS {checks} archive/resource checks\n')

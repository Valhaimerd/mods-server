from pathlib import Path
from zipfile import ZipFile
import hashlib,json,struct
ROOT=Path(__file__).resolve().parents[4];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/paladin-doctrine/armor';checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=WORK.parent/'before'/r['jar'];after=WORK/'built'/r['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
 with ZipFile(before) as a,ZipFile(after) as b:
  assert b.testzip() is None and a.namelist()==b.namelist()
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed'] and len(r['changed'])==1 and not r['added']
  for n in a.namelist():
   if n not in r['changed']:assert a.read(n)==b.read(n);checks+=1
  helper=b.read(r['changed'][0]);assert struct.unpack('>H',helper[6:8])[0]==69 and b'net/minecraft/client/' not in helper
 for label in ['candidate','legacy']:
  assert (HERE/(label+'-results.txt')).read_text().startswith('PASS 29')
  assert json.loads((HERE/(label+'-tested-mods.json')).read_text())[r['jar']]==r['sha256']
for label in ['baseline','baseline-legacy']:
 assert 'failures=25' in (HERE/(label+'-results.txt')).read_text()
print(f'PASS {checks} unchanged archive entries; helper-only Java25 overlays; native fresh/legacy RED-to-GREEN')

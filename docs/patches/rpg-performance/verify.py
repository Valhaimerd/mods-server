from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/rpg-performance'
r=json.loads((HERE/'artifacts.json').read_text());candidate=WORK/'built'/r['jar'] if '--candidate' in sys.argv else ROOT/'mods'/r['jar'];before=WORK/'before'/r['jar']
assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
assert hashlib.sha256(candidate.read_bytes()).hexdigest()==r['sha256']
for name,digest in r['sources'].items():assert hashlib.sha256((HERE/name).read_bytes()).hexdigest()==digest
with ZipFile(before) as a,ZipFile(candidate) as b:
 assert b.testzip() is None
 assert set(a.namelist())<=set(b.namelist())
 assert set(b.namelist())-set(a.namelist())==set(r['added'])
 assert {n for n in a.namelist() if a.read(n)!=b.read(n)}=={'spell_engine.mixins.json'}
 original=json.loads(a.read('spell_engine.mixins.json'));updated=json.loads(b.read('spell_engine.mixins.json'))
 for entry in ['performance.TimedEntityAgeMixin','performance.CloudLifecycleMixin']:updated['mixins'].remove(entry)
 assert updated==original
 for n in r['added']:assert int.from_bytes(b.read(n)[6:8],'big')==69 and 'Probe' not in n
 assert not any('performance_probe' in n.lower() for n in b.namelist())
if '--candidate' not in sys.argv:
 assert candidate.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 assert len(list((ROOT/'mods').glob('spell_engine-*.jar')))==1
print('PASS exact lifecycle overlay: original bytecode, spell data, party rules and prior patches preserved; two Java25 mixins; CRC/source/archive hashes')
if '--runtime' in sys.argv:
 for result,tested in [(WORK/'server/performance-result.txt',WORK/'server/tested-sha256.txt'),(WORK/'regression/server/warrior-result.txt',WORK/'regression/tested-sha256.txt')]:
  assert result.read_text().startswith('PASS'),result
  assert tested.read_text().strip()==r['sha256'],tested
  print(result.read_text().strip())

if '--runtime' in sys.argv:
 assert (WORK/'client/tooltip-result.txt').read_text().startswith('PASS 43')
 assert (WORK/'client-tested-sha256.txt').read_text().strip()==r['sha256']
 assert hashlib.sha256((WORK/'client/mods'/r['jar']).read_bytes()).hexdigest()==r['sha256']
 print('PASS 43 native client tooltip checks; exact lifecycle build loaded on client and integrated server')

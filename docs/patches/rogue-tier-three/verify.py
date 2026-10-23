from pathlib import Path
from zipfile import ZipFile
import json,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=ROOT/'tmp/rogue-tier-three/before'/r['jar'];active=ROOT/'mods'/r['jar'];fixed=ROOT/'mod fixing/1 fixed packs'/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 assert hashlib.sha256(active.read_bytes()).hexdigest()==r['sha256']
 assert active.read_bytes()==fixed.read_bytes()
 with ZipFile(before) as a,ZipFile(active) as b:
  assert b.testzip() is None
  assert set(b.namelist())-set(a.namelist())==set(r['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  assert not any('Probe' in n or 'PatchTierThree' in n for n in b.namelist())
  for n in r['changed']+r['added']:
   if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
 print('PASS exact entry delta, production-only Java 25, CRC, active/fixed hash:',r['jar'])

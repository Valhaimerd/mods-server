from pathlib import Path
from zipfile import ZipFile
import json,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=ROOT/'tmp/trap-rain-balance/before'/r['jar'];active=ROOT/'mods'/r['jar'];fixed=ROOT/'mod fixing/1 fixed packs'/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 assert hashlib.sha256(active.read_bytes()).hexdigest()==r['sha256']
 assert active.read_bytes()==fixed.read_bytes()
 with ZipFile(before) as a,ZipFile(active) as b:
  assert b.testzip() is None and a.namelist()==b.namelist()
  delta={n for n in a.namelist() if a.read(n)!=b.read(n)};assert delta==set(r['changed'])
  assert len(delta)==2 and all(n.endswith('.json') for n in delta)
 print('PASS exact two-resource change, CRC, active/fixed hashes:',r['jar'])

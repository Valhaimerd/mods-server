from pathlib import Path
from zipfile import ZipFile
import hashlib,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
archer_layers={r['jar']:r for r in json.loads((ROOT/'docs/patches/archer-tier-one/artifacts.json').read_text())}
for r in json.loads((HERE/'artifacts.json').read_text()):
 active=ROOT/'mods'/r['jar'];before=ROOT/'tmp/rogue-tier-one/revision-before'/r['jar'];fixed=ROOT/'mod fixing/1 fixed packs'/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 layer=archer_layers.get(r['jar']);layered=layer and layer['before_sha256']==r['sha256']
 expected=layer['sha256'] if layered else r['sha256']
 downstream_file=ROOT/'docs/patches/rogue-tier-three/artifacts.json'
 downstream=next((x for x in json.loads(downstream_file.read_text()) if x['jar']==r['jar']),None) if downstream_file.exists() else None
 if downstream and downstream['before_sha256']==expected:
  expected=downstream['sha256']
 assert hashlib.sha256(active.read_bytes()).hexdigest()==expected
 audited=ROOT/'tmp/archer-tier-one/follow-before'/r['jar'] if layered else ROOT/'tmp/rogue-tier-three/before'/r['jar'] if downstream and downstream['before_sha256']==r['sha256'] else active
 assert hashlib.sha256(audited.read_bytes()).hexdigest()==r['sha256']
 assert active.read_bytes()==fixed.read_bytes()
 with ZipFile(before) as a,ZipFile(audited) as b:
  assert b.testzip() is None
  assert set(b.namelist())-set(a.namelist())==set(r['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  for n in r['changed']+r['added']:
   assert 'Probe' not in n and 'PatchRogue' not in n
   if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
 print('PASS exact changed entries, active/fixed hashes and Java 25 production classes:',r['jar'])

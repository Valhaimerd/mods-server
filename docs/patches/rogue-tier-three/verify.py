from pathlib import Path
from zipfile import ZipFile
import json,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=ROOT/'tmp/rogue-tier-three/before'/r['jar'];active=ROOT/'mods'/r['jar'];fixed=ROOT/'mod fixing/1 fixed packs'/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 radius_file=ROOT/'docs/patches/rain-radius/artifacts.json'
 radius=next((x for x in json.loads(radius_file.read_text()) if x['jar']==r['jar']),None) if radius_file.exists() else None
 layered=radius and radius['before_sha256']==r['sha256']
 expected=radius['sha256'] if layered else r['sha256']
 icon_file=ROOT/'docs/patches/slice-evasion-icon/artifacts.json'
 icon=next((x for x in json.loads(icon_file.read_text()) if x['jar']==active.name),None) if icon_file.exists() else None
 if icon and icon['before_sha256']==expected:expected=icon['sha256']
 cast_file=ROOT/'docs/patches/bear-trap-cast/artifacts.json'
 cast=next((x for x in json.loads(cast_file.read_text()) if x['jar']==active.name),None) if cast_file.exists() else None
 cast_layered=cast and cast['before_sha256']==expected
 if cast_layered:expected=cast['sha256']
 assert hashlib.sha256(active.read_bytes()).hexdigest()==expected
 audited=ROOT/'tmp/rain-radius/before'/r['jar'] if layered else ROOT/'tmp/bear-trap-cast'/('before-'+r['jar']) if cast_layered else active

 assert active.read_bytes()==fixed.read_bytes()
 with ZipFile(before) as a,ZipFile(audited) as b:
  assert b.testzip() is None
  assert set(b.namelist())-set(a.namelist())==set(r['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  assert not any('Probe' in n or 'PatchTierThree' in n for n in b.namelist())
  for n in r['changed']+r['added']:
   if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
 print('PASS exact entry delta, production-only Java 25, CRC, active/fixed hash:',r['jar'])

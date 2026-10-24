from pathlib import Path
from zipfile import ZipFile
import json,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
r=json.loads((HERE/'artifacts.json').read_text())[0];before=ROOT/'tmp/asgardian-implementation'/('before-'+r['jar']);active=ROOT/'mods'/r['jar']
assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
assert hashlib.sha256(active.read_bytes()).hexdigest()==r['sha256']
assert active.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
with ZipFile(before) as a,ZipFile(active) as b:
 assert b.testzip() is None
 assert set(b.namelist())-set(a.namelist())==set(r['added'])
 assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
 assert a.read('META-INF/neoforge.mods.toml')==b.read('META-INF/neoforge.mods.toml')
 assert not any('Probe' in n for n in b.namelist())
 for n in r['changed']+r['added']:
  if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
 for lang in ['en_us','es_es']:
  o=json.loads(b.read('assets/iourusraces/lang/'+lang+'.json'))
  assert all('race.iourusraces.asgardian'+suffix in o for suffix in ['', '.trait.1','.trait.2','.trait.3','.description','.tooltip'])
print('PASS exact changed entries, new server handler, original metadata, localized traits, Java 25, CRC and matching active/fixed hashes')

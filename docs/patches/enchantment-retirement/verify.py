from pathlib import Path
from zipfile import ZipFile
import hashlib,json
root=Path(__file__).resolve().parents[3];here=Path(__file__).resolve().parent
manifest=json.loads((here/'artifacts.json').read_text());checks=0
for r in manifest['removed_jars']:
 assert not (root/'mods'/r['jar']).exists(),r['jar'];checks+=1
for r in manifest['repacked']:
 p=root/'mods'/r['jar'];assert hashlib.sha256(p.read_bytes()).hexdigest()==r['sha256'];checks+=1
 with ZipFile(p) as z:
  assert z.testzip() is None
  assert len(z.namelist())==len(set(z.namelist()))
  for name in z.namelist():
   if name.startswith('data/') and name.endswith('.json'):
    data=json.loads(z.read(name));assert data is not None
    if '/enchantment/' in name and '/tags/' not in name:
     id=name.split('/')[1]+':'+Path(name).stem
     assert id not in manifest['retired_enchantments'],id
    checks+=1
 with ZipFile(root/'tmp/enchantment-retirement/before'/r['jar']) as old,ZipFile(p) as new:
  edits=set(r['changed'])|set(r['removed'])
  for name in old.namelist():
   if name not in edits:assert old.read(name)==new.read(name),name
  checks+=1
print('PASS',checks,'archive, registry-resource, hash and unchanged-entry checks')

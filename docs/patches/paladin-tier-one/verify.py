from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/paladin-tier-one';installed='--installed' in sys.argv
rows=json.loads((H/'artifacts.json').read_text());unchanged=0
for row in rows:
 a=W/'before'/row['jar'];b=(R/'mods' if installed else W/'built')/row['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 assert sha(a)==row['before_sha256'] and sha(b)==row['sha256']
 if installed:assert b.read_bytes()==(R/'mod fixing/1 fixed packs'/row['jar']).read_bytes()
 with ZipFile(a) as old,ZipFile(b) as new:
  assert new.testzip() is None
  assert set(new.namelist())==set(old.namelist())|set(row['added'])
  assert [n for n in old.namelist() if old.read(n)!=new.read(n)]==row['changed']
  unchanged+=len(old.namelist())-len(row['changed'])
  for n in new.namelist():
   if n.endswith('.json'):json.loads(new.read(n))
  if row['jar'].startswith('spell_engine-'):
   blob=new.read('net/spell_engine/internals/PaladinTierOne.class');assert int.from_bytes(blob[6:8],'big')==69 and b'net/minecraft/client/' not in blob
  for n in old.namelist():
   if 'ClassArmorBalance' in n:assert old.read(n)==new.read(n)
for label in ['candidate','candidate-legacy']:
 assert (H/(label+'-result.txt')).read_text().startswith('PASS 75 checks; failures=0')
 hashes=json.loads((H/(label+'-tested.json')).read_text())
 for row in rows:assert hashes[row['jar']]==row['sha256']
assert (H/'client/client-result.txt').read_text().startswith('PASS 24 checks')
client=json.loads((H/'client/runtime-manifest.json').read_text())
for row in rows:assert client[row['jar']]==row['sha256']
subprocess.run([sys.executable,str(H/'verify_resources.py'),str(R/'mods' if installed else W/'built')],check=True)
print(f'PASS {unchanged} unchanged entries;75 fresh +75 legacy native checks;24 client checks; tested hashes match'+('; active/fixed copies match' if installed else ''))

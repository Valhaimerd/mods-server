from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/paladin-passives';r=json.loads((H/'artifacts.json').read_text());installed='--installed' in sys.argv
before=W/'before'/r['jar'];after=(R/'mods' if installed else W/'built')/r['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
if installed:assert after.read_bytes()==(R/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
with ZipFile(before) as a,ZipFile(after) as b:
 assert b.testzip() is None
 assert set(b.namelist())==set(a.namelist())|set(r['added'])
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
 for n in a.namelist():
  if '/paladin_tier_2_' in n:assert a.read(n)==b.read(n)
 assert int.from_bytes(b.read(r['added'][0])[6:8],'big')==69
 count=len(a.namelist())-len(r['changed'])
for label in ['candidate','candidate-legacy']:
 assert (H/(label+'-result.txt')).read_text().startswith('PASS 35 checks; failures=0')
 assert json.loads((H/(label+'-tested.json')).read_text())[r['jar']]==r['sha256']
assert (H/'client/client-result.txt').read_text().startswith('PASS 15 checks')
assert json.loads((H/'client/runtime-manifest.json').read_text())[r['jar']]==r['sha256']
subprocess.run([sys.executable,str(H/'verify_resources.py'),str(after.parent)],check=True)
print(f'PASS {count} unchanged entries;35 fresh +35 legacy native checks;15 client checks; tested hashes match'+('; active/fixed copies match' if installed else ''))

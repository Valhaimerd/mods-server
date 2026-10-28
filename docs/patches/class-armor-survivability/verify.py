from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-armor-survivability'
rows=json.loads((H/'artifacts.json').read_text());policy=json.loads((H/'policy.json').read_text());assert len(policy)==33
count=0
for x in rows:
 before=W/'before'/x['jar'];after=(R/'mods' if '--installed' in sys.argv else W/'built')/x['jar'];digest=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 assert digest(before)==x['before_sha256'] and digest(after)==x['sha256']
 if '--installed' in sys.argv:assert after.read_bytes()==(R/'mod fixing/1 fixed packs'/x['jar']).read_bytes()
 with ZipFile(before) as a,ZipFile(after) as b:
  assert b.testzip() is None and set(b.namelist())==set(a.namelist())|set(x['added'])
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==x['changed']
  assert len(x['changed'])==len(x['added'])==1
  assert x['changed'][0].endswith('.class') and x['added'][0].endswith('/ClassArmorBalance.class')
  blob=b.read(x['added'][0]);assert int.from_bytes(blob[6:8],'big')==69 and b'net/minecraft/client/' not in blob
  count+=len(a.namelist())-1
for label in ['candidate','candidate-legacy']:
 assert (H/(label+'-result.txt')).read_text().startswith('PASS 1626 checks; failures=0')
 tested=json.loads((H/(label+'-tested.json')).read_text())
 for x in rows:assert tested[x['jar']]==x['sha256']
assert (H/'baseline-result.txt').read_text().startswith('FAIL 726 checks; failures=350')
assert (H/'client/client-result.txt').read_text().startswith('PASS 462 checks')
c=json.loads((H/'client/runtime-manifest.json').read_text())
for x in rows:assert c[x['jar']]==x['sha256']
print(f'PASS:33 sets/132 pieces; {count} unchanged archive entries;1626 fresh +1626 legacy server checks;462 client checks; exact tested hashes'+('; active/fixed copies match' if '--installed' in sys.argv else ''))

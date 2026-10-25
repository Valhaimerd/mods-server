from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/building-axis-unlimited';r=json.loads((HERE/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
before=WORK/('before-'+r['jar']);after=(ROOT/'mods' if '--installed' in sys.argv else WORK/'built')/r['jar']
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
for n,v in r['sources'].items():assert sha(HERE/n)==v
with ZipFile(before) as a,ZipFile(after) as b:
 assert b.testzip() is None and set(b.namelist())==set(a.namelist())|set(r['added'])
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
 assert len(r['changed'])==5 and r['added']==['sophisticated/building/pack/AxisPolicy.class']
 for n in r['changed']+r['added']:assert n.endswith('.class') and int.from_bytes(b.read(n)[6:8],'big')==69
 for n in ['sophisticated/building/CommonConfig.class','sophisticated/building/CommonConfig$MaxBlocksPlacedAtOnce.class','sophisticated/building/ServerConfig.class','sophisticated/building/systems/ServerBlockPlacer.class','sophisticated/building/utilities/BlockSet.class']:
  assert a.read(n)==b.read(n)
assert (HERE/'runtime-tested-sha256.txt').read_text().strip()==sha(after)
assert (HERE/'candidate-results.txt').read_text().startswith('PASS 31')
checks=(HERE/'candidate-checks.txt').read_text()
for s in ['actual Survival placement across 32-block axis actual=32','server truncates oversized request to existing 128-block batch','actual 32-block placement consumes 32 materials actual remaining=224','native array reach label renders Unlimited']:assert s in checks
if '--installed' in sys.argv:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS tested axis-only overlay; 31 client/server checks; batch/material/placement/reach config and server placer preserved.')

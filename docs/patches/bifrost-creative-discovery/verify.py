from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys

HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/bifrost-creative-discovery'
r=json.loads((HERE/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
before=WORK/('before-'+r['jar']);after=WORK/'built'/r['jar'] if '--candidate' in sys.argv else ROOT/'mods'/r['jar']
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
assert sha(ROOT/'docs/patches/asgardian-expansion/src/ModItems.java')==r['source_sha256']
with ZipFile(before) as a,ZipFile(after) as b:
    assert b.testzip() is None and a.namelist()==b.namelist()
    assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']==['com/emandev/bifrost/ModItems.class']
    data=b.read(r['changed'][0]);assert int.from_bytes(data[6:8],'big')==69
    assert b'BuildCreativeModeTabContentsEvent' in data and b'creativeContents' in data
if '--runtime' in sys.argv:
    assert (HERE/'runtime-tested-sha256.txt').read_text().strip()==sha(after)
    results=(HERE/'candidate-results.txt').read_text().splitlines()
    assert not any(s.startswith('FAIL') for s in results),results
    assert sum(s.startswith('PASS') for s in results)>=25
if '--candidate' not in sys.argv:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS exact one-class creative registration repair; all recipes, gameplay, models, icon and other classes preserved.')

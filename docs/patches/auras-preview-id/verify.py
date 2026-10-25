from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/auras-preview-id';r=json.loads((HERE/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
afile=WORK/('before-'+r['jar']);bfile=(ROOT/'mods' if '--installed' in sys.argv else WORK/'built')/r['jar']
assert sha(afile)==r['before_sha256'] and sha(bfile)==r['sha256']
for name,value in r['sources'].items():assert sha(HERE/name)==value
with ZipFile(afile) as a,ZipFile(bfile) as b:
 assert b.testzip() is None and set(b.namelist())==set(a.namelist())|set(r['added'])
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']==['vazkii/patchouli/common/util/EntityUtil.class']
 assert r['added']==['vazkii/patchouli/common/util/PreviewEntityIds.class']
 for n in r['changed']+r['added']:assert int.from_bytes(b.read(n)[6:8],'big')==69
runtime=json.loads((HERE/'runtime-tested-mods.json').read_text());assert runtime[r['jar']]==sha(bfile)
assert sha(ROOT/'mods/animal_pen-neoforge-26.2-2.5.0.jar')==runtime['animal_pen-neoforge-26.2-2.5.0.jar']
assert (HERE/'baseline-results.txt').read_text().startswith('FAIL java.lang.IllegalStateException: Tried to access entity ID before ID assignment')
assert (HERE/'candidate-results.txt').read_text().startswith('PASS 48')
checks=(HERE/'candidate-checks.txt').read_text();assert checks.count('actual Animal Pen guidebook entry rendered')==8
assert 'preview is not registered or ticking in the world' in checks and 'preview identity stays stable across repeated frames' in checks
assert not list((ROOT/'mod store').glob('auras-*.jar')) and not list((ROOT/'mods').glob('auras-*.jar'))
assert len(list((ROOT/'mod store').glob('*.jar')))==23
assert '#### Auras' not in (ROOT/'docs/MODLIST.md').read_text(encoding='utf-8')
if '--installed' in sys.argv:assert bfile.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS Auras removed; exact tested Patchouli preview fix installed; Animal Pen unchanged; 48 checks and eight real book pages passed.')

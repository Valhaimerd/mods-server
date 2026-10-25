from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/elf-slowfall-delay'
r=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'));sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
before=WORK/('before-'+r['jar']);after=(ROOT/'mods' if '--installed' in sys.argv else WORK/'built')/r['jar']
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
assert sha(HERE/'RaceBalanceRules.java')==r['source_sha256']
with ZipFile(before) as a,ZipFile(after) as b:
 assert b.testzip() is None and a.namelist()==b.namelist()
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']==['com/eiden/iourusraces/race/RaceBalanceRules.class']
 assert int.from_bytes(b.read(r['changed'][0])[6:8],'big')==69
assert (HERE/'runtime-tested-sha256.txt').read_text().strip()==sha(after)
assert (HERE/'candidate-results.txt').read_text().startswith('PASS')
assert '4 actual connected-client bunny hops' in (HERE/'candidate-trace.txt').read_text()
if '--installed' in sys.argv:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS exact tested one-class Java-25 repair; other races/resources preserved; connected jumps and long drop passed.')

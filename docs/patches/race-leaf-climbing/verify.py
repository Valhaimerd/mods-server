from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/race-leaf-climbing';r=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'));sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
before=WORK/('before-'+r['jar']);after=(ROOT/'mods' if '--installed' in sys.argv else WORK/'built')/r['jar']
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
for name,value in r['sources'].items():assert sha(HERE/name)==value
with ZipFile(before) as a,ZipFile(after) as b:
 assert b.testzip() is None and set(b.namelist())==set(a.namelist())|set(r['added'])
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
 assert r['added']==['com/eiden/iourusraces/mixin/LivingEntityClimbingMixin.class']
 assert set(r['changed'])=={'iourusraces.mixins.json','assets/iourusraces/lang/en_us.json','assets/iourusraces/lang/es_es.json','com/eiden/iourusraces/race/RaceVisualRules.class'}
 assert a.read('com/eiden/iourusraces/race/RaceBalanceRules.class')==b.read('com/eiden/iourusraces/race/RaceBalanceRules.class')
 for n in r['changed']+r['added']:
  if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
 for lang in ['en_us','es_es']:
  name=f'assets/iourusraces/lang/{lang}.json';old=json.loads(a.read(name));new=json.loads(b.read(name));assert old.keys()==new.keys()
  expected={f'race.iourusraces.{race}.{suffix}' for race in ['elf','goblin'] for suffix in ['trait.2','tooltip']}
  assert {k for k in old if old[k]!=new[k]}==expected
  if lang=='en_us':assert all(not any(c.isdigit() or c=='%' for c in new[k]) for k in expected)
assert (HERE/'runtime-tested-sha256.txt').read_text().strip()==sha(after)
assert (HERE/'candidate-results.txt').read_text().startswith('PASS 76')
checks=(HERE/'candidate-checks.txt').read_text();assert checks.count('connected movement phase')==5 and 'GOBLIN rise=3.' in checks
if '--installed' in sys.argv:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS exact tested Java-25 leaf collision/climbing overlay; 76 runtime checks; delayed Elf falling and all unrelated classes preserved.')

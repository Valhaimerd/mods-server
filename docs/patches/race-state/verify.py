from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-state';r=json.loads((HERE/'artifacts.json').read_text())[0];folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods';before=WORK/('before-'+r['jar']);after=folder/r['jar']
assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256'] and hashlib.sha256(after.read_bytes()).hexdigest()==r['sha256']
with ZipFile(before) as a,ZipFile(after) as b:
 assert b.testzip() is None and set(a.namelist())<=set(b.namelist())
 assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
 assert sorted(set(b.namelist())-set(a.namelist()))==r['added']
 assert a.read('META-INF/neoforge.mods.toml')==b.read('META-INF/neoforge.mods.toml')
 assert a.read('com/eiden/iourusraces/race/RaceBalanceRules.class')==b.read('com/eiden/iourusraces/race/RaceBalanceRules.class')
 for n in r['changed']+r['added']:
  assert 'Probe' not in n
  if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
 if folder.resolve()==(ROOT/'mods').resolve():assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
for path in ['server/asgard-result.txt','client/race-client-result.txt']:
 result=(WORK/path).read_text();assert result.startswith('PASS ');print(result.strip())
print('PASS race-state overlay, preserved prior Orc/race policy, metadata, Java 25, CRC and matching fixed copy')

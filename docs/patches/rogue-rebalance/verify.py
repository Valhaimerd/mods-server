from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/rogue-rebalance';candidate='--candidate' in sys.argv
records=json.loads((HERE/'artifacts.json').read_text());client=json.loads((WORK/'client-tested-hashes.json').read_text())
for r in records:
 jar=(WORK/'built' if candidate else ROOT/'mods')/r['jar'];before=WORK/'before'/r['jar'];assert hashlib.sha256(jar.read_bytes()).hexdigest()==r['sha256'];assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 with ZipFile(before) as a,ZipFile(jar) as b:
  assert b.testzip() is None;assert set(a.namelist())<=set(b.namelist());assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed'];assert sorted(set(b.namelist())-set(a.namelist()))==r['added']
  assert all(n.endswith('.json') for n in r['changed'])
  for n in r['added']:assert int.from_bytes(b.read(n)[6:8],'big')==69 and 'Probe' not in n
  if r['jar'].startswith('spell_engine'):
   old=json.loads(a.read('spell_engine.mixins.json'));new=json.loads(b.read('spell_engine.mixins.json'));new['mixins'].remove('roguerebalance.RogueAdjustmentMixin');assert old==new
  if r['jar'].startswith('rogues'):
   trap=json.loads(b.read('data/rogues/spell/bear_trap.json'));assert trap['cost']['cooldown']['duration']==4 and trap['impacts'][0]['action']['damage']['spell_power_coefficient']==.4
 assert (WORK/'server/mods'/r['jar']).read_bytes()==jar.read_bytes()
 assert client[r['jar']]==r['sha256'] and (WORK/'client/mods'/r['jar']).read_bytes()==jar.read_bytes()
 if not candidate:assert (ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()==jar.read_bytes()
for result in [WORK/'server/warrior-result.txt',WORK/'client/tooltip-result.txt']:
 assert result.read_text().startswith('PASS');print(result.read_text().strip())
print('PASS exact three-archive overlay, preserved prior bytecode/party/lifecycle patches, Java25/CRC, tested hash identity')

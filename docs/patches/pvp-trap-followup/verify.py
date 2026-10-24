from pathlib import Path
from zipfile import ZipFile
import hashlib,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pvp-trap-followup'
for r in json.loads((HERE/'artifacts.json').read_text()):
 active=ROOT/'mods'/r['jar'];before=WORK/'before'/r['jar'];assert hashlib.sha256(active.read_bytes()).hexdigest()==r['sha256']
 with ZipFile(before) as a,ZipFile(active) as b:
  assert b.testzip() is None;assert set(a.namelist())==set(b.namelist());assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
 if r['changed']:assert active.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 tested=WORK/'regression/server/mods'/r['jar'];assert tested.read_bytes()==active.read_bytes()
with ZipFile(next((ROOT/'mods').glob('rogues-*.jar'))) as z:
 trap=json.loads(z.read('data/rogues/spell/bear_trap.json'));assert trap['cost']['cooldown']=={'duration':4,'haste_affected':True};assert trap['impacts'][0]['action']['damage']['spell_power_coefficient']==.4
r=json.loads((ROOT/'docs/patches/pvp-flagging-26.2/artifacts.json').read_text());assert r['pvp_damage_multiplier']==.3
active=ROOT/'mods/pvp_flagging-26.2-1.1.2.jar';assert hashlib.sha256(active.read_bytes()).hexdigest()==r['output_sha256'];assert active.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/active.name).read_bytes()
assert (ROOT/'tmp/pvp-26.2/server/tested-sha256.txt').read_text()==r['output_sha256']
for result in [ROOT/'tmp/pvp-26.2/server/result.txt',WORK/'regression/server/warrior-result.txt']:
 assert result.read_text().startswith('PASS');print(result.read_text().strip())
print('PASS production hashes, exact overlay, fixed copies, 4s haste cooldown and unchanged ordinary 40% trap')

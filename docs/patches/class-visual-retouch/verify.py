from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/class-visual-retouch';installed='--installed' in sys.argv;root=R/'mods' if installed else W/'built';rows=json.loads((H/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();visual={'visuals','sound','start_sound','presence_sound','animation','particles','travel_particles','interval_particles','client_data'}
def mechanics(value):
 if isinstance(value,list):return [mechanics(x) for x in value]
 if isinstance(value,dict):return {k:mechanics(v) for k,v in value.items() if k not in visual}
 return value
checked=preserved=0
for row in rows:
 before=W/'before'/row['jar'];after=root/row['jar'];assert sha(before)==row['before_sha256'];assert sha(after)==row['sha256']
 if installed:assert after.read_bytes()==(R/'mod fixing/1 fixed packs'/row['jar']).read_bytes()
 with ZipFile(before) as a,ZipFile(after) as b:
  assert b.testzip() is None;assert set(b.namelist())==set(a.namelist())|set(row['added'])
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==row['changed'];preserved+=len(a.namelist())-len(row['changed'])
  for n in a.namelist():
   if n.endswith('/connections.json') or n.endswith('/skills.json') or n.endswith('/definitions.json') or n.endswith('neoforge.mods.toml'):assert a.read(n)==b.read(n),n
   if n.startswith('data/') and '/spell/' in n and n.endswith('.json'):
    assert mechanics(json.loads(a.read(n)))==mechanics(json.loads(b.read(n))),n;checked+=1
   if n.endswith(('/mob_effect/shock.png','/mob_effect/bear_trap.png','/mob_effect/frozen.png','/mob_effect/stun.png')):assert a.read(n)==b.read(n),n
  for n in row['added']:
   assert 'probe' not in n.lower(),n
   if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
print('PASS',len(rows),'archive identities;',checked,'spell gameplay documents unchanged;',preserved,'preserved entries'+('; active/fixed match' if installed else ''))
if '--runtime' in sys.argv:
 for label in ['candidate','candidate-legacy']:
  assert (H/(label+'-result.txt')).read_text().startswith('PASS 229 checks; failures=0')
  tested=json.loads((H/(label+'-tested.json')).read_text())
  for row in rows:assert tested[row['jar']]==row['sha256'],(label,row['jar'])
 print('PASS final fresh/stale native test identities')

from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
h=Path(__file__).resolve().parent;r=h.parents[2];w=r/'tmp/paladin-followup';installed='--installed' in sys.argv;root=r/'mods' if installed else w/'built';rows=json.loads((h/'artifacts.json').read_text());preserved=0;sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for row in rows:
 a=w/'before'/row['jar'];b=root/row['jar'];assert sha(a)==row['before_sha256'];assert sha(b)==row['sha256']
 if installed:assert b.read_bytes()==(r/'mod fixing/1 fixed packs'/row['jar']).read_bytes()
 with ZipFile(a) as old,ZipFile(b) as new:
  assert new.testzip() is None;assert set(new.namelist())==set(old.namelist())|set(row['added']);assert [n for n in old.namelist() if old.read(n)!=new.read(n)]==row['changed'];preserved+=len(old.namelist())-len(row['changed'])
  for n in new.namelist():
   if n.endswith('.class') and n in row['changed']+row['added']:assert b'PURGATORY-END' not in new.read(n);assert int.from_bytes(new.read(n)[6:8],'big')==69
   assert 'probe' not in n.lower(),n
  for n in old.namelist():
   if n.endswith(('connections.json','skills.json','definitions.json','neoforge.mods.toml')) or '/textures/mob_effect/' in n:assert old.read(n)==new.read(n),n
 if '--runtime' in sys.argv:
  for label in ['candidate','candidate-legacy']:
   assert (h/(label+'-result.txt')).read_text().startswith('PASS 241 checks; failures=0');tested=json.loads((h/(label+'-tested.json')).read_text());assert tested[row['jar']]==row['sha256']
print('PASS3archive identities;',preserved,'unrelated entries preserved; metadata/tree/icons preserved; no probes or diagnostic bytecode'+('; active/fixed match' if installed else ''))

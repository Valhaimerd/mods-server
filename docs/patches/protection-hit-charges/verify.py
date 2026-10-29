from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
h=Path(__file__).resolve().parent;r=h.parents[2];w=r/'tmp/protection-hit-charges';installed='--installed' in sys.argv;root=r/'mods' if installed else w/'built';preserved=0
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for row in json.loads((h/'artifacts.json').read_text()):
 a=w/'before'/row['jar'];b=root/row['jar'];assert sha(a)==row['before_sha256'];assert sha(b)==row['sha256']
 if installed and row['changed']:assert b.read_bytes()==(r/'mod fixing/1 fixed packs'/row['jar']).read_bytes()
 with ZipFile(a) as old,ZipFile(b) as new:
  assert new.testzip() is None;assert set(new.namelist())==set(old.namelist())|set(row['added']);assert [n for n in old.namelist() if old.read(n)!=new.read(n)]==row['changed'];preserved+=len(old.namelist())-len(row['changed'])
  for n in row['changed']+row['added']:
   if n.endswith('.class'):assert int.from_bytes(new.read(n)[6:8],'big')==69
  assert not any('protectionprobe' in n.lower() for n in new.namelist())
 for label in ['candidate','candidate-legacy']:
  assert (h/(label+'-result.txt')).read_text().startswith('PASS 0 failures / 27 checks');assert json.loads((h/(label+'-tested.json')).read_text())[row['jar']]==row['sha256']
print('PASS: fresh/stale runtime hashes match; archive integrity and Java25 verified;',preserved,'unrelated entries preserved'+('; installed/fixed copies match' if installed else ''))

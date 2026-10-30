from pathlib import Path
from zipfile import ZipFile
import hashlib,json

R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-follow-up'
preserved=0;changed=[]
for row in json.loads((H/'artifacts.json').read_text(encoding='utf-8')):
 if not(row['changed'] or row['added']):continue
 active=R/'mods'/row['jar'];active=active if active.exists() else R/'mod store'/row['jar']
 expected=row['sha256']
 for p in [active,R/'mod fixing/1 fixed packs'/row['jar']]:assert hashlib.sha256(p.read_bytes()).hexdigest()==expected,p
 with ZipFile(W/'before'/row['jar']) as before,ZipFile(active) as final:
  old=set(before.namelist());new=set(final.namelist());assert new-old==set(row['added']);assert old<=new
  for name in old:
   if name in row['changed']:continue
   assert before.read(name)==final.read(name),(active,name);preserved+=1
  for name in set(row['changed'])|set(row['added']):
   assert 'local/paladinprobe/' not in name and 'local/vanishprobe/' not in name,name
   if name.endswith('.class'):assert int.from_bytes(final.read(name)[6:8],'big')==69,name
 changed.append(active.name)
for label in ['candidate','candidate-legacy']:
 assert (H/(label+'-result.txt')).read_text().startswith('PASS'),label
 tested=json.loads((H/(label+'-tested.json')).read_text(encoding='utf-8'))
 for row in json.loads((H/'installed.json').read_text(encoding='utf-8')):assert tested[Path(row['path']).name]==row['sha256'],(label,row['path'])
print(f'PASS: {len(changed)} installed/fixed repacks; {preserved} unrelated archive entries preserved; Java 25; fresh/stale tested hashes match.')

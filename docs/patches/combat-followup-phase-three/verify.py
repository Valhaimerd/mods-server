from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-followup-phase-three';records=json.loads((H/'artifacts.json').read_text());count=0
for row in records:
 b=(R/'mods' if '--installed' in sys.argv else W/'built')/row['jar'];assert hashlib.sha256(b.read_bytes()).hexdigest()==row['after']
 with ZipFile(W/'before'/row['jar']) as za,ZipFile(b) as zb:
  assert set(za.namelist()).issubset(zb.namelist());assert zb.testzip() is None
  for n in za.namelist():
   if n not in row['changed']:assert za.read(n)==zb.read(n),n;count+=1
  assert za.read('META-INF/neoforge.mods.toml')==zb.read('META-INF/neoforge.mods.toml')
  assert not any(n.startswith(('local/remote/','local/audit/')) for n in zb.namelist())
 if '--installed' in sys.argv:assert hashlib.sha256((R/'mod fixing/1 fixed packs'/row['jar']).read_bytes()).hexdigest()==row['after']
print('PASS',count,'unchanged-entry preservation checks')
(H/'preservation-result.txt').write_text(f'PASS {count} unchanged entries, metadata, archive validity and hashes verified.\n')

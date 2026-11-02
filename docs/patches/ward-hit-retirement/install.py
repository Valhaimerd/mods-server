from pathlib import Path
from zipfile import ZipFile
import json,hashlib,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/ward-hit-retirement';sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert (H/'candidate-result.txt').read_text().startswith('PASS')
rows=json.loads((H/'artifacts.json').read_text());tested=json.loads((H/'candidate-tested.json').read_text());retired=R/'mod fixing/retired packs/ward-hit-retirement-20261102';retired.mkdir(parents=True,exist_ok=True)
for row in rows:
 n=row['jar'];assert sha(R/'mods'/n)==row['before'];assert sha(W/'built'/n)==row['after']==tested[n]
 with ZipFile(W/'before'/n) as a,ZipFile(W/'built'/n) as b:assert b.testzip() is None;assert {k for k in a.namelist() if a.read(k)!=b.read(k)}==set(row['changed'])
removed=[]
for pattern in ('ly-fangs-*','scalarrpgutils-*'):
 p=next((R/'mods').glob(pattern));removed.append({'jar':p.name,'sha256':sha(p)});shutil.copy2(p,retired/p.name);p.unlink()
 fixed=R/'mod fixing/1 fixed packs'/p.name
 if fixed.exists():fixed.unlink()
for row in rows:
 n=row['jar'];shutil.copy2(R/'mods'/n,retired/n)
 for target in (R/'mods'/n,R/'mod fixing/1 fixed packs'/n):shutil.copy2(W/'built'/n,target)
(H/'removed.json').write_text(json.dumps(removed,indent=2)+'\n');print('Installed three repacks and retired Fangs/Scalar; RPG Series retained.')

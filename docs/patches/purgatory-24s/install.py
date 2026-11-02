from pathlib import Path
from zipfile import ZipFile
import json,hashlib,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/purgatory-24s';sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert (H/'candidate-result.txt').read_text().startswith('PASS 50')
rows=json.loads((H/'artifacts.json').read_text());tested=json.loads((H/'candidate-tested.json').read_text())
for row in rows:
 name=row['jar'];assert sha(R/'mods'/name)==row['before'];assert sha(W/'built'/name)==row['after']==tested[name]
 with ZipFile(W/'before'/name) as a,ZipFile(W/'built'/name) as b:assert b.testzip() is None;assert {k for k in a.namelist() if a.read(k)!=b.read(k)}==set(row['changed']);assert set(a.namelist())==set(b.namelist())
backup=R/'mod backup/purgatory-24s-2026-11-02';backup.mkdir(parents=True,exist_ok=True)
for row in rows:
 name=row['jar'];shutil.copy2(R/'mods'/name,backup/name)
 for target in (R/'mods'/name,R/'mod fixing/1 fixed packs'/name):shutil.copy2(W/'built'/name,target);assert sha(target)==row['after']
print('Installed two tested timing-only replacements; unrelated pending changes preserved.')

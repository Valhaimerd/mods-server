from pathlib import Path
import json,hashlib,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/purgatory-continuous-pull';sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert (H/'focused-results.txt').read_text().startswith('PASS');assert (W/'client/pull-client-result.txt').read_text()=='PASS'
rows=json.loads((H/'artifacts.json').read_text())
for row in rows:
 n=row['jar'];assert sha(R/'mods'/n)==row['before'];assert sha(W/'built'/n)==row['after']
 # Final localization-only polish need not repeat the already-passing client locomotion test.
 from zipfile import ZipFile
 with ZipFile(W/'client/mods'/n) as tested,ZipFile(W/'built'/n) as final:
  for key in final.namelist():
   if key.endswith('.class'):assert final.read(key)==tested.read(key),(n,key)
backup=R/'mod backup/purgatory-continuous-pull-2026-11-02';backup.mkdir(parents=True,exist_ok=True)
for row in rows:
 n=row['jar'];shutil.copy2(R/'mods'/n,backup/n)
 for target in (R/'mods'/n,R/'mod fixing/1 fixed packs'/n):shutil.copy2(W/'built'/n,target);assert sha(target)==row['after']
for name in ('pull-client-checks.txt','pull-client-result.txt'):shutil.copy2(W/'client'/name,H/name)
print('Installed three matching Paladin/Skill Tree/Spell Engine replacements; Ward tuning preserved.')

from pathlib import Path
import json,hashlib,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-followup-phase-one';records=json.loads((H/'artifacts.json').read_text());backup=R/'mod backup/combat-followup-phase-one-2026-11-02';backup.mkdir(parents=True,exist_ok=True)
assert (H/'candidate-result.txt').read_text().startswith('PASS')
assert (W/'client/spatial-client-result.txt').read_text().strip()=='PASS'
for row in records:
 active=R/'mods'/row['jar'];assert hashlib.sha256(active.read_bytes()).hexdigest()==row['before'],'Active input changed: '+str(active)
 assert hashlib.sha256((W/'built'/row['jar']).read_bytes()).hexdigest()==row['after']
for row in records:
 active=R/'mods'/row['jar'];shutil.copy2(active,backup/active.name)
 for directory in [R/'mods',R/'mod fixing/1 fixed packs']:
  directory.mkdir(exist_ok=True);shutil.copy2(W/'built'/row['jar'],directory/row['jar'])
for src,dest in [(W/'client/spatial-client-checks.txt',H/'client-checks.txt'),(W/'client/spatial-client-result.txt',H/'client-result.txt'),(W/'handbook-review.json',H/'handbook-review.json')]:shutil.copy2(src,dest)
(H/'completion.json').write_text(json.dumps({'status':'installed','date':'2026-11-02','jars':[{k:v for k,v in r.items() if k in ['jar','after']} for r in records],'native_result':(H/'candidate-result.txt').read_text().strip(),'client_checks':sum(s.startswith('PASS ') for s in (H/'client-checks.txt').read_text().splitlines()),'personal_installations_modified':False,'committed_or_pushed':False},indent=2)+'\n')
print('Installed six matching replacements; originals backed up; personal installations untouched')

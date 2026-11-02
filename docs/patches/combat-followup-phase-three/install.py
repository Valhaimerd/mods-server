from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-followup-phase-three';T=W/'candidate';records=json.loads((H/'artifacts.json').read_text());backup=R/'mod backup/combat-followup-phase-three-2026-11-02'
assert (T/'client/remote-result.txt').read_text().strip()=='PASS'
for row in records:
 for p,key in [(R/'mods'/row['jar'],'before'),(W/'built'/row['jar'],'after'),(T/'server/mods'/row['jar'],'after'),(T/'client/mods'/row['jar'],'after')]:assert hashlib.sha256(p.read_bytes()).hexdigest()==row[key],p
backup.mkdir(parents=True,exist_ok=True)
for row in records:
 p=R/'mods'/row['jar'];shutil.copy2(p,backup/p.name)
 for dest in [R/'mods',R/'mod fixing/1 fixed packs']:shutil.copy2(W/'built'/p.name,dest/p.name)
for label in ['baseline','candidate']:
 for src,name in [(W/label/'client/remote-checks.txt','remote-'+label+'-checks.txt'),(W/label/'client/remote-result.txt','remote-'+label+'-result.txt')]:shutil.copy2(src,H/name)
 (H/('remote-'+label+'-tested.json')).write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in (W/label/'server/mods').glob('*.jar')},indent=2)+'\n')
(H/'completion.json').write_text(json.dumps({'status':'installed','date':'2026-11-02','jars':[{k:v for k,v in r.items() if k in ['jar','after']} for r in records],'remote_checks':sum(s.startswith('PASS ') for s in (H/'remote-candidate-checks.txt').read_text().splitlines()),'native_checks':21,'trajectory_checks':128,'personal_installations_modified':False,'committed_or_pushed':False},indent=2)+'\n')
print('Installed two matching archery replacements; phases one and two retained')

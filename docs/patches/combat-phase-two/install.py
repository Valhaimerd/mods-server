from pathlib import Path
import hashlib,json,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-phase-two'
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
records=json.loads((H/'artifacts.json').read_text());baseline=json.loads((H/'baseline.json').read_text())
for file in ('candidate-result.txt','regression-result.txt','archive-result.txt'):assert (H/file).read_text().startswith('PASS'),file
assert (W/'client/spatial-client-result.txt').read_text().startswith('PASS')
for record in records:
 n=record['jar'];assert sha(R/'mods'/n)==record['before'],n+' changed since snapshot'
 assert sha(W/'built'/n)==record['after']
 for result in ('candidate-tested.json','regression-tested.json'):
  assert json.loads((H/result).read_text())[n]==record['after'],(result,n)
 assert sha(W/'client/mods'/n)==record['after'],n+' client tested'
backup=R/'mod backup/combat-phase-two-2026-11-02';backup.mkdir(parents=True,exist_ok=True)
for record in records:
 n=record['jar'];saved=backup/n
 if saved.exists():assert sha(saved)==record['before']
 else:shutil.copy2(R/'mods'/n,saved)
 for target in (R/'mods'/n,R/'mod fixing/1 fixed packs'/n):shutil.copy2(W/'built'/n,target);assert sha(target)==record['after']
for n,expected in baseline.items():
 if n not in {r['jar'] for r in records}:assert sha(R/'mods'/n)==expected,n+' unrelated snapshot changed'
print('Installed five tested same-filename JARs into repository mods and fixed packs; backups retained.')

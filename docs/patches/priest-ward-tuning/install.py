from pathlib import Path
import hashlib,json,shutil,subprocess,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/priest-ward-tuning'
subprocess.run([sys.executable,str(H/'verify.py')],check=True)
assert (H/'client-result.txt').read_text().strip()=='PASS'
tested=json.loads((H/'client-tested.json').read_text());rows=json.loads((H/'artifacts.json').read_text())
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for row in rows:
 assert tested[row['jar']]==row['after']==sha(W/'built'/row['jar'])
 for folder,label in [('mods','active'),('mod fixing/1 fixed packs','fixed')]:
  original=R/folder/row['jar'];assert sha(original)==row['before'],str(original)
  backup=R/'mod fixing/retired packs/priest-ward-tuning-2026-11-04'/label/row['jar'];backup.parent.mkdir(parents=True,exist_ok=True)
  if backup.exists():assert sha(backup)==row['before']
  else:shutil.copy2(original,backup)
for row in rows:
 for folder in ['mods','mod fixing/1 fixed packs']:
  target=R/folder/row['jar'];shutil.copy2(W/'built'/row['jar'],target);assert sha(target)==row['after']
subprocess.run([sys.executable,str(H/'verify.py'),'--installed'],check=True)
(H/'installation.json').write_text(json.dumps({'installed':True,'runtime_checks':sum(line.startswith('PASS ') for line in (H/'client-checks.txt').read_text().splitlines()),'jars':{r['jar']:r['after'] for r in rows}},indent=2)+'\n')
print('Installed exact runtime-tested candidates in active/fixed packs; originals backed up.')

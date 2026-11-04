from pathlib import Path
import hashlib,json,shutil,subprocess,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/archer-assist-repair'
subprocess.run([sys.executable,str(H/'verify.py')],check=True)
row=json.loads((H/'artifact.json').read_text());name=row['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert (H/'candidate-result.txt').read_text().startswith('PASS')
assert json.loads((H/'candidate-tested.json').read_text())[name]==row['after']
for folder,label in [('mods','active'),('mod fixing/1 fixed packs','fixed')]:
 original=R/folder/name;assert sha(original)==row['before'],str(original)
 backup=R/'mod fixing/retired packs/archer-assist-repair-2026-11-04'/label/name;backup.parent.mkdir(parents=True,exist_ok=True)
 if backup.exists():assert sha(backup)==row['before']
 else:shutil.copy2(original,backup)
for folder in ['mods','mod fixing/1 fixed packs']:shutil.copy2(W/'built'/name,R/folder/name)
subprocess.run([sys.executable,str(H/'verify.py'),'--installed'],check=True)
(H/'installation.json').write_text(json.dumps({'installed':True,'jar':name,'sha256':row['after']},indent=2)+'\n')
print('Installed exact native-tested replacement in active/fixed packs; originals backed up.')

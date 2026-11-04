"""Install only exact verified candidate bytes, retaining actual current originals."""
from pathlib import Path
import subprocess,json,hashlib,shutil,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-7-9';sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
subprocess.run([sys.executable,str(H/'verify.py')],check=True)
for row in json.loads((H/'artifacts.json').read_text()):
 name=row['jar'];assert sha(R/'mods'/name)==row['before'],name+' changed since snapshot'
 for folder,label in [('mods','active'),('mod fixing/1 fixed packs','fixed')]:
  target=R/folder/name;backup=R/'mod fixing/retired packs/class-balance-phases-7-9-2026-11-04'/label/name;backup.parent.mkdir(parents=True,exist_ok=True)
  if target.exists():
   if backup.exists():assert backup.read_bytes()==target.read_bytes(),'Existing backup mismatch'
   else:shutil.copy2(target,backup)
  target.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(W/'built'/name,target);assert sha(target)==row['after']
subprocess.run([sys.executable,str(H/'verify.py'),'--installed'],check=True)
print('Installed four tested same-name replacements; no handbook rebuild or Git push')

from pathlib import Path
from zipfile import ZipFile
import subprocess,shutil,json,hashlib
R=Path(__file__).resolve().parents[3];W=R/'tmp/combat-followup-phase-three';(W/'before').mkdir(parents=True,exist_ok=True);J='C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/java.exe'
records=[]
for pattern in ['crossarms-*','spell_engine-*']:
 p=next((R/'mods').glob(pattern));b=W/'before'/p.name
 if not b.exists():shutil.copy2(p,b)
 records.append({'jar':p.name,'sha256':hashlib.sha256(b.read_bytes()).hexdigest()})
targets={next((W/'before').glob('crossarms-*')):None,next((W/'before').glob('spell_engine-*')):['net/spell_engine/internals/ArrowAssist.class'],R/'tmp/better-revive-regression/server/libraries/net/neoforged/minecraft-server-patched/26.2.0.90/minecraft-server-patched-26.2.0.90.jar':['net/minecraft/world/item/CrossbowItem.class','net/minecraft/world/entity/projectile/arrow/AbstractArrow.class']}
for jar,entries in targets.items():
 with ZipFile(jar) as z:
  if entries is None:entries=[n for n in z.namelist() if n.endswith('.class') and n.startswith('local/crossarms/')]
  for n in entries:
   p=W/'inspect'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(n));subprocess.run([J,'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(p),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,check=True)
  if jar.name.startswith('crossarms'):
   for n in z.namelist():
    if n.endswith(('.mcfunction','.json')) and ('frenzy' in n or n.endswith('mixins.json')):
     p=W/'resources'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(n))
(W/'baseline.json').write_text(json.dumps(records,indent=2)+'\n');print('Captured two current inputs and firing/arrow runtime')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/spawn-spring';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True);jar=next((ROOT/'mods').glob('spreadspawnpoints-*.jar'));before=WORK/('before-'+jar.name)
if not before.exists():shutil.copy2(jar,before)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));classes=WORK/'classes';classes.mkdir(exist_ok=True)
subprocess.run([str(java/'javac.exe'),'--release','25','-cp',str(asm),'-d',str(classes),str(HERE/'PatchSpring.java')],check=True)
n='xyz/verarr/spreadspawnpoints/spawnpoints/SpawnPointManager.class'
with ZipFile(before) as z:(WORK/'input.class').write_bytes(z.read(n))
subprocess.run([str(java/'java.exe'),'-cp',str(classes)+';'+str(asm),'PatchSpring',str(WORK/'input.class'),str(WORK/'output.class')],check=True)
with ZipFile(before) as a,ZipFile(OUT/jar.name,'w') as b:
 for entry in a.infolist():b.writestr(entry,(WORK/'output.class').read_bytes() if entry.filename==n else a.read(entry))
r={'jar':jar.name,'before_sha256':hashlib.sha256(before.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/jar.name).read_bytes()).hexdigest(),'changed':[n]};(HERE/'artifacts.json').write_text(json.dumps([r],indent=2)+'\n');print('Built Spring-default same-filename candidate')

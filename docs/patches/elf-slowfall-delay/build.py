from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/elf-slowfall-delay';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=ROOT/'mods/iourusraces-1.1.0.jar';before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=str(before)+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'RaceBalanceRules.java')]
a=WORK/'compile.args';a.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args),encoding='utf-8');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
key='com/eiden/iourusraces/race/RaceBalanceRules.class'
with ZipFile(before) as z,ZipFile(OUT/active.name,'w') as out:
 for info in z.infolist():out.writestr(info,(classes/key).read_bytes() if info.filename==key else z.read(info))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(HERE/'artifacts.json').write_text(json.dumps({'jar':active.name,'before_sha256':sha(before),'sha256':sha(OUT/active.name),'source_sha256':sha(HERE/'RaceBalanceRules.java'),'changed':[key]},indent=2)+'\n',encoding='utf-8')
print('Built delayed Elf slow falling candidate')

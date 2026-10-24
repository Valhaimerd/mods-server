"""Append native creative-tab population; preserve all current Bifrost repairs."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess

HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/bifrost-creative-discovery'
OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('bifrost-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
base=(ROOT/'tmp/asgardian-expansion/compile.args').read_text()
cp=before.as_posix()+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);source=ROOT/'docs/patches/asgardian-expansion/src/ModItems.java'
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(source)]
argfile=WORK/'compile.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run(['C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javac.exe','@'+str(argfile)],check=True)
entry='com/emandev/bifrost/ModItems.class'
with ZipFile(before) as src,ZipFile(OUT/active.name,'w') as dst:
    for info in src.infolist():dst.writestr(info,(classes/entry).read_bytes() if info.filename==entry else src.read(info))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
record={'jar':active.name,'before_sha256':sha(before),'sha256':sha(OUT/active.name),'changed':[entry],'source_sha256':sha(source)}
(HERE/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n')
print('Built creative-tab registration; all other classes, recipes, models and resources unchanged.')

"""Set current maximum health on the existing revive JARs' downed-entry paths."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import subprocess

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
WORK=ROOT/'tmp/downed-backpack'
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
CLASSES=WORK/'classes'
cp=(WORK/'classpath.txt').read_text()
args='--release\n25\n-cp\n"'+cp+'"\n-d\n"'+CLASSES.as_posix()+'"\n"'+(HERE/'PatchDownedHealth.java').as_posix()+'"\n'
(WORK/'health-compile.args').write_text(args)
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'health-compile.args')],check=True)
specs=[
    ('PlayerRevive_NEOFORGE_v2.1.2_mc26.2.jar','208da992b409d77414df048703cbdf04d655a51d3381dbe4508416604cef533e',
     ['team/creative/playerrevive/server/PlayerReviveServer.class','team/creative/playerrevive/server/ReviveEventServer.class']),
    ('Better Revive - 2.2.0 - NeoForge - 26.2-MultiGraveFix-Java25.jar','3e883f32cf27bb0519da5bb73ad4be88541ee2f13abdc6f94c43d2f2abd7a750',
     ['de/mrstilu2k6/better_revive/util/DownedPlayerManager.class']),
]
reports=[]
for name,sha,targets in specs:
    source=WORK/'before'/name
    assert hashlib.sha256(source.read_bytes()).hexdigest()==sha
    with ZipFile(source) as z:
        for target in targets:
            path=WORK/'health-classes'/target;path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(z.read(target))
            args='-cp\n"'+CLASSES.as_posix()+';'+cp+'"\nPatchDownedHealth\n"'+path.as_posix()+'"\n'
            (WORK/'health-patch.args').write_text(args)
            subprocess.run([str(JAVA/'java.exe'),'@'+str(WORK/'health-patch.args')],check=True)
    with ZipFile(source) as src,ZipFile(WORK/name,'w') as dst:
        for entry in src.infolist():
            dst.writestr(entry,(WORK/'health-classes'/entry.filename).read_bytes() if entry.filename in targets else src.read(entry.filename))
    reports.append({'jar':name,'before_sha256':sha,'after_sha256':hashlib.sha256((WORK/name).read_bytes()).hexdigest(),'changed_entries':targets})
(WORK/'health-artifacts.json').write_text(json.dumps(reports,indent=2)+'\n')
print(json.dumps(reports,indent=2))

"""Disposable connected client: active animation mods, no personal world changes."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil, subprocess, uuid, sys, hashlib, time, json

ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/ultimate-armor-visuals';WORK.mkdir(exist_ok=True);CLIENT=WORK/'client';CLIENT.mkdir(exist_ok=True)
mods=CLIENT/'mods';mods.mkdir(exist_ok=True)
source=ROOT/'tmp/archer-tier-one/client'
for p in (source/'mods').glob('*.jar'):
    if 'probe' not in p.name.lower() and (ROOT/'mods'/p.name).exists():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
for p in (WORK/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
if not (CLIENT/'saves/Punchy Flat').exists():shutil.copytree(source/'saves/Punchy Flat',CLIENT/'saves/Punchy Flat')
(CLIENT/'launch.py').write_text((ROOT/'tmp/rogue-tier-two/client/launch.py').read_text().replace('GearProbe','Berserker'+uuid.uuid4().hex[:6]))
shutil.copy2(ROOT/'tmp/rogue-tier-two/client/options.txt',CLIENT/'options.txt')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text()
cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'probe-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'ClientArmorProbe.java')]
argfile=WORK/'probe.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(mods/'ultimate-armor-probe.jar','w',ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="ultimate_armor_probe"\nversion="1"\ndisplayName="Isolated Berserker appearance probe"\n')
    for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
if '--collect' not in sys.argv:
    (CLIENT/'visual-result.txt').unlink(missing_ok=True)
    subprocess.run(['python',str(CLIENT/'launch.py'),'--world'],check=True)
deadline=time.monotonic()+240
while not (CLIENT/'visual-result.txt').exists() and time.monotonic()<deadline:
    time.sleep(1)
assert (CLIENT/'visual-result.txt').exists(), 'Client result missing; inspect isolated launcher-output.log'
result=(CLIENT/'visual-result.txt').read_text()
shutil.copy2(CLIENT/'visual-results.txt',HERE/'runtime-results.txt')
print(result.strip())
assert result.startswith('PASS'),result

records=[json.loads((HERE/"artifacts.json").read_text())]
(HERE/"runtime-tested.json").write_text(json.dumps({r["jar"]:hashlib.sha256((mods/r["jar"]).read_bytes()).hexdigest() for r in records},indent=2)+"\n")

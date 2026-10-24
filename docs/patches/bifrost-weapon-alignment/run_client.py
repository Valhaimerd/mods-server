"""Disposable connected client: active animation mods, no personal world changes."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil, subprocess, uuid, sys, hashlib, time

ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/bifrost-weapon-alignment';CLIENT=WORK/'client';CLIENT.mkdir(exist_ok=True)
mods=CLIENT/'mods';mods.mkdir(exist_ok=True)
source=ROOT/'tmp/archer-tier-one/client'
for p in (source/'mods').glob('*.jar'):
    if 'probe' not in p.name.lower() and (ROOT/'mods'/p.name).exists():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
for pattern in ['punchy-*.jar','modded-swords-x-punchy-*.jar']:
    for p in (ROOT/'mod store').glob(pattern):shutil.copy2(p,mods/p.name)
for pattern in ['bifrost-*.jar','iourusraces-*.jar','bettercombat-*.jar']:
    for p in (ROOT/'mods').glob(pattern):shutil.copy2(p,mods/p.name)
candidate=next((WORK/'built').glob('bifrost-*.jar'))
tested=WORK/('before-'+candidate.name) if '--baseline' in sys.argv else candidate
shutil.copy2(tested,mods/tested.name.removeprefix('before-'))
if not (CLIENT/'saves/Punchy Flat').exists():shutil.copytree(source/'saves/Punchy Flat',CLIENT/'saves/Punchy Flat')
(CLIENT/'launch.py').write_text((ROOT/'tmp/rogue-tier-two/client/launch.py').read_text().replace('GearProbe','Bifrost'+uuid.uuid4().hex[:6]))
shutil.copy2(ROOT/'tmp/rogue-tier-two/client/options.txt',CLIENT/'options.txt')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text()
cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'probe-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'ClientWeaponProbe.java')]
argfile=WORK/'probe.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(mods/'bifrost-weapon-probe.jar','w',ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="bifrost_weapon_probe"\nversion="1"\ndisplayName="Isolated BifrostWeapon render probe"\n')
    for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
if '--collect' not in sys.argv:
    (CLIENT/'weapon-result.txt').unlink(missing_ok=True)
    subprocess.run(['python',str(CLIENT/'launch.py'),'--world'],check=True)
deadline=time.monotonic()+240
while not (CLIENT/'weapon-result.txt').exists() and time.monotonic()<deadline:
    time.sleep(1)
assert (CLIENT/'weapon-result.txt').exists(), 'Client result missing; inspect isolated launcher-output.log'
result=(CLIENT/'weapon-result.txt').read_text()
label='baseline' if '--baseline' in sys.argv else 'candidate'
shutil.copy2(CLIENT/'weapon-results.txt',HERE/(label+'-results.txt'))
print(label+': '+result.strip())
if label=='candidate':
    assert result.startswith('PASS'),result
    (HERE/'runtime-tested-sha256.txt').write_text(hashlib.sha256(tested.read_bytes()).hexdigest()+'\n')
else:assert result.startswith('FAIL'), 'Baseline unexpectedly passed'

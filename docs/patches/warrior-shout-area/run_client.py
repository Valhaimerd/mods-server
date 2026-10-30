from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,json
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/warrior-shout-area';CLIENT=W/('client-'+__import__('uuid').uuid4().hex[:8]);CLIENT.mkdir(exist_ok=True)
mods=CLIENT/'mods';mods.mkdir(exist_ok=True)
source=R/'tmp/class-follow-up/client'
for p in (source/'mods').glob('*.jar'):
    if 'probe' in p.name.lower():continue
    active=next((x for x in [(R/'mods'/p.name),(R/'mod store'/p.name)] if x.exists()),None)
    if active:shutil.copy2(active,mods/p.name)
for candidate in (W/'built').glob('*.jar'):shutil.copy2(candidate,mods/candidate.name)
for pattern in ['PaperDoll*','*PuzzlesLib*','arsenal-*']:
 for active in [*(R/'mods').glob(pattern+'.jar'),*(R/'mod store').glob(pattern+'.jar')]:shutil.copy2(active,mods/active.name)
if not (CLIENT/'saves/Punchy Flat').exists():shutil.copytree(source/'saves/Punchy Flat',CLIENT/'saves/Punchy Flat')
shutil.copy2(source/'launch.py',CLIENT/'launch.py');shutil.copy2(source/'options.txt',CLIENT/'options.txt')
lines=(R/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines();cp=';'.join(str(p) for p in mods.glob('*.jar'))+';'+lines[lines.index('"-cp"')+1].strip('"')
C=W/'client-probe-classes';C.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(C),str(H/'ClientShoutProbe.java')]
(W/'probe.args').write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args))
J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(J/'javac.exe'),'@'+str(W/'probe.args')],check=True)
with ZipFile(mods/'vanish-probe.jar','w',ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="vanish_render_probe"\nversion="1"\ndisplayName="Isolated Warrior Shout area preview"\n')
    for p in C.rglob('*.class'):z.write(p,p.relative_to(C).as_posix())
(CLIENT/'render-result.txt').unlink(missing_ok=True)
config=CLIENT/'config/paperdoll-client.toml'
if config.exists():config.write_text(config.read_text().replace('first_person_only = true','first_person_only = false').replace('display_time = 15','display_time = 0'))
(W/'last-client.txt').write_text(str(CLIENT));print('INSTANCE',CLIENT,flush=True)
subprocess.run(['python',str(CLIENT/'launch.py'),'--world'],check=True)

import time
result=CLIENT/'shout-client-result.txt';deadline=time.monotonic()+240
while not result.exists() and time.monotonic()<deadline:time.sleep(1)
assert result.exists(), 'No result: inspect '+str(CLIENT/'logs/latest.log')
print(result.read_text());shutil.copy2(result,H/'client-result.txt');shutil.copy2(CLIENT/'shout-captures.txt',H/'client-captures.txt')
assert result.read_text().startswith('PASS'),result.read_text()
(H/'client-tested.json').write_text(json.dumps({p.name:__import__('hashlib').sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name.lower()},indent=2)+'\n')

"""Isolated integrated client: actual casting, synchronized HUD and death/respawn."""
from pathlib import Path
import shutil,subprocess,zipfile,json,uuid
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/client-optimization';I=W/'client';M=I/'mods';M.mkdir(parents=True,exist_ok=True)
for n in ['spatial-client-result.txt','spatial-client-checks.txt']:(I/n).unlink(missing_ok=True)
for p in M.glob('*.jar'):p.unlink()
for folder in ['mods','mod store']:
 for p in (R/folder).glob('*.jar'):
  if not p.name.startswith('vectors-'):shutil.copy2(p,M/p.name)
for row in json.loads((H/'downloads.json').read_text()):
 p=W/row['file'];shutil.copy2(p,M/p.name)
world=I/'saves/Punchy Flat'
if world.exists():
 assert world.resolve().is_relative_to(W.resolve())
 archived=world.with_name('Prior-'+uuid.uuid4().hex);assert archived.resolve().is_relative_to(W.resolve());world.rename(archived)
if not world.exists():shutil.copytree(R/'tmp/paladin-doctrine/client/saves/Punchy Flat',world)
(I/'options.txt').write_text('pauseOnLostFocus:false\nguiScale:3\nrenderDistance:3\nsimulationDistance:5\nmaxFps:30\n')
C=W/'client-probe-classes';C.mkdir(exist_ok=True);cp=(W/'classpath.txt').read_text();args=['--release','25','-proc:none','-encoding','UTF-8','-cp',';'.join(str(p) for p in (W/'built').glob('*.jar'))+';'+cp,'-d',str(C),str(H/'OptimizationProbe.java')];a=W/'client-probe.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin');subprocess.run([str(J/'javac.exe'),'@'+str(a)],check=True)
with zipfile.ZipFile(M/'spatial-client-probe.jar','w') as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="spatial_client_probe"\nversion="1"\ndisplayName="Isolated Echoing client test"\n')
 for p in C.rglob('*.class'):z.write(p,p.relative_to(C).as_posix())
# The full pack has WoVer's interactive first-launch welcome; this fixture is unattended.
welcome=I/'config/wover/client.json'
welcome.parent.mkdir(parents=True,exist_ok=True)
settings=json.loads(welcome.read_text()) if welcome.exists() else {}
settings.setdefault('internal',{})['did_present_welcome_screen']=True
welcome.write_text(json.dumps(settings,indent=2)+'\n')
s=(R/'docs/patches/class-visual-retouch/client/launch-isolated-client.py').read_text();s=s.replace('Path(__file__).resolve().parents[4] / "tmp/class-visual-retouch/client"','Path(__file__).resolve().parent');s=s.replace('C:\\Users\\Valhaimerd\\.gradle\\jdks\\eclipse_adoptium-25-amd64-windows.2\\bin\\java.exe',str(J/'java.exe'));(I/'launch.py').write_text(s)
subprocess.run(['python',str(I/'launch.py')],check=True)

# A launch is not a test result. Wait for the probe's explicit completion record.
import time,hashlib
result=I/'spatial-client-result.txt';deadline=time.monotonic()+420
while not result.exists() and time.monotonic()<deadline:time.sleep(1)
if not result.exists():raise RuntimeError('Client probe did not finish; inspect '+str(I/'logs/latest.log'))
for name in ['spatial-client-result.txt','spatial-client-checks.txt']:shutil.copy2(I/name,H/name.replace('spatial-client','client'))
(H/'client-tested.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in M.glob('*.jar')},indent=2)+'\n')
print(result.read_text().strip(),sum(line.startswith('PASS ') for line in (I/'spatial-client-checks.txt').read_text().splitlines()),'client checks')
if not result.read_text().startswith('PASS'):raise SystemExit(1)

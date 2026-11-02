"""Isolated integrated client: actual casting, synchronized HUD and death/respawn."""
from pathlib import Path
import shutil,subprocess,zipfile,json
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/purgatory-continuous-pull';I=W/'client';M=I/'mods';M.mkdir(parents=True,exist_ok=True)
for p in M.glob('*.jar'):p.unlink()
for p in (R/'tmp/combat-phase-two/client/mods').glob('*.jar'):
 if 'probe' not in p.name:shutil.copy2(p,M/p.name)
for p in (W/'built').glob('*.jar'):shutil.copy2(p,M/p.name)
world=I/'saves/Punchy Flat'
if not world.exists():shutil.copytree(R/'tmp/paladin-doctrine/client/saves/Punchy Flat',world)
(I/'options.txt').write_text('pauseOnLostFocus:false\nguiScale:3\nrenderDistance:3\nsimulationDistance:5\nmaxFps:30\n')
C=W/'client-probe-classes';C.mkdir(exist_ok=True);cp=(W/'classpath.txt').read_text();args=['--release','25','-proc:none','-encoding','UTF-8','-cp',';'.join(str(p) for p in (W/'built').glob('*.jar'))+';'+cp,'-d',str(C),str(H/'PullClientProbe.java')];a=W/'client-probe.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(J/'javac.exe'),'@'+str(a)],check=True)
with zipfile.ZipFile(M/'pull-client-probe.jar','w') as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="pull_client_probe"\nversion="1"\ndisplayName="Isolated Echoing client test"\n')
 for p in C.rglob('*.class'):z.write(p,p.relative_to(C).as_posix())
s=(R/'docs/patches/class-visual-retouch/client/launch-isolated-client.py').read_text();s=s.replace('Path(__file__).resolve().parents[4] / "tmp/class-visual-retouch/client"','Path(__file__).resolve().parent');s=s.replace('C:\\Users\\Valhaimerd\\.gradle\\jdks\\eclipse_adoptium-25-amd64-windows.2\\bin\\java.exe',str(J/'java.exe'));(I/'launch.py').write_text(s)
subprocess.run(['python',str(I/'launch.py'),'--world'],check=True)

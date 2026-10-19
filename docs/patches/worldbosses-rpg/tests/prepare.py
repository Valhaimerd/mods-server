"""Prepare only the isolated server under tmp/worldbosses-rpg/server."""
from pathlib import Path
import subprocess,zipfile,shutil
ROOT=Path(__file__).resolve().parents[4]
w=ROOT/'tmp/worldbosses-rpg'
candidate=w/'world-bosses-neoforge-26.2-2.0.0.jar'
cp=str(candidate)+';'+w.joinpath('classpath.txt').read_text().strip()
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(w/'probe-classes'),str(Path(__file__).with_name('RaidProbe.java'))]
(w/'probe.args').write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in args),encoding='utf-8')
subprocess.run(['C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javac.exe','@'+str(w/'probe.args')],check=True)
server=w/'server'
(server/'mods').mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(server/'mods/raid-probe.jar','w') as z:
    z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="worldbosses_rpg_probe"\nversion="1.0"\n')
    for f in (w/'probe-classes').rglob('*.class'):z.write(f,f.relative_to(w/'probe-classes').as_posix())
shutil.copy2(candidate,server/'mods'/candidate.name)
shutil.copy2(ROOT/'mods/geckolib-neoforge-26.2-5.5.6.jar',server/'mods/geckolib-neoforge-26.2-5.5.6.jar')
(server/'eula.txt').write_text('eula=true\n')
(server/'server.properties').write_text('online-mode=false\nserver-port=25594\nmax-tick-time=-1\nview-distance=2\nsimulation-distance=2\ndifficulty=hard\nlevel-type=minecraft\\:flat\ngenerate-structures=false\nspawn-protection=0\n')
print('Prepared',server)

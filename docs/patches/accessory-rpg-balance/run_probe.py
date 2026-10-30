"""Build and run the isolated accessory regression probe; never touches personal instances."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import subprocess, shutil, argparse

ROOT=Path(__file__).resolve().parents[3]
HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/accessory-rpg-balance'
SERVER=WORK/'server'
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
N='Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar'
A='artifacts-neoforge-15.1.3+26.2.1.jar'
p=argparse.ArgumentParser();p.add_argument('--staged',action='store_true');p.add_argument('--start',action='store_true');p.add_argument('--integrations',action='store_true');args=p.parse_args()
WORK.mkdir(parents=True,exist_ok=True);(SERVER/'mods').mkdir(parents=True,exist_ok=True)
libs=SERVER/'libraries'
if not libs.exists():
 subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{libs}' -Target '{ROOT/'tmp/artifacts-balance/server/libraries'}' | Out-Null"],check=True)
(SERVER/'eula.txt').write_text('eula=true\n')
(SERVER/'server.properties').write_text('online-mode=false\nserver-port=25593\nlevel-type=minecraft:flat\ngenerate-structures=false\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=180000\n')
for name,folder in [(N,'nameless-balance'),(A,'artifacts-balance')]:
 source=ROOT/('tmp/'+folder if args.staged else 'mods')/name
 shutil.copy2(source,SERVER/'mods'/name)
shutil.copy2(ROOT/'mods/curios-neoforge-16.0.0+26.2.jar',SERVER/'mods/curios-neoforge-16.0.0+26.2.jar')
for name in ['better-party-neoforge-26.2-1.1.7.jar','spell_power-neoforge-1.6.1+26.2.jar','ranged_weapon_api-neoforge-4.0.0+26.2.jar']:
 if args.integrations:shutil.copy2(ROOT/'mods'/name,SERVER/'mods'/name)
 else:(SERVER/'mods'/name).unlink(missing_ok=True)
shutil.copy2(ROOT/'docs/patches/nameless-balance/balance.json',SERVER/'nameless-policy.json')
shutil.copy2(ROOT/'docs/patches/artifacts-balance/balance.json',SERVER/'artifacts-policy.json')
cp=';'.join([str(SERVER/'mods'/N),str(SERVER/'mods'/A),(ROOT/'tmp/artifacts-balance/classpath.txt').read_text().strip()])
classes=WORK/'probe-classes';classes.mkdir(exist_ok=True)
argv=['--release','25','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(HERE/'Probe.java')]
argfile=WORK/'probe.args';argfile.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in argv))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(SERVER/'mods/accessory-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="accessory_probe"\nversion="1"\ndisplayName="Isolated accessory regression probe"\n')
 for file in classes.rglob('*.class'):z.write(file,file.relative_to(classes).as_posix())
if args.start:
 (SERVER/'result.txt').unlink(missing_ok=True)
 with (WORK/'server.log').open('w') as log:
  process=subprocess.Popen([str(JAVA/'java.exe'),'-Xmx2G','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT)
 print('Started isolated server PID',process.pid,'result:',SERVER/'result.txt')

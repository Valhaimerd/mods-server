"""Run actual feeding/alchemy in an isolated NeoForge server. --baseline reproduces waste."""
from pathlib import Path
from zipfile import ZipFile
import argparse
import shutil
import subprocess

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
WORK=ROOT/'tmp/downed-backpack'
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
parser=argparse.ArgumentParser()
parser.add_argument('--baseline',action='store_true')
parser.add_argument('--no-revive',action='store_true')
parser.add_argument('--health',action='store_true')
parser.add_argument('--health-baseline',action='store_true')
args=parser.parse_args()
server=WORK/('no-revive-server' if args.no_revive else 'server')
mods=server/'mods';mods.mkdir(parents=True,exist_ok=True)
assert (server/'libraries').exists(),'Link libraries to the cached isolated NeoForge server first'
cp=(WORK/'classes').as_posix()+';'+(WORK/'classpath.txt').read_text()
compile_args='--release\n25\n-cp\n"'+cp+'"\n-d\n"'+(WORK/'probe-classes').as_posix()+'"\n"'+(HERE/'ConsumablesProbe.java').as_posix()+'"\n"'+(HERE/'HealthProbe.java').as_posix()+'"\n'
(WORK/'probe.args').write_text(compile_args)
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'probe.args')],check=True)
patterns=['sophisticatedcore-*.jar','sophisticatedbackpacks-*.jar']
if not args.no_revive:
    patterns+=['CreativeCore*.jar','PlayerRevive*.jar','Better Revive - 2.2*.jar','curios-*.jar','Better Revive X PlayerRevive*.jar']
for pattern in patterns:
    p=next((ROOT/'mods').glob(pattern))
    if p.name.startswith('sophisticatedcore'):p=(WORK/'before' if args.baseline else WORK)/p.name
    if args.health and (p.name.startswith('PlayerRevive_') or p.name.startswith('Better Revive - 2.2')):p=(WORK/'before' if args.health_baseline else WORK)/p.name
    shutil.copy2(p,mods/p.name)
with ZipFile(mods/'downed-probe.jar','w') as z:
    z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="downed_backpack_probe"\nversion="1"\ndisplayName="Downed backpack probe"\n')
    for p in (WORK/'probe-classes').rglob('*.class'):z.write(p,p.relative_to(WORK/'probe-classes').as_posix())
(server/'eula.txt').write_text('eula=true\n')
props=(ROOT/'tmp/nameless-balance/server/server.properties').read_text()
props='\n'.join('level-name=DownedFixture' if l.startswith('level-name=') else 'server-port=25595' if l.startswith('server-port=') else l for l in props.splitlines())+'\n'
(server/'server.properties').write_text(props)
(server/'consumables-result.txt').unlink(missing_ok=True)
options=['-Xmx2G','-Xverify:all']
if args.no_revive:options+=['-Ddowned.probe.absent=true']
if args.health:options+=['-Ddowned.probe.health=true']
log=WORK/('health-baseline.log' if args.health_baseline else 'health-runtime.log' if args.health else 'baseline.log' if args.baseline else 'no-revive.log' if args.no_revive else 'runtime.log')
with log.open('w',encoding='utf-8') as output:
    subprocess.run([str(JAVA/'java.exe'),*options,'@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=server,stdout=output,stderr=subprocess.STDOUT)
result=server/'consumables-result.txt'
text=result.read_text() if result.exists() else 'No result; inspect '+str(log)
print(text)
if not text.startswith('PASS '):raise SystemExit(1)

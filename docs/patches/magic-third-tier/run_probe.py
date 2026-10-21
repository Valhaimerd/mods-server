"""Prepare and run the isolated NeoForge fixture, using the existing server library cache."""
from pathlib import Path
import shutil
import subprocess
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / 'tmp/magic-third-tier'
SERVER = WORK / 'server'
JAVA = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
SERVER.mkdir(parents=True, exist_ok=True)
assert (SERVER / 'libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt').exists(), 'Link the isolated libraries directory to the cached NeoForge 26.2.0.90 server first.'
mods = SERVER / 'mods'; mods.mkdir(exist_ok=True)
for p in (ROOT / 'tmp/magic-revision-five/server/mods').glob('*.jar'):
    if p.name != 'overhaul-probe.jar': shutil.copy2(p, mods / p.name)
for name in ['better-party-neoforge-26.2-1.1.7.jar', 'better-party-tamed-mobs-neoforge-26.2-1.0.4.jar', 'pvp_flagging-26.2-1.1.2.jar']:
    shutil.copy2(ROOT / 'mods' / name, mods / name)
for p in (WORK / 'built').glob('*.jar'): shutil.copy2(p, mods / p.name)
for name in ['eula.txt', 'server.properties']:
    if not (SERVER / name).exists(): shutil.copy2(ROOT / 'tmp/magic-revision-five/server' / name, SERVER / name)
props = SERVER / 'server.properties'
props.write_text(props.read_text().replace('server-port=25593', 'server-port=25594'))
classes = WORK / 'probe-classes'; classes.mkdir(exist_ok=True)
base = (WORK / 'compile.args').read_text(encoding='utf-8')
cp = ';'.join(p.as_posix() for p in (WORK / 'built').glob('*.jar')) + ';' + base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + classes.as_posix() + '"\n'
args += '\n'.join('"' + p.as_posix() + '"' for p in HERE.glob('*Probe.java'))
(WORK / 'probe.args').write_text(args, encoding='utf-8')
subprocess.run([str(JAVA / 'javac.exe'), '@' + str(WORK / 'probe.args')], check=True)
with zipfile.ZipFile(ROOT / 'tmp/magic-revision-five/server/mods/overhaul-probe.jar') as z:
    files = {n: z.read(n) for n in z.namelist() if not n.endswith('.class')}
for p in classes.rglob('*.class'): files[p.relative_to(classes).as_posix()] = p.read_bytes()
with zipfile.ZipFile(mods / 'overhaul-probe.jar', 'w', zipfile.ZIP_DEFLATED) as z:
    for name, data in files.items(): z.writestr(name, data)
(SERVER / 'revision-result.txt').unlink(missing_ok=True)
with (WORK / 'runtime.log').open('w', encoding='utf-8') as log:
    process = subprocess.run([str(JAVA / 'java.exe'), '-Xmx3G', '@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt', 'nogui'], cwd=SERVER, stdout=log, stderr=subprocess.STDOUT)
result = SERVER / 'revision-result.txt'
print(result.read_text() if result.exists() else 'No probe result; inspect tmp/magic-third-tier/runtime.log')
if process.returncode or not result.exists() or not result.read_text().startswith('PASS '): raise SystemExit(1)

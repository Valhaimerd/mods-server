"""Run instrumented accessory operation-count tests in an ignored isolated server."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import argparse, subprocess, shutil

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'tmp/accessory-performance'
SERVER = WORK / 'server'
JAVA = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/java.exe')
N = 'Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar'
p = argparse.ArgumentParser()
p.add_argument('--staged', action='store_true')
p.add_argument('--label', choices=['before', 'after'], default='before')
args = p.parse_args()
SERVER.joinpath('mods').mkdir(parents=True, exist_ok=True)
if not (SERVER / 'libraries').exists():
    target = (ROOT / 'tmp/accessory-rpg-balance/server/libraries').resolve()
    subprocess.run(['powershell', '-NoProfile', '-Command', f"New-Item -ItemType Junction -Path '{SERVER / 'libraries'}' -Target '{target}' | Out-Null"], check=True)
(SERVER / 'eula.txt').write_text('eula=true\n')
(SERVER / 'server.properties').write_text('online-mode=false\nserver-port=25617\nlevel-type=minecraft:flat\ngenerate-structures=false\nview-distance=2\nsimulation-distance=2\nmax-tick-time=180000\n')
for name in ['artifacts-neoforge-15.1.3+26.2.1.jar', 'curios-neoforge-16.0.0+26.2.jar']:
    shutil.copy2(ROOT / 'mods' / name, SERVER / 'mods' / name)
development_cp = (ROOT / 'tmp/nameless-balance/classpath.txt').read_text(encoding='utf-8-sig').strip()
(WORK / 'classes').mkdir(exist_ok=True)
argv = ['--release', '25', '-proc:none', '-encoding', 'UTF-8', '-cp', development_cp, '-d', str(WORK / 'classes'), str(Path(__file__).with_name('HotspotProbe.java')), str(Path(__file__).with_name('CountQueries.java'))]
argfile = WORK / 'compile.args'
argfile.write_text('\n'.join('"' + a.replace('\\', '/') + '"' for a in argv), encoding='utf-8')
subprocess.run([str(JAVA.with_name('javac.exe')), '@' + str(argfile)], check=True)
source = ROOT / ('tmp/nameless-balance' if args.staged else 'mods') / N
entry = 'com/cozary/nameless_trinkets/items/trinkets/Tick.class'
with ZipFile(source) as archive:
    (WORK / 'Tick.original.class').write_bytes(archive.read(entry))
cp = str(WORK / 'classes') + ';' + development_cp
argv = ['-cp', cp, 'CountQueries', str(WORK / 'Tick.original.class'), str(WORK / 'Tick.counted.class')]
argfile = WORK / 'instrument.args'
argfile.write_text('\n'.join('"' + a.replace('\\', '/') + '"' for a in argv), encoding='utf-8')
subprocess.run([str(JAVA), '@' + str(argfile)], check=True)
with ZipFile(source) as archive, ZipFile(SERVER / 'mods' / N, 'w') as output:
    for info in archive.infolist():
        output.writestr(info, (WORK / 'Tick.counted.class').read_bytes() if info.filename == entry else archive.read(info.filename))
with ZipFile(SERVER / 'mods/hotspot-probe.jar', 'w', ZIP_DEFLATED) as archive:
    archive.writestr('META-INF/neoforge.mods.toml', 'modLoader="javafml"\nloaderVersion="[11,12)"\nlicense="MIT"\n[[mods]]\nmodId="accessory_hotspot_probe"\nversion="1"\ndisplayName="Isolated accessory hotspot probe"\n')
    for file in (WORK / 'classes/local/accessoryperf').rglob('*.class'):
        archive.write(file, file.relative_to(WORK / 'classes').as_posix())
(SERVER / 'hotspot-results.txt').unlink(missing_ok=True)
with (WORK / (args.label + '.log')).open('w', encoding='utf-8') as log:
    result = subprocess.run([str(JAVA), '-Xmx2G', '@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt', 'nogui'], cwd=SERVER, stdout=log, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
report = SERVER / 'hotspot-results.txt'
if not report.exists():
    raise RuntimeError(f'No probe result; inspect {args.label}.log, exit {result.returncode}')
shutil.copy2(report, WORK / (args.label + '-results.txt'))
lines = report.read_text().splitlines()
for line in lines:
    if line.startswith(('METRIC ', 'FAIL ', 'ERROR ')):
        print(line)
if result.returncode or any(line.startswith(('FAIL', 'ERROR')) for line in lines):
    raise SystemExit(1)
print('PASS:', sum(line.startswith('PASS ') for line in lines), 'checks')

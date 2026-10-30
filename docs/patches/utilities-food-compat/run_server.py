"""Run the existing Utilities suite plus native Primordial armor coexistence."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import subprocess, shutil, uuid, json, hashlib, sys

R = Path(__file__).resolve().parents[3]
H = Path(__file__).resolve().parent
W = R / 'tmp/utilities-food-compat'
baseline = '--baseline' in sys.argv
label = 'baseline' if baseline else 'candidate'
server = W / (label + '-' + uuid.uuid4().hex[:8])
mods = server / 'mods'
mods.mkdir(parents=True)
for p in (R / 'tmp/priest-tier-three/server/mods').glob('*.jar'):
    if 'probe' not in p.name and (R / 'mods' / p.name).exists():
        shutil.copy2(R / 'mods' / p.name, mods / p.name)
for pattern in ['orb_of_oblivion*.jar', 'adventurerskills*.jar', 'puffish*.jar', 'alexscaves*.jar', 'codxlib*.jar']:
    for p in (R / 'mods').glob(pattern):
        shutil.copy2(p, mods / p.name)
shutil.copy2(W / ('baseline.jar' if baseline else 'orb_of_oblivion_puffish_compat-1.0.2.jar'), mods / 'orb_of_oblivion_puffish_compat-1.0.2.jar')
subprocess.run(['cmd', '/c', 'mklink', '/J', str(server / 'libraries'), str(R / 'tmp/pvp-26.2/server/libraries')], check=True, capture_output=True)
(server / 'eula.txt').write_text('eula=true\n')
(server / 'server.properties').write_text('online-mode=false\nlevel-type=minecraft:flat\nserver-port=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=-1\n')
source = (H.parent / 'utilities-paths/Probe.java').read_text()
source = source.replace('   result="PASS "+checks.size()', (H / 'extra-checks.java.txt').read_text() + '\n   result="PASS "+checks.size()')
(server / 'Probe.java').write_text(source)
cp = ';'.join(p.as_posix() for p in mods.glob('*.jar')) + ';' + (R / 'tmp/utilities/classpath.txt').read_text().strip()
classes = server / 'probe-classes'
classes.mkdir()
args = ['--release', '25', '-proc:none', '-encoding', 'UTF-8', '-cp', cp, '-d', str(classes), str(server / 'Probe.java')]
a = server / 'probe.args'
a.write_text('\n'.join('"' + s.replace('\\', '/') + '"' for s in args))
java = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java / 'javac.exe'), '@' + str(a)], check=True)
with ZipFile(mods / 'utility-probe.jar', 'w', ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml', 'modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="utility_probe"\nversion="1"\ndisplayName="Utilities food coexistence regression"\n')
    for p in classes.rglob('*.class'):
        z.write(p, p.relative_to(classes).as_posix())
    # Fixture-only bridge: AC reads forge:raw_meats but ships c:raw_meats.
    z.writestr('data/forge/tags/item/raw_meats.json', '{"values":["minecraft:beef"]}')
(W / ('last-' + label + '.txt')).write_text(str(server))
print('INSTANCE', server, flush=True)
with (server / 'console.log').open('w', encoding='utf-8') as log:
    result = subprocess.run([str(java / 'java.exe'), '-Xmx3G', '-Xverify:all', '@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt', 'nogui'], cwd=server, stdout=log, stderr=subprocess.STDOUT, timeout=180)
if baseline:
    log = (server / 'console.log').read_text(encoding='utf-8', errors='replace')
    assert 'ac_eat' in log and 'Redirect conflict' in log and not (server / 'utilities-result.txt').exists()
    (H / 'baseline-result.txt').write_text('REPRODUCED: Alex\'s Caves ac_eat Redirect conflict prevents bootstrap.\n')
else:
    result = (server / 'utilities-result.txt').read_text()
    print(result)
    assert result.startswith('PASS'), result
    shutil.copy2(server / 'utilities-result.txt', H / 'runtime-result.txt')
    shutil.copy2(server / 'utilities-checks.txt', H / 'runtime-checks.txt')
(H / (label + '-mods.json')).write_text(json.dumps({p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name}, indent=2) + '\n')

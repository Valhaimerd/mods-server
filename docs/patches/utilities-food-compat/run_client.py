"""Bootstrap the complete distributed client pack in a disposable game directory."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil, subprocess, uuid, hashlib, json, time

R = Path(__file__).resolve().parents[3]
H = Path(__file__).resolve().parent
W = R / 'tmp/utilities-food-compat'
client = W / ('client-' + uuid.uuid4().hex[:8])
mods = client / 'mods'
mods.mkdir(parents=True)
for folder in ['mods', 'mod store']:
    for p in (R / folder).glob('*.jar'):
        assert not (mods / p.name).exists()
        shutil.copy2(p, mods / p.name)
shutil.copy2(W / 'orb_of_oblivion_puffish_compat-1.0.2.jar', mods / 'orb_of_oblivion_puffish_compat-1.0.2.jar')
cp = ';'.join(p.as_posix() for p in mods.glob('*.jar')) + ';' + (R / 'tmp/utilities/classpath.txt').read_text().strip()
classes = client / 'probe-classes'
classes.mkdir()
args = ['--release', '25', '-proc:none', '-encoding', 'UTF-8', '-cp', cp, '-d', str(classes), str(H / 'ClientProbe.java')]
a = client / 'compile.args'
a.write_text('\n'.join('"' + s.replace('\\', '/') + '"' for s in args))
java = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java / 'javac.exe'), '@' + str(a)], check=True)
with ZipFile(mods / 'utilities-food-client-probe.jar', 'w', ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml', 'modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="utilities_food_client_probe"\nversion="1"\ndisplayName="Food coexistence client bootstrap check"\n')
    for p in classes.rglob('*.class'):
        z.write(p, p.relative_to(classes).as_posix())
shutil.copy2(R / 'tmp/utilities/client-36a729a2/launch.py', client / 'launch.py')
(W / 'last-client.txt').write_text(str(client))
print('INSTANCE', client, flush=True)
subprocess.run(['python', str(client / 'launch.py')], check=True)
result = client / 'food-client-result.txt'
deadline = time.monotonic() + 300
while not result.exists() and time.monotonic() < deadline:
    time.sleep(1)
assert result.exists(), 'No client success marker; inspect ' + str(client)
print(result.read_text())
shutil.copy2(result, H / 'client-result.txt')
(H / 'client-mods.json').write_text(json.dumps({p.name: hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name}, indent=2) + '\n')

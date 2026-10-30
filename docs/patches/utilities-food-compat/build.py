"""Overlay only FoodMixin on the captured current compatibility JAR."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib, json, subprocess

H = Path(__file__).resolve().parent
R = H.parents[2]
W = R / 'tmp/utilities-food-compat'
W.mkdir(parents=True, exist_ok=True)
base = W / 'baseline.jar'
if not base.exists():
    base.write_bytes((R / 'mods/orb_of_oblivion_puffish_compat-1.0.2.jar').read_bytes())
classes = W / 'classes'
classes.mkdir(exist_ok=True)
cp = str(base) + ';' + (R / 'tmp/utilities/classpath.txt').read_text().strip()
args = ['--release', '25', '-proc:none', '-encoding', 'UTF-8', '-cp', cp,
        '-d', str(classes), str(H.parent / 'utilities-paths/src/FoodMixin.java')]
argfile = W / 'compile.args'
argfile.write_text('\n'.join('"' + a.replace('\\', '/') + '"' for a in args))
java = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java / 'javac.exe'), '@' + str(argfile)], check=True)
entry = 'net/valhaimerd/orboblivioncompat/mixin/FoodMixin.class'
with ZipFile(base) as z:
    old = {n: z.read(n) for n in z.namelist()}
new = dict(old)
new[entry] = (classes / entry).read_bytes()
out = W / 'orb_of_oblivion_puffish_compat-1.0.2.jar'
with ZipFile(out, 'w', ZIP_DEFLATED) as z:
    for n, data in new.items():
        z.writestr(n, data)
with ZipFile(out) as z:
    assert z.testzip() is None and len(z.namelist()) == len(set(z.namelist()))
assert [n for n in old if old[n] != new[n]] == [entry]
record = {'jar': out.name, 'before_sha256': hashlib.sha256(base.read_bytes()).hexdigest(),
          'sha256': hashlib.sha256(out.read_bytes()).hexdigest(), 'changed': [entry],
          'preserved_entries': len(old) - 1}
(H / 'artifacts.json').write_text(json.dumps(record, indent=2) + '\n')
print(json.dumps(record, indent=2))

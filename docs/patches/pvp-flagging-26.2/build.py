"""Compile the pack's damage-only replacement; retain archive identity and notices."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib, subprocess, json

ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).resolve().parent
WORK = ROOT / 'tmp/pvp-26.2'
WORK.mkdir(exist_ok=True)
original = WORK / 'upstream.jar'
assert hashlib.sha256(original.read_bytes()).hexdigest() == 'dbc1313dd4328c0ebc39702f7d940ca2bb78b1d27f5f8219cfb3f7a1fc97b64e', 'Unexpected upstream archive'
classes = WORK / 'retention-classes'
classes.mkdir(exist_ok=True)
sources = sorted((HERE / 'src').rglob('*.java'))
assert len(sources) == 2
cp = (ROOT / 'tmp/artifacts-balance/classpath.txt').read_text().strip()
args = ['--release', '25', '-encoding', 'UTF-8', '-cp', cp, '-d', str(classes), *map(str, sources)]
argfile = WORK / 'retention-compile.args'
argfile.write_text('\n'.join('"' + a.replace('\\', '/') + '"' for a in args))
java = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java / 'javac.exe'), '@' + str(argfile)], check=True)
with ZipFile(original) as archive:
    files = {n: archive.read(n) for n in archive.namelist()
             if n.startswith('META-INF/') and not n.endswith('/')}
    files['pvp-flagging.png'] = archive.read('pvp-flagging.png')
meta = files['META-INF/neoforge.mods.toml'].decode()
meta = meta.replace('26.1.2-1.1.2', '26.2-1.1.2').replace('versionRange="[26.1.2]"', 'versionRange="[26.2,26.3)"').replace('versionRange="[26.1.2,)"', 'versionRange="[26.2.0.90,)"')
meta = meta.replace('[[mixins]]\nconfig="mixins.pvp_flagging.json"', '')
meta = meta.replace('Allows players to flag/unflag themselves for PVP and server operators to set up pvp zones', 'Pack replacement: retains 50% of player-to-player damage; party mods control friendly fire. No flagging, cooldowns, zones or indicators.')
files['META-INF/neoforge.mods.toml'] = meta.encode()
replacements = {p.relative_to(classes).as_posix(): p.read_bytes() for p in classes.rglob('*.class')}
assert len(replacements) == 2, replacements.keys()
files.update(replacements)
output = WORK / 'pvp_flagging-26.2-1.1.2.jar'
with ZipFile(output, 'w', ZIP_DEFLATED) as archive:
    for name, data in files.items():
        archive.writestr(name, data)
report = {'upstream_sha256': hashlib.sha256(original.read_bytes()).hexdigest(),
          'output_sha256': hashlib.sha256(output.read_bytes()).hexdigest(),
          'pvp_damage_multiplier': 0.5, 'mode': 'damage-only', 'production_classes': sorted(replacements)}
(WORK / 'build-result.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report, indent=2))

"""Repack the current Sophisticated Core with downed-consumption guards."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import subprocess

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / 'tmp/downed-backpack'
CLASSES = WORK / 'classes'
CLASSES.mkdir(parents=True, exist_ok=True)
JAVA = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
base = (ROOT / 'tmp/magic-third-tier/compile.args').read_text()
cp = base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
cp += ';' + ';'.join(p.as_posix() for p in (ROOT / 'mods').glob('*.jar'))
(WORK / 'classpath.txt').write_text(cp)
args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + CLASSES.as_posix() + '"\n'
args += '\n'.join('"' + p.as_posix() + '"' for p in HERE.glob('*.java') if not p.stem.endswith('Probe'))
(WORK / 'compile.args').write_text(args)
subprocess.run([str(JAVA / 'javac.exe'), '@' + str(WORK / 'compile.args')], check=True)
name = 'sophisticatedcore-26.2-1.5.0.2337.jar'
source = WORK / 'before' / name
assert hashlib.sha256(source.read_bytes()).hexdigest() == '3479a05f86fc65064c77099ec93e67c6f8e31953b1ef506ce5f83eec556c71a6'
targets = ['net/p3pp3rf1y/sophisticatedcore/upgrades/' + n + '/' + cls + '.class'
           for n, cls in [('feeding', 'FeedingUpgradeWrapper'), ('alchemy', 'AlchemyUpgradeWrapper')]]
with ZipFile(source) as z:
    for target in targets:
        path = WORK / 'patched' / target
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(z.read(target))
        toolargs = WORK / 'patch.args'
        toolargs.write_text('-cp\n"' + CLASSES.as_posix() + ';' + cp + '"\nPatchConsumables\n"' + path.as_posix() + '"\n')
        subprocess.run([str(JAVA / 'java.exe'), '@' + str(toolargs)], check=True)
with ZipFile(source) as src, ZipFile(WORK / name, 'w') as dst:
    for entry in src.infolist():
        path = WORK / 'patched' / entry.filename
        dst.writestr(entry, path.read_bytes() if entry.filename in targets else src.read(entry.filename))
    helper = 'net/p3pp3rf1y/sophisticatedcore/compat/DownedConsumables.class'
    dst.writestr(helper, (CLASSES / helper).read_bytes())
report = {'jar': name, 'before_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
          'after_sha256': hashlib.sha256((WORK / name).read_bytes()).hexdigest(),
          'changed_entries': targets, 'added_entries': [helper]}
(WORK / 'artifacts.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report, indent=2))

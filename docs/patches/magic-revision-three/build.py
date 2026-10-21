"""Rebuild from saved current inputs; never automatically installs candidates."""
from pathlib import Path
import copy
import hashlib
import json
import subprocess
import sys
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / 'tmp/magic-revision-three'
SOURCE = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else ROOT / 'mods'
OUT = WORK / 'built'
JAVA = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
OUT.mkdir(parents=True, exist_ok=True)
subprocess.run([sys.executable, str(HERE / 'repack.py'), str(SOURCE), str(OUT)], check=True)
classes = WORK / 'classes'; classes.mkdir(exist_ok=True)
args = (ROOT / 'tmp/magic-balance-audit/compile.args').read_text()
cp = args.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
cp = (SOURCE / 'spell_engine-neoforge-1.10.5+26.2.jar').as_posix() + ';' + cp
compile_args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + classes.as_posix() + '"\n'
compile_args += '\n'.join('"' + (HERE / name).as_posix() + '"' for name in ['RevisionRules.java', 'PatchRevision.java'])
(WORK / 'compile.args').write_text(compile_args)
subprocess.run([str(JAVA / 'javac.exe'), '@' + str(WORK / 'compile.args')], check=True)
asm = next((ROOT / 'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))

patches = {
    'spell_engine-neoforge-1.10.5+26.2.jar': {
        'net/spell_engine/internals/cost/SpellCooldowns.class': 'cooldown',
        'net/spell_engine/internals/impact/SpellImpacts.class': 'impact',
        'net/spell_engine/internals/SpellParameters.class': 'duration',
        'net/spell_engine/internals/casting/SpellCaster$Entity.class': 'beam',
    },
    'skill_tree-neoforge-1.6.1+26.2.jar': {'net/skill_tree_rpgs/effect/SkillEffects.class': 'exposure'},
}
for name, entries in patches.items():
    source = SOURCE / name if name.startswith('spell_engine') else OUT / name
    with zipfile.ZipFile(source) as z:
        infos = z.infolist(); files = {i.filename: z.read(i) for i in infos}
    for path, mode in entries.items():
        original = WORK / (mode + '.class'); patched = WORK / (mode + '.patched')
        original.write_bytes(files[path])
        subprocess.run([str(JAVA / 'java.exe'), '-cp', str(classes) + ';' + str(asm), 'PatchRevision', str(original), str(patched), mode], check=True)
        files[path] = patched.read_bytes()
    if name.startswith('spell_engine'):
        for p in (classes / 'net').rglob('*.class'): files[p.relative_to(classes).as_posix()] = p.read_bytes()
    with zipfile.ZipFile(OUT / name, 'w') as z:
        for info in infos: z.writestr(copy.copy(info), files.pop(info.filename))
        for path, data in files.items(): z.writestr(path, data)
    print('Patched candidate', OUT / name)

records = []
for p in sorted(OUT.glob('*.jar')):
    with zipfile.ZipFile(SOURCE / p.name) as before, zipfile.ZipFile(p) as after:
        assert after.testzip() is None
        assert set(before.namelist()) <= set(after.namelist())
        changed = [n for n in before.namelist() if before.read(n) != after.read(n)]
        added = sorted(set(after.namelist()) - set(before.namelist()))
    records.append({'jar': p.name, 'before_sha256': hashlib.sha256((SOURCE / p.name).read_bytes()).hexdigest(), 'after_sha256': hashlib.sha256(p.read_bytes()).hexdigest(), 'changed_entries': changed, 'added_entries': added})
(WORK / 'artifacts.json').write_text(json.dumps(records, indent=2) + '\n')

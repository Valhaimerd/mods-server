"""Repack saved revision-five archives, retaining earlier patches and current filenames."""
from pathlib import Path
import copy
import hashlib
import json
import subprocess
import sys
import zipfile
import third_tier_data
import summons_data
import walls_data

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / 'tmp/magic-third-tier'
SOURCE = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else WORK / 'before'
OUT = WORK / 'built'; OUT.mkdir(parents=True, exist_ok=True)
CLASSES = WORK / 'classes'; CLASSES.mkdir(exist_ok=True)
JAVA = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
base = (ROOT / 'tmp/magic-balance-audit/compile.args').read_text(encoding='utf-8')
cp = ';'.join(p.as_posix() for p in SOURCE.glob('*.jar')) + ';' + ';'.join(p.as_posix() for p in (ROOT / 'mods').glob('*.jar'))
cp += ';' + base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
sources = [p for p in HERE.glob('*.java') if not p.stem.endswith('Probe')]
args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + CLASSES.as_posix() + '"\n'
args += '\n'.join('"' + p.as_posix() + '"' for p in sources)
(WORK / 'compile.args').write_text(args, encoding='utf-8')
subprocess.run([str(JAVA / 'javac.exe'), '@' + str(WORK / 'compile.args')], check=True)
asm = next((ROOT / 'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))
tool_cp = str(CLASSES) + ';' + str(asm)

def patch(files, path, tool, mode=None):
    original = WORK / 'patch-input.class'; patched = WORK / 'patch-output.class'
    original.write_bytes(files[path])
    command = [str(JAVA / 'java.exe'), '-cp', tool_cp, tool]
    if tool == 'PatchSummons': command += [mode, str(original), str(patched)]
    else:
        command += [str(original), str(patched)]
        if mode: command += [mode]
    subprocess.run(command, check=True)
    files[path] = patched.read_bytes()

records = []
for name in ['wizards-neoforge-3.1.2+26.2.jar', 'skill_tree-neoforge-1.6.1+26.2.jar', 'spell_engine-neoforge-1.10.5+26.2.jar']:
    with zipfile.ZipFile(SOURCE / name) as z:
        infos = z.infolist(); files = {i.filename: z.read(i) for i in infos}; original = dict(files)
    for transform in [third_tier_data.transform, summons_data.transform, walls_data.transform]: transform(name, files)
    if name.startswith('spell_engine'):
        config = json.loads(files['spell_engine.mixins.json'])
        config['mixins'].append('thirdtier.FrozenEntityMoveMixin')
        files['spell_engine.mixins.json'] = (json.dumps(config, indent=2) + '\n').encode()
        party = WORK / 'party-classes'
        subprocess.run([str(JAVA / 'java.exe'), '-cp', tool_cp, 'PatchParty', str(SOURCE / name), str(party)], check=True)
        for p in party.rglob('*.class'): files[p.relative_to(party).as_posix()] = p.read_bytes()
        for path, mode in [('entity/SummonedEntity', 'entity'), ('internals/impact/SpellImpacts', 'impacts'), ('internals/delivery/LaunchGeometry', 'launch'), ('internals/casting/MobCastController', 'controller')]:
            patch(files, 'net/spell_engine/' + path + '.class', 'PatchSummons', mode)
        for path in ['internals/cost/SpellCooldowns', 'internals/cost/SpellCooldownManager', 'entity/SpellCloud', 'internals/impact/SpellImpacts']:
            patch(files, 'net/spell_engine/' + path + '.class', 'PatchWalls')
        patch(files, 'net/spell_engine/internals/cost/SpellCooldowns.class', 'PatchThirdTier', 'cooldown')
        for p in (CLASSES / 'net').rglob('*.class'): files[p.relative_to(CLASSES).as_posix()] = p.read_bytes()
    elif name.startswith('wizards'):
        patch(files, 'net/wizards/entity/FrostElementalEntity.class', 'PatchSummons', 'frost')
        patch(files, 'net/wizards/client/entity/FireHydraModel.class', 'PatchSummons', 'model')
        patch(files, 'net/wizards/effect/WizardsEffects.class', 'PatchThirdTier', 'effects')
    with zipfile.ZipFile(OUT / name, 'w') as z:
        for info in infos: z.writestr(copy.copy(info), files.pop(info.filename))
        for path, data in sorted(files.items()):
            info = zipfile.ZipInfo(path, (2026, 10, 21, 0, 0, 0)); info.compress_type = zipfile.ZIP_DEFLATED
            z.writestr(info, data)
    with zipfile.ZipFile(OUT / name) as z:
        current = {n: z.read(n) for n in z.namelist()}; assert z.testzip() is None
        for path, data in current.items():
            if path.endswith('.json'): json.loads(data)
    records.append({'jar': name, 'before_sha256': hashlib.sha256((SOURCE / name).read_bytes()).hexdigest(), 'after_sha256': hashlib.sha256((OUT / name).read_bytes()).hexdigest(), 'changed_entries': [n for n in original if original[n] != current[n]], 'added_entries': sorted(set(current) - set(original))})
    print('Built', OUT / name)
(WORK / 'artifacts.json').write_text(json.dumps(records, indent=2) + '\n', encoding='utf-8')

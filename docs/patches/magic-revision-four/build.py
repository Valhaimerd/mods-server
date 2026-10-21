"""Apply to saved revision-three archives, preserving prior code/resource changes."""
from pathlib import Path
import copy
import json
import hashlib
import subprocess
import sys
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / 'tmp/magic-revision-four'
SOURCE = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else ROOT / 'mods'
OUT = WORK / 'built'; OUT.mkdir(parents=True, exist_ok=True)
CLASSES = WORK / 'classes'; CLASSES.mkdir(exist_ok=True)
JAVA = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
base = (ROOT / 'tmp/magic-balance-audit/compile.args').read_text()
cp = ';'.join(p.as_posix() for p in SOURCE.glob('*.jar')) + ';' + base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + CLASSES.as_posix() + '"\n'
args += '\n'.join('"' + p.as_posix() + '"' for p in [HERE / 'ShieldRework.java', HERE / 'PatchShield.java', HERE / 'RevisionRules.java'])
(WORK / 'compile.args').write_text(args)
subprocess.run([str(JAVA / 'javac.exe'), '@' + str(WORK / 'compile.args')], check=True)
asm = next((ROOT / 'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))
records = []
for name in ['wizards-neoforge-3.1.2+26.2.jar', 'skill_tree-neoforge-1.6.1+26.2.jar', 'spell_engine-neoforge-1.10.5+26.2.jar']:
    with zipfile.ZipFile(SOURCE / name) as z:
        infos = z.infolist(); files = {i.filename: z.read(i) for i in infos}; original_files = dict(files)
    if name.startswith('wizards'):
        def edit(id, change):
            path = f'data/wizards/spell/{id}.json'; d = json.loads(files[path]); change(d)
            files[path] = (json.dumps(d, indent=2) + '\n').encode()
        def missile(d):
            d['range'] = 32.0
            d['impacts'][0]['action']['damage']['spell_power_coefficient'] = .84
        edit('arcane_missile', missile)
        edit('arcane_explosion', lambda d: d['active']['cast'].update(duration=2.0))
        edit('fire_breath', lambda d: d['cost']['cooldown'].update(duration=8.0, proportional=True))
        edit('arcane_barrage', lambda d: d['impacts'][0]['action']['summon']['behaviour']['lifespan'].update(active_seconds=6))
        nova = json.loads(files['data/wizards/spell/frost_nova.json'])
        burst = {'school': 'spell_power:frost', 'range': 5.0, 'tier': 0, 'type': 'PASSIVE', 'target': {'type': 'AREA', 'area': {}}, 'deliver': {'type': 'DIRECT'},
                 'release': copy.deepcopy(nova['release']), 'impacts': [{'action': {'type': 'STATUS_EFFECT', 'status_effect': {'effect_id': 'wizards:frozen', 'duration': 2.0, 'amplifier': 1, 'amplifier_cap': 9, 'apply_mode': 'ADD'}}}], 'cost': {'durability': 0, 'cooldown': {'duration': 0}}}
        for particle in burst['release'].get('visuals', {}).get('particles', []):
            if 'scale' in particle.get('appearance', {}): particle['appearance']['scale'] = 5.0
        files['data/wizards/spell/frost_shield_burst.json'] = (json.dumps(burst, indent=2) + '\n').encode()
        path = 'net/wizards/effect/WizardsEffects.class'; original = WORK / 'effects.class'; patched = WORK / 'effects.patched'
        original.write_bytes(files[path])
        subprocess.run([str(JAVA / 'java.exe'), '-cp', str(CLASSES) + ';' + str(asm), 'PatchShield', str(original), str(patched)], check=True)
        files[path] = patched.read_bytes()
        files['net/wizards/effect/ShieldRework.class'] = (CLASSES / 'net/wizards/effect/ShieldRework.class').read_bytes()
        path = 'assets/wizards/lang/en_us.json'; lang = json.loads(files[path])
        lang['spell.wizards.arcane_missile.description'] = 'Channel 12 homing missiles over 4 sec, each dealing 28%% Arcane damage before modifiers. Range 32 blocks; cooldown 8-16 sec by channel completion, before haste.'
        lang['spell.wizards.frost_shield.description'] = 'Become immune to incoming damage for {effect_duration} sec with reduced movement. On natural expiry, release a non-damaging 5-block burst that freezes enemies for 2 sec.'
        lang['spell.wizards.frost_shield_burst.name'] = 'Frost Shield Expiry Burst'
        lang['spell.wizards.frost_shield_burst.description'] = 'Freezes enemies within 5 blocks for 2 sec when Frost Shield naturally expires. Deals no damage.'
        files[path] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()
    elif name.startswith('spell_engine'):
        files['net/spell_engine/internals/RevisionRules.class'] = (CLASSES / 'net/spell_engine/internals/RevisionRules.class').read_bytes()
    else:
        path = 'assets/skill_tree_rpgs/lang/en_us.json'; lang = json.loads(files[path])
        lang['spell.skill_tree_rpgs.arcane_tier_3_spell_1_modifier_2.description'] = 'Channel for 10 sec over 50 deliveries. Damage starts at 10%%, increasing by 0.15 percentage points per delivery to 15%%. Beam width grows from 1.3x by 0.04x per delivery to 3x.'
        lang['spell.skill_tree_rpgs.arcane_tier_2_spell_2_modifier_1.description'] = 'Arcane Explosion hits always reset its cooldown, but Explosion damage becomes 180%%. No proc cooldown.'
        files[path] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()
    changed = [n for n, data in original_files.items() if files[n] != data]
    added = sorted(set(files) - set(original_files))
    with zipfile.ZipFile(OUT / name, 'w') as z:
        for info in infos: z.writestr(copy.copy(info), files.pop(info.filename))
        for path, data in files.items(): z.writestr(path, data)
    records.append({'jar': name, 'before_sha256': hashlib.sha256((SOURCE / name).read_bytes()).hexdigest(), 'after_sha256': hashlib.sha256((OUT / name).read_bytes()).hexdigest(), 'changed_entries': changed, 'added_entries': added})
    print('Built', OUT / name)
(WORK / 'artifacts.json').write_text(json.dumps(records, indent=2) + '\n')

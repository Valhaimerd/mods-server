"""Layer the requested balance values over saved revision-four pack JARs."""
from pathlib import Path
import copy
import hashlib
import json
import subprocess
import sys
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / 'tmp/magic-revision-five'
SOURCE = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else WORK / 'before'
OUT = WORK / 'built'; OUT.mkdir(parents=True, exist_ok=True)
CLASSES = WORK / 'classes'; CLASSES.mkdir(exist_ok=True)
JAVA = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
rules = (HERE.parent / 'magic-revision-four/RevisionRules.java').read_text()
assert rules.count('spell_power_coefficient = 1.8f;') == 1
(WORK / 'RevisionRules.java').write_text(rules.replace('spell_power_coefficient = 1.8f;', 'spell_power_coefficient = 1.6f;'))
base = (ROOT / 'tmp/magic-balance-audit/compile.args').read_text()
cp = ';'.join(p.as_posix() for p in SOURCE.glob('*.jar')) + ';' + base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + CLASSES.as_posix() + '"\n'
args += '\n'.join('"' + p.as_posix() + '"' for p in [WORK / 'RevisionRules.java', HERE.parent / 'skill-overhaul/SkillOverhaul.java'])
(WORK / 'compile.args').write_text(args)
subprocess.run([str(JAVA / 'javac.exe'), '@' + str(WORK / 'compile.args')], check=True)
names = ['wizards-neoforge-3.1.2+26.2.jar', 'skill_tree-neoforge-1.6.1+26.2.jar', 'spell_engine-neoforge-1.10.5+26.2.jar', 'adventurerskills-EN-1.3-26.2-neoforge-fixed.jar', 'orb_of_oblivion_puffish_compat-1.0.2.jar']
records = []
for name in names:
    with zipfile.ZipFile(SOURCE / name) as z:
        infos = z.infolist(); files = {i.filename: z.read(i) for i in infos}; original = dict(files)
    def edit(path, change):
        d = json.loads(files[path]); change(d)
        files[path] = (json.dumps(d, indent=2, ensure_ascii=False) + '\n').encode()
    if name.startswith('wizards'):
        for id, coefficient in [('arcane_missile', .78), ('arcane_explosion', 2.6), ('fire_breath', .65), ('fire_slash', 1.0)]:
            edit(f'data/wizards/spell/{id}.json', lambda d, c=coefficient: d['impacts'][0]['action']['damage'].update(spell_power_coefficient=c))
        for id, cooldown in [('frost_nova', 12), ('fire_meteor', 20)]:
            edit(f'data/wizards/spell/{id}.json', lambda d, c=cooldown: d['cost']['cooldown'].update(duration=c))
        # Barrage shares Arcane Bolt with wands: give emitters their own copy.
        bolt = json.loads(files['data/wizards/spell/arcane_bolt.json'])
        bolt['impacts'][0]['action']['damage']['spell_power_coefficient'] = .4
        files['data/wizards/spell/arcane_barrage_bolt.json'] = (json.dumps(bolt, indent=2) + '\n').encode()
        edit('data/wizards/spell/arcane_barrage.json', lambda d: d['impacts'][0]['action']['summon']['behaviour']['actions'][0]['spell_cast'].update(spell_id='wizards:arcane_barrage_bolt'))
        def lang(d):
            d['spell.wizards.arcane_missile.description'] = 'Channel 12 homing missiles over 4 sec, each dealing 26%% Arcane damage before modifiers. Range 32 blocks; cooldown 8-16 sec by channel completion, before haste.'
            d['spell.wizards.arcane_explosion.description'] = 'Create an explosion centered on the targeted enemy, dealing {damage} Arcane damage within {impact_range} blocks.'
            d['spell.wizards.fire_breath.description'] = 'Channel flame in front of you, dealing 13%% Fire damage per delivery without distance falloff. Cooldown 4-8 sec by channel completion, before haste.'
            d['spell.wizards.fire_slash.description'] = 'Launch a wide wave of flame with a 0.1 sec cast, dealing {damage} Fire damage to enemies in its path.'
            d['spell.wizards.arcane_barrage.description'] += ' Each bolt deals 40%% of its emitter\'s Arcane damage.'
            d['spell.wizards.arcane_barrage_bolt.name'] = 'Arcane Emitter Bolt'
            d['spell.wizards.arcane_barrage_bolt.description'] = 'Emitter projectile dealing 40%% of emitter Arcane damage. Ordinary wand Arcane Bolt is unchanged.'
            d['spell.wizards.frost_shield.description'] = 'Block every incoming damage hit for {effect_duration} sec with reduced movement; hits do not consume the shield. Natural expiry releases a non-damaging 5-block burst that freezes enemies for 2 sec.'
        edit('assets/wizards/lang/en_us.json', lang)
    elif name.startswith('spell_engine'):
        files['net/spell_engine/internals/RevisionRules.class'] = (CLASSES / 'net/spell_engine/internals/RevisionRules.class').read_bytes()
    elif name.startswith('skill_tree'):
        edit('assets/skill_tree_rpgs/lang/en_us.json', lambda d: d.update({'spell.skill_tree_rpgs.arcane_tier_2_spell_2_modifier_1.description': 'Arcane Explosion hits always reset its cooldown, but Explosion damage becomes 160%%. No proc cooldown.'}))
    elif name.startswith('adventurer'):
        edit('data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json', lambda d: d['magistrate']['description'].update(text=d['magistrate']['description']['text'].replace('\n+10% Spell Critical Chance', '')))
    else:
        files['net/valhaimerd/orboblivioncompat/SkillOverhaul.class'] = (CLASSES / 'net/valhaimerd/orboblivioncompat/SkillOverhaul.class').read_bytes()
    with zipfile.ZipFile(OUT / name, 'w') as z:
        for info in infos: z.writestr(copy.copy(info), files.pop(info.filename))
        for path, data in files.items(): z.writestr(path, data)
    with zipfile.ZipFile(OUT / name) as z:
        current = {n: z.read(n) for n in z.namelist()}
        assert z.testzip() is None
    records.append({'jar': name, 'before_sha256': hashlib.sha256((SOURCE / name).read_bytes()).hexdigest(), 'after_sha256': hashlib.sha256((OUT / name).read_bytes()).hexdigest(), 'changed_entries': [n for n in original if original[n] != current[n]], 'added_entries': sorted(set(current) - set(original))})
    print('Built', OUT / name)
(WORK / 'artifacts.json').write_text(json.dumps(records, indent=2) + '\n')

"""Use percentages in RPG node/spell tooltips; retain formulas only in developer notes.

Reads the current pack, writes candidates to tmp/simple-skill-descriptions.
Only English descriptions and the Barrage bolt's requested 40% coefficient change.
"""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import re

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / 'tmp/simple-skill-descriptions'
OUT.mkdir(parents=True, exist_ok=True)
WARDS = {
    'arcane_tier_3_passive_2': 'Casting an Arcane spell grants an absorption shield for 8 sec: 1 heart, plus 1 heart for every 5 Arcane power. Cooldown: 16 sec.',
    'fire_tier_3_passive_2': 'Casting a Fire spell grants an absorption shield for 8 sec: 1 heart, plus 1 heart for every 10 Fire power. Attackers take 30%% Fire damage. Cooldown: 16 sec.',
    'frost_tier_3_passive_2': 'Casting a Frost spell grants an absorption shield for 8 sec: 1 heart, plus 1 heart for every 10 Frost power. Slows attackers. Cooldown: 16 sec.',
}
OVERRIDES = {
    **WARDS,
    'arcane_tier_3_spell_1_modifier_2': 'Channel for 10 sec. Each hit deals 10%% Arcane damage, rising to 15%%. The beam starts 30%% wider, grows by 10%% of its original width per hit, and reaches 400%% extra width.',
    'arcane_tier_2_spell_2_modifier_1': 'Arcane Explosion deals 160%% Arcane damage. Hitting an enemy resets its cooldown.',
    'arcane_tier_2_spell_2_modifier_2': 'Each Arcane Explosion hit causes another explosion after 0.5 sec, dealing 30%% Arcane damage.',
    'frost_tier_1_passive_1': "Frost hits apply Winter's Chill for 2 sec. Enemies take 8%% more Frost critical damage per stack, up to 40%% at 5 stacks.",
    'frost_tier_2_passive_2': 'After rolling, your next Frost spell within 5 sec deals 20%% more damage. Cooldown: 10 sec.',
    'fire_tier_4_spell_1_modifier_2': 'Wall of Flames heals you and party allies for 0.5%% of your Fire power each hit.',
}

def impacts(obj, kind):
    found = []
    if isinstance(obj, dict):
        action = obj.get('action', {})
        if action.get('type') == kind:
            found.append(action.get(kind.lower(), {}).get('spell_power_coefficient', 1))
        for value in obj.values():
            found.extend(impacts(value, kind))
    elif isinstance(obj, list):
        for value in obj:
            found.extend(impacts(value, kind))
    return found

report = []
for prefix, namespace in [('skill_tree-', 'skill_tree_rpgs'), ('wizards-', 'wizards'),
                          ('archers-', 'archers'), ('paladins-', 'paladins'), ('rogues-', 'rogues')]:
    source = next((ROOT / 'mods').glob(prefix + '*.jar'))
    changed = []
    keys = []
    with ZipFile(source) as src, ZipFile(OUT / source.name, 'w') as dst:
        language_path = f'assets/{namespace}/lang/en_us.json'
        for entry in src.infolist():
            old = data = src.read(entry.filename)
            if entry.filename == 'data/wizards/spell/arcane_barrage_bolt.json':
                spell = json.loads(data)
                spell['impacts'][0]['action']['damage']['spell_power_coefficient'] = .4
                data = (json.dumps(spell, indent=2) + '\n').encode()
            if entry.filename == language_path:
                lang = json.loads(data)
                for key, value in list(lang.items()):
                    if not key.startswith('spell.') or not key.endswith('.description'):
                        continue
                    original = value
                    spell_id = key.split('.')[2]
                    path = f'data/{namespace}/spell/{spell_id}.json'
                    if path in src.namelist():
                        spell = json.loads(src.read(path))
                        school = spell.get('school', 'spell_power:healing').split(':')[-1]
                        label = {'physical_ranged': 'ranged damage', 'physical_melee': 'melee damage',
                                 'physical_melee_dual': 'melee damage'}.get(school, school.title() + ' power')
                        cast = spell.get('active', {}).get('cast', {})
                        factor = cast.get('duration', 1) / cast['channel']['ticks'] if cast.get('type') == 'CHANNEL' else 1
                        for token, kind in [('damage', 'DAMAGE'), ('heal', 'HEAL')]:
                            coefficients = impacts(spell, kind)
                            for index, coefficient in enumerate(coefficients):
                                percent = f'{coefficient * factor * 100:g}%% of your {label}'
                                value = value.replace('{' + token + (f'_{index + 1}' if index else '') + '}', percent)
                        # These caster modifiers are already percentages when rendered.
                    if namespace == 'skill_tree_rpgs':
                        value = OVERRIDES.get(spell_id, value)
                    if namespace == 'wizards' and spell_id in ['arcane_barrage', 'arcane_barrage_bolt']:
                        value = value.replace('50%%', '40%%')
                    value = value.replace('before haste', '(reduced by haste)').replace('No proc cooldown.', '')
                    value = value.replace('deliveries', 'hits').replace('delivery', 'hit').replace('per impact', 'per hit')
                    value = value.replace('1.2x movement speed', '+20%% movement speed')
                    for school_name in ['Arcane', 'Fire', 'Frost']:
                        for suffix in [school_name.lower() + ' spell damage', school_name.lower() + ' damage', 'damage']:
                            value = value.replace('of your ' + school_name + ' power ' + suffix, school_name + ' damage')
                    value = value.replace('of your Fire power fire spell and', 'Fire damage and')
                    value = value.replace('of your Healing power damage', 'of your Healing power as damage')
                    value = value.replace('of your ranged damage damage', 'ranged damage').replace('of your melee damage damage', 'melee damage')
                    if namespace == 'wizards' and spell_id == 'fire_storm':
                        value = 'Channel flames around you, dealing 40%% Fire damage per hit within 15 blocks.'
                    if namespace == 'wizards' and spell_id == 'frost_elemental':
                        value = 'Summon a Frost Elemental for 20 sec. Its health, armor and melee damage grow with your Frost power. It has 20%% of your Frost power; its Frost Shard deals 100%% and Super Nova deals 180%% of its own power. Cooldown: 30 sec (reduced by haste).'

                    if value != original:
                        lang[key] = value.strip()
                        keys.append(key)
                data = (json.dumps(lang, ensure_ascii=False, indent=2) + '\n').encode()
            if data != old:
                changed.append(entry.filename)
            dst.writestr(entry, data)
    report.append({'jar': source.name, 'before_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
                   'after_sha256': hashlib.sha256((OUT / source.name).read_bytes()).hexdigest(),
                   'changed_entries': changed, 'description_keys': keys})
(OUT / 'artifacts.json').write_text(json.dumps(report, indent=2) + '\n')
print('Updated', sum(len(r['description_keys']) for r in report), 'descriptions across', len(report), 'JARs')

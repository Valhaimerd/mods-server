"""Update only physical skill rewards and Frenzy damage in current pack archives.

Writes candidates to tmp/physical-rebalance; preserves all other ZIP entries.
"""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json

ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / 'tmp/physical-rebalance'
OUT.mkdir(parents=True, exist_ok=True)
report = []
for name in ['adventurerskills-EN-1.3-26.2-neoforge-fixed.jar',
             'skill_tree-neoforge-1.6.1+26.2.jar', 'crossarms-1.4.jar']:
    source = ROOT / 'mods' / name
    candidate = OUT / name
    changed = []
    with ZipFile(source) as src, ZipFile(candidate, 'w') as dst:
        for entry in src.infolist():
            old = data = src.read(entry.filename)
            if entry.filename.endswith('/definitions.json') and '/puffish_skills/categories/' in entry.filename:
                definitions = json.loads(data)
                if name.startswith('adventurer'):
                    for key, value, title in [('melee_damage+3%', .02, '+2% Melee Damage'),
                                              ('ranged_damage+3%', .03, '+3% Ranged Damage')]:
                        definitions[key]['title'] = title
                        definitions[key]['rewards'][0]['data']['value'] = value
                    martial = definitions['martial_prowess']
                    for reward in martial['rewards']:
                        if reward['data'].get('attribute') == 'puffish_attributes:melee_damage':
                            reward['data']['value'] = .06
                    martial['description']['text'] = martial['description']['text'].replace('+8% Melee Damage', '+6% Melee Damage')
                else:
                    for key, node in definitions.items():
                        if key.startswith('paladin'):
                            continue
                        attributes = {r.get('data', {}).get('attribute') for r in node.get('rewards', [])}
                        for reward in node.get('rewards', []):
                            attribute = reward.get('data', {}).get('attribute')
                            if attribute in ['minecraft:attack_damage', 'ranged_weapon:damage']:
                                reward['data']['value'] = .02 if attribute == 'minecraft:attack_damage' else .03
                        if attributes & {'minecraft:attack_damage', 'ranged_weapon:damage'}:
                            def text(value):
                                if isinstance(value, str):
                                    return value.replace('+3% Attack Damage', '+2% Attack Damage').replace('+2% Ranged Damage', '+3% Ranged Damage')
                                if isinstance(value, list):
                                    return [text(v) for v in value]
                                if isinstance(value, dict):
                                    return {k: text(v) for k, v in value.items()}
                                return value
                            node['description'] = text(node['description'])
                data = (json.dumps(definitions, ensure_ascii=False, indent=2) + '\n').encode()
            if name == 'crossarms-1.4.jar' and entry.filename == 'data/crossarms/function/enchantment/frenzy/tag.mcfunction':
                data = (ROOT / 'docs/patches/crossarms-firing/overrides' / entry.filename).read_bytes()
            if data != old:
                changed.append(entry.filename)
            dst.writestr(entry, data)
    report.append({'jar': name, 'before_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
                   'after_sha256': hashlib.sha256(candidate.read_bytes()).hexdigest(), 'changed_entries': changed})
(OUT / 'artifacts.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report, indent=2))

"""Layer emitter damage and Charged Beam width over the current pack archives."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import subprocess

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'tmp/arcane-emitter-beam'
CLASSES = WORK / 'classes'
CLASSES.mkdir(parents=True, exist_ok=True)
base = (ROOT / 'tmp/magic-third-tier/compile.args').read_text()
cp = ';'.join(p.as_posix() for p in (ROOT / 'mods').glob('*.jar'))
cp += ';' + base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + CLASSES.as_posix() + '"\n'
rules = (ROOT / 'docs/patches/magic-revision-four/RevisionRules.java').read_text()
assert rules.count('spell_power_coefficient = 1.8f;') == 1
(WORK / 'RevisionRules.java').write_text(rules.replace('spell_power_coefficient = 1.8f;', 'spell_power_coefficient = 1.6f;'))
args += '"' + (WORK / 'RevisionRules.java').as_posix() + '"\n'
(WORK / 'compile.args').write_text(args)
subprocess.run(['C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javac.exe', '@' + str(WORK / 'compile.args')], check=True)
report = []
for name in ['wizards-neoforge-3.1.2+26.2.jar', 'skill_tree-neoforge-1.6.1+26.2.jar', 'spell_engine-neoforge-1.10.5+26.2.jar']:
    source = ROOT / 'mods' / name
    output = WORK / name
    changed = []
    with ZipFile(source) as src, ZipFile(output, 'w') as dst:
        for entry in src.infolist():
            old = data = src.read(entry.filename)
            if entry.filename == 'data/wizards/spell/arcane_barrage_bolt.json':
                spell = json.loads(data)
                spell['impacts'][0]['action']['damage']['spell_power_coefficient'] = .4
                data = (json.dumps(spell, indent=2) + '\n').encode()
            if entry.filename in ['assets/wizards/lang/en_us.json', 'assets/skill_tree_rpgs/lang/en_us.json']:
                lang = json.loads(data)
                for key in ['spell.wizards.arcane_barrage.description', 'spell.wizards.arcane_barrage_bolt.description']:
                    if key in lang:
                        lang[key] = lang[key].replace('50%%', '40%%')
                key = 'spell.skill_tree_rpgs.arcane_tier_3_spell_1_modifier_2.description'
                if key in lang:
                    lang[key] = lang[key].replace('0.04x per delivery to 3x', '0.10x per delivery to 5x')
                data = (json.dumps(lang, ensure_ascii=False, indent=2) + '\n').encode()
            if entry.filename == 'net/spell_engine/internals/RevisionRules.class':
                data = (CLASSES / entry.filename).read_bytes()
            if data != old:
                changed.append(entry.filename)
            dst.writestr(entry, data)
    report.append({'jar': name, 'before_sha256': hashlib.sha256(source.read_bytes()).hexdigest(),
                   'after_sha256': hashlib.sha256(output.read_bytes()).hexdigest(), 'changed_entries': changed})
(WORK / 'artifacts.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report, indent=2))

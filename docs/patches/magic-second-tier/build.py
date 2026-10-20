"""Apply after first-tier repacks; compile narrow engine hooks and repack resources."""
from pathlib import Path
import copy
import json
import subprocess
import sys
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / 'tmp/magic-second-tier'
SOURCE = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / 'mods'
OUT = WORK / 'built'
JAVA = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
OUT.mkdir(parents=True, exist_ok=True)
classes = WORK / 'classes'
classes.mkdir(exist_ok=True)
args = (ROOT / 'tmp/magic-balance-audit/compile.args').read_text()
cp = args.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
cp += ';' + ';'.join(p.resolve().as_posix() for p in SOURCE.glob('*.jar'))
compile_args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + classes.as_posix() + '"\n'
compile_args += '\n'.join('"' + (HERE / n).as_posix() + '"' for n in ['SecondTierRules.java', 'PatchEngine.java'])
(WORK / 'compile.args').write_text(compile_args)
subprocess.run([str(JAVA / 'javac.exe'), '@' + str(WORK / 'compile.args')], check=True)

def damage(d, value):
    next(i['action']['damage'] for i in d['impacts'] if i['action']['type'] == 'DAMAGE')['spell_power_coefficient'] = value

for name, ns in [('wizards-neoforge-3.1.2+26.2.jar', 'wizards'), ('skill_tree-neoforge-1.6.1+26.2.jar', 'skill_tree_rpgs'), ('spell_engine-neoforge-1.10.5+26.2.jar', 'spell_engine')]:
    with zipfile.ZipFile(SOURCE / name) as z:
        infos = z.infolist()
        files = {i.filename: z.read(i) for i in infos}
    def edit(id, change):
        path = f'data/{ns}/spell/{id}.json'
        d = json.loads(files[path]); change(d)
        files[path] = (json.dumps(d, indent=2, ensure_ascii=False) + '\n').encode()
    if ns == 'wizards':
        def beam(d):
            d['cost']['cooldown'].update(duration=30.0, proportional=True)
            d['target']['beam']['width'] = .12
        edit('arcane_beam', beam)
        def barrage(d):
            d['cost']['cooldown']['duration'] = 6.0
            d['impacts'][0]['action']['summon']['behaviour']['lifespan']['active_seconds'] = 7
        edit('arcane_barrage', barrage)
        def meteor(d):
            damage(d, 1.5)
            d['cost']['cooldown']['duration'] = 16.0
            d['area_impact']['radius'] = 10.0
            d['area_impact']['area']['distance_dropoff'] = 'NONE'
            d['deliver']['meteor']['launch_radius'] = 5.0
        edit('fire_meteor', meteor)
        def storm(d):
            damage(d, 1.12)  # 1.12 * 5 / 8 = .70 per delivery
            d['cost']['cooldown'].update(duration=25.0, proportional=True)
            d['range'] = 15.0
            for p in d.get('release', {}).get('visuals', {}).get('particles', []):
                if 'scale' in p.get('appearance', {}): p['appearance']['scale'] = 15.0
        edit('fire_storm', storm)
        def shield(d):
            d['cost']['cooldown']['duration'] = 12.0
            d['impacts'][0]['action']['status_effect'].update(duration=3.0, amplifier=1)
        edit('frost_shield', shield)
        def lance(d):
            damage(d, 2.0)
            d['active']['cast']['duration'] = 1.0
            d['cost']['cooldown']['duration'] = 12.0
            d['deliver']['projectile']['launch_properties']['velocity'] = 1.5
            d['impacts'][0]['action']['damage']['knockback'] = 2.0
            effect = d['impacts'][1]['action']['status_effect']
            effect.update(effect_id='wizards:frozen', duration=4.0)
            effect.pop('apply_limit', None)
        edit('frost_lance', lance)
        langpath = 'assets/wizards/lang/en_us.json'
        lang = json.loads(files[langpath])
        lang['spell.wizards.frost_lance.description'] = 'Launch an ice lance dealing {damage} damage. Freezes hit enemies for 4 sec after a 0.5 sec delay.'
        files[langpath] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()
    elif ns == 'skill_tree_rpgs':
        edit('arcane_tier_3_spell_1_modifier_1', lambda d: d['modifiers'][0]['impacts'][0]['action']['status_effect'].update(amplifier_cap=24))
        def splash(d):
            damage(d, .5)
            d['deliver']['delay'] = 5
            d['deliver']['clouds'][0]['volume']['radius'] = 5.0
        edit('fire_tier_3_spell_1_modifier_2', splash)
        def whirlpool(d):
            m = d['modifiers'][0]
            m['impacts'][0]['action']['status_effect'].update(duration=1.0, amplifier=1)
            m['impacts'][1]['action']['velocity']['push']['z'] = -.45
            for p in m.get('release', {}).get('particles', []):
                if 'scale' in p.get('appearance', {}): p['appearance']['scale'] = 15.0
        edit('fire_tier_3_spell_2_modifier_1', whirlpool)
        edit('frost_tier_3_spell_1_modifier_1', lambda d: d['modifiers'][0]['impacts'][0]['action']['status_effect'].update(duration=3.0))
        edit('frost_tier_3_spell_1_modifier_2', lambda d: d['modifiers'][0].update(effect_duration_add=1.0))
        def shatter(d):
            area = d['modifiers'][0]['replacing_area_impact']
            area['radius'] = 4.0
            area['area']['distance_dropoff'] = 'NONE'
        edit('frost_tier_3_spell_2_modifier_2', shatter)
        langpath = 'assets/skill_tree_rpgs/lang/en_us.json'
        lang = json.loads(files[langpath])
        lang['spell.skill_tree_rpgs.fire_tier_3_spell_2_modifier_1.description'] = 'Firestorm pulls enemies inward across its 15-block radius and applies Slowness II for 1 sec, refreshed on hit.'
        files[langpath] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()
    else:
        for entry in ['net/spell_engine/internals/cost/SpellCooldowns.class', 'net/spell_engine/internals/impact/SpellImpacts.class']:
            original = WORK / Path(entry).name
            original.write_bytes(files[entry])
            patched = original.with_suffix('.patched')
            subprocess.run([str(JAVA / 'java.exe'), '-cp', str(classes) + ';' + str(next((ROOT / 'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))), 'PatchEngine', str(original), str(patched)], check=True)
            files[entry] = patched.read_bytes()
        for p in (classes / 'net').rglob('*.class'):
            files[p.relative_to(classes).as_posix()] = p.read_bytes()
    with zipfile.ZipFile(OUT / name, 'w') as z:
        for info in infos: z.writestr(copy.copy(info), files.pop(info.filename))
        for path, data in files.items(): z.writestr(path, data)
    print('Built', OUT / name)

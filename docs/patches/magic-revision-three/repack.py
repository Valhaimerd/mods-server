"""Resource stage for the third magic balance pass; apply to current active archives."""
from pathlib import Path
import copy
import json
import sys
import zipfile


def transform(files, ns):
    def edit(id, operation):
        path = f'data/{ns}/spell/{id}.json'
        data = json.loads(files[path]); operation(data)
        files[path] = (json.dumps(data, indent=2, ensure_ascii=False) + '\n').encode()
    def damage(data, amount):
        next(i['action']['damage'] for i in data['impacts'] if i['action']['type'] == 'DAMAGE')['spell_power_coefficient'] = amount
    if ns == 'wizards':
        # Channel normalization is four seconds / twelve missiles.
        def missiles(d):
            damage(d, .72)
            d['cost']['cooldown'].update(duration=16.0, proportional=True)
        edit('arcane_missile', missiles)
        edit('arcane_explosion', lambda d: damage(d, 2.8))
        edit('fire_breath', lambda d: damage(d, .75))
        edit('fire_storm', lambda d: damage(d, .64))
        def meteor(d):
            d['active']['cast']['duration'] = 2.0
            d['area_impact']['radius'] = 6.0
            projectile = d['deliver']['meteor']['projectile']
            for model in projectile['client_data']['composite_model']['models']:
                model['fx']['scale'] = 1.5
            for particle in d['area_impact'].get('visuals', {}).get('particles', []):
                if particle['id'].startswith('spell_engine:area_effect_'):
                    particle['appearance']['scale'] = 6.0
        edit('fire_meteor', meteor)
        edit('frost_spikes', lambda d: damage(d, .8))
        edit('frost_nova', lambda d: damage(d, 1.8))
        def beam(d):
            d['target']['beam']['width'] = .104  # original .08 * 1.3
        edit('arcane_beam', beam)
        path = 'assets/wizards/lang/en_us.json'
        lang = json.loads(files[path])
        lang['spell.wizards.arcane_missile.description'] = 'Channel 12 homing missiles over 4 sec, each dealing 24%% Arcane spell damage before modifiers. Cooldown scales from 8 to 16 sec with channel completion, before haste.'
        lang['spell.wizards.arcane_beam.description'] = 'Channel 25 deliveries over 5 sec, each dealing 20%% Arcane spell damage before modifiers. Cooldown scales from 15 to 30 sec with channel completion, before haste. Charged Beam changes channel duration and damage. Consumes all Arcane Charges.'
        files[path] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()
    else:
        def chain(d):
            damage(d, .3)
            d['target'] = {'type': 'FROM_TRIGGER'}
            d['deliver']['delay'] = 10
            d['passive']['triggers'][0]['chance'] = 1.0
            d['area_impact']['force_indirect'] = False
            d['cost']['cooldown'] = {'duration': 0.0}
            d['passive']['triggers'][0]['cap_per_tick'] = 0
        edit('arcane_tier_2_spell_2_modifier_2', chain)
        def echo(d):
            d['passive']['triggers'][0].update(chance=1.0)
            d['cost']['cooldown']['duration'] = 0.0
        edit('arcane_tier_2_spell_2_modifier_1', echo)
        edit('fire_tier_3_spell_1_modifier_2', lambda d: damage(d, .1))
        edit('frost_tier_2_spell_2_modifier_2', lambda d: d['modifiers'][0]['power_modifier'].update(power_multiplier=.25 / .8 - 1.1))
        edit('frost_tier_2_spell_1_modifier_1', lambda d: damage(d, .2))
        edit('frost_tier_2_spell_1_root', lambda d: d['modifiers'][0].update(effect_duration_add=.5))
        def exposure(d):
            d['modifiers'][0]['impacts'][0]['action']['status_effect']['duration'] = 3.0
        edit('arcane_tier_3_spell_1_modifier_1', exposure)
        def charged(d):
            d['modifiers'] = [{'spell_pattern': 'wizards:arcane_beam', 'channel_ticks_add': 25}]
        edit('arcane_tier_3_spell_1_modifier_2', charged)
        def shatter(d):
            d['modifiers'][0]['replacing_area_impact']['radius'] = 8.0
            d['modifiers'][0]['projectile_perks'] = {'pierce': -999}
        edit('frost_tier_3_spell_2_modifier_2', shatter)

        path = 'assets/skill_tree_rpgs/lang/en_us.json'
        lang = json.loads(files[path])
        lang.update({
            'skill.skill_tree_rpgs.arcane_tier_3_spell_1_modifier_2.title': 'Charged Beam',
            'spell.skill_tree_rpgs.arcane_tier_3_spell_1_modifier_2.name': 'Charged Beam',
            'spell.skill_tree_rpgs.arcane_tier_3_spell_1_modifier_2.description': 'Channel for 10 sec over 50 deliveries. Damage starts at 10%%, increasing by 0.25 percentage points per delivery to 20%%. Beam width grows from 1.3x by 0.04x per delivery to 3x.',
            'spell.skill_tree_rpgs.arcane_tier_3_spell_1_modifier_1.description': 'Beam hits apply Arcane Exposure for 3 sec: +4%% Arcane damage taken per level, up to 25 levels.',
            'spell.skill_tree_rpgs.arcane_tier_2_spell_2_modifier_1.description': 'Arcane Explosion hits always reset its cooldown, but Explosion damage becomes 200%%. No proc cooldown.',
            'spell.skill_tree_rpgs.arcane_tier_2_spell_2_modifier_2.description': 'Each Arcane Explosion hit guarantees a secondary explosion at the struck enemy after 0.5 sec, dealing 30%% damage. No proc cooldown.',
            'spell.skill_tree_rpgs.frost_tier_3_spell_2_modifier_2.description': 'Ice Lance stops at its first enemy. The direct hit retains 200%% damage and 4 sec delayed Frozen; its 8-block splash deals 50%% damage and 1 sec delayed Frozen without damage falloff.',
            'spell.skill_tree_rpgs.frost_tier_2_spell_2_modifier_2.description': 'Frost Spikes raises two additional rows at +/-10 degrees, dealing 25%% damage per impact with Improved Frost Spikes.',
        })
        files[path] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()


if __name__ == '__main__':
    source, output = map(Path, sys.argv[1:3])
    output.mkdir(parents=True, exist_ok=True)
    for name, ns in [('wizards-neoforge-3.1.2+26.2.jar', 'wizards'), ('skill_tree-neoforge-1.6.1+26.2.jar', 'skill_tree_rpgs')]:
        with zipfile.ZipFile(source / name) as z:
            infos = z.infolist(); files = {i.filename: z.read(i) for i in infos}
        transform(files, ns)
        with zipfile.ZipFile(output / name, 'w') as z:
            for info in infos: z.writestr(copy.copy(info), files[info.filename])
        print('Built resource candidate', output / name)

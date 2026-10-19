"""Repack first-tier spell data without changing classes or prior skill patches."""
from pathlib import Path
import copy
import json
import sys
import zipfile

NAMES = ('wizards-neoforge-3.1.2+26.2.jar', 'skill_tree-neoforge-1.6.1+26.2.jar')


def transform(files, namespace):
    def edit(id, change):
        path = f'data/{namespace}/spell/{id}.json'
        data = json.loads(files[path])
        change(data)
        files[path] = (json.dumps(data, indent=2, ensure_ascii=False) + '\n').encode()

    def damage(data, coefficient):
        next(i['action']['damage'] for i in data['impacts'] if i['action']['type'] == 'DAMAGE')['spell_power_coefficient'] = coefficient

    def freeze(data):
        status = next(i['action']['status_effect'] for i in data['impacts'] if i['action']['type'] == 'STATUS_EFFECT')
        status['duration'] = 2.0
        status.pop('apply_limit', None)

    def placement(distance, index, yaw=None):
        p = {'delay_ticks': index * 2, 'location_offset_by_look': float(distance), 'apply_yaw': True}
        if yaw is not None:
            p['location_yaw_offset'] = float(yaw)
        return p

    if namespace == 'wizards':
        edit('arcane_missile', lambda d: d['cost']['cooldown'].update(duration=4.0, proportional=True))
        def explosion(d):
            damage(d, 2.8)
            d['range'] = 7.0
        edit('arcane_explosion', explosion)
        def breath(d):
            # Engine normalizes channel damage by duration / delivery count: 5 / 25.
            damage(d, 1.4)
            d['range'] = 8.0
            d['target']['area']['distance_dropoff'] = 'NONE'
            d['cost']['cooldown'].update(duration=4.0, proportional=True)
        edit('fire_breath', breath)
        def slash(d):
            damage(d, 1.5)
            d['active']['cast'].update(duration=0.1, type='STANDARD')
            d['active']['cast'].pop('charge', None)
            d['cost']['cooldown']['duration'] = 3.0
            d['deliver']['projectile']['launch_properties']['velocity'] = 0.9
        edit('fire_slash', slash)
        def nova(d):
            damage(d, 2.0)
            d['active']['cast']['duration'] = 1.0
            d['range'] = 10.0
            freeze(d)
        edit('frost_nova', nova)
        def spikes(d):
            damage(d, 2.4)
            freeze(d)
            cloud = d['deliver']['clouds'][0]
            cloud['placement'] = placement(1, 0)
            cloud['additional_placements'] = [placement(i, i - 1) for i in range(2, 9)]
        edit('frost_spikes', spikes)
    else:
        edit('arcane_tier_2_spell_2_root', lambda d: d['modifiers'][0].update(range_add=2.0))
        edit('arcane_tier_2_spell_2_modifier_2', lambda d: damage(d, 1.4))
        def explosive(d):
            damage(d, 0.3)
            d['cost']['cooldown']['duration'] = 0.2
        edit('fire_tier_2_spell_1_modifier_1', explosive)
        edit('fire_tier_2_spell_1_modifier_2', lambda d: d['modifiers'][0].update(range_add=4.0))
        edit('fire_tier_2_spell_2_modifier_1', lambda d: d['modifiers'][0].update(projectile_scale_multiply=0.45))
        edit('frost_tier_2_spell_1_modifier_1', lambda d: damage(d, 0.8))
        edit('frost_tier_2_spell_2_modifier_1', lambda d: d['modifiers'][0].update(additional_placements=[placement(i, i - 1) for i in range(9, 17)]))
        edit('frost_tier_2_spell_2_modifier_2', lambda d: d['modifiers'][0].update(additional_placements=[placement(i, i - 1, yaw) for yaw in (-45, 45) for i in range(1, 9)]))
        # These two tooltips contain literal numbers; other affected descriptions use live placeholders.
        path = 'assets/skill_tree_rpgs/lang/en_us.json'
        lang = json.loads(files[path])
        lang['spell.skill_tree_rpgs.fire_tier_2_spell_2_modifier_1.description'] = 'Flame Slash is 45%% larger.'
        lang['spell.skill_tree_rpgs.frost_tier_2_spell_2_modifier_1.description'] = 'Frost Spikes raises 8 additional spikes, extending the row to 16 blocks.'
        files[path] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()


def main():
    source, output = map(Path, sys.argv[1:3])
    output.mkdir(parents=True, exist_ok=True)
    for name, namespace in zip(NAMES, ('wizards', 'skill_tree_rpgs')):
        with zipfile.ZipFile(source / name) as original:
            files = {i.filename: original.read(i) for i in original.infolist()}
            transform(files, namespace)
            with zipfile.ZipFile(output / name, 'w') as target:
                for info in original.infolist():
                    target.writestr(copy.copy(info), files[info.filename])
        print('Built', output / name)


if __name__ == '__main__':
    main()

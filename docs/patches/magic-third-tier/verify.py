"""Check the final resources and preserve the preceding spell-balance helpers."""
from pathlib import Path
import json
import math
import sys
import zipfile

ROOT = Path(sys.argv[1]) if len(sys.argv) > 1 else Path('mods')
count = 0
def check(actual, expected):
    global count
    assert actual == expected or isinstance(actual, (int, float)) and isinstance(expected, (int, float)) and math.isclose(actual, expected, abs_tol=1e-6), (actual, expected)
    count += 1

with zipfile.ZipFile(ROOT / 'wizards-neoforge-3.1.2+26.2.jar') as wiz, zipfile.ZipFile(ROOT / 'skill_tree-neoforge-1.6.1+26.2.jar') as tree, zipfile.ZipFile(ROOT / 'spell_engine-neoforge-1.10.5+26.2.jar') as engine:
    def spell(id):
        namespace, path = id.split(':'); return json.loads((wiz if namespace == 'wizards' else tree).read(f'data/{namespace}/spell/{path}.json'))
    def mod(id): return spell('skill_tree_rpgs:' + id)['modifiers'][0]
    blink = spell('wizards:arcane_blink')
    check(blink['impacts'][0]['action']['teleport']['forward']['distance'], 10)
    check(blink['deliver']['custom']['handler'], 'wizards:burst_blink')
    check(mod('arcane_tier_4_spell_1_root')['teleport_distance_add'], 5)
    check(mod('arcane_tier_4_spell_1_modifier_1')['cooldown_duration_deduct'], 3)
    speed = mod('arcane_tier_4_spell_1_modifier_2')['impacts'][0]['action']['status_effect']
    check(speed['effect_id'], 'minecraft:speed'); check(speed['duration'], 4); check(speed['amplifier'], 0)
    burst = spell('wizards:burst_blink_explosion')
    check(burst['impacts'][0]['action']['damage']['spell_power_coefficient'], .5)
    check(burst['area_impact']['radius'], 5); check(burst['area_impact']['area']['distance_dropoff'], 'NONE')
    check(spell('wizards:arcane_evocation')['cost']['cooldown']['duration'], 35)
    check(mod('arcane_tier_4_spell_2_modifier_1')['channel_ticks_add'], 2)
    check(mod('arcane_tier_4_spell_2_modifier_1')['effect_amplifier_cap_add'], 2)
    check(mod('arcane_tier_4_spell_2_modifier_2')['effect_duration_add'], 5)
    blizzard = spell('wizards:frost_blizzard')
    check(blizzard['cost']['cooldown']['duration'], 20); check(blizzard['cost']['cooldown']['proportional'], True)
    check(blizzard['impacts'][0]['action']['damage']['spell_power_coefficient'], .4)
    check(blizzard['area_impact']['radius'], 3); check(blizzard['area_impact']['area']['distance_dropoff'], 'NONE')
    check(spell('wizards:fire_wall')['impacts'][0]['action']['damage']['spell_power_coefficient'], .2)
    check(mod('fire_tier_4_spell_1_modifier_1')['cloud']['radius_add'], .3)
    check(mod('fire_tier_4_spell_1_modifier_2')['impacts'][0]['action']['heal']['spell_power_coefficient'], .005)
    for id in ['fire_hydra', 'frost_elemental']:
        d = spell('wizards:' + id)
        check(d['cost']['cooldown']['duration'], 30); check(d['cost']['cooldown']['haste_affected'], True)
        check(d['impacts'][0]['action']['summon']['behaviour']['lifespan']['active_seconds'], 20)
    check(spell('wizards:elemental_frost_shard')['impacts'][0]['action']['damage']['spell_power_coefficient'], 1)
    for id, radius, duration in [('elemental_frost_nova', 10, 2), ('legion_frost_nova', 6, .5), ('frost_nova', 10, 2)]:
        nova = spell('wizards:' + id)
        check(nova['range'], radius)
        check(nova['impacts'][0]['action']['damage']['spell_power_coefficient'], 1.8)
        check(nova['impacts'][1]['action']['status_effect']['duration'], duration)
    breath = spell('wizards:ancient_hydra_breath')
    check(breath['range'], 12); check(breath['target']['area']['angle_degrees'], 40)
    check(breath['active']['cast']['duration'], 5); check(breath['active']['cast']['channel']['ticks'], 25)
    check(breath['impacts'][0]['action']['damage']['spell_power_coefficient'] * 5 / 25, .12)
    check(breath['cost']['cooldown']['duration'], 0)
    check(spell('wizards:hydra_fireball')['impacts'][0]['action']['damage']['spell_power_coefficient'], .55)
    check(spell('wizards:fireball')['impacts'][0]['action']['damage']['spell_power_coefficient'], .8)
    # The earlier first-/second-tier revisions must survive this repack.
    for id, coefficient in [('arcane_missile', .78), ('arcane_explosion', 2.6), ('fire_breath', .65), ('fire_slash', 1)]:
        check(spell('wizards:' + id)['impacts'][0]['action']['damage']['spell_power_coefficient'], coefficient)
    for name in ['RevisionRules', 'SecondTierRules', 'PartyRules', 'WallRules', 'SummonRules', 'ThirdTierRules']:
        check(f'net/spell_engine/internals/{name}.class' in engine.namelist(), True)
    for z in [wiz, tree, engine]:
        check(z.testzip(), None)
        check(any('Probe' in p for p in z.namelist()), False)
        for path in z.namelist():
            if path.endswith('.json'): json.loads(z.read(path))
            if path.endswith('.class'): assert int.from_bytes(z.read(path)[6:8], 'big') <= 69, path
print(f'PASS {count} third-tier structural checks')

"""Wizard passive balance, preserving node IDs and mutually exclusive pairs."""
import json


def transform(name, files):
    if not name.startswith('skill_tree-'):
        return
    files['assets/skill_tree_rpgs/textures/mob_effect/arcane_vulnerability.png'] = files['assets/skill_tree_rpgs/textures/mob_effect/arcane_exposure.png']
    def put(path, value): files[path] = (json.dumps(value, indent=2, ensure_ascii=False) + '\n').encode()
    def status(effect, duration, amplifier=0):
        return {'action': {'type': 'STATUS_EFFECT', 'status_effect': {'effect_id': effect, 'duration': duration, 'amplifier': amplifier}}}
    nodes = {}
    for school in ['arcane', 'fire', 'frost']:
        for tier in range(1, 4):
            for number in [1, 2]:
                key = f'{school}_tier_{tier}_passive_{number}'
                d = json.loads(files[f'data/skill_tree_rpgs/spell/{key}.json'])
                d.setdefault('cost', {}).setdefault('cooldown', {})['haste_affected'] = True
                nodes[key] = d
    def node(key): return nodes[key]
    def chance(key, value):
        for t in node(key)['passive']['triggers']: t['chance'] = value
    def cooldown(key, value): node(key)['cost']['cooldown']['duration'] = value
    def damage(key, value): node(key)['impacts'][0]['action']['damage']['spell_power_coefficient'] = value
    def next_spell(key, effect):
        d = node(key)
        d['deliver'] = {'type': 'DIRECT'}
        d['target'] = {'type': 'CASTER'}
        d['impacts'] = [status(effect, 5)]
        d['impacts'][0]['action']['apply_to_caster'] = True
    chance('arcane_tier_1_passive_1', .25)
    node('arcane_tier_1_passive_1')['area_impact']['radius'] = 5
    chance('arcane_tier_1_passive_2', 1); cooldown('arcane_tier_1_passive_2', 3)
    damage('arcane_tier_2_passive_1', .4)
    node('arcane_tier_2_passive_1')['impacts'].append(status('minecraft:weakness', 6, 1))
    phase = node('arcane_tier_2_passive_2')
    phase['impacts'][0]['action'] = {'type': 'IMMUNITY', 'apply_to_caster': True, 'immunity': {'duration_ticks': 6}}
    cooldown('arcane_tier_2_passive_2', 0)
    chance('arcane_tier_3_passive_1', 1); cooldown('arcane_tier_3_passive_1', 16)
    next_spell('arcane_tier_3_passive_1', 'skill_tree_rpgs:presence_of_mind')
    chance('arcane_tier_3_passive_2', 1)
    node('arcane_tier_3_passive_2')['impacts'][0]['action']['status_effect']['amplifier_power_multiplier'] = .2
    cooldown('fire_tier_1_passive_2', 8)
    damage('fire_tier_2_passive_1', .3)
    node('fire_tier_2_passive_1')['impacts'] = [node('fire_tier_2_passive_1')['impacts'][0], {'action': {'type': 'FIRE', 'fire': {'duration': 4, 'tick_offset': 0}}}]
    chance('fire_tier_2_passive_2', 1); cooldown('fire_tier_2_passive_2', 4)
    chance('fire_tier_3_passive_1', 1); damage('fire_tier_3_passive_1', .3); cooldown('fire_tier_3_passive_1', 8)
    chance('fire_tier_3_passive_2', 1)
    node('fire_tier_3_passive_2')['deliver']['stash_effect']['amplifier_power_multiplier'] = .1
    node('fire_tier_1_passive_1')['impacts'][0]['action']['status_effect']['duration'] = 2
    chance('frost_tier_1_passive_1', 1)
    node('frost_tier_1_passive_1')['impacts'][0]['action']['status_effect']['duration'] = 2
    chance('frost_tier_1_passive_2', 1); cooldown('frost_tier_1_passive_2', 12)
    node('frost_tier_2_passive_1')['impacts'][0]['action']['status_effect']['duration'] = 4
    chance('frost_tier_2_passive_2', 1)
    next_spell('frost_tier_2_passive_2', 'skill_tree_rpgs:arctic_reflex')
    chance('frost_tier_3_passive_1', 1)
    node('frost_tier_3_passive_1')['impacts'][0]['action']['cooldown']['actives']['id'] = '~^wizards:frost(?!_blizzard$|_elemental$).*'
    chance('frost_tier_3_passive_2', 1)
    node('frost_tier_3_passive_2')['deliver']['stash_effect']['amplifier_power_multiplier'] = .1
    for key, d in nodes.items(): put(f'data/skill_tree_rpgs/spell/{key}.json', d)
    path = 'assets/skill_tree_rpgs/lang/en_us.json'; lang = json.loads(files[path])
    descriptions = {
        'arcane_tier_1_passive_1': 'Arcane damage has a 25%% chance to explode for 40%% power within 5 blocks, excluding the struck target. Cooldown 1 sec before haste.',
        'arcane_tier_1_passive_2': 'Arcane damage heals you for 10%% power, with a 3 sec cooldown before haste.',
        'arcane_tier_2_passive_1': 'Rolling leaves a trap for 5 sec: 40%% Arcane damage and Weakness II for 6 sec. Radius 1.5 blocks.',
        'arcane_tier_2_passive_2': 'Rolling grants 0.3 sec damage immunity. No additional cooldown.',
        'arcane_tier_3_passive_1': 'Burst Blink or Evocation readies your next Arcane spell for 5 sec. Its hits apply 20%% Arcane Vulnerability for 6 sec. Cooldown 16 sec before haste.',
        'arcane_tier_3_passive_2': 'Casting an Arcane spell grants an 8 sec ward. Absorption: 2 x (1 + floor(0.2 x Arcane power)) HP. Cooldown 16 sec before haste.',
        'fire_tier_1_passive_1': 'Fire damage has a 50%% chance to add 4%% Fire Vulnerability per stack for 2 sec, up to 5 stacks.',
        'fire_tier_1_passive_2': 'Fire damage has a 20%% chance to stun enemies for 2 sec. Cooldown 8 sec before haste.',
        'fire_tier_2_passive_1': 'Rolling leaves a trap for 5 sec: 30%% Fire damage and 4 sec burning. Radius 1.5 blocks.',
        'fire_tier_2_passive_2': 'Rolling grants +50%% base movement speed for 2 sec. Cooldown 4 sec before haste.',
        'fire_tier_3_passive_1': 'Taking damage causes an explosion for 30%% Fire power within 5 blocks. Cooldown 8 sec before haste.',
        'fire_tier_3_passive_2': 'Casting a Fire spell grants an 8 sec ward and 30%% power retaliation. Absorption: 2 x (1 + floor(0.1 x Fire power)) HP. Cooldown 16 sec before haste.',
        'frost_tier_1_passive_1': "Frost damage adds Winter's Chill for 2 sec: +8 percentage points of Frost critical damage vulnerability per stack, up to 5 stacks.",
        'frost_tier_1_passive_2': 'Frost damage freezes enemies for 3 sec. Cooldown 12 sec before haste.',
        'frost_tier_2_passive_1': 'Rolling leaves a trap for 5 sec, freezing enemies for 4 sec within 1.5 blocks.',
        'frost_tier_2_passive_2': 'Rolling readies your next Frost spell for 5 sec. That spell deals 1.2x damage. Cooldown 10 sec before haste.',
        'frost_tier_3_passive_1': 'Taking damage resets Frost spell cooldowns, excluding Blizzard and Frost Elemental. Cooldown 30 sec before haste.',
        'frost_tier_3_passive_2': 'Casting a Frost spell grants an 8 sec ward that slows attackers. Absorption: 2 x (1 + floor(0.1 x Frost power)) HP. Cooldown 16 sec before haste.',
    }
    for key, value in descriptions.items(): lang[f'spell.skill_tree_rpgs.{key}.description'] = value
    lang.update({'effect.skill_tree_rpgs.arcane_vulnerability': 'Arcane Vulnerability',
                 'effect.skill_tree_rpgs.arcane_vulnerability.description': '20% increased Arcane damage taken.',
                 'effect.skill_tree_rpgs.presence_of_mind.description': 'Your next Arcane spell applies 20% Arcane Vulnerability for 6 sec.',
                 'effect.skill_tree_rpgs.arctic_reflex.description': 'Your next Frost spell deals 1.2x damage.'})
    put(path, lang)

"""Burst Blink, Evocation and Blizzard definitions; other independent transforms follow."""
import copy
import json

def transform(name, files):
    if name.startswith('wizards'):
        def get(id): return json.loads(files[f'data/wizards/spell/{id}.json'])
        def put(id, data): files[f'data/wizards/spell/{id}.json'] = (json.dumps(data, indent=2) + '\n').encode()
        blink = get('arcane_blink')
        blink['impacts'][0]['action']['teleport']['forward']['distance'] = 10
        blink['deliver'] = {'type': 'CUSTOM', 'custom': {'handler': 'wizards:burst_blink'}}
        put('arcane_blink', blink)
        burst = get('arcane_explosion')
        burst['type'] = 'PASSIVE'; burst.pop('active', None); burst.pop('learn', None)
        burst['tier'] = 0; burst['group'] = ''; burst['target'] = {'type': 'AREA', 'area': {}}
        burst['range'] = 5; burst['area_impact']['radius'] = 5
        burst['area_impact']['area'] = {'distance_dropoff': 'NONE'}
        burst['impacts'][0]['action']['damage']['spell_power_coefficient'] = .5
        burst['cost'] = {'cooldown': {'duration': 0}, 'durability': 0, 'exhaust': 0}
        put('burst_blink_explosion', burst)
        evocation = get('arcane_evocation'); evocation['cost']['cooldown']['duration'] = 35
        evocation['impacts'][0]['action']['status_effect']['duration'] = 10
        put('arcane_evocation', evocation)
        blizzard = get('frost_blizzard'); blizzard['cost']['cooldown'].update(duration=20, proportional=True)
        blizzard['impacts'][0]['action']['damage']['spell_power_coefficient'] = .4
        blizzard['area_impact']['radius'] = 3; blizzard['area_impact']['area']['distance_dropoff'] = 'NONE'
        put('frost_blizzard', blizzard)
        path = 'assets/wizards/lang/en_us.json'; lang = json.loads(files[path])
        lang.update({
            'spell.wizards.arcane_blink.name': 'Burst Blink',
            'spell.wizards.arcane_blink.description': 'Blink forward {teleport_distance} blocks. Deal 50%% Arcane damage within 5 blocks at departure immediately and at arrival after 0.25 sec.',
            'spell.wizards.burst_blink_explosion.name': 'Burst Blink Explosion',
            'spell.wizards.burst_blink_explosion.description': 'Deals 50%% Arcane damage within 5 blocks at either Blink position.',
            'spell.wizards.arcane_evocation.description': 'Channel to gain Evocation, up to {effect_amplifier_cap} stacks lasting {effect_duration} sec. Each stack grants +5%% base spell critical chance and +3%% base spell haste, but +10%% base damage taken.',
        })
        files[path] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()
    elif name.startswith('skill_tree'):
        def node(id, edit):
            path = f'data/skill_tree_rpgs/spell/{id}.json'; d = json.loads(files[path]); edit(d['modifiers'][0]); files[path] = (json.dumps(d, indent=2) + '\n').encode()
        node('arcane_tier_4_spell_1_root', lambda m: m.update(teleport_distance_add=5))
        node('arcane_tier_4_spell_1_modifier_1', lambda m: m.update(cooldown_duration_deduct=3))
        node('arcane_tier_4_spell_1_modifier_2', lambda m: m.update(impacts=[{'action': {'type': 'STATUS_EFFECT', 'status_effect': {'effect_id': 'minecraft:speed', 'duration': 4, 'amplifier': 0}}}]))
        node('arcane_tier_4_spell_2_modifier_1', lambda m: m.update(channel_ticks_add=2, effect_amplifier_cap_add=2))
        node('arcane_tier_4_spell_2_modifier_2', lambda m: m.update(effect_duration_add=5))
        path = 'assets/skill_tree_rpgs/lang/en_us.json'; lang = json.loads(files[path])
        lang['skill.skill_tree_rpgs.arcane_tier_4_spell_1.title'] = 'Burst Blink'
        lang.update({
            'spell.skill_tree_rpgs.arcane_tier_4_spell_1_root.description': 'Increases Burst Blink distance by 5 blocks, to 15 blocks.',
            'spell.skill_tree_rpgs.arcane_tier_4_spell_1_modifier_1.description': 'Reduces Burst Blink cooldown by 3 sec.',
            'spell.skill_tree_rpgs.arcane_tier_4_spell_1_modifier_2.description': 'Burst Blink grants Speed I (1.2x movement speed) for 4 sec.',
            'spell.skill_tree_rpgs.arcane_tier_4_spell_2_modifier_1.description': 'Evocation channels 12 times and can reach 12 stacks.',
            'spell.skill_tree_rpgs.arcane_tier_4_spell_2_modifier_2.description': 'Evocation lasts 15 sec.',
        })
        files[path] = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode()

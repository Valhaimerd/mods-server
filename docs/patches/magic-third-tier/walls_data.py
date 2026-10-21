"""Wall subset: transform(name, files) mutates a JAR entry map in place."""
import json

def transform(name, files):
    def edit(path, fn):
        d = json.loads(files[path]); fn(d)
        files[path] = (json.dumps(d, indent=2, ensure_ascii=False) + '\n').encode()
    if name.startswith('wizards-'):
        def wall(d):
            d['impacts'][0]['action']['damage']['spell_power_coefficient'] = .2
            # Width/radius stay native; WallRules replaces spherical damage filtering and height.
            d['deliver']['clouds'][0]['volume']['area']['distance_dropoff'] = 'NONE'
        edit('data/wizards/spell/fire_wall.json', wall)
        edit('assets/wizards/lang/en_us.json', lambda d: d.update({
            'spell.wizards.fire_wall.description': 'Place up to 4 walls within 5 sec of the first cast, then begin cooldown. Each wall lasts {cloud_duration} sec, dealing 20%% Fire damage per impact in a 2-block-high volume. Unused casts expire when the 5 sec window ends.'
        }))
    elif name.startswith('skill_tree-'):
        def great(d):
            m=d['modifiers'][0]
            m.setdefault('cloud', {})['radius_add'] = .3
        edit('data/skill_tree_rpgs/spell/fire_tier_4_spell_1_modifier_1.json', great)
        edit('data/skill_tree_rpgs/spell/fire_tier_4_spell_1_modifier_2.json',
             lambda d: d['modifiers'][0]['impacts'][0]['action']['heal'].update(spell_power_coefficient=.005))
        edit('assets/skill_tree_rpgs/lang/en_us.json', lambda d: d.update({
            'spell.skill_tree_rpgs.fire_tier_4_spell_1_modifier_1.description': 'Adds 2 wall segments. Segments have 1.2-block radius and 3-block height, and block living entities while active. Projectiles pass through.',
            'spell.skill_tree_rpgs.fire_tier_4_spell_1_modifier_2.description': 'Wall of Flames heals you and party allies for 0.5%% spell power per impact.'
        }))

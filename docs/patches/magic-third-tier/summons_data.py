"""Summon data edits. transform(name, files) mutates one JAR entry mapping in place."""
import copy,json

def transform(name, files):
    def get(path): return json.loads(files[path])
    def put(path,d): files[path]=(json.dumps(d,indent=2,ensure_ascii=False)+'\n').encode()
    def mark(id): return {'entries':[{'attribute_id':'magic_pack:'+id,'modifiers':[]}]}
    if name.startswith('wizards-'):
        for id in ['fire_hydra','frost_elemental']:
            path=f'data/wizards/spell/{id}.json'; d=get(path)
            d['cost']['cooldown'].update(duration=30,haste_affected=True)
            d['impacts'][0]['action']['summon']['behaviour']['lifespan']['active_seconds']=20
            if id=='frost_elemental':
                # Stats are assigned after upstream scaling, from effective owner frost power.
                s=d['impacts'][0]['action']['summon']; s['attribute_scaling']={'entries':[]}
                s['behaviour']['actions'][0]['spell_cast']['spell_id']='wizards:elemental_frost_shard'
            put(path,d)
        shard=get('data/wizards/spell/frost_shard.json')
        shard['impacts'][0]['action']['damage']['spell_power_coefficient']=1
        put('data/wizards/spell/elemental_frost_shard.json',shard)
        breath=get('data/wizards/spell/fire_breath.json')
        breath.pop('learn',None); breath['group']='ancient_hydra'; breath['range']=12
        breath['active']['cast']['duration']=5
        breath['active']['cast']['channel']={'ticks':25}
        breath['target']['area'].update(angle_degrees=20,distance_dropoff='NONE')
        breath['impacts'][0]['action']['damage']['spell_power_coefficient']=.25
        breath['cost']={'cooldown':{'duration':0,'haste_affected':False}}
        for p in breath['active']['cast'].get('particles',[]): p['batch']['angle']=20
        put('data/wizards/spell/ancient_hydra_breath.json',breath)
        path='assets/wizards/lang/en_us.json'; lang=get(path)
        lang['spell.wizards.fire_hydra.description']='Summon hydras for 20 sec (+5 sec with Improved Hydra). Cooldown 30 sec before haste.'
        lang['spell.wizards.frost_elemental.description']='Summon an owned Frost Elemental for 20 sec (+5 sec with Improved Frost Elemental). Health 30 + 50%% of your Frost power; armor 5 + 8%%; Frost power and melee damage 50%%. Its Frost Shard deals 100%% of its own Frost power. Cooldown 30 sec before haste.'
        lang['spell.wizards.elemental_frost_shard.name']='Elemental Frost Shard'
        lang['spell.wizards.elemental_frost_shard.description']='Deal {damage} Frost damage.'
        lang['spell.wizards.ancient_hydra_breath.name']='Ancient Hydra Fire Breath'
        lang['spell.wizards.ancient_hydra_breath.description']='Channel fire from the hydra mouth.'
        put(path,lang)
    elif name.startswith('skill_tree-'):
        for path,id in [('fire_tier_4_spell_2_modifier_2','ancient_hydra'),('frost_tier_4_spell_2_modifier_1','frost_legion'),('frost_tier_4_spell_2_modifier_2','frost_colossus')]:
            full=f'data/skill_tree_rpgs/spell/{path}.json'; d=get(full)
            pattern=d['modifiers'][0]['spell_pattern']
            d['modifiers']=[{'spell_pattern':pattern,'summon_attribute_scaling':mark(id)}]
            put(full,d)
        path='assets/skill_tree_rpgs/lang/en_us.json'; lang=get(path)
        lang['spell.skill_tree_rpgs.fire_tier_4_spell_2_modifier_2.description']='Summon one giant hydra.'
        lang['spell.skill_tree_rpgs.frost_tier_4_spell_2_modifier_1.description']='Summon three Frost Elementals, each with 80%% of the base attributes.'
        lang['spell.skill_tree_rpgs.frost_tier_4_spell_2_modifier_2.description']='Summon one Frost Colossus with 250%% of the base attributes, inheriting your knockback resistance.'
        put(path,lang)

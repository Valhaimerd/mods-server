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
        nova=get('data/wizards/spell/frost_nova.json')
        nova.pop('learn',None); nova['group']='elemental_nova'
        nova['impacts'][0]['action']['damage']['spell_power_coefficient']=1.8
        put('data/wizards/spell/elemental_frost_nova.json',nova)
        legion_nova=copy.deepcopy(nova); legion_nova['range']=6
        for impact in legion_nova['impacts']:
            effect=impact['action'].get('status_effect')
            if effect and effect.get('effect_id')=='wizards:frozen': effect['duration']=.5
        for particle in legion_nova.get('release',{}).get('visuals',{}).get('particles',[]):
            if 'scale' in particle.get('appearance',{}): particle['appearance']['scale']=6
        put('data/wizards/spell/legion_frost_nova.json',legion_nova)
        fireball=get('data/wizards/spell/fireball.json')
        fireball.pop('learn',None); fireball['group']='hydra_weapon'
        fireball['impacts'][0]['action']['damage']['spell_power_coefficient']=.55
        put('data/wizards/spell/hydra_fireball.json',fireball)
        breath=get('data/wizards/spell/fire_breath.json')
        breath.pop('learn',None); breath['group']='ancient_hydra'; breath['range']=12
        breath['active']['cast']['duration']=5
        breath['active']['cast']['channel']={'ticks':25}
        breath['target']['area'].update(angle_degrees=40,distance_dropoff='NONE')
        breath['impacts'][0]['action']['damage']['spell_power_coefficient']=1.2
        breath['cost']={'cooldown':{'duration':0,'haste_affected':False}}
        for p in breath['active']['cast'].get('particles',[]): p['batch']['angle']=40
        put('data/wizards/spell/ancient_hydra_breath.json',breath)
        path='assets/wizards/lang/en_us.json'; lang=get(path)
        lang['spell.wizards.fire_hydra.description']='Summon hydras for 20 sec (+5 sec with Improved Hydra). Cooldown 30 sec before haste.'
        lang['spell.wizards.frost_elemental.description']='Summon an owned Frost Elemental for 20 sec (+5 sec with Improved Frost Elemental). Health 30 + 50%% of your Frost power; armor 5 + 8%%; melee damage 50%%; own Frost power 20%%. Its Frost Shard deals 100%% and Super Nova 180%% of its own Frost power. Cooldown 30 sec before haste.'
        lang['spell.wizards.elemental_frost_shard.name']='Elemental Frost Shard'
        lang['spell.wizards.elemental_frost_shard.description']='Deal {damage} Frost damage.'
        lang['spell.wizards.elemental_frost_nova.name']='Elemental Super Nova'
        lang['spell.wizards.elemental_frost_nova.description']='Deal 180%% of the elemental\'s own Frost power and freeze enemies for 2 sec.'
        lang['spell.wizards.legion_frost_nova.name']='Legion Super Nova'
        lang['spell.wizards.legion_frost_nova.description']='Deal 180%% of this elemental\'s own Frost power within 6 blocks, freezing enemies for 0.5 sec.'
        lang['spell.wizards.ancient_hydra_breath.name']='Ancient Hydra Fire Breath'
        lang['spell.wizards.ancient_hydra_breath.description']='Continuously breathe fire in a 40-degree cone, dealing 24%% of own Fire power per delivery. Targets the nearest visible enemy within 12 blocks.'
        lang['spell.wizards.hydra_fireball.name']='Hydra Fireball'
        lang['spell.wizards.hydra_fireball.description']="Deal 55%% of the hydra's own Fire power."
        put(path,lang)
    elif name.startswith('skill_tree-'):
        for path,id in [('fire_tier_4_spell_2_modifier_2','ancient_hydra'),('frost_tier_4_spell_2_modifier_1','frost_legion'),('frost_tier_4_spell_2_modifier_2','frost_colossus')]:
            full=f'data/skill_tree_rpgs/spell/{path}.json'; d=get(full)
            pattern=d['modifiers'][0]['spell_pattern']
            d['modifiers']=[{'spell_pattern':pattern,'summon_attribute_scaling':mark(id)}]
            put(full,d)
        path='assets/skill_tree_rpgs/lang/en_us.json'; lang=get(path)
        lang['spell.skill_tree_rpgs.fire_tier_4_spell_2_modifier_2.description']='Summon one giant hydra.'
        lang['spell.skill_tree_rpgs.frost_tier_4_spell_2_modifier_1.description']='Summon three normal-sized Frost Elementals, each with 80%% of the base attributes. Each Super Nova reaches 6 blocks and freezes for 0.5 sec.'
        lang['spell.skill_tree_rpgs.frost_tier_4_spell_2_modifier_2.description']='Summon one Frost Colossus at 1.7x size with double base attributes, inheriting your knockback resistance.'
        put(path,lang)

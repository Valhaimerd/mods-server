"""Combined overlay over the pinned, installed archives. No historical JAR rebuilds."""
import json, copy, importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
spec=importlib.util.spec_from_file_location('utility_nodes',ROOT/'docs/patches/utilities-paths/nodes.py')
u=importlib.util.module_from_spec(spec);spec.loader.exec_module(u)
DEFENSE=[
 ['xzau8wf7cmkahump','arcimlg0c3igwe0e','-7_-4','d67mbc3a2pg8781z','4zj68e0t9ythniot','2wtk419203if3s6n','bxm9laus4f3lfqjk','6tinz5atn533lzo9'],
 ['zhraasg53ibsn3dk','6i5us24w9a9ar7v1','rvzvos2233a9pi4g','rr6e15vhxj8hdyt4','owk0eiqwgomn6jwb','new_colossus_healing','new_colossus_health','ebhti3huj2v7zvny'],
 ['5h6pmokx7ro45qtw','sgg10fjuiiqlos8w','-7_4','fxxc8k4yljxkbepz','00zagtopdfilmaw7','8meyqbog5d7qmyzg','bvasjpryldz0yhpx','zfneydbdzicoc783']]
RETIRED=['v2pojgmi36k5fuxe','vgrpua0glqz5sem5']
VALUES=[
 [{'hp':2},{'armor':1},{'res':.02},{'armor':1},{'res':.02},{'hp':2},{'healing':.04},{}],
 [{'hp':2},{'armor':2},{'res':.02},{'hp':2},{'healing':.04},{'hp':2},{'healing':.04},{}],
 [{'hp':2},{'armor':1},{'res':.02},{'armor':1},{'res':.02},{'knockback':.05},{'knockback':.05},{'armor':2,'res':.04,'knockback':.1}]]
RESTORE=dict(arcane_missile=.78,arcane_explosion=2.6,arcane_beam=1.,arcane_bolt=.7,arcane_blast=.8,arcane_barrage_bolt=.4,burst_blink_explosion=.5,fire_breath=.7,fire_slash=1.,fire_meteor=1.5,fire_storm=.64,fire_wall=.2,hydra_fireball=.55,ancient_hydra_breath=1.2,fire_scorch=.6,fireball=.8,fire_blast=1.,frost_nova=1.8,frost_spikes=.8,frost_lance=2.,frost_blizzard=.4,elemental_frost_shard=1.,elemental_frost_nova=1.8,legion_frost_nova=1.8,frostbolt=.8,frost_shard=.6)
UPGRADES=dict(arcane_tier_2_spell_2_modifier_2=.3,fire_tier_2_spell_1_modifier_1=.3,fire_tier_3_spell_1_modifier_2=.1,frost_tier_2_spell_1_modifier_1=.2)
def apply(name,f):
 def edit(path,fn):
  d=json.loads(f[path]);fn(d);f[path]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
 def spell(ns,id,fn):edit(f'data/{ns}/spell/{id}.json',fn)
 def damage(d,c):
  next(i['action']['damage'] for i in d['impacts'] if i['action']['type']=='DAMAGE')['spell_power_coefficient']=c
 if name.startswith('spell_power-'):
  edit('data/minecraft/tags/damage_type/bypasses_armor.json',lambda d:d.update(values=[v for v in d['values'] if v!='#spell_power:all']))
 if name.startswith('wizards-'):
  for id,c in RESTORE.items():spell('wizards',id,lambda d,c=c:damage(d,c))
  def text(d):
   normalized={'arcane_missile':26,'arcane_beam':20,'fire_breath':14,'fire_storm':40,'frost_blizzard':26.6667,'ancient_hydra_breath':24}
   import re
   for id,c in RESTORE.items():
    key=f'spell.wizards.{id}.description'
    if key in d:d[key]=re.sub(r'\d+(?:\.\d+)?%%',f'{normalized.get(id,round(c*100,4)):g}%%',d[key],count=1)
  edit('assets/wizards/lang/en_us.json',text)
 if name.startswith('Nameless'):
  edit('nameless-pack-balance.json',lambda d:(d['removed'].append('fractured_nullstone'),d['overrides'].pop('FracturedNullstone',None)))
  # The existing retirement policy removes registration/creative/loot eligibility.
  for n in list(f):
   if n.startswith('data/') and n.endswith('.json') and 'fractured_nullstone' in n:f.pop(n)
   elif n.startswith('data/') and '/tags/item/' in n and n.endswith('.json'):
    edit(n,lambda d:d.update(values=[v for v in d['values'] if (v if isinstance(v,str) else v.get('id'))!='nameless_trinkets:fractured_nullstone']))
 if name.startswith('combat_roll-'):
  edit('data/combat_roll/enchantment/multi_roll.json',lambda d:d['effects']['minecraft:attributes'][0].update(amount={'type':'minecraft:clamped','value':{'type':'minecraft:linear','base':1.,'per_level_above_first':1.},'min':0.,'max':2.}))
 if name.startswith('rogues-'):
  for id,v in [('throw',1.8),('throw_net',1.4)]:spell('rogues',id,lambda d,v=v:d['deliver']['projectile']['launch_properties'].update(velocity=v))
  spell('rogues','shock_powder',lambda d:d['cost']['cooldown'].update(duration=12))
  spell('rogues','bear_trap',lambda d:d['deliver']['clouds'][0].update(time_to_live_seconds=25.))
  for id in ['last_stand','mortal_strike']:
   spell('rogues',id,lambda d:(d['active']['cast'].update(duration=4.),d['cost']['cooldown'].update(duration=60)))
  spell('rogues','last_stand',lambda d:d.update(impacts=[i for i in d['impacts'] if i['action'].get('status_effect',{}).get('effect_id')!='minecraft:regeneration']))
  edit('assets/rogues/lang/en_us.json',lambda d:d.update({'spell.rogues.last_stand.description':'Each pulse adds 20%% maximum health, restores the health gained, and heals another 20%% of your maximum health. Resist knockback and gain 2%% damage reduction per pulse, up to 10%%.','spell.rogues.mortal_strike.description':d['spell.rogues.mortal_strike.description'].replace('stunned for 5s','stunned for 8s')}))
 if name.startswith('skill_tree-'):
  for id,c in UPGRADES.items():spell('skill_tree_rpgs',id,lambda d,c=c:damage(d,c))
  # Non-scaling secondary effects use native impact eligibility and splash targeting.
  def debuffs(d):
   for id in ['minecraft:weakness','minecraft:blindness']:
    d['impacts'].append({'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':id,'duration':3}}})
  spell('skill_tree_rpgs','rogue_tier_2_spell_1_modifier_1',debuffs)
  spell('skill_tree_rpgs','rogue_tier_1_passive_1',lambda d:d['impacts'].append({'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':'skill_tree_rpgs:coated_regeneration_lock','duration':8}}}))
  spell('skill_tree_rpgs','rogue_tier_3_spell_2_modifier_1',lambda d:d['modifiers'][0]['impacts'][0]['action']['status_effect'].update(duration=5))
  spell('skill_tree_rpgs','rogue_tier_3_spell_2_modifier_2',lambda d:d['modifiers'][0]['cloud'].update(radius_add=.5))
  spell('skill_tree_rpgs','rogue_tier_4_spell_2_modifier_2',lambda d:(d['modifiers'][0]['impacts'][0]['action']['status_effect'].update(duration=5),d['modifiers'][0]['impacts'].append({'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':'skill_tree_rpgs:challenged','duration':5}}})))
  def texts(d):
   descriptions={
    'rogue_tier_1_passive_1':'Hits can poison enemies and prevent natural regeneration and Regeneration for 8s.',
    'rogue_tier_1_passive_2':'Hits can reduce armor by 1 + 1 per 13 D per stack, up to 3 stacks, and deal physical damage equal to 5%% of the target\'s maximum health.',
    'rogue_tier_2_spell_1_modifier_1':'Shock Powder explodes for 40%% D and applies Weakness I and Blindness for 3s.',
    'rogue_tier_2_spell_1_modifier_2':'Shock Powder leaves smoke for 8s, granting you and allies inside 65%% evasion.',
    'rogue_tier_3_spell_2_modifier_1':'Bear Trap applies Bleed for 5s.',
    'rogue_tier_3_spell_2_modifier_2':'Bear Trap triggers within 1.5 blocks and catches enemies within 2 blocks.',
    'rogue_tier_4_spell_2_modifier_2':'Mutilate applies Bleed and prevents combat rolling for 5s.',
    'warrior_tier_1_passive_2':'Taking direct damage grants 1 armor and 1 maximum heart per stack, up to 10 stacks for 8s.',
    'warrior_tier_2_spell_1_modifier_2':'Wild Charge grants 8 total armor and complete knockback resistance.',
    'frost_tier_2_spell_1_modifier_1':'Frost Nova causes extra blasts that deal 20%% P.'}
   for id,s in descriptions.items():d[f'spell.skill_tree_rpgs.{id}.description']=s
   d['spell.skill_tree_rpgs.rogue_tier_1_passive_2.name']='Vital Hit'
   d['effect.skill_tree_rpgs.fracture']='Vital Hit'
   d['effect.skill_tree_rpgs.coated_regeneration_lock']='Regeneration Blocked'
   d['effect.skill_tree_rpgs.coated_regeneration_lock.description']='Natural regeneration and Regeneration are disabled.'
   d['effect.skill_tree_rpgs.aegis_guard']='Aegis Guard'
   d['effect.skill_tree_rpgs.aegis_guard.description']='15% physical and magic damage reduction.'
   d['effect.skill_tree_rpgs.fracture.description']='Reduced armor per stack: 1 + 1 per 13 dual-wield power.'
   d['spell.skill_tree_rpgs.arcane_tier_2_spell_2_modifier_1.description']='Arcane Explosion stores 2 charges and deals 160%% damage. Charges recover one at a time.'
   d['spell.skill_tree_rpgs.arcane_tier_2_spell_2_modifier_2.description']='Arcane Explosion causes another blast after 0.5s, dealing 30%% Arcane damage.'
   d['spell.skill_tree_rpgs.arcane_tier_3_spell_1_modifier_2.description']='Channel twice as long. The beam grows wider, and its damage rises from 10%% to 15%% per hit.'
   d['spell.skill_tree_rpgs.fire_tier_2_spell_1_modifier_1.description']='Fire Breath hits have a 10%% chance to explode for up to 30%% Fire damage.'
   d['spell.skill_tree_rpgs.fire_tier_3_spell_1_modifier_2.description']='Meteor impacts leave burning ground for 6s, dealing 10%% Fire damage per hit.'
   d['spell.skill_tree_rpgs.frost_tier_2_spell_2_modifier_2.description']='Frost Spikes adds two weaker side rows, each dealing 25%% Frost damage per hit.'
  edit('assets/skill_tree_rpgs/lang/en_us.json',texts)
  f['assets/skill_tree_rpgs/textures/mob_effect/coated_regeneration_lock.png']=f['assets/skill_tree_rpgs/textures/spell/rogue_tier_1_passive_1.png']
 if name.startswith('adventurerskills-'):
  base='data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
  nodes=json.loads(f[base+'skills.json']);defs=json.loads(f[base+'definitions.json']);edges=json.loads(f[base+'connections.json'])
  for key,d in defs.items():
   if 'magistrate' in key.lower() or d.get('title')=='Magistrate':
    for r in d.get('rewards',[]):
     if r.get('data',{}).get('attribute')=='puffish_attributes:magic_resistance':r['data']['value']=.15
    d['description']={'text':'+10% Magic Damage\n+5% Spell Haste\n+15% Magic Resistance'}
  # New reward definitions preserve every selected node's existing icon.
  attrs={'hp':('max_health','addition'),'armor':('armor','addition'),'res':('puffish_attributes:resistance','multiply_base'),'healing':('puffish_attributes:healing','multiply_base'),'knockback':('knockback_resistance','addition')}
  for pi,path in enumerate(DEFENSE):
   for i,(id,values) in enumerate(zip(path,VALUES[pi])):
    old=copy.deepcopy(defs[nodes[id]['definition']]);key='defense_v2_'+id;nodes[id]['definition']=key
    old.update(size=1,rewards=[{'type':'puffish_skills:attribute','data':{'attribute':attrs[k][0],'value':v,'operation':attrs[k][1]}} for k,v in values.items()])
    old['title']=['Aegis','Colossus','Bulwark'][pi] if i==7 else ', '.join(f'+{v:g} '+k if k in ['hp','armor'] else f'+{v*100:g}% '+('Resistance' if k=='res' else 'Healing Received' if k=='healing' else 'Knockback Resistance') for k,v in values.items())
    if i==7:old['description']={'text':['Survive a direct hit to gain 15% damage reduction for 3s. Cooldown: 20s.','A direct hit below 40% health restores 10% maximum health over 5s. Cooldown: 30s.','+2 Armor\n+4% Resistance\n+10% Knockback Resistance'][pi]}
    else:old.pop('description',None)
    defs[key]=old
  all_paths=[*DEFENSE,u.F,u.A,u.I];touched=set(sum(all_paths,[])+RETIRED)
  for kind in ['normal','exclusive']:
   for direction,items in edges.get(kind,{}).items():edges[kind][direction]=[e for e in items if not any(id in touched for id in e)]
  normal=edges['normal']['bidirectional'];exclusive=edges['exclusive']['bidirectional']
  for group,root in [(DEFENSE,'ad8qet8e75ys5sek'),([u.F,u.A,u.I],u.SHARED)]:
   for path in group:
    normal.append([root,path[0]])
    normal.extend(zip(path[:4],path[1:5]));normal.extend([[path[4],path[5]],[path[4],path[6]],[path[5],path[7]],[path[6],path[7]]])
    nodes[path[7]].pop('required_skills',None)
    defs[nodes[path[7]]['definition']]['required_skills']=2
   for i in range(3):
    for j in range(i):exclusive.append([group[i][0],group[j][0]])
  for id in RETIRED:nodes.pop(id)
  for path,icon,title,description in zip([u.F,u.A,u.I],['glistering_melon_slice','diamond_boots','writable_book'],['Fitness','Adaptability','Ingenuity'],['At 3 hunger drumsticks or less, fully restore hunger and saturation. Cooldown: 3 minutes.','Halve one damaging fall and resist powder snow for 5s. Cooldown: 20s.','Food and drink buffs last 35% longer. +1 Luck.']):
   center=nodes[path[7]]['x']
   for i,id in enumerate(path):
    nodes[id].update(x=center if i<5 or i==7 else center+(-40 if i==5 else 40),y=-204-56*i if i<5 else -484 if i<7 else -540)
    d=defs[nodes[id]['definition']];d['size']=1
    if isinstance(d.get('title'),str):d['title']=d['title'].replace('\n','; ').replace('\r','')
    stats=u.REWARDS.get(id,{})
    if i<7:
     if 'climb' in stats:d['title']='+12% Climb & Sneak Speed'
     else:
      labels={'stamina':'Stamina','food':'Food & Saturation','breath':'Air Supply','jump':'Jump','fall':'Safe Fall Block','freeze':'Slower Freezing','cost':'Anvil & Enchanting XP Cost','xp':'XP Gain','durability':'Tool Durability Preservation'}
      d['title']='; '.join((f'+{v:g} ' if k=='fall' else f'{"-" if k=="cost" else "+"}{v*100:g}% ')+labels[k] for k,v in stats.items() if k in labels)
   d=defs[nodes[path[7]]['definition']];d.update(title=title,icon={'type':'item','data':{'item':'minecraft:'+icon}},description={'text':description})
  for file,d in [('skills.json',nodes),('definitions.json',defs),('connections.json',edges)]:f[base+file]=(json.dumps(d,indent=2)+'\n').encode()
  contract={'paths':DEFENSE,'removed':RETIRED,'rewards':{id:v for p,values in zip(DEFENSE,VALUES) for id,v in zip(p,values)}}
  f['defense-v2.json']=(json.dumps(contract,indent=2)+'\n').encode()

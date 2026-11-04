import json,copy
def apply(name,f):
 def read(n):return json.loads(f[n])
 def write(n,d):f[n]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
 def spell(ns,id,fn):
  n=f'data/{ns}/spell/{id}.json';d=read(n);fn(d);write(n,d)
 def text(ns,values):
  n=f'assets/{ns}/lang/en_us.json';d=read(n);d.update(values);write(n,d)
 if name.startswith('spell_engine-'):
  # Snapshot one original native spike model, retaining its animations/texture.
  from pathlib import Path
  from zipfile import ZipFile
  with ZipFile(next((Path(__file__).resolve().parents[3]/'tmp/class-balance-phases-4-6/before').glob('wizards-*'))) as archive:
   spike=json.loads(archive.read('data/wizards/spell/frost_spikes.json'))
  model=copy.deepcopy(spike['deliver']['clouds'][0]['spawn']['visuals']['models'][0])
  for transform in model.get('initial',[]):
   if transform['operation']=='translate':
    for axis in ['x','y','z']:transform[axis]*=3
  for animation in model.get('animations',[]):
   for axis in ['x','y','z']:
    if axis in animation:animation[axis]*=3
  write('mage-followup-models.json',{'spike':[model]})
 if name.startswith('wizards-'):
  spell('wizards','arcane_evocation',lambda d:d['active']['cast'].update(duration=3))
  spell('wizards','arcane_barrage_bolt',lambda d:d['deliver']['projectile'].setdefault('launch_properties',{}).update(velocity=.8))
  for id,base in [('fireball',1.),('fire_blast',1.25),('frostbolt',1.2),('frost_shard',1.2)]:
   def projectile(d,base=base):
    p=d['deliver']['projectile'];p.setdefault('launch_properties',{})['velocity']=base*1.3;p['projectile']['homing_angle']=1.5
   spell('wizards',id,projectile)
  for id in ['arcane_bolt','arcane_blast']:spell('wizards',id,lambda d:d.update(range=24))
  spell('wizards','frost_lance',lambda d:(d['active']['cast'].update(duration=.75),d['active']['cast']['charge']['bonus']['projectile_launch'].update(velocity=.8),d['deliver']['projectile']['launch_properties'].update(velocity=1.6),d['deliver']['projectile']['projectile'].update(homing_angle=1.5)))
  def targetSpike(d):
   d.update(range=24,target={'type':'AIM','aim':{'required':True}},deliver={'type':'CUSTOM','custom':{'handler':'spell_engine:targeted_frost_spike'}})
   d['active']['cast']={'duration':.2,'haste_affected':True,'animation':{'id':'spell_engine:one_handed_area_charge'}}
   d['impacts'][1]['action']['status_effect'].update(duration=1,amplifier=0,amplifier_cap=0,apply_mode='SET')
  spell('wizards','frost_spikes',targetSpike)
  # Hidden registered helpers provide native impacts/party rules without row/cloud damage.
  spike=read('data/wizards/spell/frost_spikes.json')
  for id,coefficient,duration,radius in [('frost_fan_burst',.2,.5,3)]:
   d=copy.deepcopy(spike);d.update(type='PASSIVE',tier=0,range=radius,group='',target={'type':'AREA','area':{'distance_dropoff':'NONE'}},deliver={'type':'DIRECT'},area_impact={'radius':radius,'area':{'distance_dropoff':'NONE'}},cost={'cooldown':{'duration':0},'durability':0,'exhaust':0});d.pop('active',None);d.pop('learn',None)
   d['impacts'][0]['action']['damage']['spell_power_coefficient']=coefficient;d['impacts'][1]['action']['status_effect']['duration']=duration
   write(f'data/wizards/spell/{id}.json',d)
  spell('wizards','frost_shield_burst',lambda d:(d['impacts'].insert(0,{'action':{'type':'DAMAGE','damage':{'spell_power_coefficient':.3,'knockback':0}}}),next(i['action']['status_effect'] for i in d['impacts'] if i['action']['type']=='STATUS_EFFECT').update(duration=1)))
  text('wizards',{'spell.wizards.frost_spikes.description':'Erupt an ice spike beneath your target after 0.25s, dealing 80%% P within 2 blocks and freezing enemies for 1s.','spell.wizards.arcane_evocation.description':'Channel 10 pulses over 3s, gaining critical chance and spell haste with each pulse.','effect.wizards.arcane_evocation.description':'Increased spell critical chance and spell haste.','spell.wizards.frost_shield.description':'Block attacks while shielded. When the shield expires, nearby enemies take 30%% P and are frozen for 1s.'})
 if name.startswith('paladins-'):
  for id in ['heal','holy_shock']:spell('paladins',id,lambda d:d.update(range=24))
 if name.startswith('skill_tree-'):
  def fissile(d):
   d['passive']['triggers'][0]['chance']=1;d['impacts'][0]['action']['allow_on_center_target']=True;d['impacts'][0]['action']['damage']['spell_power_coefficient']=.3;d['cost']['cooldown']['duration']=6
  spell('skill_tree_rpgs','arcane_tier_1_passive_1',fissile)
  spell('skill_tree_rpgs','arcane_tier_4_spell_1_modifier_2',lambda d:d.update(modifiers=[{'spell_pattern':'wizards:arcane_blink'}]))
  def presence(d):
   d['cost']['cooldown']['duration']=15;d['passive']['triggers']=[{'type':'SPELL_CAST','chance':1,'spell':{'id':f'wizards:{id}'}} for id in ['arcane_missile','arcane_explosion','arcane_beam','arcane_barrage','arcane_blink','arcane_evocation']]
  spell('skill_tree_rpgs','arcane_tier_3_passive_1',presence)
  spell('skill_tree_rpgs','fire_tier_1_passive_2',lambda d:d['passive']['triggers'][0].update(chance=1))
  def whirlpool(d):
   for impact in d['modifiers'][0]['impacts']:
    if impact['action']['type']=='STATUS_EFFECT':impact['action']['status_effect'].update(duration=1,amplifier=0)
    if impact['action']['type']=='VELOCITY':impact['action']['velocity']['push']['z']=-.9
  spell('skill_tree_rpgs','fire_tier_3_spell_2_modifier_1',whirlpool)
  spell('skill_tree_rpgs','fire_tier_3_passive_1',lambda d:(d.update(target={'type':'FROM_TRIGGER'},area_impact={'radius':5,'area':{'distance_dropoff':'SQUARED'}}),d['impacts'][0]['action']['damage'].update(knockback=0)))
  spell('skill_tree_rpgs','fire_tier_3_passive_2',lambda d:d['impacts'][0]['action']['damage'].update(spell_power_coefficient=.2))
  # Native stash retaliation is replaced by a shield-lifetime event with one shared clock.
  spell('skill_tree_rpgs','fire_tier_3_passive_2',lambda d:[trigger.update(chance=0) for trigger in d['deliver']['stash_effect']['triggers']])
  spell('skill_tree_rpgs','frost_tier_2_spell_1_modifier_1',lambda d:d['impacts'].append({'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':'minecraft:slowness','duration':4,'amplifier':2}}}))
  spell('skill_tree_rpgs','frost_tier_2_spell_1_modifier_2',lambda d:d.update(modifiers=[{'spell_pattern':'wizards:frost_nova','effect_duration_add':1}]))
  for id in ['frost_tier_2_spell_2_modifier_1','frost_tier_2_spell_2_modifier_2']:
   spell('skill_tree_rpgs',id,lambda d:d.update(modifiers=[{'spell_pattern':'wizards:frost_spikes'}]))
  spell('skill_tree_rpgs','frost_tier_3_spell_2_modifier_1',lambda d:d['modifiers'][0].pop('power_modifier',None))
  def winter(d):
   d['passive']['triggers']=[{'type':'SPELL_CAST','chance':1,'spell':{'id':'wizards:'+id}} for id in ['frost_nova','frost_spikes','frost_shield','frost_lance','frost_blizzard','frost_elemental']]
   d.update(target={'type':'CASTER'});d['cost']['cooldown']['duration']=0;d['impacts']=[{'action':{'type':'STATUS_EFFECT','apply_to_caster':True,'status_effect':{'effect_id':'skill_tree_rpgs:winters_chill_ready','duration':10}}}]
  spell('skill_tree_rpgs','frost_tier_1_passive_1',winter)
  spell('skill_tree_rpgs','frost_tier_1_passive_2',lambda d:d['impacts'][0]['action']['status_effect'].update(duration=5,amplifier=0,apply_mode='SET'))
  values={
   'arcane_tier_1_passive_1':'Arcane hits explode for 30%% P, including the original target.',
   'arcane_tier_4_spell_1_modifier_2':'Store 2 shorter Blinks. Each travels 7.5 blocks and blasts enemies within 2.5 blocks for 20%% P at both ends.',
   'arcane_tier_3_passive_1':'Arcane class spells ready your next Arcane spell to make enemies take 20%% more Arcane damage for 6s.',
   'arcane_tier_3_passive_2':'Gain an absorption shield and blink 4 blocks away from your attacker when a safe landing exists.',
   'fire_tier_1_passive_2':'Fire hits stun enemies for 2s.',
   'fire_tier_3_passive_1':'Your attacker erupts for 30%% P, launching nearby enemies upward.',
   'fire_tier_3_passive_2':'While your ward has absorption remaining, attackers take 20%% P, at most once every 0.5s.',
   'frost_tier_1_passive_1':'Casting a Frost class skill readies your next Frost staff or wand spell for 10s. Its hit freezes the target for 0.5s.',
   'frost_tier_1_passive_2':'A damaging Frost spell freezes its target for 5s, replacing its usual freeze duration.',
   'frost_tier_2_spell_1_modifier_1':'Frost Nova adds blasts dealing 20%% P and Slowness III for 4s.',
   'frost_tier_2_spell_1_modifier_2':'Frost Nova freezes enemies 1s longer.',
   'frost_tier_2_spell_2_modifier_1':'Frost Spikes erupts instantly and deals 100%% P.',
   'frost_tier_2_spell_2_modifier_2':'The spike explodes after 1s, dealing 20%% P and freezing enemies within 3 blocks for 0.5s.',
   'frost_tier_3_spell_2_modifier_1':'Ice Lance grows larger and ignores 30%% of enemy armor.'}
  text('skill_tree_rpgs',{**{f'spell.skill_tree_rpgs.{id}.description':v for id,v in values.items()},'spell.skill_tree_rpgs.arcane_tier_4_spell_1_modifier_2.name':'Blitz','spell.skill_tree_rpgs.frost_tier_1_passive_2.name':'Near Zero','effect.skill_tree_rpgs.winters_chill_ready':'Winter\'s Chill','effect.skill_tree_rpgs.winters_chill_ready.description':'Your next Frost staff or wand spell freezes for 0.5s.','effect.skill_tree_rpgs.frostbite_exposure':'Frostbite','effect.skill_tree_rpgs.frostbite_exposure.description':'Fully frozen by damaging cold exposure.'})
  f['assets/skill_tree_rpgs/textures/mob_effect/winters_chill_ready.png']=f['assets/skill_tree_rpgs/textures/spell/frost_tier_1_passive_1.png']
  f['assets/skill_tree_rpgs/textures/mob_effect/frostbite_exposure.png']=f['assets/skill_tree_rpgs/textures/spell/frost_tier_1_passive_2.png']

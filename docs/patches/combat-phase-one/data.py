"""Narrow data overlay. The builder supplies exact current archive contents."""
import json
MAGE={'arcane_missile':.63,'arcane_explosion':2.1,'arcane_beam':.8,'arcane_bolt':.56,'arcane_blast':.64,'arcane_barrage_bolt':.32,'burst_blink_explosion':.4,'fire_breath':.5,'fire_slash':.8,'fire_meteor':1.2,'fire_storm':.512,'fire_wall':.16,'hydra_fireball':.44,'ancient_hydra_breath':.95,'fire_scorch':.48,'fireball':.64,'fire_blast':.8,'frost_nova':1.45,'frost_spikes':.64,'frost_lance':1.6,'frost_blizzard':.315,'elemental_frost_shard':.8,'elemental_frost_nova':1.45,'legion_frost_nova':1.45,'frostbolt':.64,'frost_shard':.48}
UPGRADES={'arcane_tier_2_spell_2_modifier_2':.24,'fire_tier_2_spell_1_modifier_1':.24,'fire_tier_3_spell_1_modifier_2':.08,'frost_tier_2_spell_1_modifier_1':.16}
def apply(name,f):
 def edit(path,fn):
  d=json.loads(f[path]);fn(d);f[path]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 def spell(ns,id,fn):edit(f'data/{ns}/spell/{id}.json',fn)
 if name.startswith('wizards-'):
  for id,c in MAGE.items():spell('wizards',id,lambda d,c=c:next(i['action']['damage'] for i in d['impacts'] if i['action']['type']=='DAMAGE').update(spell_power_coefficient=c))
  for id in ['arcane_missile','arcane_explosion']:
   spell('wizards',id,lambda d:(d.update(range=40),d['cost']['cooldown'].update(duration=12,proportional=False)))
  def wizard_text(d):
   for id,old,new in [('arcane_barrage',40,32),('arcane_beam',20,16),('arcane_blast',80,64),('arcane_blink',50,40),('arcane_bolt',70,56),('arcane_explosion',260,210),('arcane_missile',26,21),('fire_blast',100,80),('fire_breath',13,10),('fire_meteor',150,120),('fire_scorch',60,48),('fire_slash',100,80),('fire_storm',40,32),('fire_wall',20,16),('fireball',80,64),('frost_lance',200,160),('frost_nova',180,145),('frost_shard',60,48),('frost_spikes',80,64),('frostbolt',80,64),('arcane_barrage_bolt',40,32),('burst_blink_explosion',50,40),('elemental_frost_shard',100,80),('elemental_frost_nova',180,145),('legion_frost_nova',180,145),('ancient_hydra_breath',24,19),('hydra_fireball',55,44)]:
    key=f'spell.wizards.{id}.description';assert f'{old}%%' in d[key],key;d[key]=d[key].replace(f'{old}%%',f'{new}%%')
   d['spell.wizards.frost_blizzard.description']='Rain ice shards around your target, each dealing 21%% Frost damage and slowing enemies.'
  edit('assets/wizards/lang/en_us.json',wizard_text)
 if name.startswith('paladins-'):
  spell('paladins','flash_heal',lambda d:d.update(range=30))
  for id,t in [('blessed_strikes',1.5),('judgement',.25)]:spell('paladins',id,lambda d,t=t:d['active']['cast'].update(duration=t))
  spell('paladins','immolation',lambda d:d['cost']['cooldown'].update(duration=12))
 if name.startswith('rogues-'):
  for id in ['throw','throw_net']:spell('rogues',id,lambda d:d['active']['cast'].update(duration=.3))
 if name.startswith('skill_tree-'):
  for id,c in UPGRADES.items():spell('skill_tree_rpgs',id,lambda d,c=c:d['impacts'][0]['action']['damage'].update(spell_power_coefficient=c))
  spell('skill_tree_rpgs','arcane_tier_2_spell_2_modifier_1',lambda d:(d.update(impacts=[]),d['passive'].update(triggers=[])))
  for id in ['arcane_tier_2_passive_1','arcane_tier_2_passive_2','fire_tier_2_passive_1','frost_tier_2_passive_1']:
   spell('skill_tree_rpgs',id,lambda d:d['cost']['cooldown'].update(duration=4,haste_affected=True))
  spell('skill_tree_rpgs','arcane_tier_2_passive_1',lambda d:next(i['action']['status_effect'] for i in d['impacts'] if i['action']['type']=='STATUS_EFFECT').update(amplifier=0))
  for school in ['arcane','fire','frost']:
   def ward(d):
    d['cost']['cooldown'].update(duration=15,haste_affected=True)
    se=d['impacts'][0]['action']['status_effect'] if school=='arcane' else d['deliver']['stash_effect']
    se['amplifier_power_multiplier']=.125
   spell('skill_tree_rpgs',school+'_tier_3_passive_2',ward)
  for category in ['class_skills','weapon_skills']:
   def nodes(d):
    for key in ['warrior_root','warrior_boost','rogue_root','rogue_boost']:
     v=d[key];r=v['rewards'][0]['data'];r['value']=.04 if key.startswith('warrior') else .02
     v['description']['text']='+4% Attack Damage' if key.startswith('warrior') else '+2% Attack Speed'
   edit(f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json',nodes)
  def tree_text(d):
   text={
    'arcane_tier_2_spell_2_modifier_1':'Arcane Explosion stores 2 charges and deals 130%% damage. Charges recover one at a time.',
    'arcane_tier_2_spell_2_modifier_2':'Arcane Explosion causes another blast after 0.5s, dealing 24%% Arcane damage.',
    'arcane_tier_3_spell_1_modifier_2':'Channel twice as long. The beam grows wider, and its damage rises from 8%% to 12%% per hit.',
    'arcane_tier_2_passive_1':'Leave a trap that deals 40%% Arcane damage and applies Weakness I for 6s.',
    'arcane_tier_3_passive_2':'Gain an 8s shield with 1 absorption heart per 8 Arcane power.',
    'fire_tier_3_passive_2':'Gain an 8s shield with 1 absorption heart per 8 Fire power. Attackers take 30%% Fire damage.',
    'frost_tier_3_passive_2':'Gain an 8s shield with 1 absorption heart per 8 Frost power. Slows attackers.',
    'fire_tier_2_spell_1_modifier_1':'Fire Breath hits have a 10%% chance to explode for up to 24%% Fire damage.',
    'fire_tier_3_spell_1_modifier_2':'Meteor impacts leave burning ground for 6s, dealing 8%% Fire damage per hit.',
    'frost_tier_2_spell_1_modifier_1':'Frost Nova causes extra blasts that deal 16%% Frost damage.',
    'frost_tier_2_spell_2_modifier_2':'Frost Spikes adds two weaker side rows, each dealing 20%% Frost damage per hit.',
    'paladin_tier_2_passive_1':'Rolling readies retaliation for 6s. The next attacker takes 15%% more damage for 8s and loses 1 HP every 0.5s for 4s.'}
   for id,text in text.items():d[f'spell.skill_tree_rpgs.{id}.description']=text
  edit('assets/skill_tree_rpgs/lang/en_us.json',tree_text)
 if name.startswith('adventurerskills-'):
  def adventure(d):
   v=d['melee_damage+3%'];assert v['rewards'][0]['data']['value']==.02
   v['title']='+3% Melee Damage';v['rewards'][0]['data']['value']=.03
  edit('data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json',adventure)

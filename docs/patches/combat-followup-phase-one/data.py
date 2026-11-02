import json,copy
def apply(name,f):
 def edit(key,fn):
  d=json.loads(f[key]);fn(d);f[key]=(json.dumps(d,indent=2,ensure_ascii=False)+'\n').encode()
 def spell(ns,id,fn):edit(f'data/{ns}/spell/{id}.json',fn)
 if name.startswith('rogues-'):
  for id in ['throw','throw_net']:spell('rogues',id,lambda d:d['deliver']['projectile'].setdefault('launch_properties',{}).update(velocity=1.6))
  def slow(d):d['impacts'][1]['action']['status_effect'].update(amplifier=3,duration=4)
  spell('rogues','throw',slow)
  def mutilate(d):
   d.update(range=8);d.pop('range_mechanic',None);d['active']['cast'].update(duration=.25,haste_affected=True);d['target']={'type':'AIM','aim':{'required':True,'sticky':True}};d['deliver']['melee']['attacks'][0]['delay']=0
  spell('rogues','mutilate',mutilate)
  text={
   'throw':'Throw your weapon for 40–80%% Melee Damage, slowing enemies by Slowness IV for 4s. Briefly weakens your melee attacks.',
   'mutilate':'Teleport in front of your enemy and strike with both weapons.',
   'charge':'Surge forward with 50%% movement speed, 4 armor and increased knockback resistance. Break free of movement impairments. Melee attacks no longer slow you or interrupt sprinting.',
   'shout':'Weaken and reveal nearby enemies for 6s, reducing melee, ranged and magic power by 20%%. Break free of movement impairments and resist them for 3s.',
   'mortal_strike':'Each pulse fully heals you and builds attack speed, basic melee damage and knockback resistance, but increases damage taken. Melee attacks no longer slow you or interrupt sprinting. Fully heals on expiry or fatal-hit rescue; rescue puts early skills on cooldown and leaves you stunned for 5s and nauseated for 10s.',
   'last_stand':'Build maximum health and 2%% damage resistance per pulse, up to 10%%. Regenerate health and resist knockback. The final pulse fully heals you.'}
  def lang(d):
   for id,v in text.items():d[f'spell.rogues.{id}.description']=v
   d['effect.rogues.demoralize.description']='Melee, ranged and magic power reduced by 20%.'
   d['effect.rogues.charge.description']='50% more movement speed, 4 armor and 25% knockback resistance. Attack freely while sprinting.'
  edit('assets/rogues/lang/en_us.json',lang)
 if name.startswith('skill_tree-'):
  spell('skill_tree_rpgs','warrior_tier_2_spell_2_modifier_2',lambda d:d['modifiers'][0].update(effect_duration_add=0))
  spell('skill_tree_rpgs','warrior_tier_2_passive_1',lambda d:d['cost']['cooldown'].update(duration=1))
  def momentum(d):d['deliver']['stash_effect']['duration']=8;d['impacts'][0]['action']['status_effect']['duration']=5
  spell('skill_tree_rpgs','warrior_tier_2_passive_2',momentum)
  spell('skill_tree_rpgs','warrior_tier_3_passive_2',lambda d:d['cost']['cooldown'].update(duration=30))
  spell('skill_tree_rpgs','warrior_tier_3_spell_2_modifier_2',lambda d:d['modifiers'][0]['impacts'][0]['action']['damage'].update(spell_power_coefficient=.2))
  spell('skill_tree_rpgs','warrior_tier_4_spell_2_modifier_2',lambda d:d['impacts'][1]['action']['status_effect'].update(amplifier=1))
  # Replace old next-hit stash with an inert selected-node marker; proximity is owned by ClassRework.
  spell('skill_tree_rpgs','warrior_tier_3_spell_1_modifier_2',lambda d:(d.update(type='MODIFIER',modifiers=[],impacts=[]),d.pop('deliver',None),d.pop('passive',None)))
  f['data/skill_tree_rpgs/spell/assassin_stride.json']=(json.dumps({'type':'MODIFIER','school':'spell_power:physical_melee','tier':1,'modifiers':[],'tooltip':{'show_activation':False,'show_range':False}},indent=2)+'\n').encode()
  for key in list(f):
   if key.startswith('data/skill_tree_rpgs/puffish_skills/categories/') and key.endswith('/definitions.json'):
    def nodes(d):
     for id in ['warrior_root','warrior_boost']:
      if id in d:d[id]['rewards'][0]['data']['value']=.05;d[id]['description']['text']='+5% Attack Damage'
     if 'rogue_root' in d:
      reward=copy.deepcopy(d['rogue_tier_1_passive_1']['rewards'][0]);reward['data']['containers'][0]['spell_ids']=['skill_tree_rpgs:assassin_stride'];d['rogue_root']['rewards'].append(reward)
      d['rogue_root']['description']['text']='+2% Attack Speed. Melee attacks no longer slow you or interrupt sprinting.'
    edit(key,nodes)
  text={
   'warrior_tier_2_spell_1_modifier_1':'Throw Weapon hits up to 2 enemies and inflicts Slowness V for 4s.',
   'warrior_tier_2_spell_1_modifier_2':'Throw Weapon deals up to 100%% Melee Damage, slows for 5s and stuns for 0.5s.',
   'warrior_tier_2_spell_2_modifier_2':'Throw Net recovers 2s faster and roots enemies for 2s.',
   'warrior_tier_1_passive_1':'Hits build 2%% melee damage per stack, up to 10%%, for 8s and restore 1 HP. Kills restore 4%% maximum health.',
   'warrior_tier_1_passive_2':'Direct hits grant 2 armor and 1 maximum heart per stack, up to 10 stacks for 8s. Newly added hearts are healed.',
   'warrior_tier_3_spell_1_modifier_1':'Wild Charge grants 6 armor and immunity to knockback.',
   'warrior_tier_3_spell_1_modifier_2':'During Wild Charge, enemies within 1.5 blocks are stunned for 2s, once per enemy.',
   'warrior_tier_3_spell_2_modifier_1':'Demoralizing Shout grants you and party allies 12%% more melee, ranged and magic power for 6s.',
   'warrior_tier_3_spell_2_modifier_2':'Deals 20%% Melee Damage, draws enemy aggression and briefly stuns. Reveals you and prevents affected enemies from rolling for 6s.',
   'warrior_tier_2_passive_1':'Rolling restores 5%% of your maximum health.',
   'warrior_tier_2_passive_2':'Rolling prepares 5 melee hits for 8s. Each hit grants 3%% attack and movement speed, up to 15%%, for 5s.',
   'warrior_tier_4_spell_1_modifier_1':'Berserker grants 6%% critical chance and critical damage per pulse, up to 30%% each.',
   'warrior_tier_4_spell_1_modifier_2':'Melee hits restore health during Berserker. Healing increases with your Melee Power.',
   'warrior_tier_4_spell_2_modifier_1':'Last Stand increases size and grants 4%% damage resistance per pulse, up to 20%%.',
   'warrior_tier_4_spell_2_modifier_2':'Direct hits during Last Stand build 2%% melee damage per stack, up to 20%%, and refresh Regeneration II for 5s.',
   'warrior_tier_3_passive_1':'As health falls, gain up to 18%% melee damage and 24%% size. Bonuses fade as you recover.'}
  def lang(d):
   for id,v in text.items():d[f'spell.skill_tree_rpgs.{id}.description']=v
   for id,title,desc in [('challenged','Challenged','Cannot combat roll.'),('assassin_stride','Assassin Stride','Melee attacks no longer slow you or interrupt sprinting.')]:
    d[f'effect.skill_tree_rpgs.{id}']=title;d[f'effect.skill_tree_rpgs.{id}.description']=desc;d[f'spell.skill_tree_rpgs.{id}.name']=title
   d['effect.skill_tree_rpgs.vitality.description']='2 armor and 1 maximum heart per stack.'
   d['effect.skill_tree_rpgs.berserker_crit.description']='6% more critical chance and critical damage per stack.'
   d['effect.skill_tree_rpgs.sentinel.description']='2% more melee damage per stack.'
   d['effect.skill_tree_rpgs.warrior_momentum.description']='3% more attack speed and movement speed per stack.'
  edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
  from pathlib import Path
  from zipfile import ZipFile
  with ZipFile(next((Path(__file__).resolve().parents[3]/'tmp/combat-followup-phase-one/before').glob('rogues-*'))) as z:f['assets/skill_tree_rpgs/textures/mob_effect/challenged.png']=z.read('assets/rogues/textures/spell/shout.png')
 if name.startswith('adventurerskills-'):
  def nodes(d):v=d['melee_damage+3%'];v['title']='+2% Melee Damage';v['rewards'][0]['data']['value']=.02
  edit('data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json',nodes)

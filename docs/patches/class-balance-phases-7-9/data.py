import json
def apply(name,f):
 def spell(ns,id,fn):
  n=f'data/{ns}/spell/{id}.json';d=json.loads(f[n]);fn(d);f[n]=(json.dumps(d,indent=2)+'\n').encode()
 def cd(ns,id,value):spell(ns,id,lambda d:d['cost']['cooldown'].update(duration=value))
 def lang(ns,changes):
  n=f'assets/{ns}/lang/en_us.json';d=json.loads(f[n]);d.update(changes);f[n]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
 if name.startswith('paladins-'):
  for id,value in [('lightwell',60),('barrier',40),('divine_protection',20),('judgement',18),('immolation',16)]:cd('paladins',id,value)
  spell('paladins','circle_of_healing',lambda d:d['impacts'][0]['action']['heal'].update(spell_power_coefficient=.6))
  spell('paladins','levitate',lambda d:d['active']['cast'].update(haste_affected=False))
  lang('paladins',{'spell.paladins.levitate.description':'Rise and repel enemies, building up to 24%% offensive power for you and nearby allies. Gain 50%% damage reduction for 6s, refreshed by each pulse.','spell.paladins.immolation.description':'Channel a binding dome of holy fire. Later stages strengthen damage and ally support while consuming your health. Finish the final stage for a devastating explosion.'})
 if name.startswith('archers-'):
  cd('archers','entangling_roots',8)
  spell('archers','entangling_roots',lambda d:d['deliver']['clouds'][0].update(time_to_live_seconds=4.5))
  spell('archers','entangling_roots',lambda d:d['impacts'].append({'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':'skill_tree_rpgs:challenged','duration':1}}}))
  spell('archers','magic_arrow',lambda d:d['deliver']['projectile']['launch_properties'].update(velocity=1.95))
  spell('archers','barrage',lambda d:d['arrow_perks'].update(damage_multiplier=.7))
  lang('archers',{'spell.archers.entangling_roots.description':'Create a slowing field that briefly prevents enemies from rolling. Maintain up to 2 fields.','spell.archers.barrage.description':'Fire a rapid volley of arrows, each dealing 70%% of a normal shot\'s damage.'})
 if name.startswith('skill_tree-'):
  spell('skill_tree_rpgs','paladin_tier_1_passive_2',lambda d:d['impacts'][0]['action']['status_effect'].update(amplifier_cap=4))
  spell('skill_tree_rpgs','priest_tier_2_passive_1',lambda d:[i['action']['status_effect'].update(duration=2) for i in d['impacts']])
  cd('skill_tree_rpgs','archer_tier_1_passive_1',4)
  cd('skill_tree_rpgs','archer_tier_3_passive_2',30)
  cd('skill_tree_rpgs','archer_tier_2_passive_2',8)
  spell('skill_tree_rpgs','archer_tier_2_passive_2',lambda d:d['impacts'][0]['action']['status_effect'].update(effect_id='skill_tree_rpgs:onslaught_ready',duration=5))
  lang('skill_tree_rpgs',{
   'spell.skill_tree_rpgs.paladin_tier_3_passive_2.name':'Being of Purelight',
   'spell.skill_tree_rpgs.paladin_tier_3_passive_2.description':'Survive a fatal hit at full health. Then lose 1 HP every 0.5s, bypassing absorption, until downed or dead.',
   'spell.skill_tree_rpgs.paladin_tier_1_passive_1.description':'Empowered melee hits deal 20%% more damage and stun for 1s, or 2s with a shield. A successful stun grants 15%% damage reduction for 2s, or 25%% with a shield.',
   'spell.skill_tree_rpgs.paladin_tier_1_passive_2.description':'Gain 5%% armor and Healing Power. Taking hits adds up to 5 stacks of 2%%, or 3%% with a shield, for 5s.',
   'spell.skill_tree_rpgs.priest_tier_3_spell_1_root.description':'Circle of Healing heals for 75%% P.',
   'spell.skill_tree_rpgs.priest_tier_2_spell_2_modifier_1.description':'Also lift nearby allies and grant them 50%% damage reduction for 6s, refreshed each pulse.',
   'spell.skill_tree_rpgs.priest_tier_2_spell_2_modifier_2.description':'Each pulse grants absorption to you and allies within 8 blocks, up to 4 stacks.',
   'spell.skill_tree_rpgs.priest_tier_2_passive_1.description':'Lose pursuers and vanish completely for 2s, hiding armor and held items.',
   'spell.skill_tree_rpgs.priest_tier_2_passive_2.description':'Ready a counter for 15s. The next damaging attack stuns its attacker and nearby enemies for 3s.',
   'spell.skill_tree_rpgs.archer_tier_1_passive_2.description':'Arrow hits grant 4%% draw speed per stack, up to 20%% for 4s.',
   'spell.skill_tree_rpgs.archer_tier_2_passive_2.description':'Rolling readies your next direct hit for 5s. Landing it grants 30%% draw speed and 15%% ranged damage for 4s.',
   'spell.skill_tree_rpgs.archer_tier_3_passive_2.description':'At low health, block 3 bursts of direct damage. Each block protects for 0.25s and grants 30%% movement speed for 2s.'})
  for effect,icon in [('ascension_guard','priest_tier_2_spell_2_root'),('vanguard_guard','paladin_tier_1_passive_1'),('break_knockback','paladin_tier_2_passive_2'),('purgatory_opening','paladin_tier_4_spell_2_root'),('purelight_cost','paladin_tier_3_passive_2'),('onslaught_ready','archer_tier_2_passive_2'),('onslaught_power','archer_tier_2_passive_2'),('deflection_speed','archer_tier_3_passive_2')]:
   key=f'assets/skill_tree_rpgs/textures/spell/{icon}.png'
   if key not in f:key='assets/skill_tree_rpgs/textures/mob_effect/'+('purgatory' if effect=='purgatory_opening' else 'ascension_power')+'.png'
   f[f'assets/skill_tree_rpgs/textures/mob_effect/{effect}.png']=f[key]

  lang('skill_tree_rpgs',{
   'effect.skill_tree_rpgs.ascension_guard':'Ascension', 'effect.skill_tree_rpgs.ascension_guard.description':'50% general damage reduction.',
   'effect.skill_tree_rpgs.vanguard_guard':'Vanguard', 'effect.skill_tree_rpgs.vanguard_guard.description':'15% general damage reduction; 25% when activated with a shield.',
   'effect.skill_tree_rpgs.break_knockback':'Break Restriction', 'effect.skill_tree_rpgs.break_knockback.description':'Immune to knockback.',
   'effect.skill_tree_rpgs.purgatory_opening':'Purgatory', 'effect.skill_tree_rpgs.purgatory_opening.description':'+30% armor.',
   'effect.skill_tree_rpgs.purelight_cost':'Being of Purelight', 'effect.skill_tree_rpgs.purelight_cost.description':'Lose 1 HP every 0.5s, bypassing absorption, until downed or dead.',
   'effect.skill_tree_rpgs.onslaught_ready':'Onslaught Ready', 'effect.skill_tree_rpgs.onslaught_ready.description':'Your next direct hit activates Onslaught.',
   'effect.skill_tree_rpgs.onslaught_power':'Onslaught', 'effect.skill_tree_rpgs.onslaught_power.description':'+30% draw speed and +15% ranged damage.',
   'effect.skill_tree_rpgs.deflection_speed':'Deflection', 'effect.skill_tree_rpgs.deflection_speed.description':'Movement speed multiplied by 1.30.',
   'effect.skill_tree_rpgs.deflection.description':'Block direct physical or magic attacks in 0.25s bursts.'})

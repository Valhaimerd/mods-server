from compile import *
from zipfile import ZipFile
import hashlib
out=W/'built';out.mkdir(exist_ok=True);records=[]
def patch(blob,mode,count):
 (W/'in.class').write_bytes(blob);subprocess.run([str(J/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchFollowUp',str(W/'in.class'),str(W/'out.class'),mode,str(count)],check=True);return (W/'out.class').read_bytes()
def enc(d):return (json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
for source in (W/'before').glob('*.jar'):
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old)
 def edit(n,fn):d=json.loads(f[n]);fn(d);f[n]=enc(d)
 if source.name.startswith('skill_tree-'):
  for n in list(f):
   if n.endswith('/definitions.json') and '/puffish_skills/categories/' in n:
    d=json.loads(f[n])
    if 'archer_root' in d:d['archer_root']['description']={'text':'+2% Ranged Damage. Closely aimed arrows gently correct near misses.'};f[n]=enc(d)
  for s,v in [('paladin_tier_1_passive_1',5),('paladin_tier_3_passive_1',80),('archer_tier_1_passive_1',6),('priest_tier_2_passive_2',12),('fire_tier_2_passive_2',5)]:
   edit('data/skill_tree_rpgs/spell/'+s+'.json',lambda d,v=v:d['cost'].update(cooldown={'duration':v,'haste_affected':True}))
  edit('data/skill_tree_rpgs/spell/paladin_tier_3_passive_1.json',lambda d:d.update(school='spell_power:healing'))
  edit('data/skill_tree_rpgs/spell/archer_tier_3_passive_1.json',lambda d:d['arrow_perks'].update(damage_multiplier=1.35))
  def fade(d):
   d['range']=0;d['target']={'type':'NONE'};d['deliver']={'type':'CUSTOM','custom':{'handler':'class_pack:fade'}}
   d['impacts']=[i for i in d['impacts'] if i['action']['type']=='STATUS_EFFECT']
   for i in d['impacts']:i['action']['status_effect']['duration']=1
  edit('data/skill_tree_rpgs/spell/priest_tier_2_passive_1.json',fade)
  def op(d):d['deliver']={'type':'CUSTOM','custom':{'handler':'class_pack:opportunist'}};d['impacts']=[];d['target']={'type':'NONE'}
  edit('data/skill_tree_rpgs/spell/rogue_tier_2_passive_2.json',op)
  def words(d):
   for k,v in list(d.items()):
    if isinstance(v,str):d[k]=v.replace('Momentum','Onslaught')
   for k,v in {
    'archer_tier_3_passive_1':'Your next normal arrow deals 35%% more damage. Use within 5s.',
    'warrior_tier_2_passive_1':'Restore 1 HP, plus 1 HP per 14 Dual Wield power.',
    'warrior_tier_2_passive_2':'Your next 5 melee hits build attack speed, up to 10%% for 3s.',
    'rogue_tier_2_passive_2':'Rolling readies your next melee hit for 5s. Landing it guarantees critical melee attacks for 1s.',
    'rogue_tier_4_spell_1_modifier_2':'Dealing damage ends Vanish. Incoming hits and other actions do not.',
    'rogue_tier_4_spell_2_modifier_1':'A successful Mutilate heals 20%% of Dual Wield power, once per cast.',
    'priest_tier_1_passive_2':'Spell damage grants 3%% spell haste per stack, up to 15%% for 6s.',
    'priest_tier_2_passive_1':'Lose your pursuers and vanish completely for 1s, hiding armor and held items.',
    'paladin_tier_3_passive_1':'When you or a nearby ally are endangered, grant defenses and heal 20%% of their maximum HP. Does not cancel the triggering hit.'
   }.items():d['spell.skill_tree_rpgs.'+k+'.description']=v
   d['effect.skill_tree_rpgs.opportunist_ready']='Opportunist Ready';d['effect.skill_tree_rpgs.opportunist_ready.description']='Your next basic melee hit opens a 1s guaranteed-critical window.'
   d['effect.skill_tree_rpgs.opportunist.description']='Basic melee attacks are guaranteed critical strikes.'
   d['effect.skill_tree_rpgs.opportunist_window']='Opportunist';d['effect.skill_tree_rpgs.opportunist_window.description']='Basic melee attacks are guaranteed critical strikes.'
   d['effect.skill_tree_rpgs.supercharge.description']='Your next normal arrow deals 35% more damage.'
   d['effect.skill_tree_rpgs.warrior_momentum.description']='Each stack increases attack speed by 2%, up to 10%.'
  edit('assets/skill_tree_rpgs/lang/en_us.json',words)
  for key in ['opportunist_ready','opportunist_window']:f['assets/skill_tree_rpgs/textures/mob_effect/'+key+'.png']=f['assets/skill_tree_rpgs/textures/mob_effect/opportunist.png']
 if source.name.startswith('rogues-'):
  edit('data/rogues/spell/vanish.json',lambda d:d['cost']['cooldown'].update(duration=8,haste_affected=True))
  edit('assets/rogues/lang/en_us.json',lambda d:d.update({'spell.rogues.mutilate.description':'Strike with both weapons for 100%% Dual Wield damage. A successful hit heals 10%% of Dual Wield power.'}))
 if source.name.startswith('spell_engine-'):
  for p in (C/'net/spell_engine/mixin/classfollowup').glob('*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  n='spell_engine.mixins.json';d=json.loads(f[n]);d['client'].append('classfollowup.CrossbowCarryMixin');f[n]=enc(d)
  for name in ['FollowUpRules','ArrowAssist','CrimsonHealing','PaladinVisuals']:
   for p in (C/'net/spell_engine/internals').glob(name+'*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  for n,mode,count in [
   ('net/spell_engine/internals/ArcherRules.class','config',1),
   ('net/spell_engine/internals/RogueAdjustments.class','config',1),
   ('net/spell_engine/entity/WarriorTierTwo.class','config',1),
   ('net/spell_engine/internals/PriestPassives.class','config',2),
   ('net/spell_engine/internals/PaladinUltimates.class','ardent',1),
   ('net/spell_engine/internals/EffectDetails.class','details',1),
   ('net/spell_engine/entity/WarriorTierTwo.class','warmup',1),
   ('net/spell_engine/internals/cost/SpellCooldowns.class','cooldown',1)]:f[n]=patch(f[n],mode,count)
 if source.name.startswith('PlayerAnimationLib'):
  n='com/zigythebird/playeranim/animation/AvatarAnimManager.class';f[n]=patch(f[n],'animationpass',1)
 if source.name.startswith('critical_strike-'):
  n='net/critical_strike/internal/CritLogic.class';f[n]=patch(f[n],'crit',1)
 with ZipFile(out/source.name,'w') as z:
  for info in infos:z.writestr(info,f[info.filename])
  for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if f[n]!=old[n]],'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built',len(records),'isolated candidates')

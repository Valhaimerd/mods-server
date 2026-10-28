from compile import *
from zipfile import ZipFile
import copy,hashlib,io
OUT=W/'built';OUT.mkdir(exist_ok=True)
def enc(d):return (json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
def patch(blob,mode,count):
 (W/'in.class').write_bytes(blob);subprocess.run([str(J/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchRevamp',str(W/'in.class'),str(W/'out.class'),mode,str(count)],check=True);return (W/'out.class').read_bytes()
def sources():
 s=(R/'docs/patches/paladin-tier-one/PaladinTierOne.java').read_text().replace('public static void init(){','public static void init(){\n  PaladinRevamp.init();')
 s=s.replace('hit|=enemy.getHealth()+enemy.getAbsorptionAmount()<before;','if(enemy.getHealth()+enemy.getAbsorptionAmount()<before){hit=true;PaladinRevamp.addControl(enemy,new MobEffectInstance(PaladinRevamp.effect("spell_engine:stun"),absolution?10:5),caster);}')
 (H/'PaladinTierOne.java').write_text(s)
 s=(R/'docs/patches/paladin-passives/PaladinPassives.java').read_text()
 s=s.replace('modifier(player.getAttribute(Attributes.ARMOR),ARMOR,vanguard&&shieldEquipped(player));','modifier(player.getAttribute(Attributes.ARMOR),ARMOR,false);modifier(player.getAttribute(SpellSchools.HEALING.attributeEntry),ARMOR,vanguard&&shieldEquipped(player));')
 s=s.replace('modifier(player.getAttribute(SpellSchools.HEALING.attributeEntry),HEALING,guardian&&player.getOffhandItem().isEmpty());','modifier(player.getAttribute(SpellSchools.HEALING.attributeEntry),HEALING,false);modifier(player.getAttribute(Attributes.ARMOR),HEALING,guardian&&player.getOffhandItem().isEmpty());')
 s=s.replace('"minecraft:armor",.05f','"TEMP",.05f').replace('"spell_power:healing",.05f','"minecraft:armor",.05f').replace('"TEMP",.05f','"spell_power:healing",.05f')
 s=s.replace('public static void configure(ConfigFile.Effects config){','public static void configure(ConfigFile.Effects config){\n net.spell_engine.internals.PaladinRevamp.configureSkill(config);')
 s=s.replace('modifier(p.getAttribute(Attributes.ARMOR),ARMOR,false);','modifier(p.getAttribute(Attributes.ARMOR),ARMOR,false);modifier(p.getAttribute(SpellSchools.HEALING.attributeEntry),ARMOR,false);').replace('modifier(p.getAttribute(SpellSchools.HEALING.attributeEntry),HEALING,false);','modifier(p.getAttribute(SpellSchools.HEALING.attributeEntry),HEALING,false);modifier(p.getAttribute(Attributes.ARMOR),HEALING,false);')
 (H/'PaladinPassives.java').write_text(s)
sources();subprocess.run([__import__('sys').executable,str(H/'compile.py')],check=True)
records=[]
for source in (W/'before').glob('*.jar'):
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old);name=source.name
 def edit(n,fn):
  d=json.loads(f[n]);fn(d);f[n]=enc(d)
 def helper(prefix):
  for p in (C/prefix).parent.glob(Path(prefix).name+'*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
 def lang(words):
  path=next(n for n in f if n.endswith('/lang/en_us.json'))
  edit(path,lambda d:d.update(words))
 if name.startswith('spell_engine-'):
  for h in ['PaladinRevamp','EffectDetails','PaladinTierOne']:helper('net/spell_engine/internals/'+h)
  helper('net/spell_engine/client/gui/EffectClientDetails')
  for n,mode,count in [('net/spell_engine/neoforge/NeoForgeMod.class','attachment',1),('net/spell_engine/internals/impact/SpellImpacts.class','impact',1),('net/spell_engine/internals/SpellTriggers.class','triggers',1),('net/spell_engine/entity/WarriorTierTwo.class','freedom',1),('net/spell_engine/client/casting/ClientCastController.class','impaired',1),('net/spell_engine/entity/SpellProjectile.class','projectile',3),('net/spell_engine/internals/delivery/ProjectileLauncher.class','launch',2)]:f[n]=patch(f[n],mode,count)
 if name.startswith('combat_roll-'):
  for n,mode,count in [('net/combat_roll/internals/RollManager.class','roll',1),('net/combat_roll/network/ServerNetwork.class','rollserver',1),('net/combat_roll/mixin/MinecraftClientMixin.class','rollground',1)]:f[n]=patch(f[n],mode,count)
 if name.startswith('paladins-'):
  n='net/paladins/effect/PaladinEffects.class';f[n]=patch(f[n],'protection',1)
  judgement=copy.deepcopy(json.loads(f['data/paladins/spell/judgement.json']))
  def protection(d):
   d['range']=8;d['active'].setdefault('cast',{})['duration']=0;d['cost']['cooldown']={'duration':28,'haste_affected':True};d['deliver']={'type':'CUSTOM','custom':{'handler':'paladin_pack:protection'}};d['impacts']=[]
  edit('data/paladins/spell/divine_protection.json',protection)
  def oath(d):
   d['school']='spell_power:healing';d.pop('area_impact',None);d['range']=15;d['active']['cast']['duration']=.75;d['active']['cast']['haste_affected']=True;d['target']={'type':'AIM','aim':{'required':False,'sticky':True,'use_caster_as_fallback':True}};d['cost']['cooldown']={'duration':20,'haste_affected':True};d['deliver']={'type':'CUSTOM','custom':{'handler':'paladin_pack:oath_launch'}};d['impacts']=[{'action':{'type':'HEAL','heal':{'spell_power_coefficient':0}}}]
  edit('data/paladins/spell/judgement.json',oath)
  hammer=copy.deepcopy(judgement)
  for k in ['learn','group','active','passive','modifiers']:hammer.pop(k,None)
  hammer['type']='PASSIVE';hammer['target']={'type':'FROM_TRIGGER'};hammer['cost']={'durability':0};hammer['impacts']=[]
  f['data/paladins/spell/oath_hammer.json']=enc(hammer)
  damage=copy.deepcopy(judgement['impacts'][0]);damage.pop('target_modifiers',None);damage.pop('power_blend',None)
  stun={'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':'spell_engine:stun','duration':1,'amplifier':0,'apply_mode':'SET'}}}
  for key,coef,seconds in [('oath_landing',.3,1),('oath_landing_judgement',.7,2),('oath_extra',1,0)]:
   impact=copy.deepcopy(damage);impact['action']['damage']['spell_power_coefficient']=coef
   impacts=[impact]
   if seconds:st=copy.deepcopy(stun);st['action']['status_effect']['duration']=seconds;impacts.append(st)
   f['data/paladins/spell/'+key+'.json']=enc({'school':'spell_power:healing' if seconds else 'spell_power:physical_melee','type':'PASSIVE','tier':2,'range':0,'target':{'type':'FROM_TRIGGER'},'impacts':impacts,'cost':{'durability':0}})
  for key,amp in [('blessed_wave',0),('blessed_wave_wrath',1)]:
   def slow(d):d['impacts'].append({'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':'minecraft:slowness','duration':8,'amplifier':amp,'apply_mode':'SET'}}})
   edit('data/paladins/spell/'+key+'.json',slow)
  # Native old undead-specific bonuses are replaced by the one scoped impact multiplier.
  for n in list(f):
   if n.startswith('data/paladins/spell/') and n.endswith('.json'):
    d=json.loads(f[n])
    for impact in d.get('impacts',[]):
     if 'target_modifiers' in impact:impact['target_modifiers']=[v for v in impact['target_modifiers'] if 'undead' not in json.dumps(v)]
    f[n]=enc(d)
  lang({'spell.paladins.judgement.name':'Divine Oath','spell.paladins.judgement.description':'Call down a holy hammer, dealing 30%% Healing Power to nearby enemies and stunning them for 1s. Bless an ally with bonus damage, armor and absorption for 7s.','spell.paladins.divine_protection.description':'Protect nearby allies with damage reduction and flat mitigation. Gain 10%% Healing Power.','spell.paladins.flash_heal.description':'Strike an enemy for 50%% Melee Damage and stun them for 0.25s. A successful hit heals you for 25%% Healing Power.'})
 if name.startswith('skill_tree-'):
  helper('net/skill_tree_rpgs/compat/PaladinPassives')
  for i in [1,2]:
   def passive(d):
    d['passive']['triggers']=([{'type':'MELEE_IMPACT','chance':1},{'type':'ARROW_IMPACT','chance':1},{'type':'SPELL_IMPACT_SPECIFIC','chance':1,'impact':{'impact_type':'DAMAGE'}}] if i==1 else [{'type':'DAMAGE_TAKEN','chance':1,'stage':'POST'}]);d['cost']['cooldown']={'duration':1 if i==1 else .25,'haste_affected':True}
   edit(f'data/skill_tree_rpgs/spell/paladin_tier_1_passive_{i}.json',passive)
  for n in list(f):
   if n.startswith('data/skill_tree_rpgs/spell/paladin_tier_3_spell_') and n.endswith('.json'):
    def reset(d):
     d['modifiers']=[{'spell_pattern':'paladins:divine_protection' if '_spell_1_' in n else 'paladins:judgement'}]
     if n.endswith('spell_2_root.json'):d['modifiers'][0]['cooldown_duration_deduct']=3
    edit(n,reset)
  def sinful(d):
   d['passive']['triggers']=[{'type':'ROLL','chance':1}];d['cost']['cooldown']={'duration':12,'haste_affected':True};d['impacts']=[{'action':{'type':'STATUS_EFFECT','status_effect':{'effect_id':'skill_tree_rpgs:sinful_retaliation_ready','duration':6,'amplifier':0,'apply_mode':'SET'}}}]
  edit('data/skill_tree_rpgs/spell/paladin_tier_2_passive_1.json',sinful)
  def freedom(d):
   d['passive']['triggers']=[{'type':'ROLL','chance':1}];d['cost']['cooldown']={'duration':10,'haste_affected':True};d['deliver']={'type':'CUSTOM','custom':{'handler':'paladin_pack:break_restriction'}};d['impacts']=[]
  edit('data/skill_tree_rpgs/spell/paladin_tier_2_passive_2.json',freedom)
  words={
   'paladin_tier_1_passive_1':('Vanguard','Gain 5%% Healing Power with a shield equipped. Successful hits grant another 5%% per stack, up to 4 stacks for 5s.'),
   'paladin_tier_1_passive_2':('Guardian','Gain 5%% base armor with an empty offhand. Taking combat damage grants another 5%% per stack, up to 4 stacks for 5s.'),
   'paladin_tier_2_spell_1_modifier_2':('Absolution','Purge Heal explodes within 3 blocks for 80%% Melee Damage and stuns enemies for 0.5s.'),
   'paladin_tier_2_spell_2_modifier_1':('Seal of Wrath','Holy waves deal 40%% Melee Damage, hit up to 2 enemies and apply Slowness II for 8s.'),
   'paladin_tier_3_spell_1_root':('Improved Divine Protection','Divine Protection lasts 10s and can be cast while controlled. Cleanses movement impairments and briefly prevents their return.'),
   'paladin_tier_3_spell_1_modifier_1':('Bulwark of Faith','Strengthens damage reduction and grants knockback resistance, scaling with your power.'),
   'paladin_tier_3_spell_1_modifier_2':('Merciful Aegis','Grants knockback resistance. Each ally’s first incoming hit begins 5s of recovery, healing 5%% Healing Power each second.'),
   'paladin_tier_3_spell_2_root':('Improved Divine Oath','Reduces Divine Oath’s cooldown by 3s.'),
   'paladin_tier_3_spell_2_modifier_1':('Decree','Divine Oath grants stronger bonus damage, armor and absorption.'),
   'paladin_tier_3_spell_2_modifier_2':('Judgement','The hammer strikes within 6 blocks for 70%% Healing Power and stuns enemies for 2s.'),
   'paladin_tier_2_passive_1':('Sinful Retaliation','Rolling prepares retaliation for 6s. The next attacker is marked, takes 15%% more damage for 8s and burns for 4s.'),
   'paladin_tier_2_passive_2':('Break Restriction','Rolling cleanses movement impairments. While ready, you can roll out of stuns, roots and freezing.')}
  def language(d):
   for key,(title,desc) in words.items():
    d['spell.skill_tree_rpgs.'+key+'.name']=title;d['spell.skill_tree_rpgs.'+key+'.description']=desc;d['skill.skill_tree_rpgs.'+key+'.title']=title
   d['effect.skill_tree_rpgs.redoubt.description']='Increased Healing Power.';d['effect.skill_tree_rpgs.vengeance.description']='Increased base armor.'
   for id,title in [('divine_oath','Divine Oath'),('merciful_recovery','Merciful Recovery'),('faith_knockback','Steadfast Faith'),('protection_power','Divine Protection'),('paladin_control_guard','Control Protection'),('sinful_retaliation_ready','Sinful Retaliation'),('sinful_mark','Marked for Retribution')]:d['effect.skill_tree_rpgs.'+id]=title
  edit('assets/skill_tree_rpgs/lang/en_us.json',language)
  for cat in ['class_skills','weapon_skills']:
   def definitions(d):
    for key in words:
     if key in d:d[key]['title']={'translate':'skill.skill_tree_rpgs.'+key+'.title'}
   edit(f'data/skill_tree_rpgs/puffish_skills/categories/{cat}/definitions.json',definitions)
 if name.startswith('EffectInsights-'):
  n='fuzs/effectinsights/common/client/gui/tooltip/MobEffectTooltipLines$2.class';f[n]=patch(f[n],'description',1)
  n='fuzs/effectinsights/common/client/gui/tooltip/PotionContentsLines.class';f[n]=patch(f[n],'attributes',1)
 # Convert the supplied official artwork only; keep IDs, transparency and GUI slot size.
 from PIL import Image
 control={
  'spell_engine-':{'stun':'stun'},'paladins-':{'judgement':'stun'},'rogues-':{'shock':'stun','net_trap':'root','bear_trap':'root'},'skill_tree-':{'natures_grasp':'root'},'wizards-':{'frozen':'frozen'}}
 for prefix,icons in control.items():
  if name.startswith(prefix):
   ns={'spell_engine-':'spell_engine','paladins-':'paladins','rogues-':'rogues','skill_tree-':'skill_tree_rpgs','wizards-':'wizards'}[prefix]
   for target,family in icons.items():
    filename={'root':'07_34_07','stun':'07_33_57','frozen':'07_33_40'}[family]
    src=Path('C:/Users/Valhaimerd/Desktop')/('ChatGPT Image Oct 28, 2026, '+filename+' PM.png')
    buf=io.BytesIO();Image.open(src).resize((64,64),Image.Resampling.NEAREST).save(buf,format='PNG');f[f'assets/{ns}/textures/mob_effect/{target}.png']=buf.getvalue()
 if name.startswith('skill_tree-'):
  for effect,base in [('divine_oath','vengeance'),('merciful_recovery','healing_focus'),('faith_knockback','redoubt'),('protection_power','healing_focus'),('paladin_control_guard','redoubt'),('sinful_retaliation_ready','vengeance'),('sinful_mark','vengeance')]:
   f[f'assets/skill_tree_rpgs/textures/mob_effect/{effect}.png']=f[f'assets/skill_tree_rpgs/textures/mob_effect/{base}.png']
 if f==old:continue
 with ZipFile(OUT/name,'w') as z:
  for info in infos:z.writestr(info,f[info.filename])
  for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
 records.append({'jar':name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=f[n]],'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built',len(records),'candidates')

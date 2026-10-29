from compile import *
from zipfile import ZipFile
import hashlib
out=W/'built';out.mkdir(exist_ok=True);records=[]
for source in (W/'before').glob('*.jar'):
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old)
 def edit(n,fn):d=json.loads(f[n]);fn(d);f[n]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 if source.name.startswith('spell_engine-'):
  for name in ['PaladinRevamp','PaladinTierOne','PaladinUltimates','SupportScaling','Vanguard','ProtectionHits']:
   for p in (C/'net/spell_engine/internals').glob(name+'*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  name='net/spell_engine/internals/PaladinRevamp.class'
  for mode,count in [('cleansevisual',1),('retaliatevisual',1),('init',1),('configure',1),('roll',4),('protectvisual',2),('oathvisual',1)]:
   (W/'in.class').write_bytes(f[name]);subprocess.run([str(J/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchTierThree',str(W/'in.class'),str(W/'out.class'),mode,str(count)],check=True);f[name]=(W/'out.class').read_bytes()
 if source.name.startswith('skill_tree-'):
  for p in (C/'net/skill_tree_rpgs/compat').glob('PaladinPassives*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  def vanguard(d):
   d['passive']['triggers']=[{'type':'MELEE_IMPACT','chance':1}];d['cost']['cooldown']={'duration':4,'haste_affected':True};d['deliver']={'type':'CUSTOM','custom':{'handler':'paladin_pack:vanguard'}};d['impacts']=[]
  edit('data/skill_tree_rpgs/spell/paladin_tier_1_passive_1.json',vanguard)
  def words(d):
   changes={
    'paladin_tier_1_passive_1':'Periodically empower a melee hit with 20%% bonus damage and a 1s stun. Equipping a shield extends the stun to 2s.',
    'paladin_tier_1_passive_2':'Gain 2%% armor and Healing Power. Taking combat damage builds four additional 2%% stacks for 5s; a shield raises each earned stack to 3%%.',
    'paladin_tier_2_spell_1_root':'Purge Heal deals 60%% Melee Damage and heals you for 25%% Healing Power.',
    'paladin_tier_2_spell_1_modifier_1':'Purge Heal restores 35%% Healing Power to you and nearby allies.',
    'paladin_tier_3_spell_1_modifier_1':'Divine Protection grants stronger damage reduction, up to 40%%, and knockback resistance.',
    'paladin_tier_3_spell_1_modifier_2':'Divine Protection grants knockback resistance. The first damaging hit starts 5s of recovery.',
    'paladin_tier_3_spell_2_modifier_1':'Divine Oath grants more bonus damage, armor and absorption.',
   }
   for k,v in changes.items():d['spell.skill_tree_rpgs.'+k+'.description']=v
   d['effect.skill_tree_rpgs.vengeance.description']='Combat hits increase armor and Healing Power; a shield strengthens each stack.'
  edit('assets/skill_tree_rpgs/lang/en_us.json',words)
 if source.name.startswith('paladins-'):
  def words(d):
   d['spell.paladins.flash_heal.description']='Strike an enemy for 50%% Melee Damage and briefly stun them. A successful hit heals you for 20%% Healing Power.'
   d['spell.paladins.divine_protection.description']='Block up to 3 incoming attacks and reduce damage taken by up to 30%%. Gain 10%% Healing Power.'
   d['spell.paladins.battle_banner.description']="Carry a banner that steadily strengthens nearby allies and heals them each second. Each ally can receive one brief rescue."
  edit('assets/paladins/lang/en_us.json',words)
 with ZipFile(out/source.name,'w') as z:
  for info in infos:z.writestr(info,f[info.filename])
  for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if f[n]!=old[n]],'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built support balance candidates')

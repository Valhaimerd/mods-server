from compile import *
from zipfile import ZipFile
import hashlib,copy
OUT=W/'built';OUT.mkdir(exist_ok=True);records=[]
def enc(d):return (json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
def patch(data,mode,count):
 (W/'in.class').write_bytes(data);subprocess.run([str(J/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchFollowup',str(W/'in.class'),str(W/'out.class'),mode,str(count)],check=True);return (W/'out.class').read_bytes()
for source in (W/'before').glob('*.jar'):
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old)
 def edit(n,fn):d=json.loads(f[n]);fn(d);f[n]=enc(d)
 if source.name.startswith('paladins-'):
  for name,co in [('oath_landing',.7),('oath_landing_judgement',1.)]:
   def change(d):d['school']='spell_power:physical_melee';d['impacts'][0]['action']['damage']['spell_power_coefficient']=co
   edit('data/paladins/spell/'+name+'.json',change)
  def lang(d):
   d['spell.paladins.battle_banner.description']="Carry a banner on your back, building nearby allies' combat buffs and healing them once per second. Each ally can receive one brief rescue from low health or a fatal hit."
   d['spell.paladins.immolation.description']='Channel holy fire that grows stronger after 10s and 20s. From 10s, control effects cannot interrupt you. Later pulses consume absorption first, then health. The final phase builds a powerful finishing explosion.'
   d['spell.paladins.judgement.description']='Bless an ally beneath a falling hammer. Its impact deals 70%% Melee Damage and briefly stuns nearby enemies, while the ally gains damage, armor and absorption.'
  edit('assets/paladins/lang/en_us.json',lang)
 if source.name.startswith('skill_tree-'):
  for tail in ['root','modifier_1','modifier_2']:
   edit('data/skill_tree_rpgs/spell/paladin_tier_4_spell_1_'+tail+'.json',lambda d:(d.update(range=8 if tail=='root' else 6),d['modifiers'][0].update(range_add=2) if tail=='root' else None))
  def lang(d):
   d['spell.skill_tree_rpgs.paladin_tier_3_spell_2_modifier_2.description']='Divine Oath strikes enemies within 6 blocks for 100%% Melee Damage and stuns them for 2s.'
   d['spell.skill_tree_rpgs.paladin_tier_4_spell_1_root.description']='Battle Banner reaches allies within 8 blocks.'
  edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
 if source.name.startswith('spell_engine-'):
  for helper in ['PaladinUltimates','PaladinVisuals','PaladinFollowup']:
   for p in (C/'net/spell_engine/internals').glob(helper+'*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  for p in (C/'net/spell_engine/client/render/paladin').glob('*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  for n,mode,count in [('net/spell_engine/internals/PaladinRevamp.class','oath',2),('net/spell_engine/entity/WarriorTierTwo.class','focus',1),('net/spell_engine/client/casting/ClientCastController.class','clientfocus',1)]:f[n]=patch(f[n],mode,count)
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,f[info.filename])
  for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=f[n]],'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built3same-name candidates preserving current class visual pass')

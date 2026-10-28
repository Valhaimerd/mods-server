from pathlib import Path
from zipfile import ZipFile
import copy,hashlib,json,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/paladin-tier-one';B=W/'before';OUT=W/'built';OUT.mkdir(exist_ok=True)
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin');C=W/'classes';C.mkdir(exist_ok=True)
lines=(R/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines();cp=lines[lines.index('"-cp"')+1].strip('"')
cp=';'.join(str(p) for p in B.glob('*.jar'))+';'+cp
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp+=';'+str(asm)+';'+str(tree)
args=['--release','25','-proc:none','-cp',cp,'-d',str(C),*[str(H/n) for n in ['PaladinTierOne.java','HealingArmorBalance.java','PatchPaladin.java']]]
(W/'compile.args').write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args))
subprocess.run([str(J/'javac.exe'),'@'+str(W/'compile.args')],check=True)
def patch(blob,mode):
 (W/'in.class').write_bytes(blob);subprocess.run([str(J/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchPaladin',str(W/'in.class'),str(W/'out.class'),mode],check=True);return (W/'out.class').read_bytes()
def encode(d):return (json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
records=[]
for pattern in ['paladins-*','skill_tree-*','spell_engine-*','armory-*','bettercombat-*']:
 source=next(B.glob(pattern+'.jar'))
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(n,f):
  d=json.loads(files[n]);f(d);files[n]=encode(d)
 if pattern=='paladins-*':
  heal=json.loads(files['data/paladins/spell/flash_heal.json'])['impacts'][0];heal['action']['heal']['spell_power_coefficient']=1
  dmg=copy.deepcopy(json.loads(files['data/paladins/spell/blessed_strikes.json'])['impacts'][0]);dmg.pop('power_blend',None)
  def purge(d):
   d['school']='spell_power:physical_melee';d['range']=24;d['cost']['cooldown'].update(duration=5,haste_affected=True);d['active']['cast']['haste_affected']=True
   d['target']['aim']={'sticky':True,'required':True,'use_caster_as_fallback':False};d['impacts']=[copy.deepcopy(dmg)];d['deliver']={'type':'CUSTOM','custom':{'handler':'paladin_pack:purge'}}
  edit('data/paladins/spell/flash_heal.json',purge)
  def prepare(d):
   d['cost']['cooldown'].update(duration=14,haste_affected=True);d['active']['cast']['haste_affected']=True
   d['deliver']={'type':'CUSTOM','custom':{'handler':'paladin_pack:prepare'}};d['impacts']=[]
  edit('data/paladins/spell/blessed_strikes.json',prepare)
  files['data/paladins/spell/paladin_restore.json']=encode({'school':'spell_power:healing','type':'PASSIVE','tier':1,'range':0,'target':{'type':'FROM_TRIGGER'},'impacts':[heal],'cost':{'durability':0}})
  with ZipFile(next(B.glob('arsenal-*.jar'))) as z:
   wave=json.loads(z.read('data/arsenal/spell/shockwave_melee.json'));model=json.loads(z.read('assets/arsenal/models/spell_projectile/shockwave_large.json'))
  model['textures']={'0':'minecraft:block/gold_block','particle':'minecraft:block/gold_block'}
  files['assets/paladins/models/spell_projectile/blessed_wave.json']=encode(model)
  wave.pop('passive',None);wave['target']={'type':'FROM_TRIGGER'};wave['range']=20;wave['cost']={'durability':0}
  proj=wave['deliver']['projectile'];proj['inherit_shooter_pitch']=True;proj['inherit_shooter_velocity']=False;proj['launch_properties']['velocity']=1.4
  proj['projectile']['perks']={'pierce':0,'ricochet':0,'bounce':0};proj['projectile']['hitbox']['width']=4.2
  fx=proj['projectile']['client_data']['composite_model']['models'][0]['fx'];fx['model_id']='paladins:spell_projectile/blessed_wave';fx['scale']=4.8
  wave['impacts']=[copy.deepcopy(dmg)];wave['impacts'][0]['action']['damage']['spell_power_coefficient']=.3
  files['data/paladins/spell/blessed_wave.json']=encode(wave)
  wave=copy.deepcopy(wave);wave['impacts'][0]['action']['damage']['spell_power_coefficient']=.4;wave['deliver']['projectile']['projectile']['perks']['pierce']=1
  files['data/paladins/spell/blessed_wave_wrath.json']=encode(wave)
  for n in list(files):
   if n.startswith('data/paladins/spell/') and n.endswith('.json'):
    d=json.loads(files[n])
    if d.get('group','').startswith('paladin_'):
     d.setdefault('cost',{}).setdefault('cooldown',{})['haste_affected']=True
     if 'cast' in d.get('active',{}):d['active']['cast']['haste_affected']=True
     files[n]=encode(d)
  def lang(d):
   for k in d:
    if isinstance(d[k],str):d[k]=d[k].replace('Flash Heal','Purge Heal')
   d['spell.paladins.flash_heal.name']='Purge Heal'
   d['spell.paladins.flash_heal.description']='Strike an enemy for 50%% Melee Damage. A successful hit heals you for 25%% Healing Power.'
   d['spell.paladins.blessed_strikes.description']='Channel five pulses, each healing you for 5%% Healing Power and preparing a holy wave for 15 seconds. Melee swings release waves for 30%% Melee Damage, reaching 20 blocks.'
   d['effect.paladins.blessed_strikes.description']='Melee swings release prepared holy waves.'
  edit('assets/paladins/lang/en_us.json',lang)
 if pattern=='skill_tree-*':
  for n in list(files):
   if n.startswith('data/skill_tree_rpgs/spell/paladin_') and n.endswith('.json'):
    d=json.loads(files[n]);key=Path(n).stem
    if d.get('type')!='MODIFIER':d.setdefault('cost',{}).setdefault('cooldown',{})['haste_affected']=True
    if key.startswith('paladin_tier_2_spell_'):
     target='paladins:flash_heal' if '_spell_1_' in key else 'paladins:blessed_strikes'
     d['modifiers']=[{'spell_pattern':target}]
     if key=='paladin_tier_2_spell_2_root':d['modifiers'][0]['cooldown_duration_deduct']=2
    files[n]=encode(d)
  words={
   'paladin_tier_2_spell_1_root':('Improved Purge Heal','Purge Heal deals 60%% Melee Damage and heals you for 30%% Healing Power.'),
   'paladin_tier_2_spell_1_modifier_1':('Divine Strength','Purge Heal restores 40%% Healing Power to you and allies within 4 blocks.'),
   'paladin_tier_2_spell_1_modifier_2':('Absolution','Purge Heal explodes within 3 blocks, dealing 80%% Melee Damage.'),
   'paladin_tier_2_spell_2_root':('Improved Blessed Strikes','Reduces Blessed Strikes cooldown by 2 seconds.'),
   'paladin_tier_2_spell_2_modifier_1':('Seal of Wrath','Holy waves deal 40%% Melee Damage and hit up to 2 enemies.'),
   'paladin_tier_2_spell_2_modifier_2':('Seal of Light','Each pulse heals you for 10%% Healing Power. The final pulse also heals allies within 4 blocks for the same amount.')}
  def lang(d):
   for k in d:
    if isinstance(d[k],str):d[k]=d[k].replace('Flash Heal','Purge Heal')
   for key,(title,desc) in words.items():
    d['spell.skill_tree_rpgs.'+key+'.name']=title;d['spell.skill_tree_rpgs.'+key+'.description']=desc;d['skill.skill_tree_rpgs.'+key+'.title']=title
  edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
  for category in ['class_skills','weapon_skills']:
   def defs(d):
    for key,(title,desc) in words.items():
     if key in d:d[key]['title']={'translate':'skill.skill_tree_rpgs.'+key+'.title'}
   edit(f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json',defs)
 if pattern=='spell_engine-*':
  files['net/spell_engine/internals/PaladinTierOne.class']=(C/'net/spell_engine/internals/PaladinTierOne.class').read_bytes()
  for n,mode in [('net/spell_engine/internals/PriestRules.class','init'),('net/spell_engine/entity/SpellCloud.class','cloud')]:files[n]=patch(files[n],mode)
 if pattern=='armory-*':files['net/armory_rpgs/item/HealingArmorBalance.class']=(C/'net/armory_rpgs/item/HealingArmorBalance.class').read_bytes()
 if pattern=='bettercombat-*':
  n='net/bettercombat/network/ServerNetwork.class';files[n]=patch(files[n],'swing')
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built',len(records),'same-name candidates')

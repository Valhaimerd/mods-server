from pathlib import Path
from zipfile import ZipFile
import copy,hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-tier-three';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True);inputs=[]
for pat in ['paladins-*.jar','skill_tree-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pat));p=BEFORE/active.name
 if not p.exists():shutil.copy2(active,p)
 inputs.append(p)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(map(str,inputs))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'));classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'PriestUltimates.java'),str(HERE/'PatchUltimates.java')]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
def patch(data,mode):
 (WORK/'in.class').write_bytes(data);subprocess.run([str(JAVA/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchUltimates',str(WORK/'in.class'),str(WORK/'out.class'),mode],check=True);return (WORK/'out.class').read_bytes()
records=[]
for source in inputs:
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(n,fn):
  o=json.loads(files[n]);before=copy.deepcopy(o);fn(o)
  if o!=before:files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 # Explicitly enable cast and cooldown haste for all Priest active/passive/helper resources.
 for n,data in old.items():
  if n.endswith('.json') and '/spell/' in n:
   o=json.loads(data)
   if o.get('group','').startswith('priest_') or n.startswith('data/skill_tree_rpgs/spell/priest_') or n.endswith('/lightwell_cleanse.json'):
    def haste(o):
     if 'active' in o:o['active'].setdefault('cast',{})['haste_affected']=True
     if 'cost' in o and 'cooldown' in o['cost']:o['cost']['cooldown']['haste_affected']=True
    edit(n,haste)
 if source.name.startswith('paladins-'):
  def well(o):
   o['cost']['cooldown'].update(duration=80,haste_affected=True)
   d=o['impacts'][0]['action']['summon'];b=d['behaviour'];b['lifespan']['active_seconds']=16;b['targeting']['automatic_targeting']='NONE'
   b['actions']=[a for a in b['actions'] if a.get('spell_cast',{}).get('spell_id')=='paladins:lightwell_orb'];b['actions'][0]['spell_cast']['cooldown']=16
   for entry in d['attribute_scaling']['entries']:
    if entry['attribute_id']=='spell_power:healing':entry['modifiers'][0]['coefficient']=.05
  edit('data/paladins/spell/lightwell.json',well)
  def mote(o):
   o['range']=16;o['deliver']['projectile'].setdefault('launch_properties',{})['velocity']=.6;o['deliver']['projectile']['projectile']['perks']['bounce']=0
   o['impacts'][0]['action']['heal']['spell_power_coefficient']=1;o['cost']['cooldown'].update(duration=.8,haste_affected=True)
  edit('data/paladins/spell/lightwell_orb.json',mote)
  def barrier(o):o['range']=10;o['active']['cast'].update(duration=1,haste_affected=True);o['cost']['cooldown'].update(duration=60,haste_affected=True)
  edit('data/paladins/spell/barrier.json',barrier)
  def lang(o):
   o['spell.paladins.lightwell.description']='Summon a hovering Lightwell that follows you for 16s and heals injured allies. Each mote heals for 100%% of its own Healing Power, inherited at 5%% of yours.'
   o['spell.paladins.barrier.description']='Raise a barrier for 10s that repels enemies and protects allies from combat damage and damage over time. Void damage and deliberate health costs still apply.'
   o['spell.paladins.lightwell_orb.description']='Heal an ally for 100%% of the Lightwell\'s Healing Power.'
  edit('assets/paladins/lang/en_us.json',lang)
  n='net/paladins/entity/BarrierEntity.class';files[n]=patch(files[n],'barrier')
 if source.name.startswith('skill_tree-'):
  prefix='data/skill_tree_rpgs/spell/'
  for key in ['priest_tier_4_spell_1_modifier_1','priest_tier_4_spell_1_modifier_2']:
   edit(prefix+key+'.json',lambda o:o.update(modifiers=[{'spell_pattern':'paladins:lightwell'}]))
  edit(prefix+'priest_tier_4_spell_2_root.json',lambda o:o.update(modifiers=[{'spell_pattern':'paladins:barrier','spawn_duration_add':2}]))
  edit(prefix+'priest_tier_4_spell_2_modifier_2.json',lambda o:o.update(modifiers=[{'spell_pattern':'paladins:barrier'}]))
  def lang(o):
   values={'priest_tier_4_spell_1_root':'Lightwell lasts 4s longer.','priest_tier_4_spell_1_modifier_1':'Each Lightwell mote removes one harmful effect from its ally.','priest_tier_4_spell_1_modifier_2':'Lightwell motes grant 1 maximum heart per hit, up to 2 hearts, for 6s. Further hits refresh the bonus.','priest_tier_4_spell_2_root':'Barrier lasts 2s longer.','priest_tier_4_spell_2_modifier_2':'Intercepted hostile projectiles strike their attacker for 25%% Healing Power damage.'}
   for key,text in values.items():o['spell.skill_tree_rpgs.'+key+'.description']=text
   for key in ['spell.skill_tree_rpgs.priest_tier_4_spell_2_modifier_2.name','skill.skill_tree_rpgs.priest_tier_4_spell_2_modifier_2.title']:o[key]='Deflective Shield'
   o['effect.skill_tree_rpgs.lightwell_vitality']='Empowered Well';o['effect.skill_tree_rpgs.lightwell_vitality.description']='Increased maximum health.'
  edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
  files['assets/skill_tree_rpgs/textures/mob_effect/lightwell_vitality.png']=files['assets/skill_tree_rpgs/textures/spell/priest_tier_1_passive_1.png']
  files[prefix+'barrier_retaliation.json']=(json.dumps({'school':'spell_power:healing','range':0,'tier':0,'impacts':[{'action':{'type':'DAMAGE','damage':{'spell_power_coefficient':.25,'knockback':0}}}],'cost':{'durability':0}},indent=2)+'\n').encode()
 if source.name.startswith('spell_engine-'):
  for name,mode in [('net/spell_engine/internals/PriestRules.class','priest'),('net/spell_engine/entity/SummonedEntity.class','summon'),('net/spell_engine/internals/delivery/ProjectileLauncher.class','launcher')]:files[name]=patch(files[name],mode)
  for p in (classes/'net/spell_engine/internals').glob('PriestUltimates*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
 with ZipFile(OUT/source.name,'w') as z:
  for i in infos:z.writestr(i,files[i.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(files.keys()-old.keys())})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Priest tier III candidates')

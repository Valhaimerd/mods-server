from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-passives';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True);inputs=[]
for pattern in ['skill_tree-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pattern));p=BEFORE/active.name
 if not p.exists():shutil.copy2(active,p)
 inputs.append(p)
# Recompile only the current tracked Priest helper, preserving all other current engine classes.
s=(ROOT/'docs/patches/priest-rework/PriestRules.java').read_text()
s=s.replace(' public static void configure(ConfigFile.Effects config){',' public static void configure(ConfigFile.Effects config){\n  PriestPassives.configure(config);')
s=s.replace('target.setAbsorptionAmount(Math.min((float)target.getAttributeValue(Attributes.MAX_ABSORPTION),absorption+Math.max(0,after-before)));','PriestPassives.absorptionGrant(target,effect.value(),Math.max(0,after-before),absorption);')
s=s.replace('entity.setAbsorptionAmount(Math.min((float)entity.getAttributeValue(Attributes.MAX_ABSORPTION),entity.getAbsorptionAmount()+perLevel*Math.max(0,level-old)));','PriestPassives.absorptionGrant(entity,effect,perLevel*Math.max(0,level-old),entity.getAbsorptionAmount());')
(WORK/'PriestRules.java').write_text(s)
base=(ROOT/'tmp/ultimate-armor-visuals/compile.args').read_text().splitlines();oldcp=base[base.index('"-cp"')+1].strip('"');cp=';'.join(str(p) for p in inputs)+';'+oldcp
classes=WORK/'classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(WORK/'PriestRules.java'),*[str(p) for p in HERE.glob('*.java') if 'Probe' not in p.name]]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for source in inputs:
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(n,fn):
  o=json.loads(files[n]);fn(o);files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 if source.name.startswith('skill_tree-'):
  prefix='data/skill_tree_rpgs/spell/'
  for key in ['priest_tier_1_passive_1','priest_tier_1_passive_2']:
   def change(o):
    o['passive']['triggers'][0]['chance']=1;o['impacts'][0]['action']['status_effect'].update(duration=6,amplifier_cap=4);o['cost']['cooldown']={'duration':.25,'haste_affected':True}
   edit(prefix+key+'.json',change)
  def fade(o):
   o['cost']['cooldown']={'duration':6,'haste_affected':True}
   o['target']['area']['include_caster']=True
   for key in ['minecraft:invisibility','skill_tree_rpgs:priest_fade']:o['impacts'].append({'action':{'type':'STATUS_EFFECT','apply_to_caster':True,'status_effect':{'effect_id':key,'duration':2}}})
  edit(prefix+'priest_tier_2_passive_1.json',fade)
  def divine(o):
   o['passive']['triggers'][0]['chance']=1;o.pop('deliver',None);o['target']={'type':'FROM_TRIGGER'}
   o['impacts']=[{'action':{'type':'STATUS_EFFECT','apply_to_caster':True,'status_effect':{'effect_id':'skill_tree_rpgs:divine_favor','duration':15}}}]
   o['cost']['cooldown']={'duration':15,'haste_affected':True}
  edit(prefix+'priest_tier_2_passive_2.json',divine)
  def lang(o):
   for key,text in {'priest_tier_1_passive_1':'Healing grants the recipient 4%% more healing and absorption received per stack, up to 20%%, for 6s.','priest_tier_1_passive_2':'Spell damage grants 2%% spell haste per stack, up to 10%%, for 6s.','priest_tier_2_passive_1':'Lose nearby pursuers and vanish completely for 2s, hiding armor and held items.','priest_tier_2_passive_2':'Ready a counter for 15s. The next damaging attack stuns its attacker and enemies within 2 blocks of you for 1s.'}.items():o['spell.skill_tree_rpgs.'+key+'.description']=text
   o['effect.skill_tree_rpgs.healing_focus.description']='Increased healing and absorption received.';o['effect.skill_tree_rpgs.divine_favor.description']='The next damaging attack triggers a stunning counter.';o['effect.skill_tree_rpgs.priest_fade']='Fade';o['effect.skill_tree_rpgs.priest_fade.description']='Fully concealed.'
  edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
  files['assets/skill_tree_rpgs/textures/mob_effect/priest_fade.png']=files['assets/skill_tree_rpgs/textures/spell/priest_tier_2_passive_1.png']
 else:
  for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
  def mixins(o):o['mixins'].append('priest.ReceivedAbsorptionMixin');o['client']+=['client.render.priest.FadeEntityMixin','client.render.priest.FadeHandsMixin']
  edit('spell_engine.mixins.json',mixins)
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys())})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Priest passive candidates')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-tuning';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True);inputs=[]
for pattern in ['paladins-*.jar','skill_tree-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 inputs.append(source)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(map(str,inputs))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'));classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'PriestTuning.java'),str(HERE/'PatchTuning.java')]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
def patch(data,mode):
 (WORK/'in.class').write_bytes(data);subprocess.run([str(JAVA/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchTuning',str(WORK/'in.class'),str(WORK/'out.class'),mode],check=True);return (WORK/'out.class').read_bytes()
records=[]
for source in inputs:
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(name,fn):
  o=json.loads(files[name]);fn(o);files[name]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 if source.name.startswith('paladins-'):
  def penance(o):o['active']['cast']['duration']=5;o['cost']['cooldown']['duration']=12;o['impacts'][0]['action']['damage']['spell_power_coefficient']=.15
  edit('data/paladins/spell/penance.json',penance)
  def holy(o):o['active']['cast']['duration']=10;o['impacts'][0]['action']['heal']['spell_power_coefficient']=.10;o['impacts'][1]['action']['damage']['spell_power_coefficient']=.20
  edit('data/paladins/spell/holy_beam.json',holy)
  def lang(o):
   o['spell.paladins.holy_beam.description']='Channel 25 holy pulses, each healing allies for 10%% Healing Power and damaging enemies for 20%%.'
   o['spell.paladins.penance.description']='Fire 10 bolts that burst within 4 blocks for 15%% Healing Power. Hits build absorption for you and nearby allies, up to 10 HP, lasting 3s.'
   o['spell.paladins.lightwell.description']='Summon a hovering Lightwell for 16s. It fires two healing motes at the injured allies with the lowest health, focusing both on a sole injured ally.'
  edit('assets/paladins/lang/en_us.json',lang)
 if source.name.startswith('skill_tree-'):
  edit('data/skill_tree_rpgs/spell/weapon_holy_root.json',lambda o:o.update(modifiers=[{'spell_pattern':'paladins:holy_shock','impact_filters':[{'type':'DAMAGE'}],'power_modifier':{'power_multiplier':.05}}]))
  def lang(o):
   for key,value in {
    'weapon_holy_root':'Holy Shock deals 5%% more damage.',
    'priest_tier_2_spell_1_root':'Holy Light heals for 15%% Healing Power and damages enemies for 25%% per hit.',
    'priest_tier_2_spell_1_modifier_1':'Holy Light heals for 20%% Healing Power per hit and doubles its knockback.',
    'priest_tier_2_spell_1_modifier_2':'Holy Light deals 35%% Healing Power per hit and burns enemies.',
    'priest_tier_3_spell_2_root':'Penance blasts deal 20%% Healing Power. Absorption lasts 1s longer.',
    'priest_tier_3_spell_2_modifier_1':'Penance hits grant you and nearby allies 1%% attack speed, draw speed and spell haste per stack, up to 10%%, for 4s.'}.items():o['spell.skill_tree_rpgs.'+key+'.description']=value
  edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
 if source.name.startswith('spell_engine-'):
  for cls,mode in [('internals/PriestRules','rules'),('internals/PriestUltimates','well'),('internals/cost/SpellCooldowns','cooldown'),('internals/delivery/SpellDelivery','delivery')]:
   n='net/spell_engine/'+cls+'.class';files[n]=patch(files[n],mode)
  for p in (classes/'net/spell_engine/internals').glob('PriestTuning*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(files.keys()-old.keys())})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built current-pack Priest tuning candidates')

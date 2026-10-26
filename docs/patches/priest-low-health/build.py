from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-low-health';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True);inputs=[]
for pattern in ['skill_tree-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 inputs.append(source)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(map(str,inputs))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'));classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'PriestDefenses.java'),str(HERE/'PatchDefenses.java')]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for source in inputs:
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(name,fn):
  o=json.loads(files[name]);fn(o);files[name]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 if source.name.startswith('skill_tree-'):
  def pain(o):o['passive']['triggers']=[];o['cost']['cooldown']['duration']=0
  edit('data/skill_tree_rpgs/spell/priest_tier_3_passive_1.json',pain)
  def orbs(o):o['deliver']['stash_effect']['amplifier']=4;o['impacts'][0]['action']['damage']['spell_power_coefficient']=.30
  edit('data/skill_tree_rpgs/spell/priest_tier_3_passive_2.json',orbs)
  def lang(o):
   o['spell.skill_tree_rpgs.priest_tier_3_passive_1.description']='Take 6%% less damage below 50%% health, rising to 12%%, 18%% and 24%% below 40%%, 30%% and 20%% health.'
   o['spell.skill_tree_rpgs.priest_tier_3_passive_2.description']='Critical spells ready 5 celestial orbs for 15s. When you or an ally within 8 blocks is hit, an orb retaliates for 30%% Healing Power.'
  edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
 if source.name.startswith('spell_engine-'):
  n='net/spell_engine/internals/PriestPassives.class';(WORK/'in.class').write_bytes(files[n]);subprocess.run([str(JAVA/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchDefenses',str(WORK/'in.class'),str(WORK/'out.class')],check=True);files[n]=(WORK/'out.class').read_bytes()
  for p in (classes/'net/spell_engine/internals').glob('PriestDefenses*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(files.keys()-old.keys())})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Priest defensive passive candidates')

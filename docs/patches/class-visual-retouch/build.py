from compile import *
from retouch_resources import tune,family
from pathlib import Path
from zipfile import ZipFile
import json,hashlib,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-visual-retouch';OUT=W/'built';OUT.mkdir(exist_ok=True);mapping=json.loads((H/'icon-map.json').read_text());rows=[];visual_rows=[]
def patch(blob,mode,count):
 (W/'in.class').write_bytes(blob);subprocess.run([str(J/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchVisuals',str(W/'in.class'),str(W/'out.class'),mode,str(count)],check=True);return (W/'out.class').read_bytes()
def enc(o):return (json.dumps(o,ensure_ascii=False,indent=2)+'\n').encode()
for source in (W/'before').glob('*.jar'):
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 for row in mapping:
  if row['jar']==source.name:
   ns,key=row['effect'].split(':');files[f'assets/{ns}/textures/mob_effect/{key}.png']=files[row['source']]
 if source.name.startswith(('rogues-','skill_tree-')):
  ns='rogues' if source.name.startswith('rogues-') else 'skill_tree_rpgs';name=f'assets/{ns}/lang/en_us.json';d=json.loads(files[name])
  for row in mapping:
   if row['effect'].startswith(ns+':'):
    k=row['effect'].split(':')[1];d['effect.'+ns+'.'+k+'.description']={'improved_stealth':"Reduces Vanish's movement penalty.",'tracking_disruption':'Unable to acquire a target.','cheat_death_evasion':'Gain 30% evasion after Cheat Death.'}[k]
  files[name]=enc(d)
 for n in list(files):
  if n.startswith('data/') and '/spell/' in n and n.endswith('.json'):
   d=json.loads(files[n]);kind=family(n,d)
   if tune(n,d):files[n]=enc(d);visual_rows.append({'jar':source.name,'resource':n,'class':kind})
 if source.name.startswith('rogues-'):
  n='assets/rogues/lang/en_us.json';d=json.loads(files[n]);d['spell.rogues.vanish.description']=d['spell.rogues.vanish.description']+' Clears existing marks.';files[n]=enc(d)
 if source.name.startswith('spell_engine-'):
  for helper in ['ClassStatusVisuals','ClassAreaVisuals','ClassHealingVisuals','ClassEffectLabels','RogueVisuals']:
   for p in (C/'net/spell_engine/internals').glob(helper+'*.class'):files[p.relative_to(C).as_posix()]=p.read_bytes()
  for n,mode,count in [('net/spell_engine/internals/EffectDetails.class','status',1),('net/spell_engine/internals/EffectDetails.class','labels',1),('net/spell_engine/internals/PaladinRevamp.class','glints',1),('net/spell_engine/internals/PreparedCounter.class','glints',1),('net/spell_engine/internals/RogueRules.class','rogue',1),('net/spell_engine/internals/RogueTierThree.class','mutilate',2),('net/spell_engine/internals/impact/SpellImpacts.class','area',1),('net/spell_engine/internals/impact/SpellImpacts.class','heal',4),('net/spell_engine/internals/PriestTuning.class','penance',3)]:files[n]=patch(files[n],mode,count)
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 rows.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(files.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(rows,indent=2)+'\n');(H/'visual-resources.json').write_text(json.dumps(visual_rows,indent=2)+'\n');print('Built',len(rows),'same-name candidates;',len(visual_rows),'presentation resources retouched')

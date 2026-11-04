from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-7-9';sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();count=0
def check(ok,label):
 global count
 if not ok:raise AssertionError(label)
 count+=1
records=json.loads((H/'artifacts.json').read_text());reports=[json.loads((H/n).read_text()) for n in ['candidate-tested.json','regression-candidate-tested.json','mage-regression-candidate-tested.json','hydra-regression-candidate-tested.json','client-tested.json']]
for row in records:
 name=row['jar'];path=W/'built'/name;check(sha(path)==row['after'],name+' candidate hash');check(sha(W/'before'/name)==row['before'],name+' pinned baseline')
 for report in reports:check(report[name]==row['after'],name+' runtime-tested exact bytes')
 if '--installed' in sys.argv:
  for folder in ['mods','mod fixing/1 fixed packs']:check(sha(R/folder/name)==row['after'],name+' '+folder)
 with ZipFile(path) as z,ZipFile(W/'before'/name) as before:
  check(z.testzip() is None,name+' CRC');check(len(z.namelist())==len(set(z.namelist())),name+' unique entries');check(z.read('META-INF/neoforge.mods.toml')==before.read('META-INF/neoforge.mods.toml'),name+' unchanged metadata');check(not any(n.startswith('local/') or 'Probe.class' in n for n in z.namelist()),name+' no probe shipped');check(not set(before.namelist())-set(z.namelist()),name+' no IDs/files deleted');check({n for n in before.namelist() if before.read(n)!=z.read(n)}==set(row['changed']),name+' exact mutation ledger');check(set(z.namelist())-set(before.namelist())==set(row['added']),name+' exact additions')
  if name.startswith('spell_engine-'):
   for n in ['MageSummons','SummonRules','MageFollowup','FrostExposure','BatchRules','RevisionRules','PriestTuning','SupportScaling']:
    key='net/spell_engine/internals/'+n+'.class';check(z.read(key)==before.read(key),'preserved '+n)
   for n in ['entity/WarriorUltimates','entity/ClassRework','internals/cost/BlinkCharges','internals/cost/EchoCharges']:
    key='net/spell_engine/'+n+'.class';check(z.read(key)==before.read(key),'preserved '+n)
  if name.startswith('skill_tree-'):
   for effect in ['ascension_guard','vanguard_guard','purelight_cost','onslaught_ready','onslaught_power','deflection_speed','purgatory_opening','break_knockback']:check(z.read('assets/skill_tree_rpgs/textures/mob_effect/'+effect+'.png').startswith(b'\x89PNG'),'valid icon '+effect)
for name in ['candidate-result.txt','regression-candidate-result.txt','mage-regression-candidate-result.txt','hydra-regression-candidate-result.txt','client-result.txt']:check((H/name).read_text().startswith('PASS'),name)
check(json.loads((H/'candidate-resources.json').read_text())['failed']==[],'resource assertions')
print('PASS',count,'artifact/install checks');(H/'verification.json').write_text(json.dumps({'passed':count,'installed':'--installed' in sys.argv},indent=2)+'\n')

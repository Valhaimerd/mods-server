from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/purelight-glow';sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();count=0
def check(b,label):
 global count
 if not b:raise AssertionError(label)
 count+=1
reports=[json.loads((H/n).read_text()) for n in ['candidate-tested.json','client-tested.json']]
for row in json.loads((H/'artifacts.json').read_text()):
 name=row['jar'];check(sha(W/'before'/name)==row['before'],'input pinned');check(sha(W/'built'/name)==row['after'],'candidate hash')
 for report in reports:check(report[name]==row['after'],'exact tested hash')
 if '--installed' in sys.argv:
  for folder in ['mods','mod fixing/1 fixed packs']:check(sha(R/folder/name)==row['after'],folder+' copy')
 with ZipFile(W/'built'/name) as z,ZipFile(W/'before'/name) as old:
  check(z.testzip() is None,'CRC');check(len(z.namelist())==len(set(z.namelist())),'no duplicates');check(z.read('META-INF/neoforge.mods.toml')==old.read('META-INF/neoforge.mods.toml'),'metadata unchanged');check(not any(n.startswith('local/') for n in z.namelist()),'no probes');check(set(old.namelist()).issubset(z.namelist()),'no removed IDs');check({n for n in old.namelist() if old.read(n)!=z.read(n)}==set(row['changed']),'exact changes');check(set(z.namelist())-set(old.namelist())==set(row['added']),'exact additions')
  if name.startswith('spell_engine-'):
   for key in ['internals/FinalRules','internals/FinalDefenses','internals/FinalDome','internals/MageSummons','internals/SummonRules','internals/PaladinUltimates']:
    n='net/spell_engine/'+key+'.class';check(z.read(n)==old.read(n),'preserved '+key)
for n in ['candidate-result.txt','client-result.txt']:check((H/n).read_text().startswith('PASS'),n)
(H/'verification.json').write_text(json.dumps({'passed':count,'installed':'--installed' in sys.argv},indent=2)+'\n');print('PASS',count,'artifact/install checks')

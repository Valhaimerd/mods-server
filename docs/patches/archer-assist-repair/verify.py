from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/archer-assist-repair'
row=json.loads((H/'artifact.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();count=0
def check(ok,label):
 global count
 assert ok,label
 count+=1
check(sha(W/'before'/row['jar'])==row['before'],'pinned current input')
check(sha(W/'built'/row['jar'])==row['after'],'candidate hash')
with ZipFile(W/'before'/row['jar']) as old,ZipFile(W/'built'/row['jar']) as new:
 check(new.testzip() is None,'archive CRC')
 check(len(new.namelist())==len(set(new.namelist())),'no duplicate members')
 check(set(old.namelist())==set(new.namelist()),'no new or removed IDs')
 check({n for n in old.namelist() if old.read(n)!=new.read(n)}==set(row['changed']),'exact one-class replacement')
 check(old.read('META-INF/neoforge.mods.toml')==new.read('META-INF/neoforge.mods.toml'),'metadata/dependencies unchanged')
 for cls in ['PriestRules','PriestTuning','FinalRules','MageFollowup','FinalPurelight','FinalDome','MageSummons','SummonRules']:
  key='net/spell_engine/internals/'+cls+'.class';check(old.read(key)==new.read(key),'retained '+cls)
 check(old.read('net/spell_engine/entity/ArcherProjectileRules.class')==new.read('net/spell_engine/entity/ArcherProjectileRules.class'),'Magic Arrows unchanged')
if '--installed' in sys.argv:
 check((H/'candidate-result.txt').read_text().startswith('PASS'),'native runtime passed')
 check(json.loads((H/'candidate-tested.json').read_text())[row['jar']]==row['after'],'exact tested bytes')
 for folder in ['mods','mod fixing/1 fixed packs']:check(sha(R/folder/row['jar'])==row['after'],'installed '+folder)
(H/'verification.json').write_text(json.dumps({'passed':count,'installed':'--installed' in sys.argv},indent=2)+'\n')
print('PASS',count,'artifact/install checks')

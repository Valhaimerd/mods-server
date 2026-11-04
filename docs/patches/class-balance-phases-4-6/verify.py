"""Archive scope, exact runtime-tested bytes, installation and unchanged prior inputs."""
from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-4-6'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
checks=0
def check(ok,label):
 global checks
 if not ok:raise AssertionError(label)
 checks+=1
records=json.loads((H/'artifacts.json').read_text())
tested=[json.loads((H/name).read_text()) for name in ['candidate-tested.json','regression-candidate-tested.json','client-tested.json']]
for row in records:
 name=row['jar'];path=W/'built'/name
 check(sha(path)==row['after'],name+' candidate hash')
 for report in tested:check(report[name]==row['after'],name+' exact tested hash')
 check(sha(W/'before'/name)==row['before'],name+' pinned original')
 if '--installed' in sys.argv:
  for folder in ['mods','mod fixing/1 fixed packs']:check(sha(R/folder/name)==row['after'],name+' '+folder)
 with ZipFile(path) as z,ZipFile(W/'before'/name) as old:
  check(z.testzip() is None,name+' CRC')
  check(len(z.namelist())==len(set(z.namelist())),name+' unique members')
  check(z.read('META-INF/neoforge.mods.toml')==old.read('META-INF/neoforge.mods.toml'),name+' original metadata')
  check(not any(n.startswith(('local/','phase_two_probe/','spatial_client_probe/')) for n in z.namelist()),name+' no probes')
  actual={n for n in old.namelist() if n in z.namelist() and z.read(n)!=old.read(n)}
  check(actual==set(row['changed']),name+' exact mutation ledger')
  check(set(z.namelist())-set(old.namelist())==set(row['added']),name+' exact additions')
  check(set(old.namelist())-set(z.namelist())==set(row['removed']),name+' exact removals')
  # Unrelated phase-one engine hooks must survive byte-for-byte.
  if name.startswith('spell_engine-'):
   for entry in ['internals/BatchRules','internals/RevisionRules','entity/WarriorUltimates','entity/ClassRework','internals/TrapRules']:
    n='net/spell_engine/'+entry+'.class';check(z.read(n)==old.read(n),'preserved '+entry)
   n='spell_engine.mixins.json'
   mixins=[json.loads(z.read(p)) for p in z.namelist() if p.endswith('.json') and 'mixin' in p]
   check(any('mages.MageArmorMixin' in d.get('mixins',[]) for d in mixins),'armor penetration mixin registered')
  if name.startswith('skill_tree-'):
   for icon in ['winters_chill_ready','frostbite_exposure']:
    check(z.read('assets/skill_tree_rpgs/textures/mob_effect/'+icon+'.png').startswith(b'\x89PNG'),'valid '+icon+' effect texture')
for result in ['candidate-result.txt','regression-candidate-result.txt','client-result.txt']:
 check((H/result).read_text().startswith('PASS'),result)
if '--installed' in sys.argv:
 current={r['jar'] for r in records}
 for row in json.loads((R/'docs/patches/class-balance-phases-1-3/artifacts.json').read_text()):
  if row['jar'] not in current:check(sha(R/'mods'/row['jar'])==row['after'],'unaffected phase1-3 '+row['jar'])
print('PASS',checks,'artifact/installation checks')
(H/'verification.json').write_text(json.dumps({'passed':checks,'installed':'--installed' in sys.argv},indent=2)+'\n')

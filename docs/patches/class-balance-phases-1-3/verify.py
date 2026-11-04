"""Verify exact tested archives, installation, retained inputs and tree contracts."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
from data import DEFENSE,RETIRED,u
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-balance-phases-1-3'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
records=json.loads((H/'artifacts.json').read_text());server=json.loads((H/'candidate-tested.json').read_text());client=json.loads((H/'client-tested.json').read_text())
checks=0
def check(ok,label):
 global checks
 if not ok:raise AssertionError(label)
 checks+=1
for row in records:
 name=row['jar'];candidate=W/'built'/name
 check(sha(candidate)==row['after']==server[name]==client[name],name+' tested hashes')
 if '--installed' in sys.argv:
  check(sha(R/'mods'/name)==row['after'],name+' active copy')
  check(sha(R/'mod fixing/1 fixed packs'/name)==row['after'],name+' fixed-pack copy')
 with ZipFile(candidate) as z:
  check(z.testzip() is None,name+' CRC');check(len(z.namelist())==len(set(z.namelist())),name+' duplicate entries')
  check(not any(n.startswith(('local/audit/','local/spatialclient/')) for n in z.namelist()),name+' no test probes')
  with ZipFile(W/'before'/name) as old:
   check(z.read('META-INF/neoforge.mods.toml')==old.read('META-INF/neoforge.mods.toml'),name+' original mod metadata')
with ZipFile(next((W/'built').glob('adventurer*'))) as z:
 base='data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
 nodes=json.loads(z.read(base+'skills.json'));defs=json.loads(z.read(base+'definitions.json'));edges=json.loads(z.read(base+'connections.json'))['normal']['bidirectional']
 for path in [*DEFENSE,u.F,u.A,u.I]:
  check(len(path)==8,'eight-node path');d=defs[nodes[path[-1]]['definition']]
  check(d['required_skills']==2 and d['size']==1,'normal-sized two-branch mastery')
  neighbors={b if a==path[-1] else a for a,b in edges if path[-1] in [a,b]}
  check(neighbors==set(path[5:7]),'only two penultimates reach mastery')
 for path,icon in zip([u.F,u.A,u.I],['glistering_melon_slice','diamond_boots','writable_book']):
  check(defs[nodes[path[-1]]['definition']]['icon']['data']['item']=='minecraft:'+icon,'requested utility icon')
  check(all('\n' not in defs[nodes[id]['definition']]['title'] for id in path),'single-line utility titles')
 check(not any(id in nodes for id in RETIRED),'retired defense leaves removed')
 with ZipFile(next((W/'before').glob('adventurer*'))) as before:
  old_nodes=json.loads(before.read(base+'skills.json'));old_defs=json.loads(before.read(base+'definitions.json'))
  check(all(defs[nodes[id]['definition']]['icon']==old_defs[old_nodes[id]['definition']]['icon'] for path in DEFENSE for id in path),'all defense icons retained')
for p in (W/'before').glob('*.jar'):
 if p.name not in {r['jar'] for r in records}:check(sha(R/'mods'/p.name)==sha(p),'unmodified shared dependency '+p.name)
check((H/'candidate-result.txt').read_text().startswith('PASS'),'native server result')
check((H/'client-result.txt').read_text().strip()=='PASS','connected client result')
check(sha(R/'modpack-player-handbook.pdf')==sha(R/'output/pdf/modpack-player-handbook.pdf'),'matching handbook copies')
print(f'PASS {checks} artifact, runtime-evidence and tree checks; {len(records)} changed archives')

from pathlib import Path
from zipfile import ZipFile
import json,hashlib
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/rpg-netherite-repairs'
record=json.loads((H/'artifacts.json').read_text());name=record['jar']
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
active=R/'mods'/name;fixed=R/'mod fixing/1 fixed packs'/name
assert sha(active)==sha(fixed)==record['sha256']
with ZipFile(W/'before'/name) as old,ZipFile(active) as new:
 assert new.testzip() is None and len(new.namelist())==len(set(new.namelist()))
 assert set(new.namelist())-set(old.namelist())==set(record['added'])
 assert all(old.read(n)==new.read(n) for n in old.namelist())
 assert not any('probe' in n.lower() for n in new.namelist())
targets=json.loads((H/'target-items.json').read_text());assert len(targets)==53
baseline=json.loads((H/'baseline-items.json').read_text())
assert len(baseline)==240 and all('minecraft:netherite_ingot' in baseline[n] for n in targets)
for label in ['candidate','module-off']:
 assert (H/(label+'-result.txt')).read_text().startswith('PASS 412')
 assert json.loads((H/(label+'-mods.json')).read_text())[name]==record['sha256']
assert (H/'client-result.txt').read_text().startswith('PASS 5')
assert json.loads((H/'client-mods.json').read_text())[name]==record['sha256']
penchant=next((R/'mods').glob('penchant-*.jar'))
assert sha(penchant)=='20a0274f92f20a3777b1a370d3df0943ce0908daea854b423bf0f6c0c3e34015'
assert (R/'modpack-player-handbook.pdf').read_bytes()==(R/'output/pdf/modpack-player-handbook.pdf').read_bytes()
print('PASS archive preservation, matching active/fixed/tested hashes, 53 target items, 412 native checks per module state, 5 connected client checks and unchanged Penchant')

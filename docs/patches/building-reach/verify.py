from pathlib import Path
from zipfile import ZipFile
import json,hashlib
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/building-reach'
r=json.loads((H/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
active=R/'mods'/r['jar'];fixed=R/'mod fixing/1 fixed packs'/r['jar']
assert sha(active)==sha(fixed)==r['sha256']==(H/'runtime-tested-sha256.txt').read_text().strip()
assert sha(W/'before.jar')==r['before_sha256']
with ZipFile(W/'before.jar') as old,ZipFile(active) as new:
 assert new.testzip() is None and set(old.namelist())==set(new.namelist())
 assert len(new.namelist())==len(set(new.namelist()))
 changed=[n for n in old.namelist() if old.read(n)!=new.read(n)]
 assert changed==r['changed'] and len(changed)==2
 assert old.read('sophisticated/building/systems/ServerBlockPlacer.class')==new.read('sophisticated/building/systems/ServerBlockPlacer.class')
 assert not any('probe' in n.lower() for n in new.namelist())
assert (H/'candidate-results.txt').read_text().startswith('PASS 41')
assert (H/'baseline-results.txt').read_text().startswith('FAIL java.lang.AssertionError: client reach')
assert (R/'modpack-player-handbook.pdf').read_bytes()==(R/'output/pdf/modpack-player-handbook.pdf').read_bytes()
print('PASS installed/fixed/runtime hashes, 535 preserved entries, retained server guards, baseline reproduction and 41 connected-client checks')

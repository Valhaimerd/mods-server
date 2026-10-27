from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/lightwell-mote-spacing';r=json.loads((HERE/'artifacts.json').read_text());installed='--installed' in sys.argv
before=WORK/'before'/r['jar'];after=(ROOT/'mods' if installed else WORK/'built')/r['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
if installed:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
with ZipFile(before) as a,ZipFile(after) as b:
 assert b.testzip() is None and a.namelist()==b.namelist()
 changed=[n for n in a.namelist() if a.read(n)!=b.read(n)];assert changed==r['changed']==['net/spell_engine/internals/PriestTuning.class']
 for n in a.namelist():
  if n not in changed:assert a.read(n)==b.read(n)
 print(f'PASS {len(a.namelist())-1} unchanged archive entries; only mote helper changed'+('; installed/fixed match' if installed else ''))

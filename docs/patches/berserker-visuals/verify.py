from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/berserker-visuals';checks=0
installed='--installed' in sys.argv
for r in json.loads((HERE/'artifacts.json').read_text()):
 a=WORK/'before'/r['jar'];b=(ROOT/'mods' if installed else WORK/'built')/r['jar']
 assert hashlib.sha256(a.read_bytes()).hexdigest()==r['before_sha256']
 assert hashlib.sha256(b.read_bytes()).hexdigest()==r['sha256']
 if installed:assert b.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 with ZipFile(a) as old,ZipFile(b) as new:
  assert new.testzip() is None and set(new.namelist())==set(old.namelist())|set(r['added'])
  assert {n for n in old.namelist() if old.read(n)!=new.read(n)}==set(r['changed'])
  for n in old.namelist():
   if n not in r['changed']:assert old.read(n)==new.read(n);checks+=1
print(f'PASS {checks} archive preservation checks; no spell data, damage, cooldown, language or mixin changes')
if '--runtime' in sys.argv:
 tested=json.loads((HERE/'runtime-tested.json').read_text())
 for r in json.loads((HERE/'artifacts.json').read_text()):assert tested[r['jar']]==r['sha256']
 result=(WORK/'client/visual-result.txt').read_text();assert result.startswith('PASS'),result;print(result.strip())

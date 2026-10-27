from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/healing-armor-balance';installed='--installed' in sys.argv;checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=WORK/'before'/r['jar'];after=(ROOT/'mods' if installed else WORK/'built')/r['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
 if installed:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 with ZipFile(before) as a,ZipFile(after) as b:
  assert b.testzip() is None and set(b.namelist())==set(a.namelist())|set(r['added'])
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed'] and len(r['changed'])==len(r['added'])==1
  for n in a.namelist():
   if n not in r['changed']:assert a.read(n)==b.read(n);checks+=1
  assert b'net/minecraft/client/' not in b.read(r['added'][0])
 for label in ['candidate','legacy']:
  assert (HERE/(label+'-results.txt')).read_text().startswith('PASS 29')
  assert json.loads((HERE/(label+'-tested-mods.json')).read_text())[r['jar']]==r['sha256']
print(f'PASS {checks} unchanged archive entries; exact hooks/helpers; fresh and legacy native checks'+('; installed/fixed match' if installed else ''))

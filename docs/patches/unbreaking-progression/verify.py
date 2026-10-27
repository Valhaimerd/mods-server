from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/unbreaking-progression';r=json.loads((HERE/'artifacts.json').read_text());installed='--installed' in sys.argv
before=WORK/'before'/r['jar'];after=(ROOT/'mods' if installed else WORK/'built')/r['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
if installed:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
with ZipFile(before) as a,ZipFile(after) as b:
 assert b.testzip() is None and set(b.namelist())==set(a.namelist())|set(r['added'])
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==['penchant.mixins.json']
 x=json.loads(a.read('penchant.mixins.json'));y=json.loads(b.read('penchant.mixins.json'));assert y['mixins'].pop()=='leveling.PendingUnbreakingMixin' and x==y
 for n in a.namelist():
  if n!='penchant.mixins.json':assert a.read(n)==b.read(n)
 for n in ['data/minecraft/enchantment/unbreaking.json','resourcepacks/durability_rework/data/minecraft/enchantment/unbreaking.json']:assert json.loads(b.read(n))['max_level']==10
 assert b'net/minecraft/client/' not in b.read(r['added'][0])
 print(f'PASS {len(a.namelist())-1} unchanged archive entries; one common mixin added'+('; installed/fixed match' if installed else ''))
for label in ['candidate','module-off']:
 assert (HERE/(label+'-results.txt')).read_text().startswith('PASS 40')
 assert json.loads((HERE/(label+'-tested-mods.json')).read_text())[r['jar']]==r['sha256']
print('PASS 40 native checks with module enabled and 40 disabled, matching candidate hashes')

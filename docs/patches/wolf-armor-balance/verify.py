from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/wolf-armor-balance';checks=0
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for r in json.loads((HERE/'artifacts.json').read_text(encoding='utf-8')):
 before=WORK/'before'/r['jar'];target=(ROOT/'mods' if '--installed' in sys.argv else WORK/'built')/r['jar'];assert sha(before)==r['before_sha256'] and sha(target)==r['sha256']
 if '--installed' in sys.argv:assert target.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/target.name).read_bytes()
 with ZipFile(before) as a,ZipFile(target) as b:
  assert b.testzip() is None
  assert set(b.namelist())==set(a.namelist())|set(r['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  for n in a.namelist():
   if n not in r['changed']:assert a.read(n)==b.read(n);checks+=1
  if r['jar'].startswith('adventurerskills'):
   key='data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json';x=json.loads(a.read(key));y=json.loads(b.read(key))
   assert y['pet_res']['title']=='+3% Pet Resistance'
   assert '+5% Pet Resistance' in y['packleader']['description']['text'] and '+10% Pet Resistance' in y['guardian_stead']['description']['text']
   for k in x:
    if k not in ['pet_res','packleader','guardian_stead']:assert x[k]==y[k]
  if r['jar'].startswith('orb_of'):
   x=json.loads(a.read('skill-overhaul-pets.json'));y=json.loads(b.read('skill-overhaul-pets.json'));assert set(x)==set(y)
   for k,v in x.items():
    expected=dict(v)
    if 'res' in expected:expected.pop('res');expected['res_pct']=.03
    if k=='iggdp0a33bljw4kl':expected['res_pct']=.05
    if k=='8wjzxjnmc1gr7dvq':expected['res_pct']=.10
    assert y[k]==expected
  if r['jar'].startswith('penchant-'):
   assert 'data/minecraft/tags/item/enchantable/armor.json' not in b.namelist()
   for n in ['data/minecraft/enchantment/unbreaking.json','resourcepacks/durability_rework/data/minecraft/enchantment/unbreaking.json']:
    x=json.loads(a.read(n));y=json.loads(b.read(n));y['supported_items']=x['supported_items'];text=json.dumps(y).replace('#penchant:unbreaking_armor','#minecraft:enchantable/armor');assert json.loads(text)==x
 if '--runtime' in sys.argv:
  for label in ['candidate','module-off']:
   assert (HERE/(label+'-results.txt')).read_text(encoding='utf-8').startswith('PASS')
   assert json.loads((HERE/(label+'-tested-mods.json')).read_text(encoding='utf-8'))[r['jar']]==r['sha256']
print(f'PASS {checks} unchanged ZIP entries; exact changes, saved IDs, unrelated rewards and Unbreaking curves preserved')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/priest-plate-doctrine'
rows=json.loads((H/'artifacts.json').read_text());count=0
for x in rows:
 a=W/'before'/x['jar'];b=(R/'mods' if '--installed' in sys.argv else W/'built')/x['jar']
 digest=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 assert digest(a)==x['before_sha256'] and digest(b)==x['sha256']
 if '--installed' in sys.argv:assert b.read_bytes()==(R/'mod fixing/1 fixed packs'/x['jar']).read_bytes()
 with ZipFile(a) as old,ZipFile(b) as new:
  assert new.testzip() is None
  assert set(new.namelist())==set(old.namelist())|set(x['added'])
  assert [n for n in old.namelist() if old.read(n)!=new.read(n)]==x['changed']
  count+=len(old.namelist())-len(x['changed'])
  assert not any(n.endswith('.class') for n in x['added'])
  if x['jar'].startswith('skill_tree-'):
   for cat in ['class_skills','weapon_skills']:
    n=f'data/skill_tree_rpgs/puffish_skills/categories/{cat}/definitions.json';o=json.loads(old.read(n));v=json.loads(new.read(n))
    for k in o:
     if k!='priest_root':assert o[k]==v[k]
    assert v['priest_root']['description']['text']=='+3% Healing Power\nPaladin plate armor is forbidden.'
    for k in o['priest_root']:
     if k!='description':assert o['priest_root'][k]==v['priest_root'][k]
   tag=json.loads(new.read('data/skill_tree_rpgs/tags/item/paladin_plate.json'))
   assert len(tag['values'])==20 and len({v['id'] for v in tag['values']})==20
   assert all(v['required']==False for v in tag['values'])
for label in ['candidate','candidate-legacy']:
 assert (H/(label+'-result.txt')).read_text().startswith('PASS 207 checks; failures=0')
 tested=json.loads((H/(label+'-tested.json')).read_text())
 for x in rows:assert tested[x['jar']]==x['sha256']
assert (H/'client/client-result.txt').read_text().startswith('PASS 70 checks')
c=json.loads((H/'client/runtime-manifest.json').read_text())
for x in rows:assert c[x['jar']]==x['sha256']
print(f'PASS: {count} unchanged archive entries; exact scope,207 fresh +207 legacy server checks,70 client checks; artifact hashes match')

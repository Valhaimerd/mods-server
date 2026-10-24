"""Check the exact overlay, unchanged neighboring content, bytecode, data and installed artifacts."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/warrior-tier-two'
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=WORK/'before'/r['jar'];active=ROOT/'mods'/r['jar'];fixed=ROOT/'mod fixing/1 fixed packs'/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 downstream_file=ROOT/'docs/patches/warrior-tier-three/artifacts.json'
 downstream=next((x for x in json.loads(downstream_file.read_text()) if x['jar']==r['jar'] and x['before_sha256']==r['sha256']),None) if downstream_file.exists() else None
 audited=ROOT/'tmp/warrior-tier-three/before'/r['jar'] if downstream else active
 assert hashlib.sha256(audited.read_bytes()).hexdigest()==r['sha256']
 expected=downstream['sha256'] if downstream else r['sha256']
 for suffix in ['class-descriptions','tenacity-followup']:
  next_file=ROOT/'docs/patches'/suffix/'artifacts.json'
  layer=next((x for x in json.loads(next_file.read_text()) if x['jar']==r['jar'] and x['before_sha256']==expected),None) if next_file.exists() else None
  if layer:expected=layer['sha256']
 assert hashlib.sha256(active.read_bytes()).hexdigest()==expected
 assert active.read_bytes()==fixed.read_bytes()
 with ZipFile(before) as a,ZipFile(audited) as b:
  assert b.testzip() is None
  assert set(a.namelist())<=set(b.namelist())
  assert set(b.namelist())-set(a.namelist())==set(r['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  for n in r['changed']+r['added']:
   assert 'Probe' not in n and 'PatchTierTwo' not in n
   if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
   if n.endswith('.json'):json.loads(b.read(n))
  for n in r['changed']:
   if n.endswith('.json') and n.startswith('data/'):
    o=json.loads(b.read(n))
    assert '/spell/warrior_' in n or o.get('group','').startswith('warrior_'),n
    if o.get('type')!='MODIFIER':
     assert o['cost']['cooldown']['haste_affected']
     if 'active' in o:assert o['active']['cast']['haste_affected']
  for n in ['assets/rogues/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']:
   if n in b.namelist():
    old=json.loads(a.read(n));new=json.loads(b.read(n))
    for k in set(old)|set(new):
     if old.get(k)!=new.get(k):assert k.startswith(('spell.rogues.throw.','spell.rogues.throw_net.','spell.skill_tree_rpgs.warrior_','effect.skill_tree_rpgs.vitality','effect.skill_tree_rpgs.killing_spree','effect.skill_tree_rpgs.warrior_','effect.rogues.charge','effect.rogues.demoralize','spell.rogues.charge.','spell.rogues.shout.')),k
 print('PASS exact Warrior-only resource changes, preserved prior entries, Java 25 classes, active/fixed hashes:',r['jar'])
result=WORK/'server/warrior-result.txt';assert result.read_text().startswith('PASS ');print(result.read_text().strip())

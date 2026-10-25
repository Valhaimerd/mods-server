from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-rework'
installed='--installed' in sys.argv;checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 source=WORK/'before'/r['jar'];target=(ROOT/'mods' if installed else WORK/'built')/r['jar']
 assert hashlib.sha256(source.read_bytes()).hexdigest()==r['before_sha256']
 assert hashlib.sha256(target.read_bytes()).hexdigest()==r['sha256']
 if installed:assert target.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 with ZipFile(source) as a,ZipFile(target) as b:
  assert b.testzip() is None and set(b.namelist())==set(a.namelist())|set(r['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  for n in a.namelist():
   if n not in r['changed']:assert a.read(n)==b.read(n);checks+=1
  for n in r['changed']:
   if n.endswith('/lang/en_us.json'):
    old=json.loads(a.read(n));new=json.loads(b.read(n))
    for key in old:
     if old[key]!=new[key]:
      assert any(x in key for x in ['priest_','holy_beam','levitate','penance','circle_of_healing']),key
      if key.endswith('.description'):assert '{' not in new[key] and 'floor' not in new[key] and 'Base cooldown' not in new[key]
     checks+=1
  if r['jar'].startswith('paladins-'):
   def spell(key):return json.loads(b.read('data/paladins/spell/'+key+'.json'))
   assert spell('penance')['tier']==2 and spell('levitate')['tier']==3
   assert spell('penance')['active']['cast']['channel']['ticks']==10
   assert spell('penance')['range']==26 and spell('holy_beam')['cost']['cooldown']['duration']==24
   assert spell('levitate')['cost']['cooldown']['proportional'] is False
   assert spell('circle_of_healing')['cost']['cooldown']['duration']==16
  if r['jar'].startswith('skill_tree-'):
   for category in ['class_skills','weapon_skills']:
    n=f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json';old=json.loads(a.read(n));new=json.loads(b.read(n));assert old.keys()==new.keys()
    allowed={f'priest_tier_{tier}_spell_2_{suffix}' for tier in [2,3] for suffix in ['root','modifier_1','modifier_2']}
    assert all(old[k]==new[k] for k in old if k not in allowed)
    for suffix in ['root','modifier_1','modifier_2']:
     first=f'priest_tier_2_spell_2_{suffix}';second=f'priest_tier_3_spell_2_{suffix}'
     assert new[first]==old[second] and new[second]==old[first];checks+=2
print(f'PASS {checks} archive, scope, branch-swap and description checks'+('; installed/fixed copies match' if installed else '; candidates'))

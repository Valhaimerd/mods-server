from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/warrior-balance-20261025'
installed='--installed' in sys.argv;checks=0
for record in json.loads((HERE/'artifacts.json').read_text()):
 source=WORK/'before'/record['jar'];target=(ROOT/'mods' if installed else WORK/'built')/record['jar']
 assert hashlib.sha256(source.read_bytes()).hexdigest()==record['before_sha256']
 assert hashlib.sha256(target.read_bytes()).hexdigest()==record['sha256']
 if installed:assert target.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/target.name).read_bytes()
 with ZipFile(source) as a,ZipFile(target) as b:
  assert b.testzip() is None
  assert set(b.namelist())==set(a.namelist())|set(record['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(record['changed'])
  for n in a.namelist():
   if n not in record['changed']:assert a.read(n)==b.read(n);checks+=1
  for n in record['changed']:
   if n.endswith('/lang/en_us.json'):
    old=json.loads(a.read(n));new=json.loads(b.read(n));assert old.keys()==new.keys()
    for k in old:
     if old[k]!=new[k]:
      assert k.endswith('.description')
      assert not any(x in new[k] for x in ['{impact','floor','percentage points','Base cooldown','reduced by haste'])
      if k.startswith('spell.'):assert '%' not in new[k].replace('%%','')
     checks+=1
  if target.name.startswith('rogues-'):
   assert json.loads(b.read('data/rogues/spell/mortal_strike.json'))['impacts'][0]['action']['status_effect']['duration']==14
  if target.name.startswith('skill_tree-'):
   assert json.loads(b.read('data/skill_tree_rpgs/spell/warrior_tier_3_spell_1_modifier_2.json'))['impacts'][0]['action']['status_effect']['duration']==1
   assert json.loads(b.read('data/skill_tree_rpgs/spell/warrior_tier_4_spell_1_root.json'))['modifiers'][0]['effect_duration_add']==3
   for key,duration in [('warrior_tier_4_spell_1_modifier_2',.2),('warrior_tier_4_spell_2_modifier_2',.25)]:
    cooldown=json.loads(b.read('data/skill_tree_rpgs/spell/'+key+'.json'))['cost']['cooldown']
    assert cooldown['duration']==duration and cooldown['haste_affected'] is True
print(f'PASS {checks} archive preservation and tooltip checks; '+('installed and fixed copies match' if installed else 'candidates'))

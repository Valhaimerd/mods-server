from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/rogue-dual-balance';folder=ROOT/'mods' if '--installed' in sys.argv else WORK/'built';checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 jar=folder/r['jar'];assert hashlib.sha256(jar.read_bytes()).hexdigest()==r['sha256']
 with ZipFile(WORK/'before'/r['jar']) as a,ZipFile(jar) as b:
  assert a.namelist()==b.namelist() and b.testzip() is None
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
  for n in a.namelist():
   if n not in r['changed']:assert a.read(n)==b.read(n)
   checks+=1
  for n in r['changed']:
   if not n.endswith('.json'):continue
   o=json.loads(b.read(n));old=json.loads(a.read(n))
   if '/definitions.json' in n:
    allowed={'attack_speed+5%'} if r['jar'].startswith('adventurer') else {'rogue_root','rogue_boost'}
    assert o.keys()==old.keys() and all(o[k]==old[k] for k in old if k not in allowed)
    for key in allowed:assert o[key]['rewards'][0]['data']['value']==(.02 if r['jar'].startswith('adventurer') else .01)
   if n.startswith('data/rogues/spell/') or n.startswith('data/skill_tree_rpgs/spell/rogue_'):assert o.get('school')!='spell_power:physical_melee',n
  if r['jar'].startswith('rogues'):
   o=json.loads(b.read('data/rogues/spell/slice_and_dice.json'));assert o['impacts'][0]['action']['damage']['spell_power_coefficient']==.08
  if r['jar'].startswith('skill_tree'):
   o=json.loads(b.read('data/skill_tree_rpgs/spell/rogue_tier_4_spell_2_modifier_2.json'));assert o['modifiers'][0]['impacts'][0]['action']['status_effect']['duration']==10
 if '--installed' in sys.argv:assert jar.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS',checks,'archive, node, dual-school, coefficient and unrelated-entry checks')

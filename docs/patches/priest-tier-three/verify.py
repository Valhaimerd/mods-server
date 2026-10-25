from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-tier-three'
installed='--installed' in sys.argv;baseline='--baseline' in sys.argv;checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 source=WORK/'before'/r['jar'];target=(WORK/'before' if baseline else ROOT/'mods' if installed else WORK/'built')/r['jar']
 assert hashlib.sha256(source.read_bytes()).hexdigest()==r['before_sha256']
 if not baseline:assert hashlib.sha256(target.read_bytes()).hexdigest()==r['sha256']
 if installed:assert target.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 with ZipFile(source) as a,ZipFile(target) as b:
  assert b.testzip() is None
  if not baseline:
   assert set(b.namelist())==set(a.namelist())|set(r['added'])
   assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  for n in a.namelist():
   if n not in r['changed']:assert a.read(n)==b.read(n);checks+=1
  def load(n):return json.loads(b.read(n))
  if r['jar'].startswith('paladins-'):
   well=load('data/paladins/spell/lightwell.json');assert well['cost']['cooldown']['duration']==80,'Lightwell cooldown must be 80s'
   assert well['cost']['cooldown']['haste_affected'] is True
   d=well['impacts'][0]['action']['summon'];assert d['behaviour']['lifespan']['active_seconds']==16 and d['behaviour']['actions'][0]['spell_cast']['cooldown']==16
   assert d['attribute_scaling']['entries'][0]['modifiers'][0]['coefficient']==.05
   orb=load('data/paladins/spell/lightwell_orb.json');assert orb['range']==16 and orb['deliver']['projectile']['launch_properties']['velocity']==.6
   assert orb['impacts'][0]['action']['heal']['spell_power_coefficient']==1 and orb['cost']['cooldown']['duration']==.8
   barrier=load('data/paladins/spell/barrier.json');assert barrier['range']==10 and barrier['active']['cast']['duration']==1 and barrier['cost']['cooldown']['duration']==60
  if r['jar'].startswith('skill_tree-'):
   root=load('data/skill_tree_rpgs/spell/priest_tier_4_spell_2_root.json');assert root['modifiers']==[{'spell_pattern':'paladins:barrier','spawn_duration_add':2}]
   lang=load('assets/skill_tree_rpgs/lang/en_us.json');assert lang['spell.skill_tree_rpgs.priest_tier_4_spell_2_modifier_2.name']==lang['skill.skill_tree_rpgs.priest_tier_4_spell_2_modifier_2.title']=='Deflective Shield'
   assert a.read('data/skill_tree_rpgs/spell/priest_tier_4_spell_2_modifier_1.json')==b.read('data/skill_tree_rpgs/spell/priest_tier_4_spell_2_modifier_1.json'),'Sacred Refuge must remain unchanged'
  for n in b.namelist():
   if n.endswith('.json') and '/spell/' in n:
    o=load(n)
    if o.get('group','').startswith('priest_') or n.startswith('data/skill_tree_rpgs/spell/priest_'):
     if 'active' in o:assert o['active']['cast']['haste_affected'] is True,n
     if 'cooldown' in o.get('cost',{}):assert o['cost']['cooldown']['haste_affected'] is True,n
     checks+=1
if '--runtime' in sys.argv:
 tested=json.loads((HERE/'runtime-tested.json').read_text())
 for r in json.loads((HERE/'artifacts.json').read_text()):
  assert tested[r['jar']]==r['sha256'],'Runtime-tested archive differs'
 assert len((HERE/'runtime-results.txt').read_text().splitlines())==594
 assert (HERE/'runtime-results.txt').read_text().count('PASS')==544
 assert 'FAIL' not in (HERE/'runtime-results.txt').read_text()
 assert (HERE/'client-results.txt').read_text().count('PASS ')>=14
print(f'PASS {checks} archive preservation and Priest haste/resource checks')

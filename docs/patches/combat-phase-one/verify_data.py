"""Verify requested public resource contracts against a directory of complete JARs."""
from pathlib import Path
import sys, zipfile, json
from data import MAGE,UPGRADES
root=Path(sys.argv[1] if len(sys.argv)>1 else 'tmp/combat-phase-one/built')
checks=0; errors=[]
def check(label, actual, expected):
 global checks
 checks+=1
 if actual!=expected: errors.append(f'{label}: {actual!r} != {expected!r}')
def read(prefix,path):
 with zipfile.ZipFile(next(root.glob(prefix+'*.jar'))) as z:return json.loads(z.read(path))
for id in ['arcane_missile','arcane_explosion']:
 d=read('wizards-',f'data/wizards/spell/{id}.json')
 check(id+' range',d['range'],40)
 check(id+' cooldown',d['cost']['cooldown']['duration'],12)
 check(id+' proportional',d['cost']['cooldown'].get('proportional',False),False)
for id,value in [('flash_heal',30)]:check(id+' range',read('paladins-',f'data/paladins/spell/{id}.json')['range'],value)
for prefix,id,duration in [('paladins-','blessed_strikes',1.5),('paladins-','judgement',.25),('rogues-','throw',.3),('rogues-','throw_net',.3)]:
 check(id+' cast',read(prefix,f'data/{prefix[:-1]}/spell/{id}.json')['active']['cast']['duration'],duration)
check('Purgatory cooldown',read('paladins-','data/paladins/spell/immolation.json')['cost']['cooldown']['duration'],12)
for school in ['arcane','fire','frost']:
 d=read('skill_tree-',f'data/skill_tree_rpgs/spell/{school}_tier_3_passive_2.json')
 check(school+' Ward cooldown',d['cost']['cooldown']['duration'],15)
for id in ['arcane_tier_2_passive_1','arcane_tier_2_passive_2','fire_tier_2_passive_1','frost_tier_2_passive_1']:
 check(id+' cooldown',read('skill_tree-',f'data/skill_tree_rpgs/spell/{id}.json')['cost']['cooldown'].get('duration',0),4)
echo=read('skill_tree-','data/skill_tree_rpgs/spell/arcane_tier_2_spell_2_modifier_1.json')
check('Echoing no resetting impacts',echo.get('impacts',[]),[])
check('Echoing no hit triggers',echo.get('passive',{}).get('triggers',[]),[])
for id,value in MAGE.items():
 d=read('wizards-',f'data/wizards/spell/{id}.json')
 check(id+' damage coefficient',next(i['action']['damage'] for i in d['impacts'] if i['action']['type']=='DAMAGE')['spell_power_coefficient'],value)
for id,value in UPGRADES.items():check(id+' upgrade damage',read('skill_tree-',f'data/skill_tree_rpgs/spell/{id}.json')['impacts'][0]['action']['damage']['spell_power_coefficient'],value)
for category in ['class_skills','weapon_skills']:
 d=read('skill_tree-',f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json')
 for key in ['warrior_root','warrior_boost','rogue_root','rogue_boost']:
  check(category+' '+key,d[key]['rewards'][0]['data']['value'],.04 if key.startswith('warrior') else .02)
check('Adventurer melee reward',read('adventurerskills-','data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json')['melee_damage+3%']['rewards'][0]['data']['value'],.03)
before=Path('tmp/combat-phase-one/before')
if root!=before:
 for jar in root.glob('*.jar'):
  if not (before/jar.name).exists():continue
  with zipfile.ZipFile(jar) as z,zipfile.ZipFile(before/jar.name) as old:
   check(jar.name+' ZIP integrity',z.testzip(),None)
   check(jar.name+' no removed entries',set(old.namelist())-set(z.namelist()),set())
   for n in z.namelist():
    if n.endswith('.class') and (n not in old.namelist() or old.read(n)!=z.read(n)):
     check(n+' Java25 bytecode',int.from_bytes(z.read(n)[6:8],'big'),69)
    if 'probe' in n.lower():errors.append('Unexpected probe entry '+n)
   if jar.name.startswith('skill_tree-'):
    for school in ['arcane','fire','frost']:
     for tier in range(1,4):
      for pair in [1,2]:
       n=f'data/skill_tree_rpgs/spell/{school}_tier_{tier}_passive_{pair}.json'
       a=json.loads(old.read(n));b=json.loads(z.read(n))
       if (tier,pair) not in [(2,1),(2,2),(3,2)]:check('Excluded passive '+n,a,b)
       elif not (school=='arcane' and (tier,pair) in [(2,1),(3,2)]):check('Retained passive impacts '+n,a.get('impacts'),b.get('impacts'))
   for n in old.namelist():
    if '/spell/' in n and n.endswith('.json') and n.startswith('data/'):
     a=json.loads(old.read(n));b=json.loads(z.read(n))
     def heals(v):
      if isinstance(v,dict):return ([v['heal']] if 'heal' in v else [])+sum((heals(x) for x in v.values()),[])
      if isinstance(v,list):return sum((heals(x) for x in v),[])
      return []
     if heals(a):check('Healing unchanged '+n,heals(a),heals(b))
print(f'{checks} resource checks, {len(errors)} failures')
print('\n'.join(errors))
sys.exit(bool(errors))


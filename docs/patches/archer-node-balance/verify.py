from pathlib import Path
from zipfile import ZipFile
import json,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/archer-node-balance';checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 p=WORK/'built'/r['jar'];assert hashlib.sha256(p.read_bytes()).hexdigest()==r['sha256']
 with ZipFile(WORK/'before'/r['jar']) as old,ZipFile(p) as new:
  assert old.namelist()==new.namelist() and new.testzip() is None
  for n in old.namelist():
   if n not in r['changed']:assert old.read(n)==new.read(n),n;checks+=1
   else:
    a=json.loads(old.read(n));b=json.loads(new.read(n));assert a.keys()==b.keys()
    allowed={'ranged_damage+3%','ranged_penetration','hunter'} if r['jar'].startswith('adventurer') else {'archer_boost','archer_root','weapon_bow_root'}
    assert all(a[k]==b[k] for k in a if k not in allowed)
    if r['jar'].startswith('adventurer'):
     assert b['ranged_damage+3%']['rewards'][0]['data']['value']==.02
     assert b['ranged_penetration']['rewards'][0]['data']=={'attribute':'minecraft:movement_speed','value':.03,'operation':'multiply_total'}
     assert all(x['data']['attribute']!='puffish_attributes:armor_shred' for x in b['hunter']['rewards'])
     assert '+10% Armor Penetration' not in b['hunter']['description']['text']
     assert next(x['data']['value'] for x in b['hunter']['rewards'] if x['data']['attribute']=='puffish_attributes:ranged_damage')==.06
    else:
     for key in allowed:assert b[key]['rewards'][0]['data']['value']==.02 and b[key]['description']['text']=='+2% Ranged Damage'
    checks+=len(allowed)
print('PASS',checks,'archive preservation and node checks')

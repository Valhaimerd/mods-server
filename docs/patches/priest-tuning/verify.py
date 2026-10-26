from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-tuning';installed='--installed' in sys.argv;checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 a=WORK/'before'/r['jar'];b=(ROOT/'mods' if installed else WORK/'built')/r['jar']
 assert hashlib.sha256(a.read_bytes()).hexdigest()==r['before_sha256'];assert hashlib.sha256(b.read_bytes()).hexdigest()==r['sha256']
 if installed:assert b.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 with ZipFile(a) as old,ZipFile(b) as new:
  assert new.testzip() is None and set(new.namelist())==set(old.namelist())|set(r['added'])
  assert {n for n in old.namelist() if old.read(n)!=new.read(n)}==set(r['changed'])
  for n in old.namelist():
   if n not in r['changed']:assert old.read(n)==new.read(n);checks+=1
  for n in r['changed']:
   if n.endswith('/lang/en_us.json'):
    x=json.loads(old.read(n));y=json.loads(new.read(n));assert x.keys()==y.keys()
    for k in x:
     if x[k]!=y[k]:assert k.endswith('.description') and any(s in k for s in ['priest_','holy_beam','penance','lightwell','weapon_holy_root']);assert '{' not in y[k] and 'floor' not in y[k];assert '%' not in y[k].replace('%%','')
     checks+=1
  if r['jar'].startswith('skill_tree'):
   o=json.loads(new.read('data/skill_tree_rpgs/spell/weapon_holy_root.json'));assert o['modifiers']==[{'spell_pattern':'paladins:holy_shock','impact_filters':[{'type':'DAMAGE'}],'power_modifier':{'power_multiplier':.05}}]
  if r['jar'].startswith('paladins'):
   p=json.loads(new.read('data/paladins/spell/penance.json'));h=json.loads(new.read('data/paladins/spell/holy_beam.json'))
   assert p['active']['cast']['duration']==5 and p['active']['cast']['channel']['ticks']==10 and p['cost']['cooldown']['duration']==12
   assert h['active']['cast']['duration']==10 and h['active']['cast']['channel']['ticks']==25 and h['cost']['cooldown']['duration']==24
   assert p['impacts'][0]['action']['damage']['spell_power_coefficient']==.15
   assert h['impacts'][0]['action']['heal']['spell_power_coefficient']==.10 and h['impacts'][1]['action']['damage']['spell_power_coefficient']==.20
   # Well power, cooldown, lifetime, range and its upgrades are handled without changing their resources.
   for name in ['lightwell','lightwell_orb','holy_shock']:assert old.read('data/paladins/spell/'+name+'.json')==new.read('data/paladins/spell/'+name+'.json')
print(f'PASS {checks} archive, description and requested-resource checks'+('; installed/fixed match' if installed else '; candidates'))

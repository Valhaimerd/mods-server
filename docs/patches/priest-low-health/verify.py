from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-low-health';installed='--installed' in sys.argv;checks=0
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
   for key in ['priest_tier_3_passive_1','priest_tier_3_passive_2']:
    name='data/skill_tree_rpgs/spell/'+key+'.json';x=json.loads(old.read(name));y=json.loads(new.read(name))
    assert x['impacts'][0]['visuals']==y['impacts'][0]['visuals'] and x['impacts'][0]['sound']==y['impacts'][0]['sound']
    checks+=2
print(f'PASS {checks} archive, description and visual-preservation checks'+('; installed/fixed match' if installed else '; candidates'))

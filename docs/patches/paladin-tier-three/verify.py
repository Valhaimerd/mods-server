from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/paladin-tier-three';installed='--installed' in sys.argv
rows=json.loads((H/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();unchanged=0
for row in rows:
 before=W/'before'/row['jar'];after=(R/'mods' if installed else W/'built')/row['jar'];assert sha(before)==row['before_sha256'];assert sha(after)==row['sha256']
 if installed:assert after.read_bytes()==(R/'mod fixing/1 fixed packs'/row['jar']).read_bytes()
 with ZipFile(before) as a,ZipFile(after) as b:
  assert b.testzip() is None;assert set(b.namelist())==set(a.namelist())|set(row['added'])
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==row['changed'];unchanged+=len(a.namelist())-len(row['changed'])
  for n in a.namelist():
   if n.endswith('/connections.json') or n.endswith('/skills.json') or n.endswith('neoforge.mods.toml'):assert a.read(n)==b.read(n),n
   if n.endswith('/definitions.json'):
    old=json.loads(a.read(n));new=json.loads(b.read(n));assert old.keys()==new.keys()
    for key in old:
     if not key.startswith('paladin_tier_4_') and key not in ['paladin_tier_3_passive_1','paladin_tier_3_passive_2']:assert old[key]==new[key],key
  for n in row['changed']+row['added']:
   assert 'probe' not in n.lower(),n
   if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69,n
for label in ['candidate','candidate-legacy']:
 assert (H/(label+'-result.txt')).read_text().startswith('PASS 196 checks; failures=0')
 tested=json.loads((H/(label+'-tested.json')).read_text())
 for row in rows:assert tested[row['jar']]==row['sha256'],(label,row['jar'])
 assert 'EnhancedCats-NeoForge-26.2-1.2.1.jar' in tested
 assert not any('yet_another_config' in n or 'cutecats' in n for n in tested)
assert (H/'client/client-result.txt').read_text().startswith('PASS 36 checks')
client=json.loads((H/'client/runtime-manifest.json').read_text())
for row in rows:assert client[row['jar']]==row['sha256'],row['jar']
if installed:
 assert not list((R/'mods').glob('cutecats-*.jar'))
 assert sha(R/'mods/EnhancedCats-NeoForge-26.2-1.2.1.jar')=='9d14cf4397733fcce03f524f66d74ffeb1a0c3bbd0cea3095a2e59fd38ef125f'
print(f'PASS {len(rows)} exact tested archives; {unchanged} preserved entries;196 fresh +196 stale native checks;36 client checks'+('; active/fixed copies match' if installed else ''))

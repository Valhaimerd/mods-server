from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys,io
from PIL import Image
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/paladin-revamp';installed='--installed' in sys.argv
rows=json.loads((H/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();unchanged=0
for row in rows:
 before=W/'before'/row['jar'];after=(R/'mods' if installed else W/'built')/row['jar'];assert sha(before)==row['before_sha256'];assert sha(after)==row['sha256']
 if installed:assert after.read_bytes()==(R/'mod fixing/1 fixed packs'/row['jar']).read_bytes()
 with ZipFile(before) as a,ZipFile(after) as b:
  assert b.testzip() is None;assert set(b.namelist())==set(a.namelist())|set(row['added'])
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==row['changed'];unchanged+=len(a.namelist())-len(row['changed'])
  for n in a.namelist():
   if n.endswith('/connections.json') or n.endswith('/skills.json') or n.endswith('neoforge.mods.toml'):assert a.read(n)==b.read(n),n
  for n in row['added']:
   assert 'probe' not in n.lower(),n
   if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69,n
  for n in row['changed']+row['added']:
   if '/textures/mob_effect/' in n:
    img=Image.open(io.BytesIO(b.read(n)));assert img.mode=='RGBA';assert img.size!=(64,64) or img.getpixel((0,0))[3]==0
for label in ['candidate','candidate-legacy']:
 assert (H/(label+'-result.txt')).read_text().startswith('PASS 77 checks; failures=0')
 tested=json.loads((H/(label+'-tested.json')).read_text())
 for row in rows:assert tested[row['jar']]==row['sha256'],(label,row['jar'])
assert (H/'client/client-result.txt').read_text().startswith('PASS 26 checks')
client=json.loads((H/'client/runtime-manifest.json').read_text())
for row in rows:assert client[row['jar']]==row['sha256'],row['jar']
with ZipFile(next((W/'built').glob('paladins-*'))) as z:
 assert json.loads(z.read('data/paladins/spell/divine_protection.json'))['cost']['cooldown']['duration']==28
 assert json.loads(z.read('data/paladins/spell/judgement.json'))['active']['cast']['duration']==.75
 for n in z.namelist():
  if n.startswith('data/paladins/spell/') and n.endswith('.json'):assert b'undead' not in z.read(n),n
print(f'PASS {len(rows)} exact tested archives; {unchanged} preserved entries; 77 fresh +77 legacy native checks;26 client checks'+('; active/fixed copies match' if installed else ''))

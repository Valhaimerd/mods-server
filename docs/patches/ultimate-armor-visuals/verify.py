from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys,io
from PIL import Image
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/ultimate-armor-visuals';r=json.loads((HERE/'artifacts.json').read_text());checks=0
installed='--installed' in sys.argv;a=WORK/'before'/r['jar'];b=(ROOT/'mods' if installed else WORK/'built')/r['jar']
assert hashlib.sha256(a.read_bytes()).hexdigest()==r['before_sha256']
assert hashlib.sha256(b.read_bytes()).hexdigest()==r['sha256']
if installed:assert b.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
with ZipFile(a) as old,ZipFile(b) as new:
 assert new.testzip() is None and set(new.namelist())==set(old.namelist())|set(r['added'])
 assert {n for n in old.namelist() if old.read(n)!=new.read(n)}==set(r['changed'])
 for n in old.namelist():
  if n not in r['changed']:assert old.read(n)==new.read(n);checks+=1
 x=json.loads(old.read('spell_engine.mixins.json'));y=json.loads(new.read('spell_engine.mixins.json'))
 assert y['client'][-2:]==['client.render.tint.UltimateRenderStateMixin','client.render.tint.UltimateGlintMixin'];y['client']=y['client'][:-2];assert x==y
 im=Image.open(io.BytesIO(new.read('assets/spell_engine/textures/misc/last_stand_glint.png'))).convert('RGBA')
 assert all(red>=green>=blue for red,green,blue,alpha in im.get_flattened_data())
 assert len({red for red,green,blue,alpha in im.get_flattened_data()})>10
 assert b'net/minecraft/client/' not in new.read('net/spell_engine/entity/LastStandAppearance.class')
print(f'PASS {checks} archive preservation checks; gold texture, client-only mixins and unchanged gameplay verified')
if '--runtime' in sys.argv:
 assert json.loads((HERE/'runtime-tested.json').read_text())[r['jar']]==r['sha256']
 result=(WORK/'client/visual-result.txt').read_text();assert result.startswith('PASS'),result;print(result.strip())

from pathlib import Path
import json,zipfile,hashlib,struct
root=Path(__file__).resolve().parents[4]
original=root/'mod fixing/delight extras (optional)/oceansdelight-neoforge-1.0.4-1.21.1.jar'
candidate=root/'tmp/delight-batch/oceansdelight/oceansdelight-26.2-1.0.4-port1.jar'
a=zipfile.ZipFile(original);b=zipfile.ZipFile(candidate)
def norm(x):
 if isinstance(x,list):return [norm(y) for y in x]
 if isinstance(x,dict):
  if set(x)=={'item'} and isinstance(x['item'],str):return x['item']
  if set(x)=={'tag'}:return '#'+x['tag']
  return {k:norm(v) for k,v in x.items()}
 return x
recipes=[n for n in a.namelist() if '/recipe/' in n and n.endswith('.json')]
for n in recipes:assert norm(json.loads(a.read(n)))==json.loads(b.read(n)),n
textures=[n for n in a.namelist() if '/textures/' in n]
for n in textures:assert a.read(n)==b.read(n),n
models=[n for n in a.namelist() if '/models/item/' in n and n.endswith('.json')]
for n in models:
 assert json.loads(a.read(n))==json.loads(b.read(n)),n
 assert 'assets/oceansdelight/items/'+Path(n).name in b.namelist(),n
loot=[n for n in a.namelist() if n.startswith('data/farmersdelight/loot_modifiers/') and n.endswith('.json')]
for n in loot:
 d=json.loads(a.read(n))
 for c in d['conditions']:
  if 'type' in c.get('predicate',{}):c['predicate']['minecraft:entity_type']=c['predicate'].pop('type')
 assert d==json.loads(b.read(n)),n
# The seven soup models preserve their geometry/UVs while using the renamed FD texture.
fd=zipfile.ZipFile(root/'mods/FarmersDelight-26.2-1.3.3-fix5.jar')
block_models=[n for n in a.namelist() if '/models/block/' in n and n.endswith('.json')]
asset_refs=0
for n in block_models:
 expected=json.loads(a.read(n).decode().replace('farmersdelight:block/tray_pie_leftover','farmersdelight:block/shepherds_pie_leftovers'))
 expected['render_type']='minecraft:translucent'
 actual=json.loads(b.read(n));assert expected==actual,n
 for value in actual.get('textures',{}).values():
  if value.startswith('#'):continue
  namespace,path=value.split(':',1)
  archive=fd if namespace=='farmersdelight' else b
  assert f'assets/{namespace}/textures/{path}.png' in archive.namelist(),(n,value)
  asset_refs+=1
classes=[n for n in b.namelist() if n.endswith('.class')]
assert all(struct.unpack('>H',b.read(n)[6:8])[0]==69 for n in classes)
assert not any(n.startswith('vectorwing/') for n in b.namelist())
assert not any('/probe/' in n for n in b.namelist())
report={'candidate':str(candidate),'sha256':hashlib.sha256(candidate.read_bytes()).hexdigest(),'original_sha256':hashlib.sha256(original.read_bytes()).hexdigest(),'recipes_preserved':len(recipes),'item_models_preserved':len(models),'textures_preserved':len(textures),'loot_modifiers_preserved':len(loot),'adapted_block_models':len(block_models),'texture_refs_resolved':asset_refs,'java_class_major':69,'embedded_fd':False}
print(json.dumps(report,indent=2))

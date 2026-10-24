"""Prove exact description-only changes, complete class coverage and installed copies."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,re,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods'
prefixes=('spell.archers.','spell.wizards.','spell.skill_tree_rpgs.archer_','spell.skill_tree_rpgs.arcane_','spell.skill_tree_rpgs.fire_','spell.skill_tree_rpgs.frost_')
total=0
for record in json.loads((HERE/'artifacts.json').read_text()):
 before=ROOT/'tmp/class-descriptions/before'/record['jar'];after=folder/record['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==record['before_sha256']
 downstream_file=ROOT/'docs/patches/tenacity-followup/artifacts.json'
 downstream=next((x for x in json.loads(downstream_file.read_text()) if x['jar']==record['jar'] and x['before_sha256']==record['sha256']),None) if downstream_file.exists() else None
 audited=ROOT/'tmp/tenacity-followup/before'/record['jar'] if downstream and folder.resolve()==(ROOT/'mods').resolve() else after
 expected=downstream['sha256'] if downstream and folder.resolve()==(ROOT/'mods').resolve() else record['sha256']
 assert hashlib.sha256(audited.read_bytes()).hexdigest()==record['sha256']
 assert hashlib.sha256(after.read_bytes()).hexdigest()==expected
 if folder.resolve()==(ROOT/'mods').resolve():assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/record['jar']).read_bytes()
 with ZipFile(before) as a,ZipFile(audited) as b:
  assert b.testzip() is None
  assert a.namelist()==b.namelist()
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==record['changed']
  for path in record['changed']:
   old=json.loads(a.read(path));new=json.loads(b.read(path));assert old.keys()==new.keys()
   assert sorted(k for k in old if old[k]!=new[k])==record['description_keys']
   for k in record['description_keys']:assert k.endswith('.description') and k.startswith(prefixes)
   for k,v in new.items():
    if not k.startswith(prefixes) or not k.endswith('.description'):continue
    assert not re.search(r'[{}]|\bfloor\b|\bceil\b|\bcoefficient\b|\bdeliverable\b',v,re.I),(k,v)
    assert not re.search(r'(?<!%)%(?!%)',v),(k,v)
    total+=1
 print('PASS language-only changes, identical gameplay/bytecode, readable descriptions:',record['jar'])
print('PASS',total,'Archer/Arcane/Fire/Frost spell and node descriptions audited')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/tenacity-followup';folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods'
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=WORK/'before'/r['jar'];after=folder/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 next_file=ROOT/'docs/patches/skill-tooltip-polish/artifacts.json'
 layer=next((x for x in json.loads(next_file.read_text()) if x['jar']==r['jar'] and x['before_sha256']==r['sha256']),None) if next_file.exists() and folder.resolve()==(ROOT/'mods').resolve() else None
 audited=ROOT/'tmp/skill-tooltip-polish/before'/r['jar'] if layer else after
 assert hashlib.sha256(audited.read_bytes()).hexdigest()==r['sha256']
 assert hashlib.sha256(after.read_bytes()).hexdigest()==(layer['sha256'] if layer else r['sha256'])
 with ZipFile(before) as a,ZipFile(audited) as b:
  assert a.namelist()==b.namelist() and b.testzip() is None
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
  if r['jar'].startswith('skill_tree'):
   path='assets/skill_tree_rpgs/lang/en_us.json';old=json.loads(a.read(path));new=json.loads(b.read(path));assert set(k for k in old if old[k]!=new[k])=={'spell.skill_tree_rpgs.warrior_tier_3_passive_2.description','spell.skill_tree_rpgs.arcane_tier_2_spell_1_modifier_1.description'}
  else:assert r['changed']==['net/spell_engine/entity/WarriorUltimates.class'] and int.from_bytes(b.read(r['changed'][0])[6:8],'big')==69
 if folder.resolve()==(ROOT/'mods').resolve():assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 print('PASS exact Tenacity follow-up overlay:',r['jar'])
result=WORK/'server/warrior-result.txt';assert result.read_text().startswith('PASS ');print(result.read_text().strip())

"""Audit the installed overlay against its pinned input archives."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,re
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/warrior-tier-three'
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=WORK/'before'/r['jar'];active=ROOT/'mods'/r['jar'];fixed=ROOT/'mod fixing/1 fixed packs'/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 overlay_file=ROOT/'docs/patches/class-descriptions/artifacts.json'
 overlay=next((x for x in json.loads(overlay_file.read_text()) if x['jar']==r['jar'] and x['before_sha256']==r['sha256']),None) if overlay_file.exists() else None
 audited=ROOT/'tmp/class-descriptions/before'/r['jar'] if overlay else active
 expected=overlay['sha256'] if overlay else r['sha256']
 followup_file=ROOT/'docs/patches/tenacity-followup/artifacts.json'
 followup=next((x for x in json.loads(followup_file.read_text()) if x['jar']==r['jar'] and x['before_sha256']==expected),None) if followup_file.exists() else None
 if followup:
  if not overlay:audited=ROOT/'tmp/tenacity-followup/before'/r['jar']
  expected=followup['sha256']
 next_file=ROOT/'docs/patches/skill-tooltip-polish/artifacts.json'
 layer=next((x for x in json.loads(next_file.read_text()) if x['jar']==r['jar'] and x['before_sha256']==expected),None) if next_file.exists() else None
 if layer:
  if not overlay and not followup:audited=ROOT/'tmp/skill-tooltip-polish/before'/r['jar']
  expected=layer['sha256']
 assert hashlib.sha256(audited.read_bytes()).hexdigest()==r['sha256']
 assert hashlib.sha256(active.read_bytes()).hexdigest()==expected
 assert active.read_bytes()==fixed.read_bytes()
 with ZipFile(before) as a,ZipFile(audited) as b:
  assert b.testzip() is None
  assert set(a.namelist())<=set(b.namelist())
  assert set(b.namelist())-set(a.namelist())==set(r['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  for n in r['changed']+r['added']:
   assert 'Probe' not in n and 'PatchUltimates' not in n
   if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
   if n.endswith('.json'):json.loads(b.read(n))
  for n in ['assets/rogues/lang/en_us.json','assets/skill_tree_rpgs/lang/en_us.json']:
   if n not in b.namelist():continue
   old=json.loads(a.read(n));new=json.loads(b.read(n))
   for k,v in new.items():
    if k.endswith('.description') and k.startswith(('spell.skill_tree_rpgs.warrior_','spell.skill_tree_rpgs.rogue_','spell.rogues.')):
     assert not re.search(r'\{|\bfloor\b|\bceil\b',v,re.I),(k,v)
    if old.get(k)!=v:assert k.startswith(('spell.rogues.mortal_strike.','spell.rogues.last_stand.','spell.skill_tree_rpgs.warrior_','spell.skill_tree_rpgs.rogue_tier_4_spell_2_root.','effect.skill_tree_rpgs.')),(n,k)
 print('PASS exact overlay, Java 25 bytecode, readable Warrior/Rogue descriptions, installed/fixed hash:',r['jar'])
result=WORK/'server/warrior-result.txt';assert result.read_text().startswith('PASS ');print(result.read_text().strip())

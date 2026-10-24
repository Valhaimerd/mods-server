from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/flat-bleed';candidate='--candidate' in sys.argv
records=json.loads((HERE/'artifacts.json').read_text());client=json.loads((WORK/'client-tested-hashes.json').read_text())
for name,digest in json.loads((HERE/'source-hashes.json').read_text()).items():assert hashlib.sha256((HERE/name).read_bytes()).hexdigest()==digest
for r in records:
 jar=(WORK/'built' if candidate else ROOT/'mods')/r['jar'];before=WORK/'before'/r['jar'];assert hashlib.sha256(jar.read_bytes()).hexdigest()==r['sha256'];assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 with ZipFile(before) as a,ZipFile(jar) as b:
  assert b.testzip() is None;assert set(a.namelist())<=set(b.namelist());assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed'];assert sorted(set(b.namelist())-set(a.namelist()))==r['added']
  if r['jar'].startswith('spell_engine'):
   assert set(r['changed'])=={'net/spell_engine/api/effect/BleedStatusEffect.class','spell_engine.mixins.json'}
   old=json.loads(a.read('spell_engine.mixins.json'));new=json.loads(b.read('spell_engine.mixins.json'));new['mixins'].remove('flatbleed.SingleBleedMixin');assert old==new
   for n in ['net/spell_engine/api/effect/BleedStatusEffect.class',*r['added']]:assert int.from_bytes(b.read(n)[6:8],'big')==69 and b.read(n)==(WORK/'classes'/n).read_bytes()
  if r['jar'].startswith('skill_tree'):
   for key,duration in [('rogue_tier_3_spell_2_modifier_1',2.5),('rogue_tier_4_spell_2_modifier_2',7.5)]:
    o=json.loads(b.read('data/skill_tree_rpgs/spell/'+key+'.json'))['modifiers'][0]['impacts'][0]['action']['status_effect'];assert o['amplifier']==0 and o['duration']==duration and 'amplifier_power_multiplier' not in o
 assert (WORK/'server/mods'/r['jar']).read_bytes()==jar.read_bytes()
 assert client[r['jar']]==r['sha256'] and (WORK/'client/mods'/r['jar']).read_bytes()==jar.read_bytes()
 if not candidate:assert (ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()==jar.read_bytes()
for result in [WORK/'server/bleed-result.txt',WORK/'client/tooltip-result.txt']:
 assert result.read_text().startswith('PASS');print(result.read_text().strip())
print('PASS exact two-archive flat Bleed overlay, sources/CRC/Java25, native and client tested hashes, preserved prior gameplay patches')

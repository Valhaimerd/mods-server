from pathlib import Path
from zipfile import ZipFile
import hashlib,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
for record in json.loads((HERE/'artifacts.json').read_text()):
 active=ROOT/'mods'/record['jar'];fixed=ROOT/'mod fixing/1 fixed packs'/record['jar']
 assert hashlib.sha256(active.read_bytes()).hexdigest()==record['sha256']
 assert active.read_bytes()==fixed.read_bytes()
 with ZipFile(active) as z:
  assert z.testzip() is None
  for name in record['changed']+record['added']:
   assert 'Probe' not in name and 'PatchEffectsConfig' not in name
   if name.endswith('.class'):assert int.from_bytes(z.read(name)[6:8],'big')==69
  if record['jar'].startswith('archers-'):
   for n in z.namelist():
    if n.startswith('data/archers/spell/') and n.endswith('.json'):
     assert json.loads(z.read(n))['cost']['cooldown']['haste_affected'] is True
   barrage=json.loads(z.read('data/archers/spell/barrage.json'))
   assert barrage['active']['cast']['duration']==.1 and barrage['cost']['cooldown']['duration']==8
   assert barrage['arrow_perks']['damage_multiplier']==.7 and barrage['deliver']['shoot_arrow']['divergence']==0
   assert barrage['deliver']['shoot_arrow']['launch_properties']['extra_launch_delay']==3
   wolf=json.loads(z.read('data/archers/spell/spirit_wolf.json'))
   assert wolf['cost']['cooldown']['duration']==35 and wolf['range']==32
   assert wolf['impacts'][0]['action']['summon']['behaviour']['lifespan']['active_seconds']==20
   spell=json.loads(z.read('data/archers/spell/entangling_roots.json'))
   assert spell['cost']['cooldown']['duration']==12
   assert spell['deliver']['clouds'][0]['volume']['radius']==6
   assert spell['deliver']['clouds'][0]['time_to_live_seconds']==6
   assert 'apply_limit' not in spell['impacts'][0]['action']['status_effect']
  if record['jar'].startswith('skill_tree-'):
   for key in ['archer_tier_1_passive_2','archer_tier_2_passive_1']:
    assert json.loads(z.read('data/skill_tree_rpgs/spell/'+key+'.json'))['impacts'][0]['action']['status_effect']['duration']==4
  if record['jar'].startswith('spell_engine-'):assert 'net/spell_engine/internals/ArcherRules.class' in z.namelist()
 print('PASS active/fixed hash, CRC, production class checks:',record['jar'])

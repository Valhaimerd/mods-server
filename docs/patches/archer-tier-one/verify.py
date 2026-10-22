from pathlib import Path
from zipfile import ZipFile
import hashlib,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
layer_file=ROOT/'docs/patches/rogue-tier-one/artifacts.json'
layers={r['jar']:r for r in json.loads(layer_file.read_text())} if layer_file.exists() else {}
for record in json.loads((HERE/'artifacts.json').read_text()):
 active=ROOT/'mods'/record['jar'];fixed=ROOT/'mod fixing/1 fixed packs'/record['jar']
 layer=layers.get(record['jar']);expected=record['sha256']
 if layer and layer['before_sha256']==expected:expected=layer['sha256']
 assert hashlib.sha256(active.read_bytes()).hexdigest()==expected
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
   assert barrage['arrow_perks']['damage_multiplier']==.6 and barrage['deliver']['shoot_arrow']['divergence']==0
   assert barrage['deliver']['shoot_arrow']['launch_properties']['extra_launch_delay']==3
   power=json.loads(z.read('data/archers/spell/power_shot.json'));assert power['cost']['cooldown']['duration']==12 and power['deliver']['stash_effect']['duration']==8 and power['impacts'][0]['action']['status_effect']['duration']==6
   rain=json.loads(z.read('data/archers/spell/rain_of_arrows.json'));assert rain['cost']['cooldown']['duration']==25 and rain['deliver']['meteor']['launch_radius']==10 and rain['deliver']['meteor']['launch_properties']['extra_launch_count']==31 and rain['deliver']['meteor']['offset_requires_sequence']< -76
   magic=json.loads(z.read('data/archers/spell/magic_arrow.json'));assert magic['target']['type']=='NONE' and magic['cost']['cooldown']['duration']==25 and magic['deliver']['projectile']['launch_properties']['extra_launch_count']==3 and magic['impacts'][0]['action']['damage']['spell_power_coefficient']==.8
   wolf=json.loads(z.read('data/archers/spell/spirit_wolf.json'))
   assert wolf['cost']['cooldown']['duration']==35 and wolf['range']==32
   assert wolf['impacts'][0]['action']['summon']['behaviour']['lifespan']['active_seconds']==20
   spell=json.loads(z.read('data/archers/spell/entangling_roots.json'))
   assert spell['cost']['cooldown']['duration']==12
   assert spell['deliver']['clouds'][0]['volume']['radius']==6
   assert spell['deliver']['clouds'][0]['time_to_live_seconds']==6
   assert 'apply_limit' not in spell['impacts'][0]['action']['status_effect']
  if record['jar'].startswith('skill_tree-'):
   conjured=json.loads(z.read('data/skill_tree_rpgs/spell/archer_tier_4_spell_2_modifier_1.json'))['modifiers'][0];assert conjured['mutate_impacts']=='APPEND' and conjured['impacts'][0]['action']['status_effect']=={'effect_id':'minecraft:glowing','duration':10}
   for suffix in ['root','modifier_2']:assert json.loads(z.read('data/skill_tree_rpgs/spell/archer_tier_4_spell_2_'+suffix+'.json'))['modifiers'][0]['power_modifier']['power_multiplier']==.125
   for key in ['archer_tier_1_passive_2','archer_tier_2_passive_1']:
    assert json.loads(z.read('data/skill_tree_rpgs/spell/'+key+'.json'))['impacts'][0]['action']['status_effect']['duration']==4
  if record['jar'].startswith('spell_engine-'):assert 'net/spell_engine/internals/ArcherRules.class' in z.namelist()
 print('PASS active/fixed hash, CRC, production class checks:',record['jar'])

# Player-facing Archer descriptions contain concrete values, not raw template tokens.
for pattern,prefix in [('archers-*.jar','spell.archers.'),('skill_tree-*.jar','spell.skill_tree_rpgs.archer_')]:
 with ZipFile(next((ROOT/'mods').glob(pattern))) as z:
  language=json.loads(z.read('assets/'+('archers' if pattern.startswith('archers') else 'skill_tree_rpgs')+'/lang/en_us.json'))
  descriptions=[v for k,v in language.items() if k.startswith(prefix) and k.endswith('.description')]
  assert descriptions and all('{' not in v and '}' not in v for v in descriptions)
  print('PASS plain Archer descriptions:',len(descriptions))

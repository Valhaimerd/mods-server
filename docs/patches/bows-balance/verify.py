from pathlib import Path
from zipfile import ZipFile
import json,sys
folder=Path(sys.argv[1] if len(sys.argv)>1 else 'mods')
removed={'too_many_bows:'+s for s in ['stormbound_signet','sharpshot_ring','dead_eyes_pendant']}
if (folder/'Too_Many_Bows-neoforge-4.0.0-26.2.jar').exists():
 with ZipFile(folder/'Too_Many_Bows-neoforge-4.0.0-26.2.jar') as z:
  assert 'version="4.0.0-26.2"' in z.read('META-INF/neoforge.mods.toml').decode()
  for name in ['PackPolicy','PackJeiPlugin']:assert f'net/bandit/many_bows/pack/{name}.class' in z.namelist()
  assert 'net/bandit/many_bows/pack/BowRangedBridge.class' in z.namelist()
  assert 'PackRangedDamageMixin' in json.loads(z.read('too_many_bows.mixins.json'))['mixins']
  assert 'modId="ranged_weapon_api"' in z.read('META-INF/neoforge.mods.toml').decode()
  for n in z.namelist():
   if n.startswith('data/') and n.endswith('.json'):
    obj=json.loads(z.read(n))
    if '/tags/item/' in n:assert not any((v if isinstance(v,str) else v.get('id')) in removed for v in obj.get('values',[])),n
    if '/recipe/' in n:assert not any('"'+id+'"' in z.read(n).decode() for id in removed),n
with ZipFile(folder/'crossarms-1.4.jar') as a,ZipFile(folder/'expanded-crossbow-enchantings-1.11.jar') as b:
 for name in ['multishot','piercing']:
  key='data/minecraft/enchantment/'+name+'.json';first=json.loads(a.read(key));assert first==json.loads(b.read(key));assert first['exclusive_set']==[]
 assert json.loads(a.read('data/minecraft/enchantment/multishot.json'))['max_level']==3
 loot=json.loads(b.read('data/minecraft/enchantment/looting.json'));assert loot['effects']['minecraft:equipment_drops'][0]['requirements']['predicate']=={'minecraft:entity_type':'minecraft:player'}
 assert all('data/crossarms/enchantment/'+name+'.json' in a.namelist() for name in ['acceleration','bombard','deflector','frenzy','recycling','ricochet','zero_drop'])
 assert not any(n.startswith('data/') and ('/capacity/' in n or n.endswith(('/capacity.json','/capacity.mcfunction'))) for n in a.namelist())
 for n in a.namelist():
  if n.startswith('data/') and n.endswith('.json'):assert 'crossarms:capacity' not in a.read(n).decode(),n
 for hand in ['mainhand','offhand']:
  predicate=json.loads(a.read(f'data/crossarms/predicate/frenzy/{hand}.json'))
  assert predicate['predicate']['equipment'][hand]['items']=='#minecraft:enchantable/crossbow'
  broken=json.loads(a.read(f'data/crossarms/predicate/frenzy/broken_{hand}.json'))
  assert broken['predicate']['equipment'][hand]['predicates']['minecraft:damage']['durability']['max']==1
 assert 'data/crossarms/function/enchantment/frenzy/break.mcfunction' not in a.namelist()
 assert 'matches 20..' in a.read('data/crossarms/function/enchantment/frenzy/heat/tick.mcfunction').decode()
 for hand in ['mainhand','offhand']:
  predicate=json.loads(a.read(f'data/crossarms/predicate/frenzy/infinity_{hand}.json'))
  assert predicate['predicate']['equipment'][hand]['predicates']['minecraft:enchantments']==[{'enchantments':'minecraft:infinity','levels':{'min':1}}]
 assert 'minecraft:intangible_projectile' in a.read('data/crossarms/item_modifier/frenzy/charge_infinite.json').decode()
 assert 'unless predicate crossarms:frenzy/infinity_$(slot)' in a.read('data/crossarms/function/enchantment/frenzy/reload.mcfunction').decode()
 assert 'minecraft:magic' in a.read('data/crossarms/function/enchantment/frenzy/heat/damage.mcfunction').decode()
 tag=a.read('data/crossarms/function/enchantment/frenzy/tag.mcfunction').decode()
 for heat,multiplier in [('..300','0.0000002'),('301..450','0.0000003'),('451..610','0.0000004'),('611..','0.0000005')]:
  assert f'matches {heat} store result entity @s damage double {multiplier} run data get entity @s damage 1000000' in tag
 assert 'local/crossarms/mixin/FrenzyAccuracyMixin.class' in a.namelist()
 assert set(json.loads(a.read('crossarms-pack.mixins.json'))['mixins'])=={'FrenzyAccuracyMixin','FrenzyReloadMixin','FrenzyRecoilMixin'}
 assert 'local/crossarms/runtime/FrenzyState.class' in a.namelist()
 assert json.loads(a.read('data/crossarms/tags/enchantment/frenzy_conflicts.json'))['values'][-1]=={'id':'nova_structures:power','required':False}
 frenzy=json.loads(a.read('data/crossarms/enchantment/frenzy.json'))
 assert frenzy['exclusive_set']=='#crossarms:frenzy_conflicts'
 assert 'minecraft:ammo_use' not in frenzy['effects'] and 'minecraft:crossbow_charge_time' not in frenzy['effects']
 assert 'frenzy_empty' in a.read('data/crossarms/function/enchantment/frenzy/tick.mcfunction').decode()
 assert 'frenzy_notice' in a.read('data/crossarms/function/load.mcfunction').decode()
 assert 'matches 0 run title' in a.read('data/crossarms/function/enchantment/frenzy/notice/tick.mcfunction').decode()
 assert 'scores={frenzy_empty=1}] run title' not in a.read('data/crossarms/function/tick.mcfunction').decode()
 assert '60..' in a.read('data/crossarms/function/enchantment/frenzy/burst/tick.mcfunction').decode()
 assert a.read('data/crossarms/function/tick.mcfunction').decode().count('function crossarms:enchantment/frenzy/burst/tick')==1
print('PASS active crossbow integration data')

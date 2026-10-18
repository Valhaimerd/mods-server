from pathlib import Path
from zipfile import ZipFile
import json,sys
folder=Path(sys.argv[1] if len(sys.argv)>1 else 'mods')
removed={'too_many_bows:'+s for s in ['stormbound_signet','sharpshot_ring','dead_eyes_pendant']}
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
 assert all('data/crossarms/enchantment/'+name+'.json' in a.namelist() for name in ['acceleration','bombard','capacity','deflector','frenzy','recycling','ricochet','zero_drop'])
print('PASS accessory removal and crossbow integration data')

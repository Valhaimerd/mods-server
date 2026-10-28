from pathlib import Path
from zipfile import ZipFile
import json,sys
folder=Path(sys.argv[1] if len(sys.argv)>1 else 'tmp/paladin-tier-one/built')
def read(pattern,path):
 with ZipFile(next(folder.glob(pattern))) as z:return json.loads(z.read(path))
p=read('paladins-*.jar','data/paladins/spell/flash_heal.json')
assert p['school']=='spell_power:physical_melee' and p['range']==24
assert p['cost']['cooldown']['duration']==5
assert not p['target']['aim'].get('use_caster_as_fallback',False)
assert p['impacts'][0]['action']['damage']['spell_power_coefficient']==.5
b=read('paladins-*.jar','data/paladins/spell/blessed_strikes.json')
assert b['active']['cast']['channel']['ticks']==5 and b['cost']['cooldown']['duration']==14
assert b['deliver']['custom']['handler']=='paladin_pack:prepare'
w=read('paladins-*.jar','data/paladins/spell/blessed_wave.json')
assert w['range']==20 and w['deliver']['projectile']['launch_properties']['velocity']==1.4
assert w['deliver']['projectile']['projectile']['perks']['pierce']==0
assert w['deliver']['projectile']['inherit_shooter_pitch']
r=read('paladins-*.jar','data/paladins/spell/blessed_wave_wrath.json')
assert r['impacts'][0]['action']['damage']['spell_power_coefficient']==.4
assert r['deliver']['projectile']['projectile']['perks']['pierce']==1
print('PASS exact tier-I spell resources')

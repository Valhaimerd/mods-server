from pathlib import Path
from zipfile import ZipFile
import json,sys
p=Path(sys.argv[1]) if len(sys.argv)>1 else Path('mods/rogues-neoforge-3.1.2+26.2.jar')
with ZipFile(p) as z:
 d=json.loads(z.read('data/rogues/spell/shout.json'))
 rings=[g for g in d['release']['visuals']['particles'] if g['id']=='spell_engine:area_circle_1']
 assert len(rings)==1, 'Shout lacks a readable ground perimeter'
 g=rings[0];a=g['appearance'];assert a['scale_with']=='RANGE' and a['scale']==1
 assert a['facing']=='GROUND' and a['glow'] and g['batch']['anchor']=='GROUND'
 assert a['color']==0xD94C45FF
 assert z.testzip() is None
print('PASS native red ground perimeter scales with the actual Shout range')

from pathlib import Path
from zipfile import ZipFile
import sys
p=Path(sys.argv[1]) if len(sys.argv)>1 else Path('mods/spell_engine-neoforge-1.10.5+26.2.jar')
with ZipFile(p) as z:
 visual=z.read('net/spell_engine/internals/PaladinVisuals.class')
 assert b'goldAreaGroups' in visual, 'Missing readable native gold ground area'
 assert b'divineStrength' in z.read('net/spell_engine/internals/PaladinTierOne.class'), 'Missing successful Divine Strength area cue'
 assert b'sealOfLight' in z.read('net/spell_engine/internals/PaladinTierOne.class'), 'Missing final-pulse area cue'
 assert b'area_circle_1' in visual and b'area_effect_293' in visual
 assert z.testzip() is None
print('PASS native gold ground area and conditional tier-I event hooks')

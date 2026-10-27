from pathlib import Path
from zipfile import ZipFile
import json,sys
folder=Path(sys.argv[1]) if len(sys.argv)>1 else Path('mods')
with ZipFile(next(folder.glob('skill_tree-*.jar'))) as z:
 o=json.loads(z.read('data/skill_tree_rpgs/spell/priest_tier_4_spell_2_root.json'))
 assert any(m['spell_pattern']=='skill_tree_rpgs:priest_tier_4_spell_2_modifier_1' and m['spawn_duration_add']==2 for m in o['modifiers']), 'Improved Barrier does not extend Sacred Refuge'
print('PASS Sacred Refuge extension resource')

from pathlib import Path
from zipfile import ZipFile
import json,sys
root=Path(__file__).resolve().parents[3]
folder=Path(sys.argv[1]) if len(sys.argv)>1 else root/'mods'
with ZipFile(next(folder.glob('skill_tree-*.jar'))) as z:
 p=json.loads(z.read('data/skill_tree_rpgs/spell/priest_tier_3_passive_1.json'))
 c=json.loads(z.read('data/skill_tree_rpgs/spell/priest_tier_3_passive_2.json'))
 assert p['passive']['triggers']==[], 'Pain Suppression still triggers from healing'
 assert p['cost']['cooldown']['duration']==0
 assert c['deliver']['stash_effect']['amplifier']==4
 assert c['impacts'][0]['action']['damage']['spell_power_coefficient']==.30
 print('PASS passive resource values')

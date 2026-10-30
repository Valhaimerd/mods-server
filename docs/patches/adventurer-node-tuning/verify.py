from pathlib import Path
from zipfile import ZipFile
import json,sys
R=Path(__file__).resolve().parents[3];W=R/'tmp/adventurer-node-tuning';folder=W/('before' if '--baseline' in sys.argv else 'built')
with ZipFile(next(folder.glob('*.jar'))) as z:d=json.loads(z.read('data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json'))
def rewards(key):return {r['data']['attribute']:r['data']['value'] for r in d[key]['rewards']}
checks=[('Bulwark resistance5',rewards('bulwark')['puffish_attributes:resistance']==.05),('Martial no critical damage','critical_strike:damage' not in rewards('martial_prowess')),('Hunter no movement','movement_speed' not in rewards('hunter')),('Bulwark description','+5% Resistance' in d['bulwark']['description']['text']),('Martial description','Critical Damage' not in d['martial_prowess']['description']['text']),('Hunter description','Movement Speed' not in d['hunter']['description']['text'])]
for label,ok in checks:print(('PASS ' if ok else 'FAIL ')+label)
assert all(ok for _,ok in checks)

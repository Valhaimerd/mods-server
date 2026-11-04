from pathlib import Path
from zipfile import ZipFile
import json, sys
R = Path(__file__).resolve().parents[3]; H = Path(__file__).resolve().parent
W = R / 'tmp/holy-wand-dual-purpose'; name = 'paladins-neoforge-3.1.2+26.2.jar'
folder = 'before' if '--baseline' in sys.argv else 'built'
with ZipFile(W / folder / name) as z, ZipFile(W / 'before' / name) as old:
    wand = json.loads(z.read('data/paladins/spell/heal.json')); original = json.loads(old.read('data/paladins/spell/heal.json'))
    damage = [i for i in wand['impacts'] if i['action']['type'] == 'DAMAGE']
    assert len(damage) == 1, 'Holy Wand needs an enemy damage impact'
    assert damage[0]['action']['damage']['spell_power_coefficient'] == .8
    assert wand['impacts'][0] == original['impacts'][0], 'Existing 50% healing retained exactly'
    assert {k:v for k,v in wand.items() if k != 'impacts'} == {k:v for k,v in original.items() if k != 'impacts'}
    assert z.testzip() is None and len(z.namelist()) == len(set(z.namelist()))
    assert set(z.namelist()) == set(old.namelist())
    assert {n for n in old.namelist() if old.read(n) != z.read(n)} == {'data/paladins/spell/heal.json', 'assets/paladins/lang/en_us.json'}
base = 'data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
layout = json.loads((R / 'docs/patches/adventurer-layout-repair/layout.json').read_text())
totals = []
with ZipFile(R / 'mods/adventurerskills-EN-1.3-26.2-neoforge-fixed.jar') as z:
    nodes, definitions = [json.loads(z.read(base + f + '.json')) for f in ['skills', 'definitions']]
    for name, path, expected in zip(['Aegis', 'Colossus', 'Bulwark'], layout['defense_paths'], [[4,2,.04,.04,0], [6,2,.02,.08,0], [2,4,.08,0,.2]]):
        keys = ['max_health','armor','puffish_attributes:resistance','puffish_attributes:healing','knockback_resistance']
        actual = {k:0 for k in keys}
        for key in path:
            for reward in definitions[nodes[key]['definition']]['rewards']:
                d = reward['data']; actual[d['attribute']] += d['value']
        assert all(abs(actual[k]-v)<1e-8 for k,v in zip(keys,expected)), (name, actual)
        totals.append({'path':name,'totals':actual})
(H / 'defense-totals.json').write_text(json.dumps(totals,indent=2)+'\n')
print('PASS: dual-purpose Holy Wand resource, unchanged healing/cadence and exact Defense reward sums.')

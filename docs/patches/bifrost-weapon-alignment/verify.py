from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys

ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/bifrost-weapon-alignment'
r=json.loads((HERE/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
before=WORK/('before-'+r['jar']);after=WORK/'built'/r['jar'] if '--candidate' in sys.argv else ROOT/'mods'/r['jar']
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
with ZipFile(before) as a,ZipFile(after) as b:
    assert b.testzip() is None
    assert set(a.namelist())<=set(b.namelist())
    assert set(b.namelist())-set(a.namelist())==set(r['added'])
    assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
    assert set(r['changed'])=={'com/emandev/bifrost/ModItems.class','META-INF/neoforge.mods.toml','assets/bifrost/models/item/stormbreaker.json'}
    assert int.from_bytes(b.read('com/emandev/bifrost/ModItems.class')[6:8],'big')==69
    assert 'modId="paladins"' in b.read('META-INF/neoforge.mods.toml').decode()
    for item,kind in [('stormbreaker','hammer'),('thors_hammer','mace'),('heimdalls_sword','sword')]:
        assert json.loads(b.read(f'data/bifrost/weapon_attributes/{item}.json'))=={'parent':'bettercombat:'+kind}
        assert 'bifrost:'+item in json.loads(b.read(f'data/rpg_series/tags/item/weapon_type/{kind}.json'))['values']
    model=json.loads(b.read('assets/bifrost/models/item/stormbreaker.json'));old=json.loads(a.read('assets/bifrost/models/item/stormbreaker.json'))
    assert model['elements'][0]['to'][1]-old['elements'][0]['to'][1]==8
    assert model['display']['firstperson_righthand']['rotation'][1]==90
    assert model['display']['firstperson_lefthand']['rotation'][1]==-90
    assert not any('probe' in n.lower() for n in b.namelist())
if '--runtime' in sys.argv:
    assert (HERE/'runtime-tested-sha256.txt').read_text().strip()==sha(after)
    lines=(HERE/'candidate-results.txt').read_text().splitlines()
    assert not any(s.startswith('FAIL') for s in lines)
    assert sum(s.startswith('PASS ') for s in lines)==12
if '--candidate' not in sys.argv:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
assert (HERE/'Bifrost Skills.md').read_bytes()==(ROOT/'mod fixing/Bifrost Skills.md').read_bytes()
print('PASS native weapon identities, skills, length/orientation and exact archive preservation; plans remain documentation only.')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys

HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/bifrost-melee-visuals'
records=json.loads((HERE/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for r in records:
    before=WORK/('before-'+r['jar']);after=WORK/'built'/r['jar'] if '--candidate' in sys.argv else ROOT/r['folder']/r['jar']
    assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
    with ZipFile(before) as a,ZipFile(after) as b:
        assert b.testzip() is None and a.namelist()==b.namelist()
        assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
        if r['folder']=='mods':
            assert set(r['changed'])=={'assets/bifrost/models/item/stormbreaker.json','assets/bifrost/textures/spell/travel.png'}
            path='assets/bifrost/models/item/stormbreaker.json';old=json.loads(a.read(path));new=json.loads(b.read(path))
            assert new['elements']==old['elements'] and new['textures']==old['textures']
            for context in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']:
                assert new['display'][context]['scale']==[round(v*1.5,5) for v in old['display'][context]['scale']]
            assert new['display']['gui']['scale'][0]<old['display']['gui']['scale'][0]
            assert b.read('assets/bifrost/textures/spell/travel.png')==(HERE/'resources/travel.png').read_bytes()
        else:
            path='assets/minecraft/punchy/compat/zz_rpg_26_2.json';assert r['changed']==[path]
            old=json.loads(a.read(path));new=json.loads(b.read(path));m=new['itemSpecific']
            for item,ref in {'bifrost:stormbreaker':'paladins:netherite_great_hammer','bifrost:thors_hammer':'paladins:netherite_mace','bifrost:heimdalls_sword':'paladins:diamond_claymore'}.items():
                assert m.pop(item)==old['itemSpecific'][ref]
            assert old==new
    if '--candidate' not in sys.argv:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
    print('PASS exact resource-only overlay:',r['jar'])
if '--runtime' in sys.argv:
    assert (HERE/'runtime-tested-sha256.txt').read_text().strip()==records[0]['sha256']
    lines=(HERE/'candidate-results.txt').read_text().splitlines()
    assert not any(l.startswith('FAIL') for l in lines)
    assert sum(l.startswith('PASS') for l in lines)>=16
print('PASS gameplay and ownership byte-identical; exact native Punchy reference mappings; model/icon verified.')

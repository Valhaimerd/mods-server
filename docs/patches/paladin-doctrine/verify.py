"""Verify this task's exact artifacts, preserved content and native evidence."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/paladin-doctrine'
installed='--installed' in sys.argv
record=json.loads((HERE/'skill-artifacts.json').read_text());unchanged=0
for row,folder in [(record,WORK/'built'),*[(r,WORK/'armor/built') for r in json.loads((HERE/'armor/artifacts.json').read_text())]]:
    before=WORK/'before'/row['jar'];after=(ROOT/'mods' if installed else folder)/row['jar']
    digest=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
    assert digest(before)==row['before_sha256']
    assert digest(after)==row['sha256']
    if installed:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/row['jar']).read_bytes()
    with ZipFile(before) as a,ZipFile(after) as b:
        assert b.testzip() is None
        assert set(b.namelist())==set(a.namelist())|set(row['added'])
        assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==row['changed']
        for n in a.namelist():
            if n not in row['changed']:assert a.read(n)==b.read(n);unchanged+=1
        if row is record:
            for category in ['class_skills','weapon_skills']:
                path=f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json'
                old=json.loads(a.read(path));new=json.loads(b.read(path))
                for key in old:
                    if key not in ['paladin_root','paladin_boost']:assert old[key]==new[key],key
                    else:
                        assert new[key]['rewards'][0]['data']['value']==.25
                        for field in old[key]:
                            if field not in ['rewards','description']:assert old[key][field]==new[key][field]
                assert 'forbidden' in new['paladin_root']['description']['text']
            for n in row['added']:
                blob=b.read(n);assert int.from_bytes(blob[6:8],'big')==69
                assert b'net/minecraft/client/' not in blob
assert (HERE/'baseline-result.txt').read_text().startswith('FAIL 7')
assert (HERE/'stale-switch-red-result.txt').read_text().startswith('FAIL 78')
assert (HERE/'candidate-result.txt').read_text().startswith('PASS 84')
assert (HERE/'client/client-result.txt').read_text().startswith('PASS 49')
for label in ['candidate','legacy']:
    assert (HERE/'armor'/(label+'-results.txt')).read_text().startswith('PASS 29')
tested=json.loads((HERE/'candidate-tested.json').read_text())
assert tested[record['jar']]==record['sha256']
client=json.loads((HERE/'client/runtime-manifest.json').read_text())
for row in [record,*json.loads((HERE/'armor/artifacts.json').read_text())]:
    assert tested[row['jar']]==client[row['jar']]==row['sha256']
print(f'PASS {unchanged} unchanged archive entries; requested node/tooltip scope; Java25 server-safe helpers; 84 native and49 client checks'+('; installed/fixed copies match' if installed else ''))

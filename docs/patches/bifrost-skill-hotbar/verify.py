"""Check exact archive scope, native spell data, icon provenance and installed copies."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys

HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/bifrost-skill-hotbar'
r=json.loads((HERE/'artifacts.json').read_text());sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
before=WORK/('before-'+r['jar']);after=WORK/'built'/r['jar'] if '--candidate' in sys.argv else ROOT/'mods'/r['jar']
assert sha(before)==r['before_sha256'] and sha(after)==r['sha256']
for name,digest in r['production_inputs'].items():assert sha(ROOT/name)==digest,name
with ZipFile(before) as a,ZipFile(after) as b:
    assert b.testzip() is None
    assert set(a.namelist())<=set(b.namelist())
    assert set(b.namelist())-set(a.namelist())==set(r['added'])
    assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
    for n in r['changed']:
        assert n.startswith(('com/emandev/bifrost/','data/bifrost/weapon_attributes/','data/rpg_series/tags/item/weapon_type/')) or n in ['asgardian-expansion.mixins.json','assets/bifrost/lang/en_us.json','data/minecraft/tags/item/swords.json'],n
    spell=json.loads(b.read('data/bifrost/spell/travel.json'))
    assert spell['type']=='ACTIVE' and spell['active']['cast']['duration']==0
    assert spell['cost']['cooldown']['duration']==0 and spell['cost']['durability']==0
    assert spell['deliver']=={'type':'CUSTOM','custom':{'handler':'bifrost:travel'}}
    assert json.loads(b.read('data/bifrost/weapon_attributes/heimdalls_sword.json'))=={'parent':'bettercombat:claymore'}
    assert not json.loads(b.read('data/rpg_series/tags/item/weapon_type/sword.json'))['values']
    mix=json.loads(b.read('asgardian-expansion.mixins.json'))
    assert 'BifrostSkillSourceMixin' in mix['mixins'] and 'BifrostHotbarMixin' in mix['client']
    assert 'BifrostHotbarMixin' not in mix['mixins']
    assert b.read('assets/bifrost/textures/spell/travel.png')==(HERE/'resources/assets/bifrost/textures/spell/travel.png').read_bytes()
    # Transit, visuals, recipes, ownership data codecs and item IDs are not retired.
    assert not any('probe' in n.lower() for n in b.namelist())
    for n in r['added']+r['changed']:
        if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69,n
if '--runtime' in sys.argv:
    assert (HERE/'runtime-tested-sha256.txt').read_text().strip()==sha(after)
    lines=(HERE/'candidate-results.txt').read_text().splitlines()
    assert not any(s.startswith('FAIL ') for s in lines),lines
    assert 'BINDING 5' in lines and 'KEYS [Right Button, 1, 2, 3, 4, 5]' in lines
    assert sum(s.startswith('PASS ') for s in lines)>=40
if '--candidate' not in sys.argv:assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS exact native Bifrost integration, claymore Hofund, icon, scoped mixins and archive preservation.')

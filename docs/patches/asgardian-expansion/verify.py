from pathlib import Path
from zipfile import ZipFile
import hashlib,json,struct,sys,tomllib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/asgardian-expansion'
record=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'))
jar=WORK/'built'/record['jar'] if '--candidate' in sys.argv else ROOT/'mods'/record['jar']
assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['sha256'],'Archive differs from recorded candidate'
for name,digest in record['inputs'].items():assert hashlib.sha256((HERE/name).read_bytes()).hexdigest()==digest,'Unbuilt source/resource: '+name
before=WORK/('before-'+record['jar']);assert hashlib.sha256(before.read_bytes()).hexdigest()==record['before_sha256']
with ZipFile(before) as z:old={n:z.read(n) for n in z.namelist()}
with ZipFile(jar) as z:
    assert z.testzip() is None
    files={n:z.read(n) for n in z.namelist()}
assert set(old)<=set(files),'Native entry removed'
assert sorted(n for n in old if old[n]!=files[n])==record['changed']
assert sorted(set(files)-set(old))==record['added']
assert all('probe' not in n.lower() for n in files),'Test code leaked into production'
for key in ['BifrostMarkerData','BifrostMarkerData$Marker','BifrostMarkerService','BifrostCommands']:
    name='com/emandev/bifrost/'+key+'.class';assert old[name]==files[name],'Original marker/command schema changed: '+key
for name,data in files.items():
    if name.endswith('.class'):assert struct.unpack('>H',data[6:8])[0]<=69,'Java newer than25: '+name
    if name.endswith('.json'):json.loads(data)
meta=tomllib.loads(files['META-INF/neoforge.mods.toml'].decode());assert meta['mods'][0]['modId']=='bifrost' and meta['mods'][0]['displayName']=='Asgardian_Mod'
assert meta['mods'][0]['version']=='0.4.3-alpha-neoforge+mc26.2'
required={d['modId'] for d in meta['dependencies']['bifrost'] if d['type']=='required'}
assert {'iourusraces','spell_engine','spell_power','better_party','neoforge','minecraft'}<=required
mixins=json.loads(files['asgardian-expansion.mixins.json']);assert mixins['package']=='com.emandev.bifrost.mixin' and mixins['required']
for name in mixins['mixins']:assert 'com/emandev/bifrost/mixin/'+name+'.class' in files
for name in ['AsgardAbilities','AsgardNetwork','AsgardClient','AsgardState','AsgardWeapons','WeaponOwnership','TransitVisuals','TransitSounds']:
    assert 'com/emandev/bifrost/'+name+'.class' in files
assert b'asgardian-expansion-v1' in files['com/emandev/bifrost/BifrostNetworking.class']
lang=json.loads(files['assets/bifrost/lang/en_us.json']);assert lang['item.bifrost.heimdalls_sword']=='Hofund' and lang['item.bifrost.stormbreaker']=='Stormbreaker'
assert 'key.bifrost.asgardian_ability' in lang
sounds=json.loads(files['assets/bifrost/sounds.json'])
for phase in ['charge','descent','impact','closure']:
    name='transit_'+phase;assert name in sounds and sounds[name]['subtitle'] in lang
    audio=files['assets/bifrost/sounds/'+name+'.ogg'];assert audio.startswith(b'OggS') and b'vorbis' in audio[:256]
recipe=json.loads(files['data/bifrost/recipe/stormbreaker.json']);assert recipe['pattern']==['OC ','OB ',' B ']
for name in ['bifrost_marker','heimdalls_sword','thors_hammer','stormbreaker']:
    recipe=json.loads(files['data/bifrost/recipe/'+name+'.json']);assert recipe['type']=='minecraft:crafting_shaped' and recipe['result']['id']=='bifrost:'+name
    unlock=json.loads(files['data/bifrost/advancement/recipes/'+name+'.json']);assert unlock['rewards']['recipes']==['bifrost:'+name]
    assert unlock['criteria']['has_ingredient']['trigger']=='minecraft:inventory_changed'
assert len(list((ROOT/'mods').glob('bifrost-*.jar')))==1,'Duplicate active mod'
print('PASS production archive: hash/CRC; unchanged native marker schema/IDs; Java25; no probe leakage; required dependencies; mixins; protocols; models/recipe/lang/sounds; source hashes')
if '--runtime' in sys.argv:
    for folder in ['server','server-no-revive']:
        place=WORK/folder;assert (place/'expansion-result.txt').read_text().startswith('PASS'),folder+' failed'
        assert (place/'tested-sha256.txt').read_text().strip()==record['sha256'],folder+' used a different candidate'
    assert (WORK/'client/expansion-client-result.txt').read_text().startswith('PASS')
    assert (WORK/'client-candidate-sha256.txt').read_text().strip()==record['sha256'],'Client used a different candidate'
    print('PASS dedicated revive/no-revive and connected client proofs match the final candidate hash')

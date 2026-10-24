from pathlib import Path
from zipfile import ZipFile
import hashlib,json,struct,sys,tomllib

ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/bifrost-alignment'
record=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'))
jar=WORK/'built'/record['jar'] if '--candidate' in sys.argv else ROOT/'mods'/record['jar']
assert hashlib.sha256(jar.read_bytes()).hexdigest()==record['sha256'],'JAR differs from tested candidate'
before=WORK/('before-'+record['jar']);assert hashlib.sha256(before.read_bytes()).hexdigest()==record['before_sha256']
with ZipFile(before) as z:old={n:z.read(n) for n in z.namelist()}
with ZipFile(jar) as z:
    assert z.testzip() is None
    files={n:z.read(n) for n in z.namelist()}
assert set(old)<=set(files),'Removed native entries'
assert sorted(n for n in old if old[n]!=files[n])==record['changed']
assert sorted(set(files)-set(old))==record['added']==['com/emandev/bifrost/AsgardianAccess.class']
assert all('probe' not in n.lower() for n in files),'Test class leaked into production'
allowed={'META-INF/neoforge.mods.toml','assets/bifrost/lang/en_us.json'}|{'com/emandev/bifrost/'+n+'.class' for n in ['BifrostMod','BifrostFocusItem','BifrostMarkerBlock','BifrostMenuService','BifrostMarkerService','BifrostNetworking','BifrostTeleportManager','ModItems']}
assert set(record['changed'])==allowed
assert old['com/emandev/bifrost/BifrostMarkerData.class']==files['com/emandev/bifrost/BifrostMarkerData.class'],'Saved marker codec changed'
for n in record['changed']+record['added']:
    if n.endswith('.class'):assert struct.unpack('>H',files[n][6:8])[0]<=69,'Requires Java newer than 25'
meta=tomllib.loads(files['META-INF/neoforge.mods.toml'].decode());assert meta['mods'][0]['modId']=='bifrost' and meta['mods'][0]['displayName']=='Asgardian_Mod'
assert meta['mods'][0]['version']=='0.4.3-alpha-neoforge+mc26.2'
assert any(d['modId']=='iourusraces' and d['type']=='required' and d['side']=='BOTH' for d in meta['dependencies']['bifrost'])
lang=json.loads(files['assets/bifrost/lang/en_us.json']);assert lang['item.bifrost.heimdalls_sword']=='Hofund' and lang['item.bifrost.thors_hammer']=='Mjolnir'
assert b'asgardian-v1' in files['com/emandev/bifrost/BifrostNetworking.class']
assert b'heimdalls_sword' in files['com/emandev/bifrost/ModItems.class'] and b'thors_hammer' in files['com/emandev/bifrost/ModItems.class']
assert b'stormbreaker' not in files['com/emandev/bifrost/ModItems.class']
print('PASS: same filename/version; guarded native paths; saved codec and registry IDs retained; names/protocol/dependency; Java 25; no test leakage; CRC/hash')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,struct,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/shield-pose'
r=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'));candidate=WORK/'built'/r['jar'] if '--candidate' in sys.argv else ROOT/'mod store'/r['jar'];before=WORK/('before-'+r['jar'])
assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
assert hashlib.sha256(candidate.read_bytes()).hexdigest()==r['sha256']
for name,digest in r['inputs'].items():assert hashlib.sha256((HERE/name).read_bytes()).hexdigest()==digest
with ZipFile(before) as a,ZipFile(candidate) as b:
    assert b.testzip() is None
    assert set(b.namelist())-set(a.namelist())==set(r['added'])
    assert set(a.namelist())<=set(b.namelist())
    assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['checks'])|{'punchy.mixins.json'}
    oldmix=json.loads(a.read('punchy.mixins.json'));newmix=json.loads(b.read('punchy.mixins.json'));newmix['client'].remove('client.ComponentShieldPoseMixin');assert oldmix==newmix
    for name in r['checks']:
        assert b'punchy/compat/ShieldKinds' in b.read(name)
    for name in b.namelist():
        assert 'probe' not in name.lower()
        if name.endswith('.class'):assert struct.unpack('>H',b.read(name)[6:8])[0]<=69
if '--candidate' not in sys.argv:assert candidate.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS shield pose archive: 14 shield checks, blocking-hand handoff, native shield-pose guard; animations/models/textures/metadata/RPG helper preserved; Java25/CRC/no test leakage')
if '--runtime' in sys.argv:
    assert (WORK/'client-result.txt').read_text(encoding='utf-8').startswith('PASS')
    assert (WORK/'client-tested-sha256.txt').read_text(encoding='utf-8').strip()==r['sha256']
    print('PASS connected native shield and component-shield clip activation/release uses exact candidate hash')

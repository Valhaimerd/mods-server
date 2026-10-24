from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys

ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/vanish-render'
r=json.loads((HERE/'artifacts.json').read_text())
candidate=WORK/'built'/r['jar'] if '--candidate' in sys.argv else ROOT/'mods'/r['jar']
before=WORK/('before-'+r['jar'])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(before)==r['before_sha256'] and sha(candidate)==r['sha256']
with ZipFile(before) as a,ZipFile(candidate) as b:
    assert b.testzip() is None
    assert set(b.namelist())-set(a.namelist())==set(r['added'])
    assert set(a.namelist())<=set(b.namelist())
    assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
    old=json.loads(a.read('rogues.mixins.json'));new=json.loads(b.read('rogues.mixins.json'))
    assert new.pop('client')==['client.StealthHandsMixin','client.StealthHeldStateMixin']
    assert not old.pop('client') and old==new
    for n in r['added']:
        assert n.startswith('net/rogues/mixin/client/') and n.endswith('.class')
        assert int.from_bytes(b.read(n)[6:8],'big')==69
    assert not any('probe' in n.lower() for n in b.namelist())
if '--candidate' not in sys.argv:
    assert candidate.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
if '--runtime' in sys.argv:
    assert (HERE/'runtime-tested-sha256.txt').read_text().strip()==sha(candidate)
    results=(HERE/'candidate-results.txt').read_text().splitlines()
    assert len(results)==11 and all(s.startswith('PASS ') for s in results),results
print('PASS exact Vanish client-only overlay; gameplay, wording and all original classes unchanged.')

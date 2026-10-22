from pathlib import Path
from zipfile import ZipFile
import hashlib
import json

ROOT=Path(__file__).resolve().parents[3]
reports=[json.loads((ROOT/'docs/patches/downed-backpack/artifacts.json').read_text())]
reports+=json.loads((ROOT/'docs/patches/downed-backpack/health-artifacts.json').read_text())
for report in reports:
    name=report['jar']
    before=ROOT/'tmp/downed-backpack/before'/name
    after=ROOT/'mods'/name
    assert hashlib.sha256(before.read_bytes()).hexdigest()==report['before_sha256']
    assert hashlib.sha256(after.read_bytes()).hexdigest()==report['after_sha256']
    with ZipFile(before) as a,ZipFile(after) as b:
        assert b.testzip() is None
        assert set(b.namelist())-set(a.namelist())==set(report.get('added_entries',[]))
        assert not set(a.namelist())-set(b.namelist())
        assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(report['changed_entries'])
        for n in report['changed_entries']+report.get('added_entries',[]):
            assert int.from_bytes(b.read(n)[6:8],'big')==69
        assert not any('ConsumablesProbe' in n or 'HealthProbe' in n or 'PatchDownedHealth' in n or 'PatchConsumables' in n for n in b.namelist())
    assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/name).read_bytes()
    print('PASS exact changes, Java 25 classes, CRC and active/fixed hashes:',name)

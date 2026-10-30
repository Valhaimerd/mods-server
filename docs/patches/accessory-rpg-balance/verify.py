"""Check shipped accessory archives, policy, item coverage and documentation."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,re

ROOT=Path(__file__).resolve().parents[3]
HERE=Path(__file__).resolve().parent
record=json.loads((HERE/'verification.json').read_text())
inventory=json.loads((HERE/'inventory.json').read_text())
assert len(inventory)==87
assert len({(r['mod'],r['id']) for r in inventory})==87
assert sum(r['changed'] for r in inventory)==32
for entry in record['archives']:
    path=ROOT/'mods'/entry['jar']
    expected=entry['sha256']
    assert hashlib.sha256(path.read_bytes()).hexdigest()==expected,path
    assert hashlib.sha256((ROOT/'mod fixing/1 fixed packs'/entry['jar']).read_bytes()).hexdigest()==expected
    with ZipFile(path) as archive:
        names=archive.namelist()
        assert archive.testzip() is None
        assert len(names)==len(set(names))
        assert 'META-INF/neoforge.mods.toml' in names
        assert not any('local/accessory' in name or 'Probe.class' in name for name in names)
        mod=entry['namespace'];policy_dir=ROOT/'docs/patches'/entry['policy_folder']
        assert json.loads(archive.read(entry['embedded_policy']))==json.loads((policy_dir/'balance.json').read_text())
        assert json.loads(archive.read('accessory-character-scaling.json'))==json.loads((HERE/'character-scaling.json').read_text())
        lang=json.loads(archive.read(f'assets/{mod}/lang/en_us.json'))
        overrides=json.loads((policy_dir/'tooltip-overrides.json').read_text())
        assert all(lang.get(key)==value for key,value in overrides.items())
        for row in inventory:
            if row['mod']==mod:assert 'item.'+mod+'.'+row['id'] in lang
        if mod=='nameless_trinkets':
            available={v.split(':')[1] for v in json.loads(archive.read('data/nameless_trinkets/tags/item/nameless_trinkets_tag.json'))['values']}
            assert len(available)==42 and available=={r['id'] for r in inventory if r['mod']==mod}
            removed=json.loads((policy_dir/'balance.json').read_text())['removed'];assert len(removed)==12
            for name in names:
                if name.startswith('data/') and ('/recipe/' in name or '/advancement/' in name):
                    assert not any('"nameless_trinkets:'+id+'"' in archive.read(name).decode() for id in removed),name
        else:
            assert '#artifacts:slot/feet' in json.loads(archive.read('data/curios/tags/item/feet.json'))['values']
            assert 'artifacts:steadfast_spikes' in json.loads(archive.read('data/artifacts/tags/item/slot/feet.json'))['values']
        tag=json.loads(archive.read('data/modpack/tags/damage_type/accessory_damage.json'))
        assert tag.get('replace') is False
        assert mod+':accessory' in tag['values']
        for value in tag['values']:
            namespace,id=value.split(':');assert f'data/{namespace}/damage_type/{id}.json' in names
        reference=(policy_dir/'effects.md').read_text(encoding='utf-8')
        assert expected in reference
        assert '\ufffd' not in reference
for row in inventory:
    assert row['id'] in (HERE/'inventory.md').read_text(encoding='utf-8')
assert record['isolated_server']['integrated']['checks']>=155
assert record['isolated_server']['minimal']['checks']>=145
print('PASS: active/fixed hashes, CRCs, shipped policies/tooltips, 87 wearable decisions, 12 retired relics, Feet slot and isolated-server records.')

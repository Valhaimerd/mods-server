"""Remove only Voltaic Shot from the existing registry-fixed When Dungeons Arise JAR."""
import argparse
import json
from pathlib import Path
import zipfile

REMOVED = {
    'data/dungeons_arise/enchantment/voltaic_shot.json',
    'data/dungeons_arise/tags/enchantment/voltaic_shot.json',
    'data/dungeons_arise/function/voltaic_arrows.mcfunction',
    'data/dungeons_arise/function/voltaic_arrows_impact_particles.mcfunction',
}

def verify(path):
    with zipfile.ZipFile(path) as jar:
        assert jar.testzip() is None
        assert not REMOVED.intersection(jar.namelist())
        for name in jar.namelist():
            assert b'voltaic' not in jar.read(name).lower(), name

def patch(source, output):
    assert source.resolve() != output.resolve()
    count = 0
    with zipfile.ZipFile(source) as old, zipfile.ZipFile(output, 'w') as new:
        assert REMOVED.issubset(old.namelist())
        for info in old.infolist():
            name = info.filename
            if name in REMOVED:
                continue
            data = old.read(info)
            if b'voltaic' in data.lower():
                obj = json.loads(data)
                if '/lang/' in name:
                    obj = {k: v for k, v in obj.items() if 'voltaic' not in k.lower()}
                elif '/loot_table/' in name:
                    for pool in obj['pools']:
                        for i, entry in enumerate(pool['entries']):
                            if 'dungeons_arise:voltaic_shot' in json.dumps(entry):
                                assert entry['type'] == 'minecraft:item'
                                pool['entries'][i] = {'type': 'minecraft:empty', 'weight': entry.get('weight', 1)}
                                count += 1
                else:
                    raise AssertionError('Unexpected reference: ' + name)
                data = (json.dumps(obj, ensure_ascii=False, indent=2) + '\n').encode()
            new.writestr(info, data)
    assert count == 5, count
    verify(output)
    print('PASS: removed registry definition, tag, two functions and five loot outcomes; no Voltaic references remain')

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('source', type=Path)
    parser.add_argument('output', type=Path, nargs='?')
    args = parser.parse_args()
    if args.output:
        patch(args.source, args.output)
    else:
        verify(args.source)
        print('PASS: no Voltaic Shot registry entry, functions, tags or references')

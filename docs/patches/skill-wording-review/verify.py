"""Verify approved copy and prove every unrelated archive entry/key is unchanged."""
from pathlib import Path
from zipfile import ZipFile
import json
import re
import sys
from build import ROOT, HERE, BACKUP, REVIEW, sha

folder = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / 'mods'
records_file = HERE / 'artifacts.json'
records = {r['jar']: r for r in json.loads(records_file.read_text())} if records_file.exists() else {}
covered = set()
changed_count = 0
for name, expected in REVIEW['source_archives'].items():
    after = folder / name
    source = BACKUP / name if (BACKUP / name).exists() else ROOT / 'mods' / name
    assert sha(source) == expected, name
    edits = {k: r for k, r in REVIEW['descriptions'].items() if r['source_jar'] == name}
    with ZipFile(source) as a, ZipFile(after) as b:
        assert b.testzip() is None, name
        assert a.namelist() == b.namelist(), name
        assert a.comment == b.comment, name
        changed_entries = []
        for entry in a.infolist():
            n = entry.filename
            before_data, after_data = a.read(n), b.read(n)
            if n.endswith('/lang/en_us.json'):
                old, new = json.loads(before_data), json.loads(after_data)
                assert old.keys() == new.keys(), n
                for key in old:
                    if key in edits:
                        wanted = edits[key]['proposed'].replace('%', '%%')
                        assert new[key] == wanted, f'{name}: {key}: wording not applied'
                        assert not re.search(r'(?<!%)%(?!%)', new[key]), key
                        covered.add(key)
                    else:
                        assert new[key] == old[key], f'Out-of-scope translation changed: {key}'
                    changed_count += new[key] != old[key]
            else:
                assert before_data == after_data, f'Non-language entry changed: {n}'
            if before_data != after_data:
                changed_entries.append(n)
        if records:
            record = records[name]
            assert sha(after) == record['sha256'], name
            assert changed_entries == record['changed_entries'], name
    if folder.resolve() == (ROOT / 'mods').resolve():
        assert after.read_bytes() == (ROOT / 'mod fixing/1 fixed packs' / name).read_bytes(), name
    print('PASS language-only archive:', name)
assert covered == REVIEW['descriptions'].keys()
assert len(covered) == 196
assert changed_count == 176, changed_count
print(f'PASS {len(covered)} approved descriptions; {changed_count} rewritten; all other keys and entries unchanged.')

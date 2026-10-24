"""Apply the approved wording to the exact reviewed archives; language only."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json
import shutil

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / 'tmp/skill-wording-implementation'
OUT = WORK / 'built'
BACKUP = ROOT / 'mod fixing/retired packs/skill-wording-2026-10-24'
REVIEW = json.loads((HERE / 'wording-review.json').read_text(encoding='utf-8'))


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    BACKUP.mkdir(parents=True, exist_ok=True)
    records = []
    for name, expected in REVIEW['source_archives'].items():
        source = ROOT / 'mods' / name
        before = BACKUP / name
        if not before.exists():
            assert sha(source) == expected, f'Reviewed source changed: {name}'
            shutil.copy2(source, before)
        assert sha(before) == expected, f'Backup mismatch: {name}'
        edits = {k: r for k, r in REVIEW['descriptions'].items() if r['source_jar'] == name}
        seen = set()
        changed = []
        with ZipFile(before) as src, ZipFile(OUT / name, 'w') as dst:
            dst.comment = src.comment
            for entry in src.infolist():
                data = src.read(entry)
                if entry.filename.endswith('/lang/en_us.json'):
                    lang = json.loads(data)
                    old = dict(lang)
                    for key in edits.keys() & lang.keys():
                        row = edits[key]
                        assert lang[key] == row['current'], key
                        lang[key] = row['proposed'].replace('%', '%%')
                        seen.add(key)
                    if lang != old:
                        data = (json.dumps(lang, indent=2, ensure_ascii=False) + '\n').encode('utf-8')
                        changed.append(entry.filename)
                dst.writestr(entry, data)
        assert seen == edits.keys(), f'Missing descriptions: {edits.keys() - seen}'
        records.append({'jar': name, 'before_sha256': expected,
                        'sha256': sha(OUT / name), 'changed_entries': changed,
                        'reviewed_keys': len(edits),
                        'changed_keys': sum(r['current'] != r['proposed'].replace('%', '%%') for r in edits.values())})
    (HERE / 'artifacts.json').write_text(json.dumps(records, indent=2) + '\n', encoding='utf-8')
    print(f'Built {len(records)} language-only candidates; active JARs unchanged.')


if __name__ == '__main__':
    main()

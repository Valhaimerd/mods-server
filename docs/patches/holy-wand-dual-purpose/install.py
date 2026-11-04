from pathlib import Path
from zipfile import ZipFile
import hashlib, json, shutil

R = Path(__file__).resolve().parents[3]
H = Path(__file__).resolve().parent
W = R / 'tmp/holy-wand-dual-purpose'
record = json.loads((H / 'artifact.json').read_text())
name = record['jar']
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
candidate = W / 'built' / name
assert sha(candidate) == record['after']
assert (H / 'client-result.txt').read_text().strip() == 'PASS'
assert json.loads((H / 'client-tested.json').read_text())[name] == record['after']
assert (H / 'defense-totals.json').exists()
layout = json.loads((R / 'docs/patches/adventurer-layout-repair/installation.json').read_text())
for folder in layout['copies']:
    assert sha(R / folder / layout['jar']) == layout['sha256']
assert json.loads((H / 'client-tested.json').read_text())[layout['jar']] == layout['sha256']
with ZipFile(candidate) as new, ZipFile(W / 'before' / name) as old:
    assert new.testzip() is None
    assert len(new.namelist()) == len(set(new.namelist()))
    assert set(new.namelist()) == set(old.namelist())
    assert {n for n in old.namelist() if old.read(n) != new.read(n)} == set(record['changed'])
    assert new.read('META-INF/neoforge.mods.toml') == old.read('META-INF/neoforge.mods.toml')
for folder, label in [('mods', 'active'), ('mod fixing/1 fixed packs', 'fixed')]:
    active = R / folder / name
    assert sha(active) == record['before'], 'Input changed: ' + str(active)
    backup = R / 'mod fixing/retired packs/holy-wand-dual-purpose-2026-11-04' / label / name
    backup.parent.mkdir(parents=True, exist_ok=True)
    if backup.exists(): assert sha(backup) == record['before']
    else: shutil.copy2(active, backup)
for folder in ['mods', 'mod fixing/1 fixed packs']:
    dest = R / folder / name
    shutil.copy2(candidate, dest)
    assert sha(dest) == record['after']
(H / 'installation.json').write_text(json.dumps({'installed': True, 'jar': name, 'sha256': record['after'], 'copies': ['mods', 'mod fixing/1 fixed packs'], 'client_checks': sum(line.startswith('PASS ') for line in (H / 'client-checks.txt').read_text().splitlines()), 'adventurer_layout_sha256': layout['sha256']}, indent=2) + '\n')
print('PASS: exact client-tested JAR installed in active/fixed packs; originals backed up.')

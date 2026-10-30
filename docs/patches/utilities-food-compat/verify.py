from pathlib import Path
from zipfile import ZipFile
import hashlib, json

H = Path(__file__).resolve().parent
R = H.parents[2]
record = json.loads((H / 'artifacts.json').read_text())
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
paths = [R / 'mods' / record['jar'], R / 'mod fixing/1 fixed packs' / record['jar']]
for p in paths:
    assert sha(p) == record['sha256'], str(p)
    with ZipFile(p) as z:
        assert z.testzip() is None
        assert len(z.namelist()) == len(set(z.namelist()))
        assert not any('probe' in n.lower() for n in z.namelist())
for name in ['candidate-mods.json', 'client-mods.json']:
    assert json.loads((H / name).read_text())[record['jar']] == record['sha256']
assert (H / 'runtime-result.txt').read_text().startswith('PASS 75')
assert (H / 'client-result.txt').read_text().startswith('PASS full distributed client pack')
assert (H / 'baseline-result.txt').read_text().startswith('REPRODUCED')
print('PASS active/fixed/tested hashes, archive integrity, 75 native checks, full-client bootstrap and baseline reproduction')

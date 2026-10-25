from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pet-vault-trinket';r=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'));sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();out=WORK/'built'/r['jar'];assert sha(out)==r['sha256']
with ZipFile(WORK/('before-'+r['jar'])) as before,ZipFile(out) as after:
 assert after.testzip() is None
 assert all(before.read(n)==after.read(n) for n in before.namelist())
 assert sorted(set(after.namelist())-set(before.namelist()))==['data/curios/tags/item/trinket.json']
 assert json.loads(after.read('data/curios/tags/item/trinket.json'))=={'replace':False,'values':['pet_vault:pet_necklace']}
assert (HERE/'candidate-results.txt').read_text(encoding='utf-8').startswith('PASS 14 ')
assert (HERE/'baseline-results.txt').read_text(encoding='utf-8').startswith('FAIL ')
assert json.loads((HERE/'runtime-tested-mods.json').read_text(encoding='utf-8'))[r['jar']]==r['sha256']
if '--installed' in sys.argv:
 for d in ['mods','mod fixing/1 fixed packs']:assert sha(ROOT/d/r['jar'])==r['sha256']
print('PASS single additive trinket tag, all existing ZIP entries unchanged, native slot/equipped runtime checks'+(', installed/fixed copies' if '--installed' in sys.argv else ''))

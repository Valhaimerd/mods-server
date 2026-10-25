from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/patpat-controls';a=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'));name=a['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
jar=ROOT/'mods'/name if '--installed' in sys.argv else WORK/'built'/name
assert sha(jar)==a['sha256']
with ZipFile(WORK/('before-'+name)) as old,ZipFile(jar) as new:
 assert set(old.namelist())==set(new.namelist()),'Unexpected added or removed archive entries'
 changed=[n for n in old.namelist() if old.read(n)!=new.read(n)]
 assert changed==a['changed'],changed
 assert int.from_bytes(new.read(changed[0])[6:8],'big')==69,'Must support Java 25'
assert 'key_patpat.keybinding.pat:key.mouse.right:CONTROL' in (ROOT/'installer/recommended-keybinds.txt').read_text(encoding='utf-8').splitlines()
for label in ['candidate','restart']:assert (HERE/(label+'-results.txt')).read_text(encoding='utf-8').startswith('PASS')
assert (HERE/'baseline-results.txt').read_text(encoding='utf-8').startswith('FAIL')
tested=json.loads((HERE/'runtime-tested-mods.json').read_text(encoding='utf-8'));assert tested[name]==sha(jar),'Runtime-tested bytes must match final JAR'
if '--installed' in sys.argv:assert sha(ROOT/'mod fixing/1 fixed packs'/name)==sha(jar)
print('PASS exact runtime-tested JAR, one changed class, Java 25, Ctrl profile, baseline/candidate/restart records')

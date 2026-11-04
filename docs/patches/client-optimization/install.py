from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/client-optimization'
assert (H/'client-result.txt').read_text().strip()=='PASS'
tested=json.loads((H/'client-tested.json').read_text());rows=json.loads((H/'downloads.json').read_text())
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
for row in rows:
 src=W/row['file'];assert sha(src)==row['sha256']==tested[row['file']]
 with ZipFile(src) as z:assert z.testzip() is None
 for folder in ['mod store','mod fixing/1 fixed packs']:
  target=R/folder/row['file']
  if target.exists():assert sha(target)==row['sha256'],str(target)
assert not any((R/'mod store').glob('vectors-*.jar'))
for row in rows:
 for folder in ['mod store','mod fixing/1 fixed packs']:
  target=R/folder/row['file'];shutil.copy2(W/row['file'],target);assert sha(target)==row['sha256']
assert len(list((R/'mods').glob('*.jar')))==305
assert len(list((R/'mod store').glob('*.jar')))==26
(H/'installation.json').write_text(json.dumps({'installed':True,'main_jars':305,'client_extra_jars':26,'runtime_checks':12,'files':{r['file']:r['sha256'] for r in rows}},indent=2)+'\n')
print('PASS: four exact full-client-tested optimizers installed; Vectors absent;305 main/26 client JARs.')

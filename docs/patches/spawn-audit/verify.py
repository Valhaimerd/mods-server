from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/spawn-spring';r=json.loads((HERE/'artifacts.json').read_text())[0];folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods';after=folder/r['jar'];before=WORK/('before-'+r['jar'])
assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256'] and hashlib.sha256(after.read_bytes()).hexdigest()==r['sha256']
with ZipFile(before) as a,ZipFile(after) as b:
 assert a.namelist()==b.namelist() and b.testzip() is None
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
 assert r['changed']==['xyz/verarr/spreadspawnpoints/spawnpoints/SpawnPointManager.class']
 assert b.read('META-INF/neoforge.mods.toml')==a.read('META-INF/neoforge.mods.toml')
 assert int.from_bytes(b.read(r['changed'][0])[6:8],'big')==69
if folder.resolve()==(ROOT/'mods').resolve():assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
for path in ['server/spawn-result.txt','client-first-join/spawn-client-result.txt']:
 text=(WORK/path).read_text();assert text.startswith('PASS ');print(text.strip())
print('PASS one-field Spring default patch, preserved metadata/entries, Java 25, CRC and active/fixed hashes')

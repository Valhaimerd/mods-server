from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/orc-followup';r=json.loads((HERE/'artifacts.json').read_text())[0];folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods';after=folder/r['jar'];before=WORK/('before-'+r['jar'])
assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
next_file=ROOT/'docs/patches/race-state/artifacts.json'
layer=next((x for x in json.loads(next_file.read_text()) if x['jar']==r['jar'] and x['before_sha256']==r['sha256']),None) if next_file.exists() and folder.resolve()==(ROOT/'mods').resolve() else None
audited=ROOT/'tmp/race-state'/('before-'+r['jar']) if layer else after
assert hashlib.sha256(audited.read_bytes()).hexdigest()==r['sha256'] and hashlib.sha256(after.read_bytes()).hexdigest()==(layer['sha256'] if layer else r['sha256'])
with ZipFile(before) as a,ZipFile(audited) as b:
 assert a.namelist()==b.namelist() and b.testzip() is None
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
 assert set(r['changed'])=={'com/eiden/iourusraces/race/RaceBalanceRules.class','assets/iourusraces/lang/en_us.json','assets/iourusraces/lang/es_es.json'}
 assert int.from_bytes(b.read('com/eiden/iourusraces/race/RaceBalanceRules.class')[6:8],'big')==69
 for lang in ['en_us','es_es']:
  old=json.loads(a.read(f'assets/iourusraces/lang/{lang}.json'));new=json.loads(b.read(f'assets/iourusraces/lang/{lang}.json'));assert old.keys()==new.keys();assert all(k.startswith('race.iourusraces.orc.') for k in old if old[k]!=new[k])
if folder.resolve()==(ROOT/'mods').resolve():assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
result=WORK/'server/asgard-result.txt';assert result.read_text().startswith('PASS ');print(result.read_text());print('PASS Orc-only helper/language changes, Java 25, CRC and active/fixed hashes')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-balance';r=json.loads((HERE/'artifacts.json').read_text())[0];folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods';jar=folder/r['jar'];before=WORK/('before-'+r['jar'])
assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
next_file=ROOT/'docs/patches/orc-followup/artifacts.json'
layer=next((x for x in json.loads(next_file.read_text()) if x['jar']==r['jar'] and x['before_sha256']==r['sha256']),None) if next_file.exists() and folder.resolve()==(ROOT/'mods').resolve() else None
audited=ROOT/'tmp/orc-followup'/('before-'+r['jar']) if layer else jar
assert hashlib.sha256(audited.read_bytes()).hexdigest()==r['sha256']
expected=layer['sha256'] if layer else r['sha256']
next_state=ROOT/'docs/patches/race-state/artifacts.json'
state=next((x for x in json.loads(next_state.read_text()) if x['jar']==r['jar'] and x['before_sha256']==expected),None) if next_state.exists() and folder.resolve()==(ROOT/'mods').resolve() else None
if state:expected=state['sha256']
assert hashlib.sha256(jar.read_bytes()).hexdigest()==expected
with ZipFile(before) as a,ZipFile(audited) as b:
 assert b.testzip() is None and set(a.namelist())<=set(b.namelist())
 assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
 assert sorted(set(b.namelist())-set(a.namelist()))==r['added']
 assert b.read('META-INF/neoforge.mods.toml')==a.read('META-INF/neoforge.mods.toml')
 assert b.read('com/eiden/iourusraces/network/RaceNetworkHandler.class')==a.read('com/eiden/iourusraces/network/RaceNetworkHandler.class')
 for n in r['changed']+r['added']:
  assert 'Probe' not in n
  if n.endswith('.class'):assert int.from_bytes(b.read(n)[6:8],'big')==69
  elif n.endswith('.json'):json.loads(b.read(n))
  else:raise AssertionError(n)
 for language in ['en_us','es_es']:
  o=json.loads(b.read('assets/iourusraces/lang/'+language+'.json'))
  assert '1.10x' in o['race.iourusraces.asgardian.tooltip']
  assert '+2% damage and mining' not in o['race.iourusraces.asgardian.trait.1']
if folder.resolve()==(ROOT/'mods').resolve():assert jar.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
result=WORK/'server/asgard-result.txt';assert result.read_text().startswith('PASS ');print(result.read_text())
assert (WORK/'baseline-client/fall-result.txt').read_text().startswith('FAIL ')
client=WORK/'client/fall-result.txt';assert client.read_text().startswith('PASS ');print(client.read_text())
print('PASS exact installed race overlay, native metadata/network preservation, Java 25, CRC, local/fixed hashes')

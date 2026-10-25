from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-orb-reset';sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();r=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'));out=WORK/'built'/r['jar'];assert sha(out)==r['sha256']
with ZipFile(WORK/('before-'+r['jar'])) as before,ZipFile(out) as final:
 assert final.testzip() is None
 assert [n for n in before.namelist() if before.read(n)!=final.read(n)]==r['changed']
 assert sorted(set(final.namelist())-set(before.namelist()))==r['added']
 assert set(r['changed'])=={'com/eiden/iourusraces/race/ClientRaceData.class','com/eiden/iourusraces/client/ClientInputHandler.class','iourusraces.mixins.json'}
 assert r['added']==['com/eiden/iourusraces/mixin/OrbRaceResetMixin.class']
 for n in r['added']+r['changed']:
  if n.endswith('.class'):assert int.from_bytes(final.read(n)[6:8],'big')==69
 assert not any('probe' in n.lower() for n in final.namelist())
for name,digest in r['sources'].items():assert sha(HERE/name)==digest,name
assert json.loads((HERE/'runtime-tested-mods.json').read_text(encoding='utf-8'))[r['jar']]==r['sha256']
assert (HERE/'client-results.txt').read_text(encoding='utf-8').startswith('PASS 125 ')
assert 'premature panel' in (HERE/'baseline-results.txt').read_text(encoding='utf-8')
assert (HERE/'optional-server-results.txt').read_text(encoding='utf-8').startswith('PASS ')
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');classes=WORK/'pure-classes'
subprocess.run([str(java/'javac.exe'),'--release','25','-cp',str(out),'-d',str(classes),str(HERE/'tests/ClientStateTest.java')],check=True)
subprocess.run([str(java/'java.exe'),'-cp',str(classes)+';'+str(out),'com.eiden.iourusraces.race.ClientStateTest'],check=True)
if '--installed' in sys.argv:
 for d in ['mods','mod fixing/1 fixed packs']:assert sha(ROOT/d/r['jar'])==r['sha256']
print('PASS exact tested race overlay, existing balance/leaf/state entries preserved, delayed-sync regression, native Orb/UI and optional dedicated server'+(', installed/fixed copies' if '--installed' in sys.argv else ''))

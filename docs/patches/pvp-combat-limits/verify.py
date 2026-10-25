from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pvp-combat-limits';sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
reports=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'))
for name,digest in json.loads((HERE/'source-hashes.json').read_text(encoding='utf-8')).items():assert sha(HERE/name)==digest,name
for r in reports:
 p=WORK/'built'/r['jar'];assert sha(p)==r['sha256'],r['jar']
 if '--installed' in sys.argv:
  for directory in ['mods','mod fixing/1 fixed packs']:assert sha(ROOT/directory/r['jar'])==r['sha256'],directory+'/'+r['jar']
 with ZipFile(WORK/('before-'+r['jar'])) as before,ZipFile(p) as final:
  assert final.testzip() is None
  changed=[n for n in before.namelist() if before.read(n)!=final.read(n)]
  assert changed==r['changed'],changed
  assert sorted(set(final.namelist())-set(before.namelist()))==r['added']
  assert not any('probe' in n.lower() or 'test.class' in n.lower() for n in final.namelist())
  for n in final.namelist():
   if n.endswith('.class') and n in r['changed']+r['added']:assert int.from_bytes(final.read(n)[6:8],'big')==69,n
for file in ['server-runtime-mods.json','client-runtime-mods.json']:
 hashes=json.loads((HERE/file).read_text(encoding='utf-8'))
 for r in reports:assert hashes[r['jar']]==r['sha256'],file+'/'+r['jar']
assert (HERE/'damage-results.txt').read_text(encoding='utf-8').startswith('PASS 118 checks')
assert (HERE/'client-results.txt').read_text(encoding='utf-8').startswith('PASS')
checks=(HERE/'server-checks.txt').read_text(encoding='utf-8').splitlines();assert len(checks)>=85 and all(s.startswith('PASS ') for s in checks)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');classes=WORK/'test-classes'
subprocess.run([str(java/'javac.exe'),'--release','25','-d',str(classes),str(HERE/'CombatState.java'),str(HERE/'tests/CombatStateTest.java')],check=True)
subprocess.run([str(java/'java.exe'),'-cp',str(classes),'io.redspace.pvp_flagging.CombatStateTest'],check=True)
print('PASS: three archive deltas, Java 25 classes, exact tested artifact hashes, dedicated damage/limits and connected HUD results'+(', installed/fixed copies' if '--installed' in sys.argv else ''))

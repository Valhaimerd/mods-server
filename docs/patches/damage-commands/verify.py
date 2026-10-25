from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/damage-commands';r=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'));sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
out=WORK/'built'/r['jar'];assert sha(out)==r['sha256'];assert sha(WORK/('before-'+r['jar']))==r['before_sha256']
with ZipFile(WORK/('before-'+r['jar'])) as before,ZipFile(out) as final:
 assert final.testzip() is None
 assert [n for n in before.namelist() if before.read(n)!=final.read(n)]==r['changed']
 assert sorted(set(final.namelist())-set(before.namelist()))==r['added']
 assert set(r['changed'])=={'io/redspace/pvp_flagging/PvpFlagging.class','io/redspace/pvp_flagging/events/ServerEventsForge.class'}
 assert r['added']==['io/redspace/pvp_flagging/DamageCommands.class']
 for n in r['added']+r['changed']:assert int.from_bytes(final.read(n)[6:8],'big')==69
 assert not any('probe' in n.lower() for n in final.namelist())
for name,digest in r['sources'].items():assert sha(HERE/name)==digest,name
assert json.loads((HERE/'runtime-tested-mods.json').read_text(encoding='utf-8'))[r['jar']]==r['sha256']
for phase in ['initial','restart']:
 assert (HERE/(phase+'-results.txt')).read_text(encoding='utf-8').startswith('PASS ')
 checks=(HERE/(phase+'-checks.txt')).read_text(encoding='utf-8').splitlines();assert len(checks)>80 and all(l.startswith('PASS ') for l in checks)
if '--installed' in sys.argv:
 for directory in ['mods','mod fixing/1 fixed packs']:assert sha(ROOT/directory/r['jar'])==r['sha256']
print('PASS exact three-class overlay, preserved combat/HUD entries, Java 25, sources, tested hash, command/damage/restart results'+(', installed/fixed copies' if '--installed' in sys.argv else ''))

"""Build the 26.2 port from the preserved pending archive and tracked Java source."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,subprocess,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/pvp-26.2';WORK.mkdir(exist_ok=True)
original=ROOT/'mod fixing/1 pending packs/pvp_flagging-26.1.2-1.1.2.jar'
if not original.exists():original=WORK/'upstream.jar'
if not (WORK/'upstream.jar').exists():(WORK/'upstream.jar').write_bytes(original.read_bytes())
original=WORK/'upstream.jar'
assert hashlib.sha256(original.read_bytes()).hexdigest()=='dbc1313dd4328c0ebc39702f7d940ca2bb78b1d27f5f8219cfb3f7a1fc97b64e','Unexpected upstream archive'
annotations=next((Path.home()/'.gradle/caches/modules-2/files-2.1/org.jetbrains/annotations').rglob('*.jar'))
cp=(ROOT/'tmp/artifacts-balance/classpath.txt').read_text().strip()+';'+str(original.resolve())+';'+str(annotations)
classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-encoding','UTF-8','-cp',cp,'-d',str(classes),*[str(p) for p in (HERE/'src').rglob('*.java')]]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
output=WORK/'pvp_flagging-26.2-1.1.2.jar'
with ZipFile(original) as a:files={n:a.read(n) for n in a.namelist() if not n.endswith('.class') and not n.endswith('/')}
meta=files['META-INF/neoforge.mods.toml'].decode().replace('26.1.2-1.1.2','26.2-1.1.2').replace('versionRange="[26.1.2]"','versionRange="[26.2,26.3)"').replace('versionRange="[26.1.2,)"','versionRange="[26.2.0.90,)"')
files['META-INF/neoforge.mods.toml']=meta.encode()
# Official Mojang names need no refmap; the upstream archive supplies none.
mixins=json.loads(files['mixins.pvp_flagging.json']);mixins.pop('refmap',None)
files['mixins.pvp_flagging.json']=(json.dumps(mixins,indent=2)+'\n').encode()
for f in classes.rglob('*.class'):files[f.relative_to(classes).as_posix()]=f.read_bytes()
with ZipFile(output,'w',ZIP_DEFLATED) as z:
 for n,b in files.items():z.writestr(n,b)
report={'upstream_sha256':hashlib.sha256(original.read_bytes()).hexdigest(),'output_sha256':hashlib.sha256(output.read_bytes()).hexdigest(),'default_pvp_damage_multiplier':.5}
(WORK/'build-result.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))

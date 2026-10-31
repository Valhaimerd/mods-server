from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import subprocess, json, hashlib
H=Path(__file__).resolve().parent; R=H.parents[2]; W=R/'tmp/rpg-netherite-repairs'
name='spell_engine-neoforge-1.10.5+26.2.jar'; source=W/'before'/name
classes=W/'classes'; classes.mkdir(exist_ok=True)
cp=';'.join(str(p.resolve()) for p in (R/'mods').glob('*.jar'))+';'+(R/'tmp/utilities/classpath.txt').read_text().strip()
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(H/'NetheriteRepairs.java')]
a=W/'compile.args'; a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(source) as z: old={n:z.read(n) for n in z.namelist()}
entry='net/spell_engine/rpg_series/item/NetheriteRepairs.class'; assert entry not in old
with ZipFile(W/'built'/name,'w',ZIP_DEFLATED) as z:
 for n,b in old.items(): z.writestr(n,b)
 z.writestr(entry,(classes/entry).read_bytes())
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(H/'artifacts.json').write_text(json.dumps({'jar':name,'before_sha256':sha(source),'sha256':sha(W/'built'/name),'added':[entry],'preserved_entries':len(old)},indent=2)+'\n')
print('Built single-class netherite repair overlay')

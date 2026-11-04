from pathlib import Path
from zipfile import ZipFile
import copy,hashlib,json,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/archer-assist-repair';C=W/'classes';C.mkdir(exist_ok=True)
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
cp=';'.join(str(p) for p in (R/'mods').glob('*.jar'))+';'+(W/'classpath.txt').read_text()
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),str(H/'ArrowAssist.java')];a=W/'compile.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run([str(J/'javac.exe'),'@'+str(a)],check=True)
name='spell_engine-neoforge-1.10.5+26.2.jar';key='net/spell_engine/internals/ArrowAssist.class';src=W/'before'/name;out=W/'built'/name
with ZipFile(src) as old,ZipFile(out,'w') as new:
 for info in old.infolist():new.writestr(copy.copy(info),(C/key).read_bytes() if info.filename==key else old.read(info.filename))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(H/'artifact.json').write_text(json.dumps({'jar':name,'before':sha(src),'after':sha(out),'changed':[key]},indent=2)+'\n')
print('Built one-class current-input Spell Engine replacement; production unchanged.')

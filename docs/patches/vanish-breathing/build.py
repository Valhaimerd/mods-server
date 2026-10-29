from pathlib import Path
from zipfile import ZipFile
import hashlib, json, subprocess, shutil
H=Path(__file__).resolve().parent; R=H.parents[2]; W=R/'tmp/vanish-breathing'; C=W/'classes'; O=W/'built'
C.mkdir(parents=True,exist_ok=True); O.mkdir(exist_ok=True)
active=next((R/'mods').glob('rogues-*.jar'))
baseline=W/'baseline'/active.name
if not baseline.exists():
    baseline.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(active,baseline)
lines=(R/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines()
cp=';'.join(str(p) for p in (R/'mods').glob('*.jar'))+';'+lines[lines.index('"-cp"')+1].strip('"')
sources=[str(p) for p in H.glob('*.java') if p.name!='ClientVanishProbe.java']
args=['--release','25','-proc:none','-cp',cp,'-d',str(C),*sources]
(W/'compile.args').write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args))
J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(J/'javac.exe'),'@'+str(W/'compile.args')],check=True)
with ZipFile(baseline) as src,ZipFile(O/active.name,'w') as dst:
    config=json.loads(src.read('rogues.mixins.json'))
    config['client']=[x for x in config['client'] if x!='client.StealthHeldStateMixin']
    config['client']+=['client.'+n for n in ['VanishRenderStateMixin','VanishLivingRendererMixin','VanishRenderTypeAccess','VanishRenderSetupAccess','VanishTextureAccess'] if 'client.'+n not in config['client']]
    replace={p.relative_to(C).as_posix():p.read_bytes() for p in C.rglob('*.class')}
    replace['rogues.mixins.json']=(json.dumps(config,indent=2)+'\n').encode()
    for e in src.infolist():dst.writestr(e,replace.pop(e.filename,src.read(e)))
    for n,b in replace.items():dst.writestr(n,b)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(H/'artifacts.json').write_text(json.dumps({'input':str(active.relative_to(R)),'input_sha256':sha(baseline),'candidate':str((O/active.name).relative_to(R)),'candidate_sha256':sha(O/active.name)},indent=2)+'\n')
print('Built '+str(O/active.name))

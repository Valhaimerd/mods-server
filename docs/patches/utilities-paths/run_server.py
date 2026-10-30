from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import subprocess,shutil,uuid,json
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/utilities';server=W/('server-'+uuid.uuid4().hex[:8]);mods=server/'mods';mods.mkdir(parents=True)
for p in (R/'tmp/priest-tier-three/server/mods').glob('*.jar'):
 if 'probe' not in p.name and (R/'mods'/p.name).exists():shutil.copy2(R/'mods'/p.name,mods/p.name)
for pattern in ['orb_of_oblivion*.jar','adventurerskills*.jar','puffish*.jar']:
 for p in (R/'mods').glob(pattern):shutil.copy2(p,mods/p.name)
for p in (W/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
shutil.copy2(R/'tmp/orb-acquisition/built/skill_tree-neoforge-1.6.1+26.2.jar',mods/'skill_tree-neoforge-1.6.1+26.2.jar')
subprocess.run(['cmd','/c','mklink','/J',str(server/'libraries'),str(R/'tmp/pvp-26.2/server/libraries')],check=True,capture_output=True)
(server/'eula.txt').write_text('eula=true\n');(server/'server.properties').write_text('online-mode=false\nlevel-type=minecraft:flat\nserver-port=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=-1\n')
cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+(W/'classpath.txt').read_text().strip();classes=W/'probe-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(H/'Probe.java')];a=W/'probe.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'utility-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="utility_probe"\nversion="1"\ndisplayName="Utilities regression"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(W/'last-server.txt').write_text(str(server));print('INSTANCE',server,flush=True)
with (W/'server.log').open('w',encoding='utf-8') as log:r=subprocess.run([str(java/'java.exe'),'-Xmx2G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=server,stdout=log,stderr=subprocess.STDOUT,timeout=180)
result=server/'utilities-result.txt';assert result.exists(),'No result; see tmp/utilities/server.log'
print(result.read_text());shutil.copy2(result,H/'runtime-result.txt');shutil.copy2(server/'utilities-checks.txt',H/'runtime-checks.txt')
assert result.read_text().startswith('PASS'),result.read_text()
(H/'runtime-mods.json').write_text(json.dumps({p.name:__import__('hashlib').sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name},indent=2)+'\n')

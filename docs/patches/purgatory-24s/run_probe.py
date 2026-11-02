from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import sys,shutil,subprocess,json,hashlib,uuid
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/purgatory-24s';baseline='--baseline' in sys.argv;label='baseline' if baseline else 'candidate';S=W/('server-'+label);M=S/'mods';M.mkdir(parents=True,exist_ok=True)
for p in M.glob('*.jar'):p.unlink()
for p in (R/'tmp/magic-third-tier/server/mods').glob('*.jar'):
 if p.name!='overhaul-probe.jar' and (R/'mods'/p.name).exists():shutil.copy2(R/'mods'/p.name,M/p.name)
for p in (W/'before').glob('*.jar'):shutil.copy2(p,M/p.name)
for p in (R/'mods').glob('curios-*.jar'):shutil.copy2(p,M/p.name)
for p in (W/('before' if baseline else 'built')).glob('*.jar'):shutil.copy2(p,M/p.name)
if not (S/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{S/'libraries'}' -Target '{R/'tmp/better-revive-regression/server/libraries'}' | Out-Null"],check=True)
(S/'eula.txt').write_text('eula=true\n');(S/'server.properties').write_text('level-name=isolated-'+uuid.uuid4().hex+'\nonline-mode=false\nlevel-type=minecraft:flat\nserver-port=0\nview-distance=2\nsimulation-distance=2\nmax-tick-time=-1\n')
C=W/'spatial-classes';C.mkdir(exist_ok=True);cp=';'.join(str(p) for p in (W/'built').glob('*.jar'))+';'+(W/'classpath.txt').read_text();J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),str(H/'TimingProbe.java')];a=W/'probe.args';a.write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in args));subprocess.run([str(J/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(M/'phase-two-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="phase_two_probe"\nversion="1"\ndisplayName="Isolated phase one test"\n')
 for p in C.rglob('*.class'):z.write(p,p.relative_to(C).as_posix())
result=S/'phase-two-result.txt';result.unlink(missing_ok=True)
with (W/(label+'.log')).open('w',encoding='utf-8') as log:r=subprocess.run([str(J/'java.exe'),'-Xmx2G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=S,stdout=log,stderr=subprocess.STDOUT,timeout=240)
print(result.read_text() if result.exists() else 'NO RESULT: inspect '+str(W/(label+'.log')))
if result.exists():shutil.copy2(result,H/(label+'-result.txt'));shutil.copy2(S/'phase-two-checks.txt',H/(label+'-checks.txt'))
(H/(label+'-tested.json')).write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in M.glob('*.jar')},indent=2)+'\n')
if not result.exists() or not result.read_text().startswith('FAIL' if baseline else 'PASS'):sys.exit(1)

from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-balance';SERVER=WORK/'server';SERVER.mkdir(exist_ok=True);mods=SERVER/'mods';mods.mkdir(exist_ok=True)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
if not (SERVER/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{ROOT/'tmp/magic-third-tier/server/libraries'}' | Out-Null"],check=True)
for p in (ROOT/'tmp/magic-third-tier/server/mods').glob('*.jar'):
 if 'probe' not in p.name.lower():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
for pattern in ['bettercombat-*.jar']:
 for p in (ROOT/'mods').glob(pattern):shutil.copy2(p,mods/p.name)
for p in (WORK/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
if not (SERVER/'server.properties').exists():
 (SERVER/'server.properties').write_text((ROOT/'tmp/magic-third-tier/server/server.properties').read_text().replace('server-port=25594','server-port=0').replace('level-name=simple-descriptions-20261021','level-name=RaceBalance'));(SERVER/'eula.txt').write_text('eula=true\n')
source=(ROOT/'docs/patches/asgardian-race/AsgardProbe.java').read_text().replace('scale*1.05','scale*1.1').replace('1.05 scale repeat','1.10 scale repeat').replace('2% mining repeat','No mining bonus repeat').replace('mining*1.02','mining').replace('mining*(r==asgard?1.02:1)','mining').replace('mob.discard();arrow.discard();lightning.discard();','mob.discard();arrow.discard();lightning.discard();RaceBalanceProbe.run(world,attacker,checks::add);')
(WORK/'AsgardProbe.java').write_text(source)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"');classes=WORK/'test-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(WORK/'AsgardProbe.java'),str(HERE/'RaceBalanceProbe.java')];(WORK/'test.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'test.args')],check=True)
with ZipFile(mods/'race-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="asgard_probe"\nversion="1"\ndisplayName="Race balance isolated probe"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
result=SERVER/'asgard-result.txt';result.unlink(missing_ok=True)
with (WORK/'server.log').open('w',encoding='utf-8') as log:r=subprocess.run([str(java/'java.exe'),'-Xmx3G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT)
print(result.read_text() if result.exists() else 'NO RESULT; inspect server.log')
if r.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)

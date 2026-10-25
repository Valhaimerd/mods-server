from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import subprocess,shutil,uuid
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-orb-reset';SERVER=WORK/('optional-'+uuid.uuid4().hex[:8]);SERVER.mkdir();mods=SERVER/'mods';mods.mkdir()
shutil.copy2(WORK/'built/iourusraces-1.1.0.jar',mods/'iourusraces-1.1.0.jar')
lib=ROOT/'tmp/pvp-26.2/server/libraries';subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{lib}' | Out-Null"],check=True)
(SERVER/'eula.txt').write_text('eula=true\n');(SERVER/'server.properties').write_text('online-mode=false\nserver-port=25593\nlevel-type=minecraft:flat\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\n')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=str(mods/'iourusraces-1.1.0.jar')+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'optional-classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'tests/OptionalServerProbe.java')];a=WORK/'optional.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'optional-race-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="optional_race_probe"\nversion="1"\ndisplayName="Optional RPG absence test"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
with (WORK/'optional-server.log').open('w',encoding='utf-8') as log:r=subprocess.run([str(java/'java.exe'),'-Xmx2G','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT,timeout=180)
result=SERVER/'optional-result.txt';assert result.exists(),'No optional-server result';text=result.read_text(encoding='utf-8');print(text);shutil.copy2(result,HERE/'optional-server-results.txt');assert r.returncode==0 and text.startswith('PASS'),text

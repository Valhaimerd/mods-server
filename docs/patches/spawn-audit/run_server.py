from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import subprocess,shutil,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/spawn-pool';SERVER=WORK/'server';SERVER.mkdir(parents=True,exist_ok=True);mods=SERVER/'mods';mods.mkdir(exist_ok=True)
spring='--spring' in sys.argv
if spring:
 WORK=ROOT/'tmp/spawn-spring';SERVER=WORK/'server';SERVER.mkdir(parents=True,exist_ok=True);mods=SERVER/'mods';mods.mkdir(exist_ok=True)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
if not (SERVER/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{ROOT/'tmp/magic-third-tier/server/libraries'}' | Out-Null"],check=True)
jar=next((ROOT/'mods').glob('spreadspawnpoints-*.jar'));shutil.copy2(jar,mods/jar.name)
if spring:shutil.copy2(WORK/'built'/jar.name,mods/jar.name)
if not (SERVER/'server.properties').exists():
 s=(ROOT/'tmp/magic-third-tier/server/server.properties').read_text().replace('server-port=25594','server-port=0').replace('level-name=simple-descriptions-20261021','level-name=SpawnAudit');(SERVER/'server.properties').write_text(s);(SERVER/'eula.txt').write_text('eula=true\n')
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=str(mods/jar.name)+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"');classes=WORK/'probe-classes';classes.mkdir(exist_ok=True)
source=HERE/'SpawnProbe.java'
if spring:
 s=source.read_text().replace('spreadspawnpoints:random','spreadspawnpoints:spring').replace('Random','Spring').replace('spreadspawnpoints:vanilla','spreadspawnpoints:spring').replace('Installed default is vanilla','Installed default is Spring').replace('{lowerX:-64,lowerZ:-64,upperX:64,upperZ:64,seed:12345L}','{seed:12345L}').replace('point.x>=-64&&point.x<=64&&point.y>=-64&&point.y<=64','Math.abs(point.x)<=4096&&Math.abs(point.y)<=4096')
 s=s.replace('Set<String> positions=new HashSet<>();','Set<String> positions=new HashSet<>();var prior=new java.util.ArrayList<org.joml.Vector2i>();')
 s=s.replace('positions.add(point.toString());','for(var other:prior)check(point.distance(other)>=128,"Spring reserves player spacing");prior.add(new org.joml.Vector2i(point));var origin=world.getRespawnData().pos();check(point.distance(new org.joml.Vector2i(origin.getX(),origin.getZ()))>=256,"Spring reserves world spawn");positions.add(point.toString());')
 source=WORK/'SpawnProbe.java';source.write_text(s)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(source)];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
with ZipFile(mods/'spawn-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="spawn_probe"\nversion="1"\ndisplayName="Isolated spawn audit"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
result=SERVER/'spawn-result.txt';result.unlink(missing_ok=True)
with (WORK/'server.log').open('w',encoding='utf-8') as log:r=subprocess.run([str(java/'java.exe'),'-Xmx2G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT)
print(result.read_text() if result.exists() else 'NO RESULT; inspect server.log')
if r.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)

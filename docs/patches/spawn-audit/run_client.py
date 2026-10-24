from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,uuid,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/spawn-pool';CLIENT=WORK/'client';CLIENT.mkdir(exist_ok=True);mods=CLIENT/'mods';mods.mkdir(exist_ok=True)
spring='--spring' in sys.argv
if spring:
 WORK=ROOT/'tmp/spawn-spring';CLIENT=WORK/'client-first-join';CLIENT.mkdir(exist_ok=True);mods=CLIENT/'mods';mods.mkdir(exist_ok=True)
jar=next((ROOT/'mods').glob('spreadspawnpoints-*.jar'));shutil.copy2(jar,mods/jar.name)
if spring:shutil.copy2(WORK/'built'/jar.name,mods/jar.name)
source=ROOT/'tmp/archer-tier-one/client'
if not (CLIENT/'saves/Punchy Flat').exists():shutil.copytree(WORK/'server/SpawnAudit' if spring else source/'saves/Punchy Flat',CLIENT/'saves/Punchy Flat')
(CLIENT/'launch.py').write_text((ROOT/'tmp/rogue-tier-two/client/launch.py').read_text().replace('GearProbe','Spawn'+uuid.uuid4().hex[:8]));shutil.copy2(ROOT/'tmp/rogue-tier-two/client/options.txt',CLIENT/'options.txt')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=str(jar)+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'client-classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
probe=HERE/'ClientSpawnProbe.java'
if spring:
 s=probe.read_text();start=s.index('  var server=e.getServer();');end=s.index(' }catch(Exception ex)',start)
 s=s[:start]+'  if(!SpawnPointManager.getInstance(e.getServer().overworld()).getSpawnPointGenerator().identifier().toString().equals("spreadspawnpoints:spring"))throw new AssertionError("Spring not active");\n'+s[end:]
 s=s.replace('point.x<200||point.x>216||point.y<200||point.y>216','Math.abs(point.x)>4096||Math.abs(point.y)>4096').replace('PASS actual client first join','PASS actual client Spring first join');probe=WORK/'ClientSpawnProbe.java';probe.write_text(s)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(probe)];(WORK/'client.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'client.args')],check=True)
with ZipFile(mods/'spawn-client-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="spawn_client_probe"\nversion="1"\ndisplayName="Isolated spawn client"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(CLIENT/'spawn-client-result.txt').unlink(missing_ok=True);subprocess.run(['python',str(CLIENT/'launch.py'),'--world'],check=True)

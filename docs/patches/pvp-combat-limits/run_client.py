from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,uuid,time,json,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pvp-combat-limits';CLIENT=WORK/('client-'+uuid.uuid4().hex[:8]);CLIENT.mkdir();mods=CLIENT/'mods';mods.mkdir()
source=ROOT/'tmp/archer-tier-one/client'
for p in (source/'mods').glob('*.jar'):
 if 'probe' not in p.name.lower() and (ROOT/'mods'/p.name).exists():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
for p in (WORK/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
shutil.copytree(source/'saves/Punchy Flat',CLIENT/'saves/Punchy Flat');(CLIENT/'launch.py').write_text((ROOT/'tmp/rogue-tier-two/client/launch.py').read_text(encoding='utf-8').replace('GearProbe','Combat'+uuid.uuid4().hex[:6]).replace('process = subprocess.Popen', 'command.extend(["--width","1280","--height","720"])\nprocess = subprocess.Popen'),encoding='utf-8');shutil.copy2(ROOT/'tmp/rogue-tier-two/client/options.txt',CLIENT/'options.txt')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'client-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'tests/CombatClientProbe.java')];a=WORK/'client.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'combat-client-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="combat_client_probe"\nversion="1"\ndisplayName="Combat HUD regression"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
print('INSTANCE',CLIENT,flush=True);subprocess.run(['python',str(CLIENT/'launch.py'),'--world'],check=True)
result=CLIENT/'combat-client-result.txt';deadline=time.monotonic()+240
while not result.exists() and time.monotonic()<deadline:time.sleep(1)
assert result.exists(),'No client result: '+str(CLIENT)
text=result.read_text(encoding='utf-8');print(text);shutil.copy2(result,HERE/'client-results.txt');shutil.copy2(CLIENT/'combat-client-checks.txt',HERE/'client-checks.txt')
assert text.startswith('PASS'),text
log=(CLIENT/'logs/latest.log').read_text(encoding='utf-8',errors='replace');assert log.count('[CHAT] PvP combat: building and mining limits apply for 40s.')==2,'Fresh/refresh notification count mismatch'
print('PASS one notice per fresh flag; refresh silent')
(HERE/'client-runtime-mods.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name},indent=2)+'\n',encoding='utf-8')

from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,sys,uuid
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/building-reach';CLIENT=WORK/(('baseline-client-' if '--baseline' in sys.argv else 'client-')+uuid.uuid4().hex[:8]);CLIENT.mkdir(parents=True,exist_ok=True);mods=CLIENT/'mods';mods.mkdir(exist_ok=True)
source=ROOT/'tmp/archer-tier-one/client'
for p in (source/'mods').glob('*.jar'):
 if 'probe' not in p.name.lower() and (ROOT/'mods'/p.name).exists():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
jar=next((ROOT/'mods').glob('sophisticatedbuilding-*.jar'));shutil.copy2(WORK/'before.jar' if '--baseline' in sys.argv else WORK/'built'/jar.name,mods/jar.name)
if not (CLIENT/'saves/Punchy Flat').exists():shutil.copytree(source/'saves/Punchy Flat',CLIENT/'saves/Punchy Flat')
(CLIENT/'launch.py').write_text((ROOT/'tmp/rogue-tier-two/client/launch.py').read_text().replace('GearProbe','RaceTest'+uuid.uuid4().hex[:6]));shutil.copy2(ROOT/'tmp/rogue-tier-two/client/options.txt',CLIENT/'options.txt')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=CLIENT/'client-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'ReachProbe.java')];(WORK/'client.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'client.args')],check=True)
with ZipFile(mods/'building-axis-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="building_axis_probe"\nversion="1"\ndisplayName="Building axis isolated probe"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(WORK/('last-baseline.txt' if '--baseline' in sys.argv else 'last-client.txt')).write_text(str(CLIENT));print('INSTANCE',CLIENT,flush=True)
(CLIENT/'axis-result.txt').unlink(missing_ok=True);subprocess.run(['python',str(CLIENT/'launch.py'),'--world'],check=True)
import time
result=CLIENT/'axis-result.txt';deadline=time.monotonic()+240
while not result.exists() and time.monotonic()<deadline:time.sleep(1)
assert result.exists(), 'Client result missing'
text=result.read_text(encoding='utf-8');print(text)
label='baseline' if '--baseline' in sys.argv else 'candidate'
shutil.copy2(result,HERE/(label+'-results.txt'));shutil.copy2(CLIENT/'axis-checks.txt',HERE/(label+'-checks.txt'))
if label=='candidate':
 assert text.startswith('PASS'),text
 import hashlib
 (HERE/'runtime-tested-sha256.txt').write_text(hashlib.sha256((mods/jar.name).read_bytes()).hexdigest()+'\n',encoding='utf-8')
else:assert text.startswith('FAIL'), 'Baseline unexpectedly passed'

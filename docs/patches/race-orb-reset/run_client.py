from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,uuid,time,json,hashlib,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-orb-reset';CLIENT=WORK/(('baseline-' if '--baseline' in sys.argv else 'client-')+uuid.uuid4().hex[:8]);CLIENT.mkdir();mods=CLIENT/'mods';mods.mkdir()
source=ROOT/'tmp/archer-tier-one/client'
for p in (source/'mods').glob('*.jar'):
 if 'probe' not in p.name.lower() and (ROOT/'mods'/p.name).exists():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
for pattern in ['iourusraces-*.jar','orb_of_oblivion*.jar']:
 for p in (ROOT/'mods').glob(pattern):shutil.copy2(p,mods/p.name)
name='iourusraces-1.1.0.jar';shutil.copy2(WORK/('before-'+name) if '--baseline' in sys.argv else WORK/'built'/name,mods/name)
shutil.copytree(source/'saves/Punchy Flat',CLIENT/'saves/Punchy Flat');(CLIENT/'launch.py').write_text((ROOT/'tmp/rogue-tier-two/client/launch.py').read_text(encoding='utf-8').replace('GearProbe','OrbTest'+uuid.uuid4().hex[:6]),encoding='utf-8');shutil.copy2(ROOT/'tmp/rogue-tier-two/client/options.txt',CLIENT/'options.txt')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=str(WORK/'built'/name)+';'+';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'client-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'tests/RaceOrbClientProbe.java')];a=WORK/'client.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'race-orb-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="race_orb_probe"\nversion="1"\ndisplayName="Race delayed-sync and orb test"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
print('INSTANCE',CLIENT,flush=True);subprocess.run(['python',str(CLIENT/'launch.py'),'--world'],check=True)
result=CLIENT/'race-orb-result.txt';deadline=time.monotonic()+240
while not result.exists() and time.monotonic()<deadline:time.sleep(1)
assert result.exists(),'No test result: '+str(CLIENT)
text=result.read_text(encoding='utf-8');print(text);label='baseline' if '--baseline' in sys.argv else 'client';shutil.copy2(result,HERE/(label+'-results.txt'));shutil.copy2(CLIENT/'race-orb-checks.txt',HERE/(label+'-checks.txt'))
if label=='baseline':assert text.startswith('FAIL') and 'premature' in text,text
else:
 assert text.startswith('PASS'),text
 (HERE/'runtime-tested-mods.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name},indent=2)+'\n',encoding='utf-8')

from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,uuid,time,json,hashlib
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/utilities';client=W/('client-'+uuid.uuid4().hex[:8]);mods=client/'mods';mods.mkdir(parents=True)
source=R/'tmp/archer-tier-one/client'
for p in (source/'mods').glob('*.jar'):
 if 'probe' not in p.name.lower() and (R/'mods'/p.name).exists():shutil.copy2(R/'mods'/p.name,mods/p.name)
for pattern in ['iourusraces-*.jar','orb_of_oblivion*.jar']:
 for p in (R/'mods').glob(pattern):shutil.copy2(p,mods/p.name)
for p in (W/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
shutil.copy2(R/'tmp/orb-acquisition/built/skill_tree-neoforge-1.6.1+26.2.jar',mods/'skill_tree-neoforge-1.6.1+26.2.jar')
shutil.copytree(source/'saves/Punchy Flat',client/'saves/Punchy Flat');(client/'launch.py').write_text((R/'tmp/rogue-tier-two/client/launch.py').read_text().replace('GearProbe','Utility'+uuid.uuid4().hex[:6]));shutil.copy2(R/'tmp/rogue-tier-two/client/options.txt',client/'options.txt')
cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+(W/'classpath.txt').read_text().strip();classes=W/'client-probe-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(H/'ClientProbe.java')];a=W/'client-probe.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'utility-client-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="utility_client_probe"\nversion="1"\ndisplayName="Utilities client verification"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(W/'last-client.txt').write_text(str(client));print('INSTANCE',client,flush=True);subprocess.run(['python',str(client/'launch.py'),'--world'],check=True)
result=client/'utility-client-result.txt';deadline=time.monotonic()+180
while not result.exists() and time.monotonic()<deadline:time.sleep(1)
assert result.exists(),'No client result: '+str(client);print(result.read_text());shutil.copy2(result,H/'client-result.txt');assert result.read_text().startswith('PASS'),result.read_text()
(H/'client-mods.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name},indent=2)+'\n')

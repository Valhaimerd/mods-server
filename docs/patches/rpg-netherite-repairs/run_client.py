from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,uuid,time,json,hashlib
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/rpg-netherite-repairs'
client=W/('client-'+uuid.uuid4().hex[:8]);mods=client/'mods';mods.mkdir(parents=True)
source=R/'tmp/utilities/client-36a729a2'
for p in (source/'mods').glob('*.jar'):
 if 'probe' not in p.name and (R/'mods'/p.name).exists():shutil.copy2(R/'mods'/p.name,mods/p.name)
for pattern in ['armory-*','penchant-*','patchouli-*','IronWolf*','Bookshelf*','PrickleMC*']:
 for p in (R/'mods').glob(pattern+'.jar'):shutil.copy2(p,mods/p.name)
for p in (W/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
shutil.copytree(source/'saves/Punchy Flat',client/'saves/Punchy Flat');shutil.copy2(source/'launch.py',client/'launch.py');shutil.copy2(source/'options.txt',client/'options.txt')
cp=';'.join(str(p) for p in mods.glob('*.jar'))+';'+(R/'tmp/utilities/classpath.txt').read_text().strip()
classes=client/'classes';classes.mkdir();a=client/'compile.args';args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(H/'ClientProbe.java')]
a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'netherite-client-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="netherite_client_probe"\nversion="1"\ndisplayName="RPG repair/progression client check"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(W/'last-client.txt').write_text(str(client));print('INSTANCE',client,flush=True);subprocess.run(['python',str(client/'launch.py'),'--world'],check=True)
result=client/'repair-client-result.txt';deadline=time.monotonic()+240
while not result.exists() and time.monotonic()<deadline:time.sleep(1)
assert result.exists(),'No result: '+str(client)
print(result.read_text());shutil.copy2(result,H/'client-result.txt');shutil.copy2(client/'repair-client-checks.txt',H/'client-checks.txt')
assert result.read_text().startswith('PASS'),result.read_text()
(H/'client-mods.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name},indent=2)+'\n')

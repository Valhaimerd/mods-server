from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import subprocess,shutil,uuid,sys,hashlib,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/unbreaking-progression';label='baseline' if '--baseline' in sys.argv else ('module-off' if '--module-off' in sys.argv else 'candidate');SERVER=WORK/(label+'-'+uuid.uuid4().hex[:8]);SERVER.mkdir();mods=SERVER/'mods';mods.mkdir()
name='penchant-neoforge-26.2.0-1.6-2.5x-progress.jar';shutil.copy2(WORK/'before'/name if label=='baseline' else WORK/'built'/name,mods/name)

for dependency in (ROOT/'mods').glob('patchouli-*.jar'):shutil.copy2(dependency,mods/dependency.name)
for pattern in ['IronWolf*','Bookshelf*','PrickleMC*']:
 for dependency in (ROOT/'mods').glob(pattern):shutil.copy2(dependency,mods/dependency.name)
config=SERVER/'config/penchant';config.mkdir(parents=True);enabled=label!='module-off';(config/'server.toml').write_text('[moduleDefaults]\ndurabilityRework = '+str(enabled).lower()+'\n',encoding='utf-8');(SERVER/'expect-module.txt').write_text(str(enabled).lower(),encoding='utf-8')
subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{ROOT/'tmp/pvp-26.2/server/libraries'}' | Out-Null"],check=True)
(SERVER/'eula.txt').write_text('eula=true\n');(SERVER/'server.properties').write_text('online-mode=false\nserver-port=0\nlevel-type=minecraft:flat\nspawn-protection=0\nview-distance=2\nsimulation-distance=2\n')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=';'.join(str(p) for p in mods.glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'test-classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'DurabilityProbe.java')];a=WORK/'probe.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'unbreaking-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="unbreaking_probe"\nversion="1"\ndisplayName="Unbreaking durability check"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
with (WORK/(label+'.log')).open('w',encoding='utf-8') as log:r=subprocess.run([str(java/'java.exe'),'-Xmx2G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT,timeout=180)
result=SERVER/'unbreaking-result.txt';assert result.exists(),'No runtime result';text=result.read_text(encoding='utf-8');print(text);shutil.copy2(result,HERE/(label+'-results.txt'));shutil.copy2(SERVER/'unbreaking-checks.txt',HERE/(label+'-checks.txt'))
if label=='baseline':assert text.startswith('FAIL')
else:
 assert r.returncode==0 and text.startswith('PASS'),text
 (HERE/(label+'-tested-mods.json')).write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name},indent=2)+'\n',encoding='utf-8')

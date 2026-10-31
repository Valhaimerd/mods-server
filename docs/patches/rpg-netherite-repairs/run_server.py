from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import subprocess,shutil,uuid,sys,json,hashlib
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/rpg-netherite-repairs'
baseline='--baseline' in sys.argv;module_off='--module-off' in sys.argv;label='baseline' if baseline else ('module-off' if module_off else 'candidate')
server=W/(label+'-'+uuid.uuid4().hex[:8]);mods=server/'mods';mods.mkdir(parents=True)
for p in (R/'tmp/priest-tier-three/server/mods').glob('*.jar'):
 if 'probe' not in p.name and (R/'mods'/p.name).exists():shutil.copy2(R/'mods'/p.name,mods/p.name)
for pattern in ['armory-*','penchant-*','patchouli-*','IronWolf*','Bookshelf*','PrickleMC*','orb_of_oblivion*','adventurerskills*','puffish*']:
 for p in (R/'mods').glob(pattern+'.jar'):shutil.copy2(p,mods/p.name)
for p in (W/('before' if baseline else 'built')).glob('*.jar'):shutil.copy2(p,mods/p.name)
if baseline:(server/'baseline').touch()
else:
 shutil.copy2(H/'baseline-items.json',server/'baseline-items.json');shutil.copy2(H/'baseline-stacks.json',server/'baseline-stacks.json')
config=server/'config/penchant';config.mkdir(parents=True)
(config/'server.toml').write_text('[moduleDefaults]\ndurabilityRework = '+str(not module_off).lower()+'\n')
(server/'expected-module.txt').write_text(str(not module_off).lower())
subprocess.run(['cmd','/c','mklink','/J',str(server/'libraries'),str(R/'tmp/pvp-26.2/server/libraries')],check=True,capture_output=True)
(server/'eula.txt').write_text('eula=true\n');(server/'server.properties').write_text('online-mode=false\nserver-port=0\nlevel-type=minecraft:flat\nview-distance=2\nsimulation-distance=2\nmax-tick-time=-1\n')
cp=';'.join(str(p) for p in mods.glob('*.jar'))+';'+(R/'tmp/utilities/classpath.txt').read_text().strip()
classes=server/'classes';classes.mkdir();a=server/'compile.args'
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(H/'Probe.java')]
a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'netherite-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="netherite_repair_probe"\nversion="1"\ndisplayName="RPG repair and worn Unbreaking check"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(W/('last-'+label+'.txt')).write_text(str(server));print('INSTANCE',server,flush=True)
with (server/'console.log').open('w',encoding='utf-8') as log:
 subprocess.run([str(java/'java.exe'),'-Xmx3G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=server,stdout=log,stderr=subprocess.STDOUT,timeout=180)
result=(server/'result.txt').read_text();print(result)
for name in ['result.txt','checks.txt']:shutil.copy2(server/name,H/(label+'-'+name))
if baseline:
 for src,dst in [('item-snapshot.json','baseline-items.json'),('saved-stacks.json','baseline-stacks.json')]:shutil.copy2(server/src,H/dst)
shutil.copy2(server/'target-items.json',H/'target-items.json')
(H/(label+'-mods.json')).write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name},indent=2)+'\n')
assert result.startswith('PASS'),result

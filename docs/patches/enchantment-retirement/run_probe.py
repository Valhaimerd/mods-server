from pathlib import Path
from zipfile import ZipFile
import json,shutil,subprocess,sys
root=Path.cwd();here=root/'docs/patches/enchantment-retirement';work=root/'tmp/enchantment-retirement';server=work/'server';server.mkdir(exist_ok=True);(server/'mods').mkdir(exist_ok=True)
manifest=json.loads((here/'artifacts.json').read_text());(work/'manifest.json').write_text(json.dumps(manifest));candidate='--candidate' in sys.argv
removed={x['jar'] for x in manifest['removed_jars']}
for p in (server/'mods').glob('*.jar'):p.unlink()
for p in (root/'tmp/melee-enchantments/server/mods').glob('*.jar'):
 if p.name=='melee-probe.jar' or candidate and p.name in removed:continue
 active=root/'mods'/p.name
 shutil.copy2(active if active.exists() else p,server/'mods'/p.name)
if candidate:
 for p in (work/'built').glob('*.jar'):shutil.copy2(p,server/'mods'/p.name)
 (server/'baseline.flag').unlink(missing_ok=True)
else:(server/'baseline.flag').touch()
if not (server/'libraries').exists():subprocess.run(['cmd','/c','mklink','/J',str(server/'libraries'),str(root/'tmp/magic-third-tier/server/libraries')],check=True)
(server/'eula.txt').write_text('eula=true\n');(server/'server.properties').write_text('online-mode=false\nserver-port=0\nlevel-type=minecraft:flat\ngenerate-structures=false\nview-distance=2\nsimulation-distance=2\nmax-tick-time=180000\n')
classes=work/'probe-classes';classes.mkdir(exist_ok=True)
cp=(root/'tmp/bows-balance/classpath.txt').read_text().strip()+';'+ ';'.join(str(p) for p in (server/'mods').glob('*.jar'))
args=['--release','25','-cp',cp,'-d',str(classes),str(here/'RetirementProbe.java')];argfile=work/'probe.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(server/'mods/retirement-probe.jar','w') as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="retirement_probe"\nversion="1"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(server/'result.txt').unlink(missing_ok=True)
with (work/('candidate.log' if candidate else 'baseline.log')).open('w') as log:subprocess.run([str(java/'java.exe'),'-Xmx3G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=server,stdout=log,stderr=subprocess.STDOUT,timeout=240)
print((server/'result.txt').read_text() if (server/'result.txt').exists() else 'NO RESULT: inspect log')

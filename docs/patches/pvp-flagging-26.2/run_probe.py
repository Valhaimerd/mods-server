from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,sys,hashlib,re,time
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pvp-26.2';SERVER=WORK/'server';java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
mods=SERVER/'mods'
properties=SERVER/'server.properties'
text=properties.read_text();text=re.sub(r'^level-name=.*\n?','',text,flags=re.M)
properties.write_text(text+'\nlevel-name=policy-test-'+str(time.time_ns())+'\n')
for old in mods.glob('*.jar'):
 if old.name!='pvp-probe.jar' and (ROOT/'mods'/old.name).exists():shutil.copy2(ROOT/'mods'/old.name,old)
for pattern in ['critical_strike-*.jar','curios-neoforge-*.jar']:
 for p in (ROOT/'mods').glob(pattern):shutil.copy2(p,mods/p.name)
selected=WORK/'pvp_flagging-26.2-1.1.2.jar' if '--candidate' in sys.argv else ROOT/'mods/pvp_flagging-26.2-1.1.2.jar'
shutil.copy2(selected,mods/selected.name);(SERVER/'tested-sha256.txt').write_text(hashlib.sha256(selected.read_bytes()).hexdigest())
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=';'.join(p.as_posix() for p in mods.glob('*.jar') if p.name!='pvp-probe.jar')+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'probe-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'tests/PvpProbe.java')];(WORK/'probe.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'probe.args')],check=True)
with ZipFile(mods/'pvp-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="pvp_probe"\nversion="1"\ndisplayName="PvP damage regression"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
result=SERVER/'result.txt';result.unlink(missing_ok=True)
with (WORK/('thirty-candidate.log' if '--candidate' in sys.argv else 'thirty-baseline.log')).open('w') as log:r=subprocess.run([str(java/'java.exe'),'-Xmx3G','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT,timeout=180)
print(result.read_text() if result.exists() else 'NO RESULT')
if r.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)

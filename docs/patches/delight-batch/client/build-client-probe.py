from pathlib import Path
import subprocess, zipfile, shutil, json, os
root=Path(__file__).resolve().parents[4]
p=root/'tmp/delight-batch/client-probe'
mc=Path(os.environ['APPDATA'])/'.minecraft'
version='neoforge-26.2.0.90'
data=json.loads((mc/'versions'/version/(version+'.json')).read_text())
libs=[mc/'libraries'/lib['artifact']['path'] for lib in data['libraries'] if 'artifact' in lib]
libs += [mc/'libraries/net/neoforged/minecraft-client-patched/26.2.0.90/minecraft-client-patched-26.2.0.90.jar', mc/'libraries/net/neoforged/neoforge/26.2.0.90/neoforge-26.2.0.90-universal.jar']
libs += list((root/'tmp/delight-batch/client-all/mods').glob('*.jar'))
cp=os.pathsep.join(str(f) for f in libs if f.is_file())
source=Path(__file__).parent/'ClientDelightProbe.java'
args=p/'compile.args'
args.write_text('--release\n25\n-d\n"'+str(p/'classes').replace('\\','/')+'"\n-classpath\n"'+cp.replace('\\','/')+'"\n"'+str(source).replace('\\','/')+'"\n')
r=subprocess.run(['C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javac.exe','@'+str(args)],capture_output=True,text=True)
(p/'compile.log').write_text(r.stdout+r.stderr)
assert r.returncode==0,r.stderr
with zipfile.ZipFile(p/'delight-client-probe.jar','w') as z:
 for f in (p/'classes').rglob('*.class'):z.write(f,f.relative_to(p/'classes').as_posix())
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="delight_client_probe"\nversion="1"\ndisplayName="Isolated Delight Client Probe"\n')
for name in ['client-base','client-all']:
 shutil.copyfile(p/'delight-client-probe.jar',root/'tmp/delight-batch'/name/'mods/delight-client-probe.jar')
print('Client probe compiled from exact client runtime libraries and packaged in isolated folders')

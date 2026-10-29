from pathlib import Path
import subprocess, zipfile, shutil, json, os
root=Path(__file__).resolve().parents[4]
p=root/'tmp/class-visual-retouch/client/probe'
mc=Path(os.environ['APPDATA'])/'.minecraft'
version='neoforge-26.2.0.90'
data=json.loads((mc/'versions'/version/(version+'.json')).read_text())
libs=[mc/'libraries'/lib['artifact']['path'] for lib in data['libraries'] if 'artifact' in lib]
libs += [mc/'libraries/net/neoforged/minecraft-client-patched/26.2.0.90/minecraft-client-patched-26.2.0.90.jar', mc/'libraries/net/neoforged/neoforge/26.2.0.90/neoforge-26.2.0.90-universal.jar']
libs += list((root/'tmp/class-visual-retouch/client/mods').glob('*.jar'))
cp=os.pathsep.join(str(f) for f in libs if f.is_file())
source=Path(__file__).parent/'VisualClientProbe.java'
args=p/'compile.args'
args.write_text('--release\n25\n-d\n"'+str(p/'classes').replace('\\','/')+'"\n-classpath\n"'+cp.replace('\\','/')+'"\n"'+str(source).replace('\\','/')+'"\n')
r=subprocess.run(['C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin/javac.exe','@'+str(args)],capture_output=True,text=True)
(p/'compile.log').write_text(r.stdout+r.stderr)
assert r.returncode==0,r.stderr
with zipfile.ZipFile(p/'class-visual-probe.jar','w') as z:
 for f in (p/'classes').rglob('*.class'):z.write(f,f.relative_to(p/'classes').as_posix())
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="paladin_client_probe"\nversion="1"\ndisplayName="Isolated Paladin Client Probe"\n')
shutil.copyfile(p/'class-visual-probe.jar',root/'tmp/class-visual-retouch/client/mods/class-visual-probe.jar')
print('Client probe compiled and packaged')

from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import subprocess, shutil, sys

ROOT=Path(__file__).resolve().parents[3]; HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/bifrost-alignment'; SERVER=WORK/('server-no-revive' if '--no-revive' in sys.argv else 'server'); SERVER.mkdir(parents=True,exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
mods=SERVER/'mods';mods.mkdir(exist_ok=True)
for p in (ROOT/'tmp/asgardian-implementation/server/mods').glob('*.jar'):
    if 'probe' not in p.name.lower():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
if '--revive' in sys.argv:
    for pattern in ['PlayerRevive_NEOFORGE*','CreativeCore_NEOFORGE*','Better Revive - *','Better Revive X PlayerRevive*','curios-neoforge*']:
        for p in (ROOT/'mods').glob(pattern):shutil.copy2(p,mods/p.name)
jar=next((ROOT/'mods').glob('bifrost-*.jar'))
shutil.copy2(WORK/'built'/jar.name if '--candidate' in sys.argv else jar,mods/jar.name)
if not (SERVER/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{ROOT/'tmp/magic-third-tier/server/libraries'}' | Out-Null"],check=True)
if not (SERVER/'server.properties').exists():
    properties=(ROOT/'tmp/asgardian-implementation/server/server.properties').read_text(encoding='utf-8').replace('AsgardianTest','BifrostAlignmentTest')
    (SERVER/'server.properties').write_text(properties,encoding='utf-8');(SERVER/'eula.txt').write_text('eula=true\n')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8')
cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+ ';'.join(p.as_posix() for p in (ROOT/'mods').glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'probe-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-cp',cp,'-d',str(classes),str(HERE/'BifrostProbe.java')]
(WORK/'probe.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8')
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'probe.args')],check=True)
with ZipFile(mods/jar.name) as z: contents={n:z.read(n) for n in z.namelist()}
contents['META-INF/neoforge.mods.toml']+=b'\n[[mods]]\nmodId="bifrost_probe"\nversion="1"\ndisplayName="Bifrost isolated regression probe"\n'
(mods/'bifrost-probe.jar').unlink(missing_ok=True)
with ZipFile(mods/jar.name,'w',ZIP_DEFLATED) as z:
    for n,data in contents.items():z.writestr(n,data)
    for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
result=SERVER/'bifrost-result.txt';result.unlink(missing_ok=True)
label='candidate' if '--candidate' in sys.argv else 'baseline'
with (WORK/(label+'-server.log')).open('w',encoding='utf-8') as log:
    proc=subprocess.run([str(JAVA/'java.exe'),'-Xmx3G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT)
print(result.read_text() if result.exists() else 'NO RESULT - inspect log')
if proc.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)

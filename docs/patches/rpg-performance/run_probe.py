from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import subprocess, shutil, sys, hashlib

ROOT=Path(__file__).resolve().parents[3]; HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/rpg-performance'; SERVER=WORK/'server'; SERVER.mkdir(parents=True,exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
mods=SERVER/'mods';mods.mkdir(exist_ok=True)
for p in (ROOT/'tmp/asgardian-implementation/server/mods').glob('*.jar'):
    if 'probe' not in p.name.lower():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
if '--revive' in sys.argv:
    for pattern in ['PlayerRevive_NEOFORGE*','CreativeCore_NEOFORGE*','Better Revive - *','Better Revive X PlayerRevive*','curios-neoforge*']:
        for p in (ROOT/'mods').glob(pattern):shutil.copy2(p,mods/p.name)
jar=next((ROOT/'mods').glob('spell_engine-*.jar'))
selected=WORK/'built'/jar.name if '--candidate' in sys.argv else jar
shutil.copy2(selected,mods/jar.name)
(SERVER/'tested-sha256.txt').write_text(hashlib.sha256(selected.read_bytes()).hexdigest(),encoding='utf-8')
if not (SERVER/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{ROOT/'tmp/magic-third-tier/server/libraries'}' | Out-Null"],check=True)
if not (SERVER/'server.properties').exists():
    properties=(ROOT/'tmp/asgardian-implementation/server/server.properties').read_text(encoding='utf-8').replace('AsgardianTest','AsgardExpansionTest')
    (SERVER/'server.properties').write_text(properties,encoding='utf-8');(SERVER/'eula.txt').write_text('eula=true\n')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8')
cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+ ';'.join(p.as_posix() for p in (ROOT/'mods').glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'probe-classes'
assert classes.resolve().is_relative_to(WORK.resolve())
if classes.exists():shutil.rmtree(classes)
classes.mkdir()
args=['--release','25','-cp',cp,'-d',str(classes),str(HERE/'PerformanceProbe.java')]
(WORK/'probe.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8')
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'probe.args')],check=True)
with ZipFile(mods/'performance-probe.jar','w',ZIP_DEFLATED) as z:
    z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="rpg_performance_probe"\nversion="1"\ndisplayName="RPG lifecycle probe"\n')
    for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
result=SERVER/'performance-result.txt';result.unlink(missing_ok=True)
label=('candidate' if '--candidate' in sys.argv else 'baseline')+('-no-revive' if '--no-revive' in sys.argv else '-revive' if '--revive' in sys.argv else '')
with (WORK/(label+'-server.log')).open('w',encoding='utf-8') as log:
    proc=subprocess.run([str(JAVA/'java.exe'),'-Xmx3G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT,timeout=180)
print(result.read_text() if result.exists() else 'NO RESULT - inspect log')
if proc.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)

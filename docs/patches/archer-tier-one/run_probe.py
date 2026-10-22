"""Exercise current pack or saved baseline in an isolated dedicated instance."""
from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import shutil, subprocess, sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/archer-tier-one';SERVER=WORK/'server';SERVER.mkdir(parents=True,exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
if not (SERVER/'libraries').exists():
 subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{ROOT/'tmp/magic-third-tier/server/libraries'}' | Out-Null"],check=True)
mods=SERVER/'mods';mods.mkdir(exist_ok=True)
for p in (ROOT/'tmp/magic-third-tier/server/mods').glob('*.jar'):
 if p.name!='overhaul-probe.jar':shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
for p in (WORK/('follow-before' if '--baseline' in sys.argv else 'built')).glob('*.jar'):shutil.copy2(p,mods/p.name)
if not (SERVER/'server.properties').exists():
 text=(ROOT/'tmp/magic-third-tier/server/server.properties').read_text().replace('server-port=25594','server-port=0').replace('level-name=simple-descriptions-20261021','level-name=ArcherTierOne')
 (SERVER/'server.properties').write_text(text);(SERVER/'eula.txt').write_text('eula=true\n')
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text(encoding='utf-8')
cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
classes=WORK/'test-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-cp',cp,'-d',str(classes),str(HERE/'ArcherProbe.java'),str(HERE/'TierTwoProbe.java'),str(HERE/'ArcherRevisionProbe.java'),str(ROOT/'docs/patches/rogue-tier-one/RogueProbe.java'),str(ROOT/'docs/patches/rogue-tier-two/RogueTierTwoProbe.java'),str(ROOT/'docs/patches/magic-third-tier/PartyProbe.java')]
(WORK/'test.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'test.args')],check=True)
with ZipFile(mods/'archer-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="archer_probe"\nversion="1"\ndisplayName="Archer probe"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
result=SERVER/'archer-result.txt';result.unlink(missing_ok=True)
label='baseline' if '--baseline' in sys.argv else 'current'
with (WORK/(label+'.log')).open('w',encoding='utf-8') as log:
 r=subprocess.run([str(JAVA/'java.exe'),'-Xmx3G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT)
print(result.read_text() if result.exists() else 'NO RESULT - inspect log')
if r.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)

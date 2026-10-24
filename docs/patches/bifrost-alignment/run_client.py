from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,sys,uuid
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/bifrost-alignment';CLIENT=WORK/'client';CLIENT.mkdir(parents=True,exist_ok=True);mods=CLIENT/'mods';mods.mkdir(exist_ok=True)
source=ROOT/'tmp/archer-tier-one/client'
for p in (source/'mods').glob('*.jar'):
    if 'probe' not in p.name.lower() and (ROOT/'mods'/p.name).exists():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
for p in (ROOT/'mods').glob('iourusraces-*.jar'):shutil.copy2(p,mods/p.name)
jar=next((WORK/'built').glob('bifrost-*.jar'));shutil.copy2(jar,mods/jar.name)
if not (CLIENT/'saves/Punchy Flat').exists():shutil.copytree(source/'saves/Punchy Flat',CLIENT/'saves/Punchy Flat')
launch=(ROOT/'tmp/rogue-tier-two/client/launch.py').read_text(encoding='utf-8').replace('GearProbe','Bifrost'+uuid.uuid4().hex[:8]);(CLIENT/'launch.py').write_text(launch,encoding='utf-8')
if not (CLIENT/'options.txt').exists():shutil.copy2(ROOT/'tmp/rogue-tier-two/client/options.txt',CLIENT/'options.txt')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'client-classes';classes.mkdir(exist_ok=True);args=['--release','25','-cp',cp,'-d',str(classes),str(HERE/'ClientBifrostProbe.java')];(WORK/'client-test.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8');java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'client-test.args')],check=True)
with ZipFile(mods/jar.name) as z:contents={n:z.read(n) for n in z.namelist()}
contents['META-INF/neoforge.mods.toml']+=b'\n[[mods]]\nmodId="bifrost_client_probe"\nversion="1"\ndisplayName="Bifrost isolated client probe"\n'
with ZipFile(mods/jar.name,'w',ZIP_DEFLATED) as z:
    for n,data in contents.items():z.writestr(n,data)
    for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
(CLIENT/'bifrost-client-result.txt').unlink(missing_ok=True)
subprocess.run([sys.executable,str(CLIENT/'launch.py'),'--world'],check=True)

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-orb-reset';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True);name='iourusraces-1.1.0.jar';before=WORK/('before-'+name)
if not before.exists():shutil.copy2(ROOT/'mods'/name,before)
expected=json.loads((ROOT/'docs/patches/race-leaf-climbing/artifacts.json').read_text(encoding='utf-8'))['sha256'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();assert sha(before)==expected,'Unexpected prior race baseline'
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=str(before)+';'+str(next((ROOT/'mods').glob('skill_tree-*.jar')))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,HERE.glob('*.java'))];a=WORK/'compile.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(before) as z:infos=z.infolist();files={i.filename:z.read(i) for i in infos}
old=dict(files)
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
key='iourusraces.mixins.json';cfg=json.loads(files[key]);cfg['mixins'].append('OrbRaceResetMixin');files[key]=(json.dumps(cfg,indent=2)+'\n').encode()
with ZipFile(OUT/name,'w') as z:
 for info in infos:z.writestr(info,files[info.filename])
 for n in sorted(set(files)-set(old)):z.writestr(n,files[n])
(HERE/'artifacts.json').write_text(json.dumps({'jar':name,'before_sha256':sha(before),'sha256':sha(OUT/name),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(set(files)-set(old)),'sources':{p.name:sha(p) for p in HERE.glob('*.java')}},indent=2)+'\n',encoding='utf-8');print('Built race sync gate and native Orb of Oblivion reset candidate')

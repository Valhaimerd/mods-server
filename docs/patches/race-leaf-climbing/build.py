from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-leaf-climbing';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=ROOT/'mods/iourusraces-1.1.0.jar';before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=str(before)+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'RaceVisualRules.java'),str(HERE/'LivingEntityClimbingMixin.java')]
a=WORK/'compile.args';a.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args),encoding='utf-8');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(before) as z:infos=z.infolist();files={i.filename:z.read(i) for i in infos}
old=dict(files)
for f in classes.rglob('*.class'):files[f.relative_to(classes).as_posix()]=f.read_bytes()
key='iourusraces.mixins.json';config=json.loads(files[key]);config['mixins'].append('LivingEntityClimbingMixin');files[key]=(json.dumps(config,indent=2)+'\n').encode()
for lang in ['en_us','es_es']:
 key=f'assets/iourusraces/lang/{lang}.json';o=json.loads(files[key])
 if lang=='en_us':
  o['race.iourusraces.elf.trait.2']='+ Gentle falls and leaf climbing'
  o['race.iourusraces.elf.tooltip']='Elves move quickly, jump higher and fall gently. They can climb leaves, but have less health.'
  o['race.iourusraces.goblin.trait.2']='+ Night vision and leaf climbing'
  o['race.iourusraces.goblin.tooltip']='Goblins are small and quick. They jump higher, see in the dark and climb leaves, but have much less health.'
 else:
  o['race.iourusraces.elf.trait.2']='+ Caidas suaves y trepar por hojas'
  o['race.iourusraces.elf.tooltip']='Los elfos son rapidos, saltan alto y caen suavemente. Trepan por hojas, pero tienen menos vida.'
  o['race.iourusraces.goblin.trait.2']='+ Vision nocturna y trepar por hojas'
  o['race.iourusraces.goblin.tooltip']='Los goblins son pequenos y rapidos. Saltan alto, ven en la oscuridad y trepan por hojas, pero tienen menos vida.'
 files[key]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
with ZipFile(OUT/active.name,'w') as out:
 for info in infos:out.writestr(info,files[info.filename])
 for name in sorted(set(files)-set(old)):out.writestr(name,files[name])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(HERE/'artifacts.json').write_text(json.dumps({'jar':active.name,'before_sha256':sha(before),'sha256':sha(OUT/active.name),'sources':{f.name:sha(f) for f in HERE.glob('*.java') if 'Probe' not in f.name},'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))},indent=2)+'\n',encoding='utf-8')
print('Built universal solid leaves and Elf/Goblin leaf-only climbing candidate')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/class-armor-survivability';BEFORE=WORK/'before';OUT=WORK/'built';OUT.mkdir(exist_ok=True)
inputs=[]
for pattern in ['wizards-*.jar','rogues-*.jar','archers-*.jar','paladins-*.jar','armory-*.jar']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 inputs.append(source)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(map(str,[*inputs,next((ROOT/'mods').glob('spell_engine-*')),asm,tree]))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"');classes=WORK/'classes';classes.mkdir(exist_ok=True)
packages=['net.wizards.item','net.rogues.item.armor','net.archers.item','net.paladins.item.armor','net.armory_rpgs.item'];sources=[HERE/'PatchArmor.java']
for package in packages:
 p=WORK/'src'/package.replace('.','/')/'ClassArmorBalance.java';p.parent.mkdir(parents=True,exist_ok=True);p.write_text((HERE/'ClassArmorBalance.java').read_text().replace('package MOD_PACKAGE;',f'package {package};'));sources.append(p)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*map(str,sources)];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for source,package,cls in zip(inputs,packages,['WizardArmors','RogueArmors','ArcherArmors','Armors','ArmorSets']):
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old);prefix=package.replace('.','/');name=prefix+'/'+cls+'.class';helper=prefix+'/ClassArmorBalance'
 (WORK/'in.class').write_bytes(files[name]);subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchArmor',str(WORK/'in.class'),str(WORK/'out.class'),helper],check=True);files[name]=(WORK/'out.class').read_bytes();files[helper+'.class']=(classes/(helper+'.class')).read_bytes()
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys())})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built five same-name class-armor candidates')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[4];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/paladin-doctrine/armor';BEFORE=WORK.parent/'before';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text()
inputs=[next(BEFORE.glob('paladins-*.jar')),next(BEFORE.glob('armory-*.jar'))]
cp=';'.join(map(str,[*inputs,next((ROOT/'mods').glob('spell_engine-*'))]))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);sources=[]
packages=['net.paladins.item.armor','net.armory_rpgs.item']
for package in packages:
 p=WORK/'src'/package.replace('.','/')/'HealingArmorBalance.java';p.parent.mkdir(parents=True,exist_ok=True)
 p.write_text((HERE/'HealingArmorBalance.java').read_text().replace('package MOD_PACKAGE;',f'package {package};'));sources.append(p)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*map(str,sources)]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for source,package in zip(inputs,packages):
 helper=package.replace('.','/')+'/HealingArmorBalance.class'
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 assert helper in old,'Expected already-hooked input helper'
 files=dict(old);files[helper]=(classes/helper).read_bytes()
 with ZipFile(OUT/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':[]})
 assert records[-1]['changed']==[helper]
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print(json.dumps(records,indent=2))

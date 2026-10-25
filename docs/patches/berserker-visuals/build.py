from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/berserker-visuals';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');inputs=[]
for pattern in ['skill_tree-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 inputs.append(source)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=';'.join(map(str,inputs))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'))
classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),*[str(HERE/n) for n in ['BerserkerAppearance.java','BerserkerSparkles.java','PatchAppearance.java']]]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for source in inputs:
 common=source.name.startswith('spell_engine');target='net/spell_engine/entity/WarriorUltimates.class' if common else 'net/skill_tree_rpgs/client/SkillTreeClientMod.class'
 helper='net/spell_engine/entity/BerserkerAppearance.class' if common else 'net/skill_tree_rpgs/client/effect/BerserkerSparkles.class'
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old);(WORK/'in.class').write_bytes(files[target]);subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchAppearance',str(WORK/'in.class'),str(WORK/'out.class'),'common' if common else 'client'],check=True)
 files[target]=(WORK/'out.class').read_bytes();files[helper]=(classes/helper).read_bytes()
 with ZipFile(OUT/source.name,'w') as z:
  for i in infos:z.writestr(i,files[i.filename])
  z.writestr(helper,files[helper])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[target],'added':[helper]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built effect-bound tint/sparkle candidates')

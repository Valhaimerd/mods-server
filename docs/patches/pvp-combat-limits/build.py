from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pvp-combat-limits';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
names=['pvp_flagging-26.2-1.1.2.jar','sophisticatedbuilding-neoforge-26.2-5.1.0.jar','onekeyminer-neoforge-1.6.9-26.2.jar']
EXPECTED_BASELINES={'pvp_flagging-26.2-1.1.2.jar': 'e6aaacdf67b31dd74d3f4ff246dee02ed291b6b68e8f9ca0f409f7b99e7d3943', 'sophisticatedbuilding-neoforge-26.2-5.1.0.jar': 'd89bdde9f8088f5b46a7046ff1ff7febe34a300929462f1ed0aa7cf2eedad9d1', 'onekeyminer-neoforge-1.6.9-26.2.jar': '284e73f1727b5f2411fbe9ccc676e2d627b9fab4be98f8ff1c65e441f4085ce3'}
baselines=[]
for name in names:
 before=WORK/('before-'+name)
 if not before.exists():shutil.copy2(ROOT/'mods'/name,before)
 assert hashlib.sha256(before.read_bytes()).hexdigest()==EXPECTED_BASELINES[name], "Expected pre-combat baseline; restore it from the retired-pack backup before rebuilding: "+name
 baselines.append(before)
classes=WORK/'classes';classes.mkdir(exist_ok=True)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=';'.join(map(str,baselines))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')+';'+str(next((ROOT/'mods').glob('better-party-neoforge-*.jar')))
asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm-tree').rglob('asm-tree-*.jar'));cp+=';'+str(asm)+';'+str(tree)
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,HERE.glob('*.java'))];a=WORK/'compile.args';a.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args),encoding='utf-8');subprocess.run([str(JAVA/'javac.exe'),'@'+str(a)],check=True)
reports=[]
for index,before in enumerate(baselines):
 with ZipFile(before) as z:infos=z.infolist();files={i.filename:z.read(i) for i in infos}
 old=dict(files)
 if index==0:
  for p in (classes/'io/redspace/pvp_flagging').rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
  files['pvp_flagging.mixins.json']=(json.dumps({'required':True,'package':'io.redspace.pvp_flagging.mixin','compatibilityLevel':'JAVA_25','mixins':['BreakSuccessMixin','PlaceSuccessMixin'],'injectors':{'defaultRequire':1}})+'\n').encode()
  meta=files['META-INF/neoforge.mods.toml'].decode();meta=meta.replace('No flagging, cooldowns, zones or indicators.','40-second combat flags restrict advanced building/mining and apply ordinary block quotas with a status HUD.');meta+='\n[[mixins]]\nconfig="pvp_flagging.mixins.json"\n';files['META-INF/neoforge.mods.toml']=meta.encode()
  icons=HERE/'assets';icons.mkdir(exist_ok=True)
  for target,source in [('combat','Pixel Art Crossed Iron Swords.png'),('breaking','ChatGPT Image Oct 25, 2026, 05_45_46 PM.png'),('placing','ChatGPT Image Oct 25, 2026, 05_45_57 PM.png')]:
   p=icons/(target+'.png')
   if not p.exists():shutil.copy2(Path('C:/Users/Valhaimerd/Desktop')/source,p)
   files['assets/pvp_flagging/textures/gui/'+target+'.png']=p.read_bytes()
 else:
  paths=['sophisticated/building/systems/ServerBlockPlacer.class','sophisticated/building/systems/UndoRedo.class'] if index==1 else ['org/xiyu/onekeyminer/chain/ChainActionLogic.class']
  for key in paths:
   inp=WORK/'in.class';out=WORK/'out.class';inp.write_bytes(files[key]);subprocess.run([str(JAVA/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchTools',str(inp),str(out)],check=True);files[key]=out.read_bytes()
  helper='sophisticated/building/pack/BuildingGuard.class' if index==1 else 'org/xiyu/onekeyminer/pack/MiningGuard.class';files[helper]=(classes/helper).read_bytes()
 with ZipFile(OUT/names[index],'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(set(files)-set(old)):z.writestr(n,files[n])
 sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 reports.append({'jar':names[index],'before_sha256':sha(before),'sha256':sha(OUT/names[index]),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(reports,indent=2)+'\n',encoding='utf-8');print('Built three candidates; not installed')

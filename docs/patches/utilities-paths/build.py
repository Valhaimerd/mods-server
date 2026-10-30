from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,subprocess,shutil
from nodes import *
H=Path(__file__).resolve().parent;R=H.parents[2];W=R/'tmp/utilities';O=W/'built';O.mkdir(parents=True,exist_ok=True)
names=['adventurerskills-EN-1.3-26.2-neoforge-fixed.jar','orb_of_oblivion_puffish_compat-1.0.2.jar']
before=W/'before';before.mkdir(exist_ok=True)
for n in names:
 if not (before/n).exists():shutil.copy2(R/'mods'/n,before/n)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
with ZipFile(before/names[0]) as z:files={n:z.read(n) for n in z.namelist()}
original=dict(files);pre='data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
s,d,c=[json.loads(files[pre+n+'.json']) for n in ['skills','definitions','connections']]
for n in REMOVED:s.pop(n,None)
for group in c.values():
 for key,edges in group.items():group[key]=[e for e in edges if not any(n in REMOVED for n in e)]
def attr(key,value):
 native={'stamina':('puffish_attributes:stamina','multiply_base'),'jump':('minecraft:jump_strength','multiply_base'),'fall':('minecraft:safe_fall_distance','addition'),'sneak':('minecraft:sneaking_speed','multiply_base'),'luck':('minecraft:luck','addition')}
 a,op=native.get(key,('orb_of_oblivion_puffish_compat:utility_'+key,'addition'))
 return {'type':'puffish_skills:attribute','data':{'attribute':a,'value':value,'operation':op}}
labels={'stamina':'Stamina','food':'Food Restored and Saturation Received','breath':'Underwater Breathing Duration','jump':'Jump Strength','fall':'Safe Fall Distance','climb':'Ladder/Vine Climbing Speed','sneak':'Sneak Speed','freeze':'Slower Environmental Freezing','cost':'Anvil and Enchanting XP Cost','xp':'Ordinary XP Gain','durability':'Chance to Preserve Tool Durability','luck':'Luck'}
icons={'stamina':'golden_carrot','food':'bread','breath':'turtle_helmet','jump':'slime_ball','fall':'feather','climb':'ladder','sneak':'leather_boots','freeze':'powder_snow_bucket','cost':'enchanted_book','xp':'experience_bottle','durability':'anvil','luck':'emerald'}
for n,rewards in REWARDS.items():
 if n not in s:continue
 title=[]
 for key,v in rewards.items():
  if key=='luck':continue
  title.append((f'+{v:g} Block ' if key=='fall' else f'{"-" if key=="cost" else "+"}{round(v*100)}% ')+labels[key])
 key='utility_'+n;s[n]['definition']=key
 d[key]={'title':'\n'.join(title),'size':1,'icon':{'type':'item','data':{'item':'minecraft:'+icons[next(iter(rewards))]}},'frame':{'type':'advancement','data':{'frame':'task'}},'rewards':[attr(k,v) for k,v in rewards.items()]}
masteries=[('Fitness','When hunger falls to 3 drumsticks or less, restore 4 drumsticks. Cooldown: 3 minutes.','golden_carrot',-340),('Adaptability','Soften a damaging landing by 50%. For 5 seconds, resist powder-snow sinking and freezing damage. Cooldown: 20 seconds.','leather_boots',0),('Ingenuity','Beneficial food and drink effects last 35% longer. +1 Luck.','enchanted_book',340)]
for path,(name,desc,icon,x) in zip([F,A,I],masteries):
 n=path[-1];key='utility_'+n;s[n]={'definition':key,'x':x,'y':-476,'root':False}
 d[key]={'title':name,'description':{'text':desc},'size':1.5,'icon':{'type':'item','data':{'item':'minecraft:'+icon}},'frame':{'type':'advancement','data':{'frame':'challenge'}},'required_skills':7,'rewards':[attr('luck',1)] if path==I else []}
 # Native neighbor-count requirements appear correctly in both server and client UI.
 for ordinary in path[:-1]:
  if [ordinary,n] not in c['normal']['bidirectional']:c['normal']['bidirectional'].append([ordinary,n])
for a,b in [(F[0],A[0]),(F[0],I[0]),(A[0],I[0])]:c['exclusive']['bidirectional'].append([a,b])
for n,data in [('skills',s),('definitions',d),('connections',c)]:files[pre+n+'.json']=(json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode()
def write(name,files):
 with ZipFile(O/name,'w',ZIP_DEFLATED) as z:
  for n,b in files.items():z.writestr(n,b)
write(names[0],files)
subprocess.run(['python',str(H/'verify.py'),str(O/names[0])],check=True)
cp=';'.join(str(p.resolve()) for p in (R/'mods').glob('*.jar'))+';'+(W/'classpath.txt').read_text().strip()
classes=W/'classes'
if classes.exists():
 assert classes.resolve().parent==W.resolve();shutil.rmtree(classes)
classes.mkdir()
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(classes),*map(str,(H/'src').glob('*.java'))]
argfile=W/'compile.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8')
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(before/names[1]) as z:files={n:z.read(n) for n in z.namelist()}
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
files['utilities-nodes.json']=json.dumps(CONTRACT,indent=2).encode()
mixins=[p.stem for p in (H/'src').glob('*.java') if ('Mixin' in p.stem or p.stem.endswith('Access')) and p.stem!='EnchantScreenMixin']
files['utilities.mixins.json']=json.dumps({'required':True,'minVersion':'0.8','package':'net.valhaimerd.orboblivioncompat.mixin','compatibilityLevel':'JAVA_25','mixins':mixins,'client':['EnchantScreenMixin'],'injectors':{'defaultRequire':1}}).encode()
toml='META-INF/neoforge.mods.toml';files[toml]+=b'\n[[mixins]]\nconfig="utilities.mixins.json"\n'
files['assets/orb_of_oblivion_puffish_compat/lang/en_us.json']=json.dumps({'effect.orb_of_oblivion_puffish_compat.sure_footing':'Sure Footing','effect.orb_of_oblivion_puffish_compat.sure_footing.description':'Resists powder-snow sinking and freezing damage.'}).encode()
# Reuse an installed, legible boot icon; no missing-effect sprite.
with ZipFile(before/names[0]) as z:
 candidates=[n for n in z.namelist() if n.endswith('.png') and ('boot' in n or 'feather' in n)]
 if candidates:files['assets/orb_of_oblivion_puffish_compat/textures/mob_effect/sure_footing.png']=z.read(candidates[0])
if 'assets/orb_of_oblivion_puffish_compat/textures/mob_effect/sure_footing.png' not in files:
 from PIL import Image,ImageDraw
 import io
 im=Image.new('RGBA',(18,18));draw=ImageDraw.Draw(im);draw.polygon([(4,2),(10,2),(10,10),(15,11),(15,15),(3,15),(3,10),(4,10)],fill='#bce9ff',outline='#294c76');b=io.BytesIO();im.save(b,format='PNG');files['assets/orb_of_oblivion_puffish_compat/textures/mob_effect/sure_footing.png']=b.getvalue()
write(names[1],files)
records=[]
for name in names:
 with ZipFile(before/name) as z:old={n:z.read(n) for n in z.namelist()}
 with ZipFile(O/name) as z:new={n:z.read(n) for n in z.namelist()};assert z.testzip() is None
 records.append({'jar':name,'before_sha256':sha(before/name),'sha256':sha(O/name),'changed':[n for n in old if old[n]!=new.get(n)],'added':sorted(set(new)-set(old))})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Staged Utilities tree and compatibility runtime')

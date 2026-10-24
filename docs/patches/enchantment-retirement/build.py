"""Remove retired modded registry entries and their acquisition references."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,re,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/enchantment-retirement';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True);BEFORE=WORK/'before';BEFORE.mkdir(exist_ok=True)
retired=json.loads((HERE/'retired-ids.json').read_text());IDS=set(retired);TOKENS=IDS|{'#'+x for x in IDS}
remove_patterns=['Barricades-*.jar','life-steal-*.jar','air-jump-*.jar','protection-grace-*.jar','tense-enchantment-*.jar'];removed_jars=[]
for pattern in remove_patterns:
 p=next((ROOT/'mods').glob(pattern),None)
 if p is None:p=next((ROOT/'mod fixing/retired packs/enchantment-retirement-20261024').glob(pattern))
 removed_jars.append({'jar':p.name,'sha256':hashlib.sha256(p.read_bytes()).hexdigest()})
removed_names={r['jar'] for r in removed_jars}
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm-tree').rglob('asm-tree-*.jar'));classes=WORK/'patch-classes';classes.mkdir(exist_ok=True)
subprocess.run([str(java/'javac.exe'),'--release','25','-cp',str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'PatchRetirement.java')],check=True)
cp=(ROOT/'tmp/bows-balance/classpath.txt').read_text().strip()
args=['--release','25','-cp',cp,'-d',str(classes),str(HERE/'RetiredEnchantmentsMixin.java'),str(HERE/'RetiredProgressMixin.java')]
(WORK/'codec.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'codec.args')],check=True)
def patch(data,mode):
 (WORK/'in.class').write_bytes(data);subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchRetirement',str(WORK/'in.class'),str(WORK/'out.class'),mode],check=True);return (WORK/'out.class').read_bytes()
def contains(o):
 if isinstance(o,str):return o in TOKENS
 if isinstance(o,list):return any(contains(v) for v in o)
 if isinstance(o,dict):return any(k in TOKENS or contains(v) for k,v in o.items())
 return False
def clean(o):
 if isinstance(o,str):return None if o in TOKENS else o
 if isinstance(o,list):return [v for x in o if (v:=clean(x)) is not None]
 if not isinstance(o,dict):return o
 # Function IDs are a different registry; Gravity legitimately calls nova_structures:ghasted.
 if o.get('type')=='minecraft:run_function':return o
 # A book whose only enchantment source is retired should leave the pool entirely.
 if o.get('type')=='minecraft:item' and o.get('name') in ['minecraft:book','minecraft:enchanted_book'] and contains(o):
  updated={k:clean(v) for k,v in o.items()};encoded=json.dumps(updated)
  remaining=updated.get('functions',[])
  if not any(f and f.get('function') in ['minecraft:enchant_randomly','minecraft:set_enchantments','minecraft:enchant_with_levels','minecraft:set_components'] for f in remaining):return None
 if o.get('function') in ['minecraft:enchant_randomly','minecraft:enchant_with_levels'] and isinstance(o.get('options'),str) and o['options'] in TOKENS:return None
 if set(o)=={'id','required'} and o['id'] in TOKENS:return None
 result={k:v for k,x in o.items() if k not in TOKENS and (v:=clean(x)) is not None}
 if result.get('function')=='minecraft:set_enchantments' and not result.get('enchantments'):return None
 return result
records=[]
for active in sorted((ROOT/'mods').glob('*.jar')):
 if active.name in removed_names:continue
 with ZipFile(active) as z:
  needed=any(n.startswith('data/') and n.endswith(('.json','.mcfunction')) and any(x.encode() in z.read(n) for x in IDS) for n in z.namelist())
 if not needed and not active.name.startswith(('betterarcheology-','EnderZoology-')):continue
 source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old);removed=[]
 for n,data in old.items():
  if not n.startswith('data/'):continue
  if n.endswith('.json'):
   o=json.loads(data);id=n.split('/')[1]+':'+Path(n).stem
   if ('/enchantment/' in n and '/tags/' not in n and id in IDS) or ('/recipe/' in n and contains(o)):
    del files[n];removed.append(n);continue
   if contains(o):
    new=clean(o)
    if new!=o:files[n]=(json.dumps(new,indent=2,ensure_ascii=False)+'\n').encode()
  elif n.endswith('.mcfunction'):
   lines=data.decode().splitlines();new=[s for s in lines if not ('enchant' in s and any(re.search(re.escape(id)+r'(?![a-z0-9_./-])',s) for id in IDS))]
   if new!=lines:files[n]=('\n'.join(new)+'\n').encode()
 if active.name.startswith('betterarcheology-'):
  n='betterarcheology.mixins.json';o=json.loads(files[n]);o['mixins'].remove('PenetratingStrikeMixin');files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='net/Pandarix/item/ModItemGroup.class';files[n]=patch(files[n],'creative')
 if active.name.startswith('EnderZoology-'):
  n='fuzs/enderzoology/common/attachment/SoulboundItems.class';files[n]=patch(files[n],'soulbound')
 if active.name.startswith('penchant-'):
  n='penchant.mixins.json';o=json.loads(files[n]);o['mixins']+=['disable.RetiredEnchantmentsMixin','disable.RetiredProgressMixin'];files[n]=(json.dumps(o,indent=2)+'\n').encode()
  for p in (classes/'archives').rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
 with ZipFile(OUT/active.name,'w') as z:
  for entry in infos:
   if entry.filename in files:z.writestr(entry,files[entry.filename])
  for n in files.keys()-old.keys():z.writestr(n,files[n])
 records.append({'jar':active.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':[n for n in files if n not in old or old[n]!=files[n]],'removed':removed})
(HERE/'artifacts.json').write_text(json.dumps({'retired_enchantments':sorted(IDS),'removed_jars':removed_jars,'repacked':records},indent=2)+'\n');print('Built',len(records),'repacked candidates;',len(removed_jars),'JAR removals pending runtime verification')

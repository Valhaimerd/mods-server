"""Rebuild three same-filename candidates; originals remain under tmp/bows-balance."""
from pathlib import Path
from zipfile import ZipFile,ZipInfo,ZIP_DEFLATED
import argparse,hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/bows-balance'
p=argparse.ArgumentParser();p.add_argument('--classpath-file',default=str(WORK/'classpath.txt'));args=p.parse_args()
manifest=json.loads((HERE/'upstream.json').read_text())
manifest={name:sha for name,sha in manifest.items() if not name.startswith('Too_Many')}
for name,sha in manifest.items():assert hashlib.sha256((WORK/('original-'+name)).read_bytes()).hexdigest()==sha,name
sources=sorted((ROOT/'docs/patches/crossarms-firing/src').rglob('*.java'))
for stale in (WORK/'classes').rglob('*.class'):stale.unlink()
a=['--release','25','-encoding','UTF-8','-cp',Path(args.classpath_file).read_text().strip(),'-d',str(WORK/'classes'),*[str(x) for x in sources]]
(WORK/'compile.args').write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in a))
if sources:subprocess.run(['C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javac.exe','@'+str(WORK/'compile.args')],check=True)
removed={'too_many_bows:'+x for x in ['stormbound_signet','sharpshot_ring','dead_eyes_pendant']}
with ZipFile(WORK/'original-crossarms-1.4.jar') as z:
 shared={n:json.loads(z.read(n)) for n in ['data/minecraft/enchantment/multishot.json','data/minecraft/enchantment/piercing.json']}
for obj in shared.values():obj['exclusive_set']=[]
report={}
crossarms_overrides=ROOT/'docs/patches/crossarms-firing/overrides'
for name in manifest:
 changed=[]
 with ZipFile(WORK/('original-'+name)) as src,ZipFile(WORK/name,'w') as dst:
  for entry in src.infolist():
   n=entry.filename;old=src.read(n);data=old
   if name=='crossarms-1.4.jar' and ('/capacity/' in n or n.endswith(('/capacity.json','/capacity.mcfunction')) or n in ['data/crossarms/function/enchantment/frenzy/break.mcfunction','data/crossarms/function/enchantment/frenzy/read_damage_mainhand.mcfunction','data/crossarms/function/enchantment/frenzy/read_damage_offhand.mcfunction','data/crossarms/function/enchantment/frenzy/set_damage.mcfunction']):
    changed.append('deleted '+n);continue
   if name.startswith('Too_Many'):
    candidate=WORK/'classes'/n
    if n.endswith('.class') and candidate.is_file():data=candidate.read_bytes()
    if n=='too_many_bows.mixins.json':
     mixins=json.loads(data);mixins['mixins'].append('PackRangedDamageMixin');data=(json.dumps(mixins,indent=2)+'\n').encode()
    if n=='META-INF/neoforge.mods.toml':
     data+=b'\n[[dependencies.too_many_bows]]\nmodId="ranged_weapon_api"\ntype="required"\nversionRange="[4.0.0,)"\nordering="AFTER"\nside="BOTH"\n'
    if n.startswith('data/') and n.endswith('.json'):
     obj=json.loads(data)
     if any(x in n for x in ['/recipe/','/advancement/']) and any('"'+key+'"' in data.decode() for key in removed):changed.append('deleted '+n);continue
     if '/tags/item/' in n and 'values' in obj:
      kept=[x for x in obj['values'] if (x if isinstance(x,str) else x.get('id')) not in removed]
      if kept!=obj['values']:
       obj['values']=kept;data=(json.dumps(obj,indent=2)+'\n').encode()
   elif n in shared:data=(json.dumps(shared[n],indent=2)+'\n').encode()
   elif name.startswith('expanded-') and n=='data/minecraft/enchantment/looting.json':
    obj=json.loads(data)
    obj['effects']['minecraft:equipment_drops'][0]['requirements']['predicate']={'minecraft:entity_type':'minecraft:player'}
    data=(json.dumps(obj,indent=2)+'\n').encode()
   if name=='crossarms-1.4.jar' and (crossarms_overrides/n).is_file():data=(crossarms_overrides/n).read_bytes()
   if name=='crossarms-1.4.jar' and n.endswith('.json') and n.startswith('data/'):
    def filter_capacity(obj):
     if isinstance(obj,list):return [filter_capacity(x) for x in obj if x!='crossarms:capacity']
     if isinstance(obj,dict):return {k:filter_capacity(v) for k,v in obj.items() if k!='crossarms:capacity'}
     return obj
    obj=json.loads(data);filtered=filter_capacity(obj)
    if filtered!=obj:data=(json.dumps(filtered,indent=2)+'\n').encode()
   if name=='crossarms-1.4.jar' and n=='META-INF/neoforge.mods.toml':
    data+=b'\n[[mixins]]\nconfig="crossarms-pack.mixins.json"\n'
   if data!=old:changed.append(n)
   dst.writestr(entry,data)
  if name.startswith('Too_Many'):
   for f in sorted((WORK/'classes').rglob('*.class')):
    n=f.relative_to(WORK/'classes').as_posix()
    if n not in src.namelist():
     info=ZipInfo(n,(1980,1,1,0,0,0));info.compress_type=ZIP_DEFLATED
     dst.writestr(info,f.read_bytes());changed.append(n)
  if name=='crossarms-1.4.jar':
   config={'required':True,'minVersion':'0.8','package':'local.crossarms.mixin','compatibilityLevel':'JAVA_25','mixins':['FrenzyAccuracyMixin'],'injectors':{'defaultRequire':1}}
   additions={'crossarms-pack.mixins.json':(json.dumps(config,indent=2)+'\n').encode()}
   additions.update({f.relative_to(WORK/'classes').as_posix():f.read_bytes() for f in (WORK/'classes').rglob('*.class')})
   for n,data in additions.items():
    info=ZipInfo(n,(1980,1,1,0,0,0));info.compress_type=ZIP_DEFLATED;dst.writestr(info,data);changed.append(n)
   for f in sorted(crossarms_overrides.rglob('*')):
    if not f.is_file():continue
    n=f.relative_to(crossarms_overrides).as_posix()
    if n not in src.namelist():
     info=ZipInfo(n,(1980,1,1,0,0,0));info.compress_type=ZIP_DEFLATED
     dst.writestr(info,f.read_bytes());changed.append(n)
 report[name]={'sha256':hashlib.sha256((WORK/name).read_bytes()).hexdigest(),'changed':changed}
(WORK/'build-result.json').write_text(json.dumps(report,indent=2)+'\n');print(json.dumps(report,indent=2))

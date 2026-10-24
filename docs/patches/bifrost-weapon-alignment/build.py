"""Layer weapon identity and model changes over the current repaired Bifrost."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/bifrost-weapon-alignment'
OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('bifrost-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
source=ROOT/'docs/patches/asgardian-expansion/src/ModItems.java'
base=(ROOT/'tmp/asgardian-expansion/compile.args').read_text()
cp=before.as_posix()+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(source)]
argfile=WORK/'compile.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(before) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old)
meta=files['META-INF/neoforge.mods.toml'].decode()
assert 'modId="paladins"' not in meta
meta+='\n[[dependencies.bifrost]]\nmodId="paladins"\ntype="required"\nversionRange="[3.1.2,)"\nordering="AFTER"\nside="BOTH"\n'
files['META-INF/neoforge.mods.toml']=meta.encode()
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
model_path='assets/bifrost/models/item/stormbreaker.json';model=json.loads(old[model_path])
# Lengthen the shaft upward, moving the existing head with it; preserve all UVs.
model['elements'][0]['to'][1]+=8
for e in model['elements'][1:]:e['from'][1]+=8;e['to'][1]+=8
for hand in ['firstperson_righthand','thirdperson_righthand']:model['display'][hand]['rotation'][1]=90
for hand in ['firstperson_lefthand','thirdperson_lefthand']:model['display'][hand]['rotation'][1]=-90
for hand in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']:
    model['display'][hand]['scale']=[0.6,0.6,0.6]
    model['display'][hand]['translation']=[0,-1,0]
files[model_path]=(json.dumps(model,indent=2)+'\n').encode()
for item,kind in [('stormbreaker','hammer'),('thors_hammer','mace'),('heimdalls_sword','sword')]:
    files[f'data/bifrost/weapon_attributes/{item}.json']=(json.dumps({'parent':'bettercombat:'+kind},indent=2)+'\n').encode()
    files[f'data/rpg_series/tags/item/weapon_type/{kind}.json']=(json.dumps({'replace':False,'values':['bifrost:'+item]},indent=2)+'\n').encode()
for tag,item in [('great_hammers','stormbreaker'),('maces','thors_hammer')]:
    files[f'data/paladins/tags/item/{tag}.json']=(json.dumps({'replace':False,'values':['bifrost:'+item]},indent=2)+'\n').encode()
files['data/minecraft/tags/item/swords.json']=b'{"replace":false,"values":["bifrost:heimdalls_sword"]}\n'
with ZipFile(OUT/active.name,'w') as z:
    for entry in infos:z.writestr(entry,files[entry.filename])
    for name in sorted(set(files)-set(old)):z.writestr(name,files[name])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
record={'jar':active.name,'before_sha256':sha(before),'sha256':sha(OUT/active.name),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))}
(HERE/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n')
print('Built weapon-reference, skill-container, combat-profile and longer/reversed Stormbreaker model overlay.')

"""Apply native Bifrost skill integration over the current repaired archive."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess

HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/bifrost-skill-hotbar'
OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('bifrost-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
base=(ROOT/'tmp/asgardian-expansion/compile.args').read_text()
cp=before.as_posix()+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True)
names=['ModItems','BifrostMod','BifrostFocusItem','AsgardClient','AsgardNetwork','AsgardianAccess','AsgardAbilities','WeaponOwnership','BifrostSkills','BifrostSkillSourceMixin','BifrostHotbarMixin']
src=ROOT/'docs/patches/asgardian-expansion/src'
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(src/(n+'.java')) for n in names]]
argfile=WORK/'compile.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(before) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old)
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
mix=json.loads(files['asgardian-expansion.mixins.json']);mix['mixins'].append('BifrostSkillSourceMixin')
mix.setdefault('client',[]).append('BifrostHotbarMixin')
files['asgardian-expansion.mixins.json']=(json.dumps(mix,indent=2)+'\n').encode()
for p in (HERE/'resources').rglob('*'):
    if p.is_file():files[p.relative_to(HERE/'resources').as_posix()]=p.read_bytes()
langpath='assets/bifrost/lang/en_us.json';lang=json.loads(files[langpath])
lang.update({'spell.bifrost.travel.name':'Bifrost','spell.bifrost.travel.description':'Open your Bifrost destinations.'})
files[langpath]=(json.dumps(lang,indent=2,ensure_ascii=False)+'\n').encode()
for kind in ['claymore']:
    files[f'data/bifrost/weapon_attributes/heimdalls_sword.json']=(json.dumps({'parent':'bettercombat:'+kind},indent=2)+'\n').encode()
    files[f'data/rpg_series/tags/item/weapon_type/{kind}.json']=b'{"replace":false,"values":["bifrost:heimdalls_sword"]}\n'
files['data/paladins/tags/item/claymores.json']=b'{"replace":false,"values":["bifrost:heimdalls_sword"]}\n'
files['data/rpg_series/tags/item/weapon_type/sword.json']=b'{"replace":false,"values":[]}\n'
files['data/minecraft/tags/item/swords.json']=b'{"replace":false,"values":[]}\n'
with ZipFile(OUT/active.name,'w') as z:
    for entry in infos:z.writestr(entry,files[entry.filename])
    for name in sorted(set(files)-set(old)):z.writestr(name,files[name])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
record={'jar':active.name,'before_sha256':sha(before),'sha256':sha(OUT/active.name),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))}
record['production_inputs']={(src/(n+'.java')).relative_to(ROOT).as_posix():sha(src/(n+'.java')) for n in names}
(HERE/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n')
print('Built native Bifrost hotbar skill, disabled custom combat and Diamond Claymore Hofund candidate.')

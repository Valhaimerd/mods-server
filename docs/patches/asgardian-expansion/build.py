from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import subprocess,shutil,hashlib,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/asgardian-expansion';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
active=next((ROOT/'mods').glob('bifrost-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
with ZipFile(before) as z:original={n:z.read(n) for n in z.namelist()}
classes=WORK/'classes'
assert classes.resolve().is_relative_to(WORK.resolve())
if classes.exists():shutil.rmtree(classes)
classes.mkdir()
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8')
cp=';'.join(p.as_posix() for p in (ROOT/'mods').glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in (HERE/'src').glob('*.java')]]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8')
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
files=dict(original)
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
for p in (HERE/'resources').rglob('*'):
    if p.is_file():files[p.relative_to(HERE/'resources').as_posix()]=p.read_bytes()
# Both native destination and new ability payloads require the coordinated expansion client.
key='com/emandev/bifrost/BifrostNetworking.class'
if key not in {p.relative_to(classes).as_posix() for p in classes.rglob('*.class')}:
    # Constant-pool UTF8 entry replacement, preserving all native instructions.
    old=b'asgardian-v1';new=b'asgardian-expansion-v1';needle=len(old).to_bytes(2,'big')+old
    assert files[key].count(needle)==1
    files[key]=files[key].replace(needle,len(new).to_bytes(2,'big')+new)
meta=files['META-INF/neoforge.mods.toml'].decode()
meta+='\n[[dependencies.bifrost]]\nmodId="spell_engine"\ntype="required"\nversionRange="[1.10.5,)"\nordering="AFTER"\nside="BOTH"\n'
for mod,version in [('spell_power','1.6.1'),('better_party','1.1.7')]:
    meta+=f'\n[[dependencies.bifrost]]\nmodId="{mod}"\ntype="required"\nversionRange="[{version},)"\nordering="AFTER"\nside="BOTH"\n'
meta+='\n[[mixins]]\nconfig="asgardian-expansion.mixins.json"\n'
files['META-INF/neoforge.mods.toml']=meta.encode()
mixins=[p.stem for p in (HERE/'src').glob('*Mixin.java')]
files['asgardian-expansion.mixins.json']=(json.dumps({'required':True,'minVersion':'0.8','package':'com.emandev.bifrost.mixin','compatibilityLevel':'JAVA_25','mixins':mixins,'injectors':{'defaultRequire':1}},indent=2)+'\n').encode()
with ZipFile(OUT/active.name,'w',ZIP_DEFLATED) as z:
    for n,data in files.items():z.writestr(n,data)
record={'jar':active.name,'before_sha256':hashlib.sha256(before.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':sorted(n for n in original if original[n]!=files[n]),'added':sorted(set(files)-set(original)),'inputs':{p.relative_to(HERE).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for folder in ['src','resources'] for p in (HERE/folder).rglob('*') if p.is_file()}}
(HERE/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n',encoding='utf-8')
print('Built full Asgardian expansion candidate; active pack not changed')

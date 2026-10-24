from pathlib import Path
from zipfile import ZipFile, ZIP_DEFLATED
import hashlib, json, shutil, subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/shield-pose';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
active=next((ROOT/'mod store').glob('punchy-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
base=(ROOT/'tmp/asgardian-expansion/compile.args').read_text(encoding='utf-8');cp=before.as_posix()+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'ShieldKinds.java'),str(HERE/'PatchShield.java'),str(HERE/'ComponentShieldPoseMixin.java')]
argfile=WORK/'compile.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8');subprocess.run([str(JAVA/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(before) as z:old={n:z.read(n) for n in z.namelist()}
files=dict(old);counts={}
for name,data in old.items():
    if not name.endswith('.class') or b'net/minecraft/world/item/ShieldItem' not in data and name!='punchy/config/PunchyConfig.class':continue
    # javap's explicit instruction count protects this patch against upstream drift.
    text=subprocess.check_output([str(JAVA/'javap.exe'),'-p','-c','-cp',str(before),name[:-6].replace('/','.')],text=True)
    count=sum('instanceof' in line and 'net/minecraft/world/item/ShieldItem' in line for line in text.splitlines())
    if name=='punchy/config/PunchyConfig.class':count+=1
    assert count>0,name
    source=WORK/'original.class';target=WORK/'patched.class';source.write_bytes(data)
    runargs=['-cp',str(classes)+';'+cp,'PatchShield',str(source),str(target),str(count)]
    runfile=WORK/'patch.args';runfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in runargs),encoding='utf-8');subprocess.run([str(JAVA/'java.exe'),'@'+str(runfile)],check=True)
    files[name]=target.read_bytes();counts[name]=count
assert len(counts)==8,counts
files['punchy/compat/ShieldKinds.class']=(classes/'punchy/compat/ShieldKinds.class').read_bytes()
files['punchy/mixin/client/ComponentShieldPoseMixin.class']=(classes/'punchy/mixin/client/ComponentShieldPoseMixin.class').read_bytes()
mixins=json.loads(files['punchy.mixins.json']);mixins['client'].append('client.ComponentShieldPoseMixin');files['punchy.mixins.json']=(json.dumps(mixins,indent=2)+'\n').encode()
candidate=OUT/active.name
with ZipFile(candidate,'w',ZIP_DEFLATED) as z:
    for n,data in files.items():z.writestr(n,data)
record={'jar':active.name,'before_sha256':hashlib.sha256(before.read_bytes()).hexdigest(),'sha256':hashlib.sha256(candidate.read_bytes()).hexdigest(),'checks':counts,'added':['punchy/compat/ShieldKinds.class','punchy/mixin/client/ComponentShieldPoseMixin.class'],'inputs':{p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in [HERE/'ShieldKinds.java',HERE/'PatchShield.java',HERE/'ComponentShieldPoseMixin.java']}}
(HERE/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n',encoding='utf-8');print('Built shield-component detection repair:',counts)

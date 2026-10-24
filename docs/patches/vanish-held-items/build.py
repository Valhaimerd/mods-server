from pathlib import Path
from zipfile import ZipFile
import hashlib, json, shutil, subprocess

HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
WORK=ROOT/'tmp/vanish-render'
OUT=WORK/'built'
OUT.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('rogues-*.jar'))
before=WORK/('before-'+active.name)
if not before.exists(): shutil.copy2(active,before)
base=(ROOT/'tmp/asgardian-expansion/compile.args').read_text(encoding='utf-8')
cp=before.as_posix()+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes'
classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),
      str(HERE/'StealthHandsMixin.java'),str(HERE/'StealthHeldStateMixin.java')]
argfile=WORK/'compile.args'
argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args),encoding='utf-8')
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(before) as src, ZipFile(OUT/active.name,'w') as dst:
    mixins=json.loads(src.read('rogues.mixins.json'))
    assert not mixins['client'], 'Unexpected existing client mixins: review baseline'
    mixins['client']=['client.StealthHandsMixin','client.StealthHeldStateMixin']
    for entry in src.infolist():
        data=(json.dumps(mixins,indent=2)+'\n').encode() if entry.filename=='rogues.mixins.json' else src.read(entry)
        dst.writestr(entry,data)
    added=[]
    for p in sorted(classes.rglob('*.class')):
        name=p.relative_to(classes).as_posix();dst.writestr(name,p.read_bytes());added.append(name)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
record={'jar':active.name,'before_sha256':sha(before),'sha256':sha(OUT/active.name),
        'changed':['rogues.mixins.json'],'added':added}
(HERE/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n')
print('Built client-only Vanish hand rendering hooks; production gameplay and language retained.')

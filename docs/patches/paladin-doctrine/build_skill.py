"""Build from this task's current-pack snapshot, preserving prior skill fixes."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,subprocess

ROOT=Path(__file__).resolve().parents[3]
HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/paladin-doctrine'
OUT=WORK/'built';OUT.mkdir(exist_ok=True)
source=next((WORK/'before').glob('skill_tree-*.jar'))
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm'
asm=next((libs/'asm').rglob('asm-*.jar'))
tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'))
classes=WORK/'patch-classes';classes.mkdir(exist_ok=True)
subprocess.run([str(java/'javac.exe'),'--release','25','-cp',str(asm)+';'+str(tree),'-d',str(classes),str(HERE/'PatchNodes.java')],check=True)
lines=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines()
cp=';'.join(str(p) for p in (ROOT/'mods').glob('*.jar'))+';'+lines[lines.index('"-cp"')+1].strip('"')
doctrine_classes=WORK/'doctrine-classes';doctrine_classes.mkdir(exist_ok=True)
sources=[HERE/(name+'.java') for name in ['PaladinDoctrine','PaladinEquipmentAccess','PaladinEquipmentMixin','PaladinPlayerMixin','PaladinCastingMixin']]
args=['--release','25','-proc:none','-cp',cp,'-d',str(doctrine_classes),*map(str,sources)]
argfile=WORK/'doctrine-compile.args';argfile.write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args))
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
with ZipFile(source) as z:
    infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old)
name='net/skill_tree_rpgs/skills/NodeTypes.class'
(WORK/'NodeTypes-input.class').write_bytes(files[name])
subprocess.run([str(java/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchNodes',str(WORK/'NodeTypes-input.class'),str(WORK/'NodeTypes-output.class')],check=True)
files[name]=(WORK/'NodeTypes-output.class').read_bytes()
nodes=0
for name in files:
    if not name.endswith('/definitions.json') or '/puffish_skills/categories/' not in name:continue
    data=json.loads(files[name]);modified=False
    for key in ['paladin_root','paladin_boost']:
        if key not in data:continue
        for reward in data[key].get('rewards',[]):
            d=reward.get('data',{})
            if d.get('attribute')=='spell_power:healing' and d.get('operation')=='addition':
                assert d['value']==.2,(name,key,d)
                d['value']=.25;modified=True;nodes+=1
        if key=='paladin_root':
            data[key]['description']={'text':'+0.25 Healing Power\nHealing staves and wands are forbidden.'}
    if modified:files[name]=(json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode()
assert nodes==4,nodes
name='skill_tree_rpgs.mixins.json';mixins=json.loads(files[name])
mixins['mixins']+=['PaladinEquipmentAccess','PaladinEquipmentMixin','PaladinPlayerMixin','PaladinCastingMixin']
files[name]=(json.dumps(mixins,indent=2)+'\n').encode()
name='assets/skill_tree_rpgs/lang/en_us.json';lang=json.loads(files[name])
lang['skill_tree_rpgs.paladin_oath']='Your Paladin oath forbids healing staves and wands.'
lang['skill_tree_rpgs.paladin_oath.full']='Your Paladin oath forbids healing staves and wands. Free an inventory slot to recover your weapon.'
files[name]=(json.dumps(lang,ensure_ascii=False,indent=2)+'\n').encode()
for p in doctrine_classes.rglob('*.class'):files[p.relative_to(doctrine_classes).as_posix()]=p.read_bytes()
with ZipFile(OUT/source.name,'w') as z:
    for info in infos:z.writestr(info,files[info.filename])
    for name in sorted(files.keys()-old.keys()):z.writestr(name,files[name])
record={'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys())}
(HERE/'skill-artifacts.json').write_text(json.dumps(record,indent=2)+'\n')
print(json.dumps(record,indent=2))

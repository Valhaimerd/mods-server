from pathlib import Path
from zipfile import ZipFile
import subprocess,json,hashlib,copy
from data import apply
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-followup-phase-one';C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True)
J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(map(str,[*(W/'before').glob('*.jar'),asm,tree]))+';'+(W/'classpath.txt').read_text();(W/'build-classpath.txt').write_text(cp)
def run(tool,args,label):
 a=W/(label+'.args');a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));subprocess.run([str(J/tool),'@'+str(a)],check=True)
run('javac.exe',['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),*[str(H/n) for n in ['ClassRework.java','AttackSprintMixin.java','AttackScopeMixin.java','PatchClasses.java']]],'compile')
def patch(f,key,mode):
 a=W/'in.class';b=W/'out.class';a.write_bytes(f[key]);run('java.exe',['-cp',str(C)+';'+cp,'PatchClasses',str(a),str(b),mode],'patch');f[key]=b.read_bytes()
records=[]
for src in (W/'before').glob('*.jar'):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old);apply(src.name,f)
 if src.name.startswith('spell_engine-'):
  for key,mode in [('entity/WarriorBalance','balance'),('entity/WarriorRules','rules'),('entity/WarriorTierTwo','tierTwo'),('entity/WarriorUltimates','ultimate'),('internals/FollowUpRules','follow'),('internals/SpellExecution','execution'),('internals/delivery/melee/Melee','melee')]:patch(f,'net/spell_engine/'+key+'.class',mode)
  for prefix in ['net/spell_engine/entity/ClassRework','net/spell_engine/mixin/warrior/AttackSprintMixin','net/spell_engine/mixin/warrior/AttackScopeMixin']:
   for p in C.glob(prefix+'*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  found=0
  for key in list(f):
   if key.endswith('.json') and 'mixin' in key:
    d=json.loads(f[key])
    if d.get('package')=='net.spell_engine.mixin' and 'mixins' in d:d['mixins']+=['warrior.AttackSprintMixin','warrior.AttackScopeMixin'];f[key]=(json.dumps(d,indent=2)+'\n').encode();found+=1
  assert found==1
 if src.name.startswith('bettercombat-'):patch(f,'net/bettercombat/mixin/client/ClientPlayerEntityMixin.class','movement')
 if src.name.startswith('skill_tree-'):patch(f,'net/skill_tree_rpgs/effect/SkillEffects.class','finalSkill')
 if src.name.startswith('rogues-'):patch(f,'net/rogues/effect/RogueEffects.class','finalRogue')
 if src.name.startswith('combat_roll-'):
  patch(f,'net/combat_roll/internals/RollManager.class','rollClient');patch(f,'net/combat_roll/network/ServerNetwork.class','rollServer')
 with ZipFile(O/src.name,'w') as z:
  for i in infos:z.writestr(copy.copy(i),f[i.filename])
  for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256((O/src.name).read_bytes()).hexdigest(),'changed':[n for n in old if f[n]!=old[n]],'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built',len(records),'current-input candidates; production unchanged')

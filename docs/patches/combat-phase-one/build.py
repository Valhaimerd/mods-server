from pathlib import Path
from zipfile import ZipFile
import subprocess,json,hashlib,copy
from data import apply
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-phase-one';C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True)
J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(map(str,[*(W/'before').glob('*.jar'),*(R/'mods').glob('*.jar'),asm,tree]))+';'+(R/'tmp/utilities/classpath.txt').read_text().strip()
(W/'classpath.txt').write_text(cp)
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),*[str(p) for p in H.glob('*.java') if not p.name.endswith('Probe.java')]]
(W/'compile.args').write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args))
subprocess.run([str(J/'javac.exe'),'@'+str(W/'compile.args')],check=True)
def patch(f,key,mode):
 src=W/'in.class';out=W/'out.class';src.write_bytes(f[key])
 a=W/'patch.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in ['-cp',str(C)+';'+cp,'PatchPhaseOne',str(src),str(out),mode]))
 subprocess.run([str(J/'java.exe'),'@'+str(a)],check=True);f[key]=out.read_bytes()
records=[]
for src in (W/'before').glob('*.jar'):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 f=dict(old);apply(src.name,f)
 if src.name.startswith('spell_engine-'):
  for key,mode in [('net/spell_engine/internals/RevisionRules.class','revision'),('net/spell_engine/internals/SummonRules.class','summon'),('net/spell_engine/internals/cost/SpellCooldowns.class','cost'),('net/spell_engine/internals/cost/SpellCooldownManager.class','manager'),('net/spell_engine/client/gui/HudRenderHelper$SpellHotBarWidget.class','hud'),('net/spell_engine/internals/PaladinRevamp.class','sinful')]:patch(f,key,mode)
  for prefix in ['net/spell_engine/internals/cost/EchoCharges','net/spell_engine/client/gui/EchoHud','net/spell_engine/internals/SinfulDamage']:
   for p in C.glob(prefix+'*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
 if src.name.startswith('skill_tree-'):
  key='net/skill_tree_rpgs/effect/WizardAbsorbEffect.class';f[key]=(C/key).read_bytes()
  patch(f,'net/skill_tree_rpgs/effect/SkillEffects.class','effects')
  for p in C.glob('net/skill_tree_rpgs/effect/WizardAbsorbEffect$*.class'):f[p.relative_to(C).as_posix()]=p.read_bytes()
  f['assets/skill_tree_rpgs/textures/mob_effect/sinful_suffering.png']=f['assets/skill_tree_rpgs/textures/mob_effect/sinful_retaliation_ready.png']
  key='assets/skill_tree_rpgs/lang/en_us.json';d=json.loads(f[key]);d['effect.skill_tree_rpgs.sinful_suffering']='Sinful Suffering';d['effect.skill_tree_rpgs.sinful_suffering.description']='Lose 1 HP every 0.5s. Absorption is consumed before health.';f[key]=(json.dumps(d,indent=2)+'\n').encode()
 if src.name.startswith('crossarms-'):
  key='local/crossarms/mixin/FrenzyIframesMixin.class';f[key]=(C/key).read_bytes()
  configs=[n for n in f if n.endswith('.json') and 'mixin' in n]
  found=0
  for key in configs:
   d=json.loads(f[key])
   if d.get('package')=='local.crossarms.mixin':d['mixins'].append('FrenzyIframesMixin');f[key]=(json.dumps(d,indent=2)+'\n').encode();found+=1
  assert found==1,configs
 target=O/src.name
 with ZipFile(target,'w') as z:
  for i in infos:z.writestr(copy.copy(i),f[i.filename])
  for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
 with ZipFile(target) as z:assert z.testzip() is None
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256(target.read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=f[n]],'added':sorted(f.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Built',len(records),'complete candidate archives; production unchanged.')

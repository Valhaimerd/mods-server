from pathlib import Path
from zipfile import ZipFile
import subprocess,json,hashlib,copy
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/purgatory-continuous-pull';C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True);J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(str(p) for p in (W/'before').glob('*.jar'))+';'+';'.join(str(p) for p in (R/'mods').glob('*.jar'))+';'+str(asm)+';'+str(tree)+';'+(R/'tmp/utilities/classpath.txt').read_text();(W/'classpath.txt').write_text(cp)
def run(binary,args,name):
 a=W/(name+'.args');a.write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in args));subprocess.run([str(J/binary),'@'+str(a)],check=True)
run('javac.exe',['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),*[str(p) for p in H.glob('*.java') if not p.name.endswith('Probe.java')]],'compile')
records=[]
for src in (W/'before').glob('*.jar'):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 data=dict(old)
 def classes(prefix):
  for p in C.glob(prefix+'*.class'):data[p.relative_to(C).as_posix()]=p.read_bytes()
 if src.name.startswith('spell_engine-'):
  key='net/spell_engine/internals/PaladinUltimates.class';f=W/'input.class';f.write_bytes(data[key]);dest=W/'output.class';run('java.exe',['-cp',str(C)+';'+cp,'PatchPull',str(f),str(dest)],'patch');data[key]=dest.read_bytes();classes('net/spell_engine/internals/PurgatoryPull');classes('net/spell_engine/internals/PurgatoryBoundary')
 if src.name.startswith('paladins-'):
  classes('net/paladins/client/entity/BoundaryRender')
  key='assets/paladins/lang/en_us.json';d=json.loads(data[key]);d['spell.paladins.immolation.description']='Channel holy fire within an orange enclosure, drawing enemies inward with increasing force. From 10s, control cannot interrupt you. Later pulses consume absorption, then health; the final phase builds a powerful explosion.';data[key]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 if src.name.startswith('skill_tree-'):
  for tail in ('root','modifier_1','modifier_2'):
   key=f'data/skill_tree_rpgs/spell/paladin_tier_4_spell_2_{tail}.json';d=json.loads(data[key]);d.setdefault('tooltip',{})['show_range']=False;data[key]=(json.dumps(d,indent=2)+'\n').encode()
  key='assets/skill_tree_rpgs/lang/en_us.json';d=json.loads(data[key]);k='spell.skill_tree_rpgs.paladin_tier_3_passive_1.description';assert '20%%' in d[k];d[k]=d[k].replace('20%%','30%%');d['spell.skill_tree_rpgs.paladin_tier_4_spell_2_root.description']='Purgatory burns within 7 blocks; its pull, enclosure and finishing effects reach 14 blocks.';data[key]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 out=O/src.name
 with ZipFile(out,'w') as z:
  for i in infos:z.writestr(copy.copy(i),data[i.filename])
  for key in sorted(data.keys()-old.keys()):z.writestr(key,data[key])
 with ZipFile(out) as z:assert z.testzip() is None
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256(out.read_bytes()).hexdigest(),'changed':[n for n in old if data[n]!=old[n]],'added':sorted(data.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built three isolated candidates; production unchanged')

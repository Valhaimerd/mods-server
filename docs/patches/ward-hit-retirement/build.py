from pathlib import Path
from zipfile import ZipFile
import json,subprocess,copy,hashlib,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/ward-hit-retirement';B=W/'before';B.mkdir(exist_ok=True);C=W/'classes';C.mkdir(exist_ok=True);O=W/'built';O.mkdir(exist_ok=True);J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
patterns=('skill_tree-neoforge-*','spell_engine-*','penchant-*')
for pattern in patterns:
 p=next((R/'mods').glob(pattern));q=B/p.name
 if not q.exists():shutil.copy2(p,q)
for name in ('RetiredEnchantmentsMixin.java','RetiredProgressMixin.java'):
 s=(R/'docs/patches/fall-and-toughness'/name).read_text();assert 'toughness_enchantment:toughness' in s;s=s.replace('Set.of(', 'Set.of("fangs_enchantment:fangs",',1);(H/name).write_text(s,encoding='utf-8')
cp=';'.join(str(p) for p in (R/'mods').glob('*.jar'))+';'+(R/'tmp/purgatory-continuous-pull/classpath.txt').read_text();(W/'classpath.txt').write_text(cp)
def run(tool,args,name):
 f=W/(name+'.args');f.write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in args));subprocess.run([str(J/tool),'@'+str(f)],check=True)
run('javac.exe',['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),*[str(p) for p in H.glob('*.java') if not p.name.endswith('Probe.java')]],'compile')
records=[]
for src in B.glob('*.jar'):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 data=dict(old)
 if src.name.startswith('skill_tree-'):
  for school in ('arcane','fire','frost'):
   key=f'data/skill_tree_rpgs/spell/{school}_tier_3_passive_2.json';d=json.loads(data[key]);assert d['cost']['cooldown']['duration']==15;d['cost']['cooldown']['duration']=12;d['passive']['triggers']=[{'type':'DAMAGE_TAKEN','stage':'POST','target_override':'CASTER','chance':1}];data[key]=(json.dumps(d,indent=2)+'\n').encode()
  key='assets/skill_tree_rpgs/lang/en_us.json';d=json.loads(data[key])
  for school in ('arcane','fire','frost'):
   k=f'spell.skill_tree_rpgs.{school}_tier_3_passive_2.description';d[k]='Enemy hits grant a 6s shield with 1 absorption heart per 6 '+school.title()+' power.'+(' Attackers take 30%% Fire damage.' if school=='fire' else ' Slows attackers.' if school=='frost' else '')
  data[key]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 if src.name.startswith('spell_engine-'):
  key='net/spell_engine/internals/PaladinRevamp.class';a=W/'in.class';b=W/'out.class';a.write_bytes(data[key]);run('java.exe',['-cp',str(C)+';'+cp,'PatchWard',str(a),str(b)],'patch');data[key]=b.read_bytes();key='net/spell_engine/internals/WardHitRules.class';data[key]=(C/key).read_bytes()
 if src.name.startswith('penchant-'):
  for p in (C/'archives').rglob('*.class'):data[p.relative_to(C).as_posix()]=p.read_bytes()
  key='data/minecraft/tags/enchantment/exclusive_set/thorns.json';d=json.loads(data[key]);old_values=d['values'];d['values']=[v for v in old_values if (v.get('id') if isinstance(v,dict) else v)!='fangs_enchantment:fangs'];assert len(d['values'])==len(old_values)-1;data[key]=(json.dumps(d,indent=2)+'\n').encode()
 out=O/src.name
 with ZipFile(out,'w') as z:
  for i in infos:z.writestr(copy.copy(i),data[i.filename])
  for key in sorted(data.keys()-old.keys()):z.writestr(key,data[key])
 with ZipFile(out) as z:assert z.testzip() is None
 records.append({'jar':src.name,'before':hashlib.sha256(src.read_bytes()).hexdigest(),'after':hashlib.sha256(out.read_bytes()).hexdigest(),'changed':[n for n in old if data[n]!=old[n]],'added':sorted(data.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built three narrow candidates; two provider removals pending.')

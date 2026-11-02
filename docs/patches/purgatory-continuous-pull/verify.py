from pathlib import Path
from zipfile import ZipFile
import json,subprocess,hashlib
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/purgatory-continuous-pull';J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');C=W/'probe-classes';C.mkdir(exist_ok=True)
records=json.loads((H/'artifacts.json').read_text());count=0
for row in records:
 with ZipFile(W/'before'/row['jar']) as a,ZipFile(W/'built'/row['jar']) as b:
  assert b.testzip() is None
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(row['changed']);count+=2
  assert not any(n.startswith(('local/','PullMathProbe')) for n in b.namelist());count+=1
  if row['jar'].startswith('skill_tree-'):
   for tail in ('root','modifier_1','modifier_2'):
    d=json.loads(b.read(f'data/skill_tree_rpgs/spell/paladin_tier_4_spell_2_{tail}.json'));assert not d['tooltip']['show_range'];count+=1
   lang=json.loads(b.read('assets/skill_tree_rpgs/lang/en_us.json'));assert '30%%' in lang['spell.skill_tree_rpgs.paladin_tier_3_passive_1.description'];count+=1
   for school in ('arcane','fire','frost'):
    key=f'data/skill_tree_rpgs/spell/{school}_tier_3_passive_2.json';assert a.read(key)==b.read(key);count+=1
cp=';'.join(str(p) for p in (W/'built').glob('*.jar'))+';'+(W/'classpath.txt').read_text()
def run(tool,args,name):
 f=W/(name+'.args');f.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in args));return subprocess.run([str(J/tool),'@'+str(f)],check=True,capture_output=True,text=True).stdout
run('javac.exe',['--release','25','-proc:none','-cp',cp,'-d',str(C),str(H/'PullMathProbe.java')],'math-compile')
result=run('java.exe',['-Xverify:all','-cp',str(C)+';'+cp,'PullMathProbe'],'math-run');print(result.strip());print('PASS',count,'archive/data checks');(H/'focused-results.txt').write_text(result+f'PASS {count} archive/data checks\n')

from pathlib import Path
from zipfile import ZipFile
import json,subprocess,hashlib
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/paladin-passives';C=W/'classes';C.mkdir(exist_ok=True);OUT=W/'built';OUT.mkdir(exist_ok=True)
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin');lines=(R/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines();cp=';'.join(str(p) for p in (R/'mods').glob('*.jar'))+';'+lines[lines.index('"-cp"')+1].strip('"')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'));cp+=';'+str(asm)+';'+str(tree)
args=['--release','25','-proc:none','-cp',cp,'-d',str(C),str(H/'PaladinPassives.java'),str(H/'PatchPassives.java')];(W/'compile.args').write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args));subprocess.run([str(J/'javac.exe'),'@'+str(W/'compile.args')],check=True)
source=next((W/'before').glob('*.jar'))
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
f=dict(old)
def edit(n,fn):
 d=json.loads(f[n]);fn(d);f[n]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
for i in [1,2]:
 def passive(d):
  if i==2:d['passive']['triggers']=[{'type':'MELEE_IMPACT','chance':1},{'type':'ARROW_IMPACT','chance':1},{'type':'SPELL_IMPACT_SPECIFIC','chance':1,'impact':{'impact_type':'DAMAGE'}}]
  else:d['passive']['triggers'][0]['chance']=1
  d['impacts'][0]['action']['status_effect'].update(duration=5,amplifier=1,amplifier_cap=3,apply_mode='ADD')
  d['cost']['cooldown']={'duration':1 if i==1 else 0,'haste_affected':True}
 edit(f'data/skill_tree_rpgs/spell/paladin_tier_1_passive_{i}.json',passive)
words={1:('Vanguard','Gain 5%% base armor while a shield is equipped. Blocking grants another 5%% per stack, up to 4 stacks for 5s.'),2:('Guardian','Gain 5%% Healing Power while your offhand is empty. Successful hits grant another 5%% per stack, up to 4 stacks for 5s.')}
def lang(d):
 for i,(name,desc) in words.items():
  key=f'paladin_tier_1_passive_{i}'
  d['spell.skill_tree_rpgs.'+key+'.name']=name;d['spell.skill_tree_rpgs.'+key+'.description']=desc;d['skill.skill_tree_rpgs.'+key+'.title']=name
  effect='redoubt' if i==1 else 'vengeance';d['effect.skill_tree_rpgs.'+effect]=name;d['effect.skill_tree_rpgs.'+effect+'.description']='Increased '+('armor.' if i==1 else 'Healing Power.')
edit('assets/skill_tree_rpgs/lang/en_us.json',lang)
for category in ['class_skills','weapon_skills']:
 def defs(d):
  for i in [1,2]:
   key=f'paladin_tier_1_passive_{i}'
   if key in d:d[key]['title']={'translate':'skill.skill_tree_rpgs.'+key+'.title'}
 edit(f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json',defs)
n='net/skill_tree_rpgs/effect/SkillEffects.class';(W/'in.class').write_bytes(f[n]);subprocess.run([str(J/'java.exe'),'-cp',';'.join(map(str,[C,asm,tree])),'PatchPassives',str(W/'in.class'),str(W/'out.class')],check=True);f[n]=(W/'out.class').read_bytes()
f['net/skill_tree_rpgs/compat/PaladinPassives.class']=(C/'net/skill_tree_rpgs/compat/PaladinPassives.class').read_bytes()
with ZipFile(OUT/source.name,'w') as z:
 for info in infos:z.writestr(info,f[info.filename])
 for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
r={'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=f[n]],'added':sorted(f.keys()-old.keys())};(H/'artifacts.json').write_text(json.dumps(r,indent=2)+'\n');print('Built passive candidate')

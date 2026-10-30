from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/orb-acquisition';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True);name='skill_tree-neoforge-1.6.1+26.2.jar';before=WORK/('before-'+name)
if not before.exists():shutil.copy2(ROOT/'mods'/name,before)
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=str(before)+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(HERE/'src/OrbLevelCostMixin.java')];a=WORK/'compile.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(before) as z:infos=z.infolist();files={i.filename:z.read(i) for i in infos}
old=dict(files)
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
key='skill_tree_rpgs.mixins.json';cfg=json.loads(files[key]);cfg['mixins'].append('OrbLevelCostMixin');files[key]=(json.dumps(cfg,indent=2)+'\n').encode()
key='data/skill_tree_rpgs/recipe/orb_of_oblivion.json';files[key]=(json.dumps({'type':'minecraft:crafting_shaped','category':'equipment','key':{'A':'minecraft:enchanted_golden_apple','T':'minecraft:totem_of_undying','N':'minecraft:netherite_ingot'},'pattern':['ATA','TNT','ATA'],'result':{'id':'skill_tree_rpgs:orb_of_oblivion'}},indent=2)+'\n').encode()
key='data/skill_tree_rpgs/advancement/recipes/combat/orb_of_oblivion.json';adv=json.loads(files[key]);del adv['criteria']['has_experience_bottle'];adv['criteria']['has_netherite_ingot']={'trigger':'minecraft:inventory_changed','conditions':{'items':[{'items':'minecraft:netherite_ingot'}]}};adv['requirements']=[['has_the_recipe','has_netherite_ingot']];files[key]=(json.dumps(adv,indent=2)+'\n').encode()
key='assets/skill_tree_rpgs/lang/en_us.json';lang=json.loads(files[key]);lang['item.skill_tree_rpgs.orb_of_oblivion.lore.0']='Reset your skills and race. Successful use costs 30 experience levels.';files[key]=(json.dumps(lang,ensure_ascii=False,indent=2)+'\n').encode()
with ZipFile(OUT/name,'w') as z:
 for info in infos:z.writestr(info,files[info.filename])
 for n in sorted(set(files)-set(old)):z.writestr(n,files[n])
with ZipFile(OUT/name) as z:assert z.testzip() is None;assert all(z.read(n)==v for n,v in old.items() if files[n]==v)
report={'jar':name,'before_sha256':sha(before),'sha256':sha(OUT/name),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(set(files)-set(old)),'preserved_entries':sum(files[n]==old[n] for n in old)}
(HERE/'artifacts.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report,indent=2))

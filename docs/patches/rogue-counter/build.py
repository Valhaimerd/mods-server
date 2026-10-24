from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/rogue-counter';before=WORK/'before';before.mkdir(parents=True,exist_ok=True);out=WORK/'built';out.mkdir(exist_ok=True)
for pattern in ['rogues-*.jar','skill_tree-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pattern))
 if not (before/active.name).exists():shutil.copy2(active,before/active.name)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=';'.join(p.as_posix() for p in before.glob('*.jar'))+';'+';'.join(p.as_posix() for p in (ROOT/'mods').glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);sources=[HERE/'PreparedCounter.java',HERE/'CounterMixin.java'];args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in sources]];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for saved in sorted(before.glob('*.jar')):
 with ZipFile(saved) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 if saved.name.startswith('rogues'):
  n='data/rogues/spell/mutilate.json';o=json.loads(files[n]);assert o['deliver']['melee']['attacks'][0]['damage_bonus']==1;o['deliver']['melee']['attacks'][0]['damage_bonus']=0;files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='assets/rogues/lang/en_us.json';o=json.loads(files[n]);o['spell.rogues.mutilate.description']='Strike enemies in front of you for 100%% combined dual-wield damage.';files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 if saved.name.startswith('skill_tree'):
  n='data/skill_tree_rpgs/spell/rogue_tier_3_passive_2.json';o=json.loads(files[n]);o['school']='spell_power:physical_melee';o['deliver']={'type':'DIRECT'};o['cost']['cooldown']['duration']=2;o['impacts'][0]['action']={'type':'STATUS_EFFECT','apply_to_caster':True,'status_effect':{'effect_id':'skill_tree_rpgs:prepared_counter_ready','duration':-.05}};files[n]=(json.dumps(o,indent=2)+'\n').encode()
  n='assets/skill_tree_rpgs/lang/en_us.json';o=json.loads(files[n]);o['spell.skill_tree_rpgs.rogue_tier_3_passive_2.description']='After evading an enemy attack, your next basic melee hit adds 25%% of melee power. One charge; melee skills cannot consume it.';o['effect.skill_tree_rpgs.prepared_counter_ready']='Prepared Counter';files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
  files['assets/skill_tree_rpgs/textures/mob_effect/prepared_counter_ready.png']=files['assets/skill_tree_rpgs/textures/spell/rogue_tier_3_passive_2.png']
 if saved.name.startswith('spell_engine'):
  mix=json.loads(files['spell_engine.mixins.json']);mix['mixins'].append('roguecounter.CounterMixin');files['spell_engine.mixins.json']=(json.dumps(mix,indent=2)+'\n').encode()
  for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
 with ZipFile(out/saved.name,'w') as z:
  for i in infos:z.writestr(i,files[i.filename])
  for n in sorted(set(files)-set(old)):z.writestr(n,files[n])
 records.append({'jar':saved.name,'before_sha256':hashlib.sha256(saved.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/saved.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Mutilate100% / one-hit Prepared Counter candidates')

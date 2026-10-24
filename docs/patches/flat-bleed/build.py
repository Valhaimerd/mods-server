from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/flat-bleed';before=WORK/'before';before.mkdir(parents=True,exist_ok=True);out=WORK/'built';out.mkdir(exist_ok=True)
for pattern in ['skill_tree-*.jar','spell_engine-*.jar']:
 active=next((ROOT/'mods').glob(pattern))
 if not (before/active.name).exists():shutil.copy2(active,before/active.name)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=';'.join(p.as_posix() for p in before.glob('*.jar'))+';'+';'.join(p.as_posix() for p in (ROOT/'mods').glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);sources=[HERE/'BleedStatusEffect.java',HERE/'SingleBleedMixin.java'];args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in sources]];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
def flatten(o):
 if isinstance(o,list):
  for item in o:flatten(item)
 if isinstance(o,dict):
  if o.get('effect_id')=='spell_engine:bleed':
   o['amplifier']=0
   for key in ['amplifier_power_multiplier','amplifier_cap','amplifier_cap_power_multiplier']:o.pop(key,None)
   if o.get('apply_mode')=='ADD':o['apply_mode']='SET'
  for value in list(o.values()):flatten(value)
records=[]
for saved in sorted(before.glob('*.jar')):
 with ZipFile(saved) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 for n,data in old.items():
  if n.startswith('data/') and '/spell/' in n and n.endswith('.json') and b'spell_engine:bleed' in data:
   o=json.loads(data);flatten(o);files[n]=(json.dumps(o,indent=2)+'\n').encode()
 if saved.name.startswith('skill_tree'):
  n='assets/skill_tree_rpgs/lang/en_us.json';o=json.loads(files[n])
  for key,duration in [('rogue_tier_3_spell_2_modifier_1','2.5'),('rogue_tier_4_spell_2_modifier_2','7.5')]:o['spell.skill_tree_rpgs.'+key+'.description']='Apply Bleed for '+duration+' seconds. Every 1.25 seconds it deals 2 HP while stationary or 6 HP while moving. Bleed does not scale or stack levels.'
  files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 if saved.name.startswith('spell_engine'):
  mix=json.loads(files['spell_engine.mixins.json']);mix['mixins'].append('flatbleed.SingleBleedMixin');files['spell_engine.mixins.json']=(json.dumps(mix,indent=2)+'\n').encode()
  for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
 with ZipFile(out/saved.name,'w') as z:
  for i in infos:z.writestr(i,files[i.filename])
  for n in sorted(set(files)-set(old)):z.writestr(n,files[n])
 records.append({'jar':saved.name,'before_sha256':hashlib.sha256(saved.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/saved.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built flat single-level Bleed candidates')

"""Curate skill text and synchronize tree/scroll names; preserve all combat resources."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,re,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/skill-tooltip-polish';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
wording=json.loads((HERE/'wording.json').read_text());before=WORK/'before';before.mkdir(exist_ok=True)
patterns=['archers-*.jar','wizards-*.jar','rogues-*.jar','skill_tree-*.jar','spell_engine-*.jar']
for pattern in patterns:
 source=next((ROOT/'mods').glob(pattern));saved=before/source.name
 if not saved.exists():shutil.copy2(source,saved)
all_lang={}
for jar in before.glob('*.jar'):
 with ZipFile(jar) as z:
  for n in z.namelist():
   if n.endswith('/lang/en_us.json'):all_lang.update(json.loads(z.read(n)))
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=';'.join(p.as_posix() for p in before.glob('*.jar'))+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'SkillTooltipMixin.java')];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
prefixes=('spell.archers.','spell.wizards.','spell.rogues.','spell.skill_tree_rpgs.archer_','spell.skill_tree_rpgs.rogue_','spell.skill_tree_rpgs.warrior_','spell.skill_tree_rpgs.arcane_','spell.skill_tree_rpgs.fire_','spell.skill_tree_rpgs.frost_')
records=[]
for jar in sorted(before.glob('*.jar')):
 with ZipFile(jar) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 for n,data in old.items():
  if not n.endswith('/lang/en_us.json'):continue
  lang=json.loads(data)
  for k,v in list(lang.items()):
   if not k.endswith('.description') or not k.startswith(prefixes):continue
   if k in wording['descriptions']:v=wording['descriptions'][k]
   else:
    v=re.sub(r'\s*(?:Base cooldown|Base interval|Cooldown):?\s*\d+(?:\.\d+)?\s*(?:seconds?|sec|s)\b(?:,?\s*(?:reduced by haste|affected by haste))?\.?','',v,flags=re.I)
    v=re.sub(r'\s*(?:No additional cooldown\.|No cooldown\.|Guaranteed; no additional cooldown\.)','',v,flags=re.I)
    v=re.sub(r'(\d+) percentage points(?: of)?',r'\1%%',v)
    v=v.replace('20 points','20%%').replace('50 points','50%%').replace('30 points','30%%')
    v=v.replace(' (reduced by haste)','').replace(' before haste','')
    v=re.sub(r'(\d(?:\.\d+)?) seconds?\b',r'\1s',v);v=re.sub(r'(\d(?:\.\d+)?) sec\b',r'\1s',v)
   lang[k]=v.strip()
  if n=='assets/skill_tree_rpgs/lang/en_us.json':
   for category in ['class_skills','weapon_skills']:
    path=f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json'
    for id,definition in json.loads(old[path]).items():
     title=definition.get('title',{}).get('translate');spellname='spell.skill_tree_rpgs.'+id+'.name'
     if title in lang and spellname in lang:lang[title]=lang[spellname]
   # Base skill titles are separate from the modifier definitions.
   for (family,tier,index),id in {
    ('warrior',2,1):'rogues:throw',('warrior',2,2):'rogues:throw_net',('warrior',3,1):'rogues:charge',('warrior',3,2):'rogues:shout',('warrior',4,1):'rogues:mortal_strike',('warrior',4,2):'rogues:last_stand',('archer',4,2):'archers:magic_arrow',('arcane',4,1):'wizards:arcane_blink'
   }.items():
    key=f'skill.skill_tree_rpgs.{family}_tier_{tier}_spell_{index}.title'
    if key in lang:lang[key]=all_lang['spell.'+id.replace(':','.')+'.name']
   for id in wording['triggers']:
    tooltip='data/skill_tree_rpgs/spell/'+id+'.json';o=json.loads(files[tooltip]);o.setdefault('tooltip',{})['show_activation']=True;files[tooltip]=(json.dumps(o,indent=2)+'\n').encode()
  if n=='assets/spell_engine/lang/en_us.json':
   for id,text in wording['triggers'].items():lang['spell.tooltip.activation.skill_tree_rpgs.'+id]=text.replace('%','%%')
  files[n]=(json.dumps(lang,indent=2,ensure_ascii=False)+'\n').encode() if lang!=json.loads(data) else data
 if jar.name.startswith('spell_engine-'):
  for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
  n='spell_engine.mixins.json';o=json.loads(files[n]);o['client'].append('client.SkillTooltipMixin');files[n]=(json.dumps(o,indent=2)+'\n').encode()
 with ZipFile(OUT/jar.name,'w') as z:
  for entry in infos:z.writestr(entry,files[entry.filename])
  for n in sorted(set(files)-set(old)):z.writestr(n,files[n])
 records.append({'jar':jar.name,'before_sha256':hashlib.sha256(jar.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/jar.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built tooltip candidates; combat resources unchanged')

from pathlib import Path
from zipfile import ZipFile
import json,hashlib,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/archer-node-balance';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True);records=[]
for pattern in ['adventurerskills-*','skill_tree-*']:
 active=next((ROOT/'mods').glob(pattern));source=BEFORE/active.name
 if not source.exists():shutil.copy2(active,source)
 changed=[];nodes=[]
 with ZipFile(source) as src,ZipFile(OUT/active.name,'w') as dst:
  for entry in src.infolist():
   old=data=src.read(entry)
   if entry.filename.endswith('/definitions.json') and '/puffish_skills/categories/' in entry.filename:
    d=json.loads(data)
    if pattern.startswith('adventurer'):
     d['ranged_damage+3%']['rewards'][0]['data']['value']=.02;d['ranged_damage+3%']['title']='+2% Ranged Damage'
     d['ranged_penetration']['rewards'][0]['data'].update(attribute='minecraft:movement_speed',value=.03);d['ranged_penetration']['title']='+3% Movement Speed'
     d['hunter']['rewards']=[x for x in d['hunter']['rewards'] if x.get('data',{}).get('attribute')!='puffish_attributes:armor_shred']
     d['hunter']['description']['text']=d['hunter']['description']['text'].replace('\n+10% Armor Penetration','')
     nodes+=['ranged_damage+3%','ranged_penetration','hunter']
    else:
     for key,node in d.items():
      if any(r.get('data',{}).get('attribute')=='ranged_weapon:damage' for r in node.get('rewards',[])):
       for reward in node['rewards']:
        if reward.get('data',{}).get('attribute')=='ranged_weapon:damage':reward['data']['value']=.02
       node['description']=json.loads(json.dumps(node['description']).replace('+3% Ranged Damage','+2% Ranged Damage'));nodes.append(key)
    if d!=json.loads(old):data=(json.dumps(d,indent=2,ensure_ascii=False)+'\n').encode()
   if data!=old:changed.append(entry.filename)
   dst.writestr(entry,data)
 records.append({'jar':active.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':changed,'node_edits':nodes})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print(json.dumps(records,indent=2))

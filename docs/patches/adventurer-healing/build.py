"""Extend Adventurer healing rewards without changing saved node IDs or other trees."""
from pathlib import Path
from zipfile import ZipFile
import copy,hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
WORK=ROOT/'tmp/adventurer-healing';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True)
active=next((ROOT/'mods').glob('adventurerskills-*.jar'));source=BEFORE/active.name
if not source.exists():shutil.copy2(active,source)
entry='data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json'
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old);nodes=json.loads(files[entry])
for key,attribute in [('healing+3%','puffish_attributes:healing'),('colossus','puffish_attributes:healing'),('caster_power','spell_power:arcane'),('magistrate','spell_power:arcane')]:
 node=nodes[key];reward=copy.deepcopy(next(r for r in node['rewards'] if r.get('data',{}).get('attribute')==attribute))
 assert not any(r.get('data',{}).get('attribute')=='spell_power:healing' for r in node['rewards'])
 reward['data']['attribute']='spell_power:healing';node['rewards'].append(reward)
nodes['caster_power']['title']='+3% Magic Damage'
nodes['magistrate']['description']['text']=nodes['magistrate']['description']['text'].replace('+10% Arcane/Fire/Frost Power','+10% Magic Damage')
files[entry]=(json.dumps(nodes,indent=2,ensure_ascii=False)+'\n').encode()
with ZipFile(OUT/active.name,'w') as z:
 for info in infos:z.writestr(info,files[info.filename])
record={'jar':active.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':[entry],'nodes':['healing+3%','colossus','caster_power','magistrate']}
(HERE/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n')
print('Built four-node Adventurer overlay; active pack unchanged')

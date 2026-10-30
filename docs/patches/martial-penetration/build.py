from pathlib import Path
from zipfile import ZipFile
import json,hashlib
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/martial-penetration';out=W/'built';out.mkdir(exist_ok=True);source=next((W/'before').glob('*.jar'))
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
f=dict(old);n='data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json';d=json.loads(f[n])
for reward in d['martial_prowess']['rewards']:
 if reward.get('data',{}).get('attribute')=='critical_strike:chance':reward['data'].update(attribute='puffish_attributes:armor_shred',value=.1,operation='multiply_total')
d['martial_prowess']['description']['text']=d['martial_prowess']['description']['text'].replace('+10% Critical Chance','+10% Armor Penetration')
f[n]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
with ZipFile(out/source.name,'w') as z:
 for info in infos:z.writestr(info,f[info.filename])
record={'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/source.name).read_bytes()).hexdigest(),'changed':[n]};(H/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n',encoding='utf-8');print('Built single-resource node tuning candidate')

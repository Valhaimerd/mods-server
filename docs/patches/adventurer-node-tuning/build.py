from pathlib import Path
from zipfile import ZipFile
import json,hashlib
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/adventurer-node-tuning';out=W/'built';out.mkdir(exist_ok=True);source=next((W/'before').glob('*.jar'))
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
f=dict(old);n='data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json';d=json.loads(f[n])
for r in d['bulwark']['rewards']:
 if r['data']['attribute']=='puffish_attributes:resistance':r['data']['value']=.05
d['bulwark']['description']['text']=d['bulwark']['description']['text'].replace('+2% Resistance','+5% Resistance')
for node,attribute,line in [('martial_prowess','critical_strike:damage','Critical Damage'),('hunter','movement_speed','Movement Speed')]:
 d[node]['rewards']=[r for r in d[node]['rewards'] if r.get('data',{}).get('attribute')!=attribute]
 d[node]['description']['text']='\n'.join(s for s in d[node]['description']['text'].splitlines() if line not in s)
f[n]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
with ZipFile(out/source.name,'w') as z:
 for info in infos:z.writestr(info,f[info.filename])
record={'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/source.name).read_bytes()).hexdigest(),'changed':[n]};(H/'artifacts.json').write_text(json.dumps(record,indent=2)+'\n',encoding='utf-8');print('Built single-resource node tuning candidate')

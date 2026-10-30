from pathlib import Path
from zipfile import ZipFile
import json,hashlib,shutil
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/armor-unbreaking';B=W/'before';O=W/'built';B.mkdir(parents=True,exist_ok=True);O.mkdir(exist_ok=True)
p=next((R/'mods').glob('penchant-*.jar'));before=B/p.name
if not before.exists():shutil.copy2(p,before)
with ZipFile(before) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
f=dict(old)
for n in old:
 if not n.endswith('/enchantment/unbreaking.json'):continue
 d=json.loads(old[n]);effects=d['effects']['minecraft:item_damage'];armor=[e for e in effects if e.get('requirements',{}).get('predicate',{}).get('items')=='#penchant:unbreaking_armor'];assert len(armor)==1,n
 armor[0]['effect']['chance']={'type':'minecraft:linear','base':.08,'per_level_above_first':.08}
 f[n]=(json.dumps(d,indent=2)+'\n').encode()
with ZipFile(O/p.name,'w') as z:
 for i in infos:z.writestr(i,f[i.filename])
(H/'unbreaking.json').write_bytes(f['data/minecraft/enchantment/unbreaking.json'])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(H/'artifacts.json').write_text(json.dumps({'jar':p.name,'before_sha256':sha(before),'sha256':sha(O/p.name),'changed':[n for n in old if f[n]!=old[n]],'added':[]},indent=2)+'\n',encoding='utf-8')
print('Built armor Unbreaking 8% per level candidate')

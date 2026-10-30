from pathlib import Path
from zipfile import ZipFile
import json,hashlib
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/protection-one-percent';out=W/'built';out.mkdir(exist_ok=True)
with ZipFile(Path((R/'tmp/class-follow-up/native-jar.txt').read_text(encoding='utf-8'))) as z:vanilla={k:z.read('data/minecraft/enchantment/'+k+'.json') for k in ['protection','projectile_protection','fire_protection','blast_protection']}
records=[]
for source in (W/'before').glob('*.jar'):
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 inputs={('data/minecraft/enchantment/'+k+'.json'):v for k,v in vanilla.items()} if source.name.startswith('penchant-') else {'data/spell_power/enchantment/magic_protection.json':old['data/spell_power/enchantment/magic_protection.json']}
 for n,data in inputs.items():
  d=json.loads(data)
  for effect in d['effects']['minecraft:damage_protection']:
   value=effect['effect']['value'];assert value['type']=='minecraft:linear';value['base']=.25;value['per_level_above_first']=.25
  files[n]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 with ZipFile(out/source.name,'w') as z:
  for info in infos:z.writestr(info,files[info.filename])
  for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
 records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(files.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n',encoding='utf-8');print('Built two data-only protection candidates')

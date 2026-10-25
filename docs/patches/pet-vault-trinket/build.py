from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pet-vault-trinket';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True);name='pet_vault-mc26.2-v1.5.0-neoforge.jar';before=WORK/('before-'+name)
if not before.exists():shutil.copy2(ROOT/'mods'/name,before)
with ZipFile(before) as z:infos=z.infolist();files={i.filename:z.read(i) for i in infos}
key='data/curios/tags/item/trinket.json';assert key not in files,'Expected unpatched Pet Vault baseline'
files[key]=(json.dumps({'replace':False,'values':['pet_vault:pet_necklace']},indent=2)+'\n').encode()
with ZipFile(OUT/name,'w') as z:
 for info in infos:z.writestr(info,files[info.filename])
 z.writestr(key,files[key])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(HERE/'artifacts.json').write_text(json.dumps({'jar':name,'before_sha256':sha(before),'sha256':sha(OUT/name),'changed':[],'added':[key]},indent=2)+'\n',encoding='utf-8');print('Built additive trinket tag; original necklace tag and all other entries unchanged')

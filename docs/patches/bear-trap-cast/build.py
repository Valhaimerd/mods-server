"""Repair only Bear Trap's missing non-instant casting animation."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/bear-trap-cast';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('rogues-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
with ZipFile(before) as z:files={n:z.read(n) for n in z.namelist()}
name='data/rogues/spell/bear_trap.json';old=json.loads(files[name]);fixed=json.loads(files[name]);fixed['active']['cast']['animation']={'id':'spell_engine:one_handed_ground_charge'}
with ZipFile(next((ROOT/'mods').glob('spell_engine-*.jar'))) as z:assert 'assets/spell_engine/player_animations/one_handed_ground_charge.json' in z.namelist()
files[name]=(json.dumps(fixed,indent=2,ensure_ascii=False)+'\n').encode()
with ZipFile(OUT/active.name,'w',ZIP_DEFLATED) as z:
 for n,data in files.items():z.writestr(n,data)
with ZipFile(before) as a,ZipFile(OUT/active.name) as b:
 assert b.testzip() is None and set(a.namelist())==set(b.namelist())
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==[name]
 del fixed['active']['cast']['animation']
 assert fixed==old
record={'jar':active.name,'before_sha256':hashlib.sha256(before.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':[name],'added':[]}
(HERE/'artifacts.json').write_text(json.dumps([record],indent=2)+'\n',encoding='utf-8')
print('PASS candidate: exactly Bear Trap spell JSON changed; existing gameplay values preserved; animation asset exists; archive CRC valid')

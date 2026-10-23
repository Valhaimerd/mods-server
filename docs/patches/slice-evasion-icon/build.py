"""Supply Slice Evasion's missing status texture using the installed Slice & Dice artwork."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/slice-evasion-icon';WORK.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('skill_tree-*.jar'));rogues=next((ROOT/'mods').glob('rogues-*.jar'));before=WORK/('before-'+active.name);candidate=WORK/active.name
if not before.exists():shutil.copy2(active,before)
with ZipFile(rogues) as z:icon=z.read('assets/rogues/textures/mob_effect/slice_and_dice.png')
key='assets/skill_tree_rpgs/textures/mob_effect/slice_evasion.png'
with ZipFile(before) as z:files={n:z.read(n) for n in z.namelist()}
assert key not in files
files[key]=icon
with ZipFile(candidate,'w',ZIP_DEFLATED) as z:
 for n,data in files.items():z.writestr(n,data)
with ZipFile(before) as a,ZipFile(candidate) as b:
 assert b.testzip() is None
 assert set(b.namelist())-set(a.namelist())=={key}
 assert all(a.read(n)==b.read(n) for n in a.namelist())
 assert b.read(key)==icon and icon.startswith(b'\x89PNG\r\n\x1a\n')
record={'jar':active.name,'before_sha256':hashlib.sha256(before.read_bytes()).hexdigest(),'sha256':hashlib.sha256(candidate.read_bytes()).hexdigest(),'changed':[],'added':[key],'source_jar':rogues.name,'source_entry':'assets/rogues/textures/mob_effect/slice_and_dice.png','icon_sha256':hashlib.sha256(icon).hexdigest()}
assert hashlib.sha256(active.read_bytes()).hexdigest() in [record['before_sha256'],record['sha256']],'Active JAR changed since snapshot'
backup=ROOT/'mod fixing/retired packs/slice-evasion-icon-2026-10-23';backup.mkdir(parents=True,exist_ok=True);shutil.copy2(before,backup/active.name)
shutil.copy2(candidate,active);shutil.copy2(candidate,ROOT/'mod fixing/1 fixed packs'/active.name)
(HERE/'artifacts.json').write_text(json.dumps([record],indent=2)+'\n',encoding='utf-8')
assert active.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/active.name).read_bytes()
print('PASS: exactly one PNG added; every existing entry preserved; original Slice & Dice image bytes; CRC and active/fixed copies match')

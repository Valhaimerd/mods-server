from pathlib import Path
from zipfile import ZipFile
import json,hashlib,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-followup-phase-one';records=json.loads((H/'artifacts.json').read_text());count=0
for row in records:
 a=W/'before'/row['jar'];b=(R/'mods' if '--installed' in sys.argv else W/'built')/row['jar']
 assert hashlib.sha256(b.read_bytes()).hexdigest()==row['after'],b
 with ZipFile(a) as za,ZipFile(b) as zb:
  assert zb.testzip() is None;assert set(za.namelist()).issubset(zb.namelist())
  for n in za.namelist():
   if n not in row['changed']:assert za.read(n)==zb.read(n),n;count+=1
  assert za.read('META-INF/neoforge.mods.toml')==zb.read('META-INF/neoforge.mods.toml')
  assert not any('Probe.class' in n or n.startswith('local/audit/') for n in zb.namelist())
  for n in row['changed']:
   if n.endswith('.json'):json.loads(zb.read(n))
  if row['jar'].startswith('skill_tree-'):
   for category in ['class_skills','class_skills_1']:
    key=f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json'
    if key not in zb.namelist():continue
    d=json.loads(zb.read(key))
    for node in ['warrior_root','warrior_boost']:assert d[node]['rewards'][0]['data']['value']==.05
  if row['jar'].startswith('adventurerskills-'):
   d=json.loads(zb.read('data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json'));assert d['melee_damage+3%']['rewards'][0]['data']['value']==.02
print('PASS',count,'unchanged-entry preservation checks; metadata, saved entries, node values and candidate hashes verified')
(H/'preservation-result.txt').write_text(f'PASS {count} unchanged entries; metadata, saved entries, node values and hashes verified.\n')

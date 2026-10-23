from pathlib import Path
from zipfile import ZipFile
import json,sys,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods'
with ZipFile(next(folder.glob('archers-*.jar'))) as z:
 rain=json.loads(z.read('data/archers/spell/rain_of_arrows.json'));assert rain['deliver']['meteor']['launch_radius']==6,'Rain scatter must be 6 blocks'
 assert rain['area_impact']['radius']==2.5 and rain['cost']['cooldown']['duration']==30
 assert rain['deliver']['meteor']['launch_properties']['extra_launch_count']==39
with ZipFile(next(folder.glob('skill_tree-*.jar'))) as z:
 for suffix in ['modifier_1','modifier_2']:
  m=json.loads(z.read('data/skill_tree_rpgs/spell/archer_tier_4_spell_1_'+suffix+'.json'))['modifiers'][0]
  assert 6+m.get('meteor_launch_radius_add',0)==6
for r in json.loads((HERE/'artifacts.json').read_text()) if (HERE/'artifacts.json').exists() else []:
 before=ROOT/'tmp/rain-radius/before'/r['jar'];after=folder/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 icon_file=ROOT/'docs/patches/slice-evasion-icon/artifacts.json'
 icon=next((x for x in json.loads(icon_file.read_text()) if x['jar']==after.name),None) if icon_file.exists() else None
 layered=folder==ROOT/'mods' and icon and icon['before_sha256']==r['sha256']
 assert hashlib.sha256(after.read_bytes()).hexdigest()==(icon['sha256'] if layered else r['sha256'])
 audited=ROOT/'tmp/slice-evasion-icon'/('before-'+r['jar']) if layered else after
 with ZipFile(before) as a,ZipFile(audited) as b:
  assert b.testzip() is None and set(a.namelist())==set(b.namelist())
  changed={n for n in a.namelist() if a.read(n)!=b.read(n)};assert changed==set(r['changed'])
  assert all(n.endswith('.json') for n in changed)
 if folder==ROOT/'mods':assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
print('PASS 6-block base/Torrential/Endless scatter, unchanged splash/cooldown/count, exact resource-only changes')

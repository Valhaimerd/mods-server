"""Narrow, guarded overlay onto the installed post-phase-two Skill Tree archive."""
from pathlib import Path
from zipfile import ZipFile
import copy,json,hashlib,shutil,struct
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
p=next((R/'mods').glob('skill_tree-neoforge-*.jar'));before=hashlib.sha256(p.read_bytes()).hexdigest()
with ZipFile(p) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
data=dict(old);details=[]
for school in ('arcane','fire','frost'):
 key=f'data/skill_tree_rpgs/spell/{school}_tier_3_passive_2.json';d=json.loads(data[key]);cd=d['cost']['cooldown'];assert cd['duration']==15 and cd['haste_affected']
 effect=d['impacts'][0]['action']['status_effect'] if school=='arcane' else d['deliver']['stash_effect']
 assert effect['duration']==8 and effect['amplifier_power_multiplier']==.125
 effect.update(duration=6.0,amplifier_power_multiplier=1/6)
 data[key]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 details.append({'school':school,'duration':6,'base_cooldown':15,'absorption_hp_per_complete_6_power':2})
key='assets/skill_tree_rpgs/lang/en_us.json';d=json.loads(data[key])
for school in ('arcane','fire','frost'):
 k=f'spell.skill_tree_rpgs.{school}_tier_3_passive_2.description';assert '8s shield' in d[k] and 'per 8 ' in d[k]
 d[k]=d[k].replace('8s shield','6s shield').replace('per 8 ','per 6 ')
data[key]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
W=R/'tmp/mage-ward-tuning';W.mkdir(parents=True,exist_ok=True);out=W/p.name
with ZipFile(out,'w') as z:
 for i in infos:z.writestr(copy.copy(i),data[i.filename])
with ZipFile(out) as z:
 assert z.testzip() is None
 changed={n for n in old if old[n]!=z.read(n)};assert changed=={key}|{f'data/skill_tree_rpgs/spell/{s}_tier_3_passive_2.json' for s in ('arcane','fire','frost')}
 # Check native float multiplier boundary math without another game launch.
 multiplier=struct.unpack('f',struct.pack('f',1/6))[0]
 for power,expected in ((0,0),(5,0),(6,2),(11,2),(12,4),(18,6),(24,8),(48,16)):
  assert 2*int(struct.unpack('f',struct.pack('f',power*multiplier))[0])==expected
backup=R/'mod backup/mage-ward-tuning-2026-11-02';backup.mkdir(parents=True,exist_ok=True);shutil.copy2(p,backup/p.name)
for target in (p,R/'mod fixing/1 fixed packs'/p.name):shutil.copy2(out,target)
record={'date':'2026-11-02','jar':p.name,'before':before,'after':hashlib.sha256(p.read_bytes()).hexdigest(),'changed_entries':sorted(changed),'wards':details,'verification':'ZIP CRC, exact four-entry change boundary, unchanged class bytes, JSON and float threshold checks. No long runtime test requested.'}
(H/'verification.json').write_text(json.dumps(record,indent=2)+'\n');print('Installed Ward tuning; only three spell JSONs and English descriptions changed. Focused checks passed.')

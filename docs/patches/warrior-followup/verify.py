from pathlib import Path
from zipfile import ZipFile
import hashlib,json,io
from PIL import Image
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 p=ROOT/'mods'/r['jar'];assert hashlib.sha256(p.read_bytes()).hexdigest()==r['sha256'];checks+=1
 with ZipFile(p) as z,ZipFile(ROOT/'tmp/warrior-followup/before'/p.name) as old:
  assert z.testzip() is None
  assert len(z.namelist())==len(set(z.namelist()))
  for n in old.namelist():
   if n not in r['changed']:assert z.read(n)==old.read(n),n
   checks+=1
  if p.name.startswith('skill_tree-'):
   o=json.loads(z.read('data/skill_tree_rpgs/spell/warrior_tier_4_spell_1_modifier_2.json'));assert o['type']=='MODIFIER' and 'deliver' not in o and 'passive' not in o
   lang=json.loads(z.read('assets/skill_tree_rpgs/lang/en_us.json'))
   for suffix,name in [('modifier_1','Strong Throw'),('modifier_2','Head Throw')]:
    assert lang['spell.skill_tree_rpgs.warrior_tier_2_spell_1_'+suffix+'.name']==name
    assert lang['skill.skill_tree_rpgs.warrior_tier_2_spell_1_'+suffix+'.title']==name
   for effect in ['berserker','berserker_crit','juggernaut','sentinel','warrior_freedom','warrior_endurance','warrior_momentum','warrior_momentum_ready','tenacity_ward']:
    Image.open(io.BytesIO(z.read('assets/skill_tree_rpgs/textures/mob_effect/'+effect+'.png'))).verify();checks+=1
print('PASS',checks,'hash, unchanged-entry, name, obsolete-trigger and icon checks')

from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-passives';installed='--installed' in sys.argv;checks=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 source=WORK/'before'/r['jar'];target=(ROOT/'mods' if installed else WORK/'built')/r['jar'];assert hashlib.sha256(source.read_bytes()).hexdigest()==r['before_sha256'];assert hashlib.sha256(target.read_bytes()).hexdigest()==r['sha256']
 if installed:assert target.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 with ZipFile(source) as a,ZipFile(target) as b:
  assert b.testzip() is None and set(b.namelist())==set(a.namelist())|set(r['added'])
  assert {n for n in a.namelist() if a.read(n)!=b.read(n)}==set(r['changed'])
  for n in a.namelist():
   if n not in r['changed']:assert a.read(n)==b.read(n);checks+=1
  if r['jar'].startswith('skill_tree'):
   for key in ['priest_tier_1_passive_1','priest_tier_1_passive_2']:
    o=json.loads(b.read('data/skill_tree_rpgs/spell/'+key+'.json'));assert o['passive']['triggers'][0]['chance']==1;assert o['cost']['cooldown']=={'duration':.25,'haste_affected':True};assert o['impacts'][0]['action']['status_effect']['duration']==6 and o['impacts'][0]['action']['status_effect']['amplifier_cap']==4
   o=json.loads(b.read('data/skill_tree_rpgs/spell/priest_tier_2_passive_2.json'));assert 'deliver' not in o and o['cost']['cooldown']['duration']==15 and o['passive']['triggers'][0]['chance']==1
   o=json.loads(b.read('data/skill_tree_rpgs/spell/priest_tier_2_passive_1.json'));assert o['target']['area']['include_caster'] and o['cost']['cooldown']['duration']==6
   old=json.loads(a.read('assets/skill_tree_rpgs/lang/en_us.json'));new=json.loads(b.read('assets/skill_tree_rpgs/lang/en_us.json'))
   for k in old:
    if old[k]!=new[k]:assert k.startswith(('spell.skill_tree_rpgs.priest_','effect.skill_tree_rpgs.healing_focus','effect.skill_tree_rpgs.divine_favor'))
    checks+=1
  else:
   old=json.loads(a.read('spell_engine.mixins.json'));new=json.loads(b.read('spell_engine.mixins.json'));assert new['client'][-2:]==['client.render.priest.FadeEntityMixin','client.render.priest.FadeHandsMixin'];assert new['mixins'][-1]=='priest.ReceivedAbsorptionMixin';new['client']=new['client'][:-2];new['mixins']=new['mixins'][:-1];assert new==old
   for n in ['net/spell_engine/internals/PriestRules.class','net/spell_engine/internals/PriestPassives.class']:assert b'net/minecraft/client/' not in b.read(n)
print(f'PASS {checks} archive, passive-data, description and client-isolation checks'+('; installed/fixed match' if installed else '; candidates'))

"""Prove display-only changes, synchronized names and native Supercharge values."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,re,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/skill-tooltip-polish';folder=Path(sys.argv[1]) if len(sys.argv)>1 else ROOT/'mods'
wording=json.loads((HERE/'wording.json').read_text());prefixes=('spell.archers.','spell.wizards.','spell.rogues.','spell.skill_tree_rpgs.archer_','spell.skill_tree_rpgs.rogue_','spell.skill_tree_rpgs.warrior_','spell.skill_tree_rpgs.arcane_','spell.skill_tree_rpgs.fire_','spell.skill_tree_rpgs.frost_')
all_lang={};definitions=[];descriptions=0;changed_keys=0;renames=0
for r in json.loads((HERE/'artifacts.json').read_text()):
 before=WORK/'before'/r['jar'];after=folder/r['jar']
 assert hashlib.sha256(before.read_bytes()).hexdigest()==r['before_sha256']
 assert hashlib.sha256(after.read_bytes()).hexdigest()==r['sha256']
 if folder.resolve()==(ROOT/'mods').resolve():assert after.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
 with ZipFile(before) as a,ZipFile(after) as b:
  assert b.testzip() is None and set(a.namelist())<=set(b.namelist())
  assert sorted(set(b.namelist())-set(a.namelist()))==r['added']
  assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
  for n in r['changed']:
   if n.endswith('/lang/en_us.json'):
    old=json.loads(a.read(n));new=json.loads(b.read(n));assert old.keys()<=new.keys()
    for k in new:
     if old.get(k)!=new[k]:
      assert k.endswith(('.description','.title')) or k.startswith('spell.tooltip.activation.'),(n,k)
      changed_keys+=1
      if k.endswith('.title'):renames+=1
   elif n.startswith('data/skill_tree_rpgs/spell/'):
    old=json.loads(a.read(n));new=json.loads(b.read(n));assert Path(n).stem in wording['triggers']
    old.setdefault('tooltip',{})['show_activation']=True;assert old==new,n
   else:assert n=='spell_engine.mixins.json',n
  for n in r['added']:
   assert n=='net/spell_engine/mixin/client/SkillTooltipMixin.class'
   assert int.from_bytes(b.read(n)[6:8],'big')==69
  for n in b.namelist():
   if n.endswith('/lang/en_us.json'):all_lang.update(json.loads(b.read(n)))
   if n.startswith('data/skill_tree_rpgs/puffish_skills/') and n.endswith('/definitions.json'):definitions.append(json.loads(b.read(n)))
  if r['jar'].startswith('skill_tree-'):
   o=json.loads(b.read('data/skill_tree_rpgs/spell/archer_tier_3_passive_1.json'));assert o['arrow_perks']['damage_multiplier']==1.25 and o['passive']['triggers'][0]['chance']==1 and o['cost']['cooldown']['duration']==8
 print('PASS exact presentation-only overlay:',r['jar'])
for defs in definitions:
 for id,definition in defs.items():
  title=definition.get('title',{}).get('translate');name='spell.skill_tree_rpgs.'+id+'.name'
  if title in all_lang and name in all_lang:assert all_lang[title]==all_lang[name],id
for k,v in all_lang.items():
 if not k.startswith(prefixes) or not k.endswith('.description'):continue
 descriptions+=1
 assert not re.search(r'[{}]|\bfloor\b|\bceil\b|percentage|rounded|Base cooldown:|Base interval:|reduced by haste',v,re.I),(k,v)
 assert not re.search(r'(?<!%)%(?!%)',v),(k,v)
for id in wording['triggers']:assert 'spell.tooltip.activation.skill_tree_rpgs.'+id in all_lang
assert '125%%' in all_lang['spell.skill_tree_rpgs.archer_tier_3_passive_1.description']
result=WORK/'client/tooltip-result.txt';assert result.read_text().startswith('PASS ');print(result.read_text().strip())
server=WORK/'server/warrior-result.txt';assert server.read_text().startswith('PASS ');print(server.read_text().strip())
print(f'PASS {descriptions} concise descriptions, {renames} synchronized tree titles, {len(wording["triggers"])} activation labels; {changed_keys} language edits')

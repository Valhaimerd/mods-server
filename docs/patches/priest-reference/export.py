"""Read installed Priest resources into a reproducible owner reference; never edit JARs."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
spells={};lang={};sources=[];trees={}
omit={'visuals','client_data','particles','animation','sound','sounds','start_sound','spawn_fx','despawn_fx','launch_visuals','launch_sound','travel_particles','existence_particles','swing_sound','impact_sound'}
def plain(o):
 if isinstance(o,dict):return {k:plain(v) for k,v in o.items() if k not in omit}
 if isinstance(o,list):return [plain(v) for v in o]
 return o
for pattern in ['paladins-*.jar','skill_tree-*.jar','spell_engine-*.jar','spell_power-*.jar']:
 jar=next((ROOT/'mods').glob(pattern));sources.append(f'- `{jar.name}`; SHA-256 `{hashlib.sha256(jar.read_bytes()).hexdigest()}`')
 with ZipFile(jar) as z:
  for n in z.namelist():
   if n.endswith('/lang/en_us.json'):lang.update(json.loads(z.read(n)))
   if n.endswith('.json') and '/spell/' in n and (n.startswith('data/paladins/') or n.startswith('data/skill_tree_rpgs/')):
    o=json.loads(z.read(n));keep=(n.startswith('data/paladins/') and o.get('group','').startswith('priest_')) or n.startswith('data/skill_tree_rpgs/spell/priest_') or Path(n).stem in ['lightwell_cleanse','weapon_holy_root','weapon_holy_shock_modifier_1','weapon_holy_shock_modifier_2']
    if keep:spells[n.split('/')[1]+':'+Path(n).stem]=o
  if pattern.startswith('skill_tree'):
   for category in ['class_skills','weapon_skills']:
    prefix=f'data/skill_tree_rpgs/puffish_skills/categories/{category}/'
    definitions={k:v for k,v in json.loads(z.read(prefix+'definitions.json')).items() if k.startswith('priest_') or k.startswith('weapon_holy')}
    skills={k:v for k,v in json.loads(z.read(prefix+'skills.json')).items() if v.get('definition') in definitions}
    connections=json.loads(z.read(prefix+'connections.json'))
    def relevant(o):
     if isinstance(o,dict):return {k:v for k,x in o.items() if (v:=relevant(x))}
     if isinstance(o,list):
      if all(isinstance(x,str) for x in o):return o if any(x in skills for x in o) else []
      return [v for x in o if (v:=relevant(x))]
     return o
    trees[category]={'definitions':definitions,'skills':skills,'connections':relevant(connections)}
def title(id):return lang.get('spell.'+id.replace(':','.')+'.name',id)
text=(HERE/'readable.md').read_text(encoding='utf-8')+'\n## Installed sources\n\n'+'\n'.join(sources)+'\n'
used=set()
for tier,pair in [(2,['holy_beam','levitate']),(3,['circle_of_healing','penance']),(4,['lightwell','barrier'])]:
 text+=f'\n## Tier {tier-1} - paired raw attributes\n\n| Attribute | {title("paladins:"+pair[0])} | {title("paladins:"+pair[1])} |\n|---|---|---|\n'
 for key in ['school','range','active','target','deliver','impacts','area_impact','cost']:
  values=[json.dumps(plain(spells['paladins:'+name].get(key,'engine default')),ensure_ascii=False).replace('|','&#124;') for name in pair]
  text+='| '+key+' | '+' | '.join('`'+v+'`' for v in values)+' |\n'
 for i,name in enumerate(pair,1):
  for id in ['paladins:'+name]+[f'skill_tree_rpgs:priest_tier_{tier}_spell_{i}_{s}' for s in ['root','modifier_1','modifier_2']]:
   assert id in spells,id;used.add(id);text+=f'\n### {title(id)}\n\nID: `{id}`\n\n```json\n{json.dumps(plain(spells[id]),indent=2)}\n```\n'
 text+='\n### Passive pair\n'
 for i in [1,2]:
  id=f'skill_tree_rpgs:priest_tier_{tier-1}_passive_{i}';assert id in spells;used.add(id)
  text+=f'\n#### {title(id)}\n\nID: `{id}`\n\n```json\n{json.dumps(plain(spells[id]),indent=2)}\n```\n'
assert len(used)==30
text+='\n## Base/weapon spells and helpers\n'
for id in sorted(spells.keys()-used):text+=f'\n### {title(id)}\n\nID: `{id}`\n\n```json\n{json.dumps(plain(spells[id]),indent=2)}\n```\n'
text+='\n## Exact tree definitions, placements and connections\n\n```json\n'+json.dumps(trees,indent=2)+'\n```\n'
output=ROOT/'mod fixing/Priest skills - raw attributes and nodes.md';output.write_text(text,encoding='utf-8')
assert all(f'priest_tier_{tier}_spell_{i}_{s}' in text for tier in [2,3,4] for i in [1,2] for s in ['root','modifier_1','modifier_2'])
print(f'Wrote {output}: 6 actives, 18 upgrades, 6 passives, {len(spells)-30} base/weapon/helper resources; both trees and source hashes')

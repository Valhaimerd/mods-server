"""Extract installed Rogue class data, tree grants and effect definitions without editing JARs."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json
ROOT=Path(__file__).resolve().parents[3]
spells={};lang={};sources=[]
for pattern in ['rogues-*.jar','skill_tree-*.jar','spell_engine-*.jar']:
 jar=next((ROOT/'mods').glob(pattern));sources.append(f'- `{jar.name}` ? SHA-256 `{hashlib.sha256(jar.read_bytes()).hexdigest()}`')
 with ZipFile(jar) as z:
  for name in z.namelist():
   if name.endswith('/lang/en_us.json'):lang.update(json.loads(z.read(name)))
   if name.endswith('.json') and (name.startswith('data/rogues/spell/') or name.startswith('data/skill_tree_rpgs/spell/rogue_')):
    o=json.loads(z.read(name))
    if name.startswith('data/rogues/') and not o.get('group','').startswith('rogue_'):continue
    spells[name.split('/')[1]+':'+Path(name).stem]=o
  if pattern.startswith('skill_tree'):
   prefix='data/skill_tree_rpgs/puffish_skills/categories/class_skills/'
   definitions={k:v for k,v in json.loads(z.read(prefix+'definitions.json')).items() if k.startswith('rogue_')}
   skills={k:v for k,v in json.loads(z.read(prefix+'skills.json')).items() if v.get('definition') in definitions}
   connections=json.loads(z.read(prefix+'connections.json'))

def title(id):return lang.get('spell.'+id.replace(':','.')+'.name',id)
def plain(o):
 if isinstance(o,dict):return {k:plain(v) for k,v in o.items() if k not in {'visuals','client_data','particles','animation','sound','sounds','spawn_fx','despawn_fx','launch_visuals','launch_sound','travel_particles','swing_sound','impact_sound'}}
 if isinstance(o,list):return [plain(v) for v in o]
 return o
text=(Path(__file__).with_name('readable.md')).read_text(encoding='utf-8')
text+='\n## Installed sources\n\n'+'\n'.join(sources)+'\n'
text+='\n## Raw active skills and secondary nodes\n\nOnly presentation assets are omitted. Missing mechanical fields use the verified engine defaults above.\n'
for tier,pair in [(2,['shock_powder','slice_and_dice']),(3,['shadow_step','bear_trap']),(4,['vanish','mutilate'])]:
 text+=f'\n### Active tier {tier-1}\n'
 for index,key in enumerate(pair,1):
  ids=['rogues:'+key]+[f'skill_tree_rpgs:rogue_tier_{tier}_spell_{index}_{s}' for s in ['root','modifier_1','modifier_2']]
  for id in ids:
   text+=f'\n#### {title(id)}\n\nID: `{id}`\n\n```json\n{json.dumps(plain(spells[id]),indent=2)}\n```\n'
   if id+'_bonus' in spells:
    bonus=id+'_bonus';text+=f'\nHidden companion also granted by this node: `{bonus}`\n\n```json\n{json.dumps(plain(spells[bonus]),indent=2)}\n```\n'
text+='\n## Raw passive pairs\n'
for tier in [1,2,3]:
 text+=f'\n### Passive pair {tier}\n'
 for i in [1,2]:
  id=f'skill_tree_rpgs:rogue_tier_{tier}_passive_{i}'
  text+=f'\n#### {title(id)}\n\nID: `{id}`\n\n```json\n{json.dumps(plain(spells[id]),indent=2)}\n```\n'
text+='\n## Exact class-tree grants\n\nThese record which companion spell IDs a node grants; an upgrade may grant more than its displayed spell. Ordinary Rogue stat nodes are also retained here.\n\n```json\n'+json.dumps(definitions,indent=2)+'\n```\n'
text+='\n## Tree placement and relevant connections\n\n```json\n'+json.dumps(skills,indent=2)+'\n```\n'
def relevant(o):
 if isinstance(o,dict):return {k:v for k,x in o.items() if (v:=relevant(x))}
 if isinstance(o,list):
  if all(isinstance(x,str) for x in o):return o if any(x in skills for x in o) else []
  return [v for x in o if (v:=relevant(x))]
 return o
text+='\nConnections touching Rogue nodes (other IDs may connect to adjacent ordinary nodes):\n\n```json\n'+json.dumps(relevant(connections),indent=2)+'\n```\n'
assert len(spells)==31,len(spells)
output=ROOT/'mod fixing/Rogue skills - raw attributes and nodes.md';output.write_text(text,encoding='utf-8')
print(f'Wrote {output}: 6 actives, 18 visible upgrades, 1 hidden companion, 6 passives; {len(definitions)} tree definitions')

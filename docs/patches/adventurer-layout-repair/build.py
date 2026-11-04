from pathlib import Path
from zipfile import ZipFile
import copy, hashlib, json

R = Path(__file__).resolve().parents[3]
H = Path(__file__).resolve().parent
W = R / 'tmp/adventurer-layout-repair'
NAME = 'adventurerskills-EN-1.3-26.2-neoforge-fixed.jar'
BASE = 'data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
layout = json.loads((H / 'layout.json').read_text())
with ZipFile(W / 'before' / NAME) as z:
    infos = z.infolist()
    old = {i.filename: z.read(i) for i in infos}
files = dict(old)
nodes, definitions, connections = [json.loads(old[BASE + f + '.json']) for f in ['skills', 'definitions', 'connections']]
# Restore Utility positions exactly. Only remove the old mastery spokes;
# retain the original internal branches and shared entrance.
utility = set(sum(layout['utility_paths'], []))
for key, position in layout['utility_positions'].items():
    nodes[key].update(position)
normal = connections['normal']['bidirectional']
normal[:] = [e for e in normal if not utility.intersection(e)]
normal.extend(layout['utility_edges'])
for path in layout['utility_paths']:
    normal.extend([[path[5], path[7]], [path[6], path[7]]])
# Five sequential nodes, then an equal two-node split and specialty merge.
# Keep the same IDs/rewards/prerequisites; never overlap Utility positions.
for path, y in zip(layout['defense_paths'], [-96, 0, 96]):
    positions = [(-136 - 68 * i, y) for i in range(5)] + [(-476, y - 28), (-476, y + 28), (-544, y)]
    for key, (x, row) in zip(path, positions):
        nodes[key].update(x=x, y=row)
        definition = definitions[nodes[key]['definition']]
        if key != path[-1]:
            definition['title'] = definition['title'].replace(' hp', ' Max HP').replace(' armor', ' Armor')
for name, data in [('skills', nodes), ('definitions', definitions), ('connections', connections)]:
    files[BASE + name + '.json'] = (json.dumps(data, ensure_ascii=False, indent=2) + '\n').encode()
out = W / 'built' / NAME
with ZipFile(out, 'w') as z:
    for info in infos:
        z.writestr(copy.copy(info), files[info.filename])
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
(H / 'artifact.json').write_text(json.dumps({'jar': NAME, 'before': sha(W / 'before' / NAME), 'after': sha(out), 'changed': [n for n in old if old[n] != files[n]]}, indent=2) + '\n')
print('Built focused layout candidate; production unchanged.')

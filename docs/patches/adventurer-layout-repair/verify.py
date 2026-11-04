from pathlib import Path
from zipfile import ZipFile
import itertools, json, math, sys

R = Path(__file__).resolve().parents[3]
H = Path(__file__).resolve().parent
W = R / 'tmp/adventurer-layout-repair'
NAME = 'adventurerskills-EN-1.3-26.2-neoforge-fixed.jar'
BASE = 'data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
layout = json.loads((H / 'layout.json').read_text())
folder = 'before' if '--baseline' in sys.argv else 'built'
with ZipFile(W / folder / NAME) as z:
    nodes, definitions, connections = [json.loads(z.read(BASE + f + '.json')) for f in ['skills', 'definitions', 'connections']]
with ZipFile(W / 'before' / NAME) as z:
    old_nodes, old_definitions, old_connections = [json.loads(z.read(BASE + f + '.json')) for f in ['skills', 'definitions', 'connections']]
errors = []; passed = 0
def check(condition, label):
    global passed
    if condition: passed += 1
    else: errors.append(label)
edge = lambda a, b: frozenset([a, b])
normal = {frozenset(e) for e in connections['normal']['bidirectional']}
utility = set(sum(layout['utility_paths'], []))
defense = set(sum(layout['defense_paths'], []))
check(connections['exclusive'] == old_connections['exclusive'], 'all path exclusivity unchanged')
check(set(nodes) == set(old_nodes), 'no allocated IDs added or removed')
for key in set(nodes) - utility - defense:
    check(nodes[key] == old_nodes[key], 'unrelated placement unchanged ' + key)
for key, position in layout['utility_positions'].items():
    check(all(nodes[key][k] == v for k, v in position.items()), 'original Utility position ' + key)
expected = {frozenset(e) for e in layout['utility_edges']}
for path in layout['utility_paths']:
    expected |= {edge(path[5], path[7]), edge(path[6], path[7])}
check({e for e in normal if e & utility} == expected, 'Utility original internal graph and only two mastery spokes')
check(not any(e & utility and e & defense for e in normal), 'no Defense/Utility links')
for path in layout['defense_paths']:
    expected = {edge(layout['defense_root'], path[0])}
    expected |= {edge(path[i], path[i+1]) for i in range(4)}
    expected |= {edge(path[4], path[5]), edge(path[4], path[6]), edge(path[5], path[7]), edge(path[6], path[7])}
    check({e for e in normal if e & set(path)} == expected, 'exact Defense chain/split/merge ' + path[-1])
    check(definitions[nodes[path[-1]]['definition']]['required_skills'] == 2, 'two visible predecessor requirement')
for key in utility | defense:
    new = definitions[nodes[key]['definition']]; old = old_definitions[old_nodes[key]['definition']]
    check(new.get('rewards') == old.get('rewards'), 'unchanged rewards ' + key)
    check(new['icon'] == old['icon'] and new['size'] == 1, 'preserved icon and normal size ' + key)
    check(nodes[key]['definition'] == old_nodes[key]['definition'], 'stable allocation identity ' + key)
for a, b in itertools.combinations(nodes, 2):
    if a not in utility | defense and b not in utility | defense: continue
    # Icons are 16px at size one. Require a meaningful gap, not only unique coordinates.
    if math.dist((nodes[a]['x'], nodes[a]['y']), (nodes[b]['x'], nodes[b]['y'])) < 24:
        errors.append('overlapping/too-close nodes ' + a + ' / ' + b)
# Detect an edge running through an unrelated node, the screenshot's false connection.
for e in normal:
    if not e & (defense | utility): continue
    a, b = tuple(e); ax, ay = nodes[a]['x'], nodes[a]['y']; bx, by = nodes[b]['x'], nodes[b]['y']
    length = (bx-ax)**2 + (by-ay)**2
    for key, node in nodes.items():
        if key in e or not length: continue
        t = ((node['x']-ax)*(bx-ax)+(node['y']-ay)*(by-ay))/length
        if .01 < t < .99 and math.dist((node['x'],node['y']), (ax+t*(bx-ax),ay+t*(by-ay))) < 10:
            errors.append('edge crosses node ' + a + ' / ' + b + ' -> ' + key)
def orient(a, b, c):
    return (b[0]-a[0])*(c[1]-a[1]) - (b[1]-a[1])*(c[0]-a[0])
xy = lambda key: (nodes[key]['x'], nodes[key]['y'])
for e, f in itertools.combinations(normal, 2):
    if e & f or not (e & defense or f & defense): continue
    a, b = map(xy, e); c, d = map(xy, f)
    if orient(a,b,c)*orient(a,b,d) < 0 and orient(c,d,a)*orient(c,d,b) < 0:
        errors.append('crossing Defense edges ' + str(tuple(e)) + ' / ' + str(tuple(f)))
label = 'baseline' if '--baseline' in sys.argv else 'candidate'
(H / (label + '-checks.json')).write_text(json.dumps({'passed': passed, 'failed': errors}, indent=2) + '\n')
print(label, passed, 'passed;', len(errors), 'failed'); print('\n'.join(errors[:8]))
sys.exit(1 if errors else 0)

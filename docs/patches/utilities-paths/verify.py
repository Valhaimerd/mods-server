from pathlib import Path
from zipfile import ZipFile
import json,sys
from nodes import *
R=Path(__file__).resolve().parents[3]
p=Path(sys.argv[1]) if len(sys.argv)>1 else R/'mods/adventurerskills-EN-1.3-26.2-neoforge-fixed.jar'
with ZipFile(p) as z:
 pre='data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
 s,d,c=[json.loads(z.read(pre+n+'.json')) for n in ['skills','definitions','connections']]
 assert all(n in s for n in F+A+I), 'Restored masteries missing'
 assert all(n not in s for n in REMOVED)
 for path in [F,A,I]:
  assert all(s[n]['definition'].startswith('utility_') for n in path)
  assert d[s[path[-1]]['definition']]['required_skills']==7
  neighbors={b if a==path[-1] else a for a,b in c['normal']['bidirectional'] if path[-1] in [a,b]}
  assert neighbors==set(path[:-1]), 'Mastery must require all seven own nodes'
 for a,b in [(F[0],A[0]),(F[0],I[0]),(A[0],I[0])]:
  assert [a,b] in c['exclusive']['bidirectional'] or [b,a] in c['exclusive']['bidirectional']
 for k,total in [('cost',.33),('durability',.16),('xp',.16)]:assert abs(sum(REWARDS.get(n,{}).get(k,0) for n in I)-total)<1e-8
 assert all(not r.get('type','').endswith(':command') for n in F+A+I for r in d[s[n]['definition']]['rewards'])
print('PASS: allocation, mastery requirements, exclusions and exact budgets')

from pathlib import Path
from zipfile import ZipFile
import json,sys
R=Path(__file__).resolve().parents[3];base=R/('tmp/class-follow-up/before' if '--baseline' in sys.argv else 'tmp/class-follow-up/built')
checks=[]
def check(label,actual,expected):
 checks.append(actual==expected);print(('PASS ' if actual==expected else 'FAIL ')+label+': '+str(actual)+' expected '+str(expected))
with ZipFile(next(base.glob('skill_tree-*.jar'))) as z:
 def spell(s):return json.loads(z.read('data/skill_tree_rpgs/spell/'+s+'.json'))
 for s,v in [('paladin_tier_1_passive_1',5),('paladin_tier_3_passive_1',80),('archer_tier_1_passive_1',6),('priest_tier_2_passive_2',12),('fire_tier_2_passive_2',5)]:
  d=spell(s);check(s+' cooldown',d['cost']['cooldown']['duration'],v);check(s+' haste',d['cost']['cooldown']['haste_affected'],True)
 check('Supercharge damage',spell('archer_tier_3_passive_1')['arrow_perks']['damage_multiplier'],1.35)
 d=spell('priest_tier_2_passive_1');check('Fade no radius',d['range'],0)
 check('Fade invisibility duration',[i['action']['status_effect']['duration'] for i in d['impacts'] if i['action']['type']=='STATUS_EFFECT'],[1,1])
assert all(checks),str(sum(checks))+'/'+str(len(checks))+' checks passed'

from pathlib import Path
from zipfile import ZipFile
import json,sys
root=Path(sys.argv[1]);errors=[]
def check(ok,label):
 print(('PASS ' if ok else 'FAIL ')+label)
 if not ok:errors.append(label)
with ZipFile(next(root.glob('paladins-*'))) as z:
 def spell(n):return json.loads(z.read('data/paladins/spell/'+n+'.json'))
 banner=spell('battle_banner');p=spell('immolation')
 check(banner['cost']['cooldown']['duration']==100,'Banner100s cooldown')
 check(banner.get('deliver',{}).get('custom',{}).get('handler')=='paladin_pack:banner','Banner follows caster through custom lifecycle')
 check(p['active']['cast'].get('duration')==30,'Purgatory30s channel')
 check(p['active']['cast'].get('haste_affected') is False,'Purgatory timeline fixed under haste')
 check(p['cost']['cooldown']['duration']==2,'Purgatory2s cooldown')
 lang=json.loads(z.read('assets/paladins/lang/en_us.json'));check(lang.get('spell.paladins.immolation.name')=='Purgatory','Purgatory visible rename')
with ZipFile(next(root.glob('skill_tree-*'))) as z:
 d=json.loads(z.read('data/skill_tree_rpgs/spell/paladin_tier_4_spell_2_modifier_2.json'))
 check('deliver' not in d and d.get('type')=='MODIFIER','Final Resolute removes old Consecration cloud')
 lang=json.loads(z.read('assets/skill_tree_rpgs/lang/en_us.json'));check(lang.get('spell.skill_tree_rpgs.paladin_tier_3_passive_2.name')=='Reinforced','Reinforced visible rename')
assert not errors,str(errors)

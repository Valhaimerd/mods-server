from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
from zipfile import ZipFile
import json
W=R/'tmp/combat-followup-phase-one'
with ZipFile(next((W/'built').glob('skill_tree-*'))) as z:skills=json.loads(z.read('data/skill_tree_rpgs/puffish_skills/categories/class_skills/skills.json'))
root=next(k for k,v in skills.items() if v.get('definition')=='rogue_root')
(W/'ClassClientProbe.java').write_text((H/'ClassClientProbe.java').read_text().replace('ASSASSIN_ROOT_SKILL',root))
s=(R/'docs/patches/combat-phase-two/run_client.py').read_text().replace("W=R/'tmp/combat-phase-two'","W=R/'tmp/combat-followup-phase-one'").replace("(W/'classpath.txt').read_text()","(W/'build-classpath.txt').read_text()").replace("str(H/'SpatialClientProbe.java')","str(W/'ClassClientProbe.java')")
exec(compile(s,str(__file__),'exec'))

from pathlib import Path
from zipfile import ZipFile
import json,hashlib,subprocess,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pvp-trap-followup';before=WORK/'before';before.mkdir(parents=True,exist_ok=True);out=WORK/'built';out.mkdir(exist_ok=True)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');asm=next((ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'))
subprocess.run([str(java/'javac.exe'),'--release','25','-cp',str(asm),'-d',str(WORK),str(HERE/'PatchTrap.java')],check=True)
records=[]
for pattern in ['spell_engine-*.jar','skill_tree-*.jar','rogues-*.jar']:
 active=next((ROOT/'mods').glob(pattern));saved=before/active.name
 if not saved.exists():shutil.copy2(active,saved)
 with ZipFile(saved) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 if active.name.startswith('spell_engine'):
  n='net/spell_engine/internals/RogueRules.class';src=WORK/'RogueRules.class';dst=WORK/'patched.class';src.write_bytes(files[n]);subprocess.run([str(java/'java.exe'),'-cp',str(WORK)+';'+str(asm),'PatchTrap',str(src),str(dst)],check=True);files[n]=dst.read_bytes()
 if active.name.startswith('rogues'):
  n='data/rogues/spell/bear_trap.json';o=json.loads(files[n]);assert o['cost']['cooldown']['duration']==4 and o['cost']['cooldown']['haste_affected'];assert o['impacts'][0]['action']['damage']['spell_power_coefficient']==.4
 if active.name.startswith('skill_tree'):
  n='assets/skill_tree_rpgs/lang/en_us.json';o=json.loads(files[n]);key='spell.skill_tree_rpgs.rogue_tier_3_spell_2_root.description';assert '75%%' in o[key];o[key]=o[key].replace('75%%','60%%');files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 with ZipFile(out/active.name,'w') as z:
  for i in infos:z.writestr(i,files[i.filename])
 records.append({'jar':active.name,'before_sha256':hashlib.sha256(saved.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/active.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built exact Improved Trap overlay; base trap remains 40%, cooldown 4s before haste')

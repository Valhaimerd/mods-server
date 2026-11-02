from pathlib import Path
from zipfile import ZipFile
import shutil,subprocess,json,hashlib
R=Path(__file__).resolve().parents[3];W=R/'tmp/combat-followup-phase-one';W.mkdir(exist_ok=True)
J='C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/java.exe'
records=[]
for pattern in ['rogues-*.jar','skill_tree-*.jar','spell_engine-*.jar','adventurerskills-*.jar','bettercombat-*.jar','combat_roll-*.jar']:
 p=next((R/'mods').glob(pattern));b=W/'before'/p.name
 if not b.exists():shutil.copy2(p,b)
 records.append({'jar':p.name,'sha256':hashlib.sha256(b.read_bytes()).hexdigest()})
 with ZipFile(b) as z:
  for n in z.namelist():
   if n.endswith('.class') and (any(x in n for x in ['WarriorRules','WarriorTierTwo','WarriorUltimates','WarriorBalance','RogueRules','RogueTierThree','RogueDual','PlayerEntityMixin','ClientPlayerEntityMixin','ClientNetworkHandlerMixin','RollManager']) or ('bettercombat' in n and 'mixin' in n) or ('combat_roll' in n and 'mixin' in n)):
    dest=W/'inspect'/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(n))
   if n.endswith('.json') and (n.startswith('data/rogues/spell/') or n.startswith('data/skill_tree_rpgs/spell/warrior_') or ('tree' in n and ('warrior' in n or 'rogue' in n))):
    dest=W/'resources'/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(n))
(W/'baseline.json').write_text(json.dumps(records,indent=2))
subprocess.run([J,'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(W/'before'/next((W/'before').glob('bettercombat-*.jar')).name),'--outputdir',str(W/'decompiled-bettercombat'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,check=True)
for n in ['WarriorRules','WarriorTierTwo','WarriorUltimates','WarriorBalance','RogueRules']:
 subprocess.run([J,'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(W/'inspect/net/spell_engine/entity'/f'{n}.class'),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL,check=True)
print('snapshotted',len(records),'archives; extracted current runtime and resources')

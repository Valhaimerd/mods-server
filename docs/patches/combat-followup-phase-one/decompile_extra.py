exec((__import__('pathlib').Path(__file__).with_name('signatures.py')).read_text().split('for name in')[0])
with ZipFile(next((W/'before').glob('spell_engine-*'))) as z:
 for n in ['net/spell_engine/internals/delivery/melee/Melee.class','net/spell_engine/internals/FollowUpRules.class','net/spell_engine/internals/SpellExecution.class']:
  p=W/'inspect'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(n))
  subprocess.run([str(J/'java.exe'),'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(p),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
with ZipFile(next((W/'before').glob('combat_roll-*'))) as z:
 for n in ['net/combat_roll/network/ServerNetwork.class','net/combat_roll/internals/RollManager.class']:
  p=W/'inspect'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(n));subprocess.run([str(J/'java.exe'),'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(p),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
with ZipFile(next((W/'before').glob('skill_tree-*'))) as z:
 n='net/skill_tree_rpgs/effect/SkillEffects.class';p=W/'inspect'/n;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(z.read(n));subprocess.run([str(J/'java.exe'),'-jar',str(R/'tmp/bows-balance/cfr.jar'),str(p),'--outputdir',str(W/'decompiled'),'--silent','true'],stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)

from pathlib import Path
from zipfile import ZipFile
import copy, hashlib, json, subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/priest-ward-tuning'
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('*.jar'))
tree=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm-tree').rglob('*.jar'))
C=W/'classes';C.mkdir(exist_ok=True)
cp=';'.join(map(str,[*(W/'before').glob('*.jar'),asm,tree]))+';'+(W/'classpath.txt').read_text().strip()
args=['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(C),str(H/'PatchPriestWard.java'),str(H/'ArcaneWardRange.java')]
argfile=W/'compile.args';argfile.write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(J/'javac.exe'),'@'+str(argfile)],check=True)
records=[]
for src in (W/'before').glob('*.jar'):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(n,fn):
  d=json.loads(files[n]);fn(d);files[n]=(json.dumps(d,ensure_ascii=False,indent=2)+'\n').encode()
 def spell(ns,id,fn):edit(f'data/{ns}/spell/{id}.json',fn)
 def lang(ns,values):edit(f'assets/{ns}/lang/en_us.json',lambda d:d.update(values))
 if src.name.startswith('paladins-'):
  spell('paladins','holy_beam',lambda d:d['cost']['cooldown'].update(duration=12,proportional=False))
  spell('paladins','penance',lambda d:d['impacts'][0]['action']['damage'].update(spell_power_coefficient=.4))
  spell('paladins','circle_of_healing',lambda d:d['impacts'][0]['action']['heal'].update(spell_power_coefficient=.65))
  lang('paladins',{'spell.paladins.circle_of_healing.description':'Heal yourself and nearby allies for 65%% P.','spell.paladins.penance.description':'Fire 10 bolts that burst within 4 blocks for 40%% P. Hits build absorption for you and nearby allies, up to 10 HP, lasting 3s.'})
 if src.name.startswith('skill_tree-'):
  spell('skill_tree_rpgs','priest_tier_3_passive_2',lambda d:d['impacts'][0]['action']['damage'].update(spell_power_coefficient=.4,knockback=1.0))
  lang('skill_tree_rpgs',{'spell.skill_tree_rpgs.priest_tier_3_spell_2_root.description':'Penance blasts deal 50%% P. Absorption lasts 1s longer.','spell.skill_tree_rpgs.priest_tier_3_spell_1_root.description':'Circle of Healing heals for 90%% P.','spell.skill_tree_rpgs.priest_tier_3_passive_2.description':'Critical spells ready 5 celestial orbs for 15s. When you or a nearby ally is hit, an orb retaliates for 40%% P with strong knockback.','spell.skill_tree_rpgs.arcane_tier_3_passive_2.description':'Gain an absorption shield. If your attacker is within 4 blocks, blink 4 blocks away when a safe landing exists.'})
 if src.name.startswith('spell_engine-'):
  for cls,mode in [('PriestRules','cooldown'),('PriestTuning','penance'),('FinalRules','circle'),('MageFollowup','ward')]:
   key=f'net/spell_engine/internals/{cls}.class';a=W/'input.class';b=W/'output.class';a.write_bytes(files[key])
   arguments=['-cp',str(C)+';'+cp,'PatchPriestWard',str(a),str(b),mode]
   argfile=W/'patch.args';argfile.write_text('\n'.join('"'+v.replace('\\','/')+'"' for v in arguments))
   subprocess.run([str(J/'java.exe'),'@'+str(argfile)],check=True);files[key]=b.read_bytes()
  files['net/spell_engine/internals/ArcaneWardRange.class']=(C/'net/spell_engine/internals/ArcaneWardRange.class').read_bytes()
 out=W/'built'/src.name
 with ZipFile(out,'w') as z:
  for info in infos:z.writestr(copy.copy(info),files[info.filename])
  for key in files.keys()-old.keys():z.writestr(key,files[key])
 sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 records.append({'jar':src.name,'before':sha(src),'after':sha(out),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys())})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Built three focused candidates from current installed JARs; production unchanged.')

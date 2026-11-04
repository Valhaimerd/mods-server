from pathlib import Path
from zipfile import ZipFile
import copy,hashlib,json,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/penance-specializations'
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin');C=W/'classes';C.mkdir(exist_ok=True)
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('*.jar'));cp=';'.join(map(str,[C,asm,tree]))
subprocess.run([str(J/'javac.exe'),'--release','25','-cp',cp,'-d',str(C),str(H/'PatchHysteria.java')],check=True)
records=[]
for src in (W/'before').glob('*.jar'):
 with ZipFile(src) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 if src.name.startswith('spell_engine-'):
  key='net/spell_engine/internals/PriestRules.class';a=W/'input.class';b=W/'output.class';a.write_bytes(files[key]);subprocess.run([str(J/'java.exe'),'-cp',cp,'PatchHysteria',str(a),str(b)],check=True);files[key]=b.read_bytes()
 if src.name.startswith('skill_tree-'):
  key='assets/skill_tree_rpgs/lang/en_us.json';lang=json.loads(files[key]);lang['spell.skill_tree_rpgs.priest_tier_3_spell_2_modifier_1.description']='Penance hits grant you and nearby allies 1.5%% attack speed, draw speed and spell haste per stack, up to 15%%, for 4s.';lang['spell.skill_tree_rpgs.priest_tier_3_spell_2_modifier_2.description']='Penance blasts make enemies take 1.5%% more damage per stack, up to 15%%, for 4s.';files[key]=(json.dumps(lang,ensure_ascii=False,indent=2)+'\n').encode()
 out=W/'built'/src.name
 with ZipFile(out,'w') as z:
  for info in infos:z.writestr(copy.copy(info),files[info.filename])
 sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 records.append({'jar':src.name,'before':sha(src),'after':sha(out),'changed':[n for n in old if old[n]!=files[n]]})
(H/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Built Hysteria and Chastise candidates.')

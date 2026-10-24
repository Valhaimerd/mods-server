"""Apply the confirmed Tenacity threshold/lifetime after the description overlay."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/tenacity-followup';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
before=WORK/'before';before.mkdir(exist_ok=True)
names=['skill_tree-neoforge-1.6.1+26.2.jar','spell_engine-neoforge-1.10.5+26.2.jar']
for name in names:
 if not (before/name).exists():shutil.copy2(ROOT/'mods'/name,before/name)
source=(ROOT/'docs/patches/warrior-tier-three/WarriorUltimates.java').read_text().replace('TENACITY_TICKS=100','TENACITY_TICKS=240').replace('ratio>.4+.000001','ratio>.3+.000001')
(WORK/'WarriorUltimates.java').write_text(source)
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in before.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
classes=WORK/'classes';classes.mkdir(exist_ok=True);java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(WORK/'WarriorUltimates.java')]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
records=[]
for name in names:
 with ZipFile(before/name) as src,ZipFile(OUT/name,'w') as dst:
  changed=[]
  for entry in src.infolist():
   old=data=src.read(entry)
   if entry.filename=='net/spell_engine/entity/WarriorUltimates.class':data=(classes/entry.filename).read_bytes()
   if entry.filename=='assets/skill_tree_rpgs/lang/en_us.json':
    o=json.loads(data);o['spell.skill_tree_rpgs.warrior_tier_3_passive_2.description']='At 1-30%% health, gain an absorption shield for 12 seconds. Shield strength is 6 HP plus 30%% of melee power, rounded to the nearest whole HP. Stuns enemies within 5 blocks for 2 seconds. Base cooldown: 25 seconds, reduced by haste.'
    o['spell.skill_tree_rpgs.arcane_tier_2_spell_1_modifier_1.description']='Arcane Missiles fires 1 additional missile with every third channel hit, starting with the first. A full channel fires 4 extra missiles.'
    data=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
   if data!=old:changed.append(entry.filename)
   dst.writestr(entry,data)
 records.append({'jar':name,'before_sha256':hashlib.sha256((before/name).read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/name).read_bytes()).hexdigest(),'changed':changed})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n');print('Built Tenacity 30% / 12-second candidates')

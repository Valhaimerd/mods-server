from pathlib import Path
from zipfile import ZipFile
import json,copy,hashlib,subprocess,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/wolf-armor-balance';BEFORE=WORK/'before';OUT=WORK/'built';OUT.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
inputs=list(BEFORE.glob('*.jar'));base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(map(str,inputs))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
libs=ROOT/'tmp/nameless-balance/server/libraries/org/ow2/asm';asm=next((libs/'asm').rglob('asm-*.jar'));tree=next((libs/'asm-tree').rglob('asm-tree-*.jar'));classes=WORK/'classes';classes.mkdir(exist_ok=True)
sources=['WolfArmorBalance.java','MixinWolf.java','IronWolfArmorNeoforge.java','WolfArmorProgress.java','PatchWolf.java']
args=['--release','25','-proc:none','-cp',cp+';'+str(asm)+';'+str(tree),'-d',str(classes),*[str(HERE/s) for s in sources]]
(WORK/'compile.args').write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
def patch(data,mode):
 (WORK/'in.class').write_bytes(data);subprocess.run([str(JAVA/'java.exe'),'-cp',';'.join(map(str,[classes,asm,tree])),'PatchWolf',str(WORK/'in.class'),str(WORK/'out.class'),mode],check=True);return (WORK/'out.class').read_bytes()
records=[]
for source in inputs:
 with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
 files=dict(old)
 def edit(n,fn):
  o=json.loads(files[n]);before=copy.deepcopy(o);fn(o)
  if o!=before:files[n]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
 if source.name.startswith('IronWolf'):
  for n in ['net/darkhax/ironwolfarmor/common/WolfArmorBalance.class','net/darkhax/ironwolfarmor/common/mixin/MixinWolf.class','net/darkhax/ironwolfarmor/neoforge/IronWolfArmorNeoforge.class']:files[n]=(classes/n).read_bytes()
  edit('assets/ironwolfarmor/lang/en_us.json',lambda o:o.update({'tooltip.ironwolfarmor.damage_reduction':'%s%% Damage Reduction'}))
 if source.name.startswith('orb_of'):
  n='net/valhaimerd/orboblivioncompat/SkillOverhaul.class';files[n]=patch(files[n],'pet')
  def pet(o):
   for v in o.values():
    if 'res' in v:v.pop('res');v['res_pct']=.03
   o['iggdp0a33bljw4kl']['res_pct']=.05;o['8wjzxjnmc1gr7dvq']['res_pct']=.10
  edit('skill-overhaul-pets.json',pet)
 if source.name.startswith('adventurerskills'):
  def descriptions(o):
   o['pet_res']['title']='+3% Pet Resistance'
   o['packleader']['description']['text']='+20% Pet Damage\n+15% Pet Speed\n+5% Pet Resistance'
   o['guardian_stead']['description']['text']='+10 Pet Hearts\n+10% Pet Resistance\n+30% Pet Knockback Resistance'
  edit('data/adventurer_skills/puffish_skills/categories/adventurer_skills/definitions.json',descriptions)
 if source.name.startswith('penchant-'):
  # Dedicated tags avoid enabling Protection/Mending or other enchants as a side effect.
  for tag,base_tag in [('items','durability'),('armor','armor')]:
   files['data/penchant/tags/item/unbreaking_'+tag+'.json']=(json.dumps({'replace':False,'values':['#minecraft:enchantable/'+base_tag,'#minecraft:enchantable/wolf_armor','minecraft:wolf_armor']},indent=2)+'\n').encode()
  for n in ['data/minecraft/enchantment/unbreaking.json','resourcepacks/durability_rework/data/minecraft/enchantment/unbreaking.json']:
   o=json.loads(files[n]);o['supported_items']='#penchant:unbreaking_items'
   files[n]=(json.dumps(o,indent=2)+'\n').replace('#minecraft:enchantable/armor','#penchant:unbreaking_armor').encode()
  n='archives/tater/penchant/mixin/leveling/ItemStackMixin.class';files[n]=patch(files[n],'progress')
  n='archives/tater/penchant/util/WolfArmorProgress.class';files[n]=(classes/n).read_bytes()
 target=OUT/source.name
 with ZipFile(target,'w') as z:
  for info in infos:z.writestr(info,files.pop(info.filename))
  for n,data in files.items():z.writestr(n,data)
 with ZipFile(target) as z:
  records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256(target.read_bytes()).hexdigest(),'changed':[n for n in old if z.read(n)!=old[n]],'added':sorted(set(z.namelist())-set(old))})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n',encoding='utf-8');print('Built',len(records),'exact overlays')

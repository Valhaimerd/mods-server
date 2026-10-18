"""Rebuild skill data from preserved, hash-checked originals; outputs under tmp only."""
from pathlib import Path
import copy, hashlib, itertools, json, zipfile
ROOT=Path(__file__).resolve().parents[3]; WORK=ROOT/'tmp/skill-overhaul'
ADV='adventurerskills-EN-1.3-26.2-neoforge-fixed.jar'; RPG='skill_tree-neoforge-1.6.1+26.2.jar'
HASH={ADV:'8bb7e7eb75229e19a70f58d2a93a65f36a51b757203635fd125f1241a97caaa4',RPG:'35c16e744b198911d203ac59d3a558d1030de5d100e7e688459f157f3425344a'}
def read(name):
 p=WORK/('original-'+name);assert hashlib.sha256(p.read_bytes()).hexdigest()==HASH[name]
 with zipfile.ZipFile(p) as z:return {n:z.read(n) for n in z.namelist()}
def put(files,path,obj):files[path]=(json.dumps(obj,ensure_ascii=False,indent=2)+'\n').encode()
def write(name,files):
 with zipfile.ZipFile(WORK/name,'w',zipfile.ZIP_DEFLATED) as z:
  for n,b in files.items():z.writestr(n,b)
def attr(name,value,op='multiply_total'):
 return {'type':'puffish_skills:attribute','data':{'attribute':name,'value':value,'operation':op}}
def scaled(x):
 if isinstance(x,str):return f'({x}) * 1.35'
 if isinstance(x,(int,float)):return x*1.35
 if isinstance(x,list):return [{**v,'expression':scaled(v['expression'])} for v in x]
 raise ValueError(x)
def xp(files,b):
 e=json.loads(files[b+'experience.json'])
 for source in e['sources']:
  if 'experience' in source.get('data',{}):source['data']['experience']=scaled(source['data']['experience'])
 put(files,b+'experience.json',e)

f=read(ADV);b='data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
d=json.loads(f[b+'definitions.json']);s=json.loads(f[b+'skills.json']);c=json.loads(f[b+'connections.json'])
for key,v in d.items():
 v['rewards']=[r for r in v.get('rewards',[]) if not (r['type']=='puffish_skills:command' and 'give ' in r['data'].get('command',''))]
 if 'description' in v and isinstance(v['description'],list):
  for text in v['description']:
   if 'text' in text:text['text']='\n'.join(l for l in text['text'].splitlines() if not l.startswith('\u00a7e'))
 if key in ['melee_damage+3%','ranged_damage+3%']:
  amount=.02 if key=='melee_damage+3%' else .03
  v['title']=v['title'].replace('3%',str(round(amount*100))+'%');v['rewards'][0]['data']['value']=amount
 if key=='attack_speed+5%':v['title']=v['title'].replace('5%','3%');v['rewards'][0]['data']['value']=.03
 if key=='anvil_repair_cost-5%':v['title']='-20% Anvil Repair Cost';v['rewards']=[attr('puffish_attributes:repair_cost',-.2)]
def define(key,title,rewards,icon='minecraft:book',description=None):
 d[key]={'title':title,'size':1,'icon':{'type':'item','data':{'item':icon}},'frame':{'type':'advancement','data':{'frame':'challenge' if description else 'task'}},'rewards':rewards}
 if description:d[key]['description']={'text':description}
def node(id,key):assert id in s;s[id]['definition']=key
schools=['arcane','fire','frost']
define('caster_power','+4% Arcane, Fire and Frost Power',[attr('spell_power:'+x,.04) for x in schools],'minecraft:blaze_powder')
define('caster_haste','+3% Spell Haste',[attr('spell_power:haste',.03)],'minecraft:clock')
# The compatibility bridge grants fixed critical-chance points after innate/gear scaling.
define('caster_critical','+2.5% Spell Critical Chance',[],'minecraft:amethyst_shard')
define('draw_speed','+3% Draw Speed (Bow and Crossbow)',[attr('ranged_weapon:haste',.03)],'minecraft:crossbow')
define('projectile_both','+25% Bow and Crossbow Projectile Speed',[attr('puffish_attributes:bow_projectile_speed',.25),attr('puffish_attributes:crossbow_projectile_speed',.25)],'minecraft:arrow')
for id,key in {'3gpel3ejgsuvozax':'caster_power','v3lnj0ubt40kezxm':'caster_haste','ro3sae58m8jzn4yz':'caster_critical','m1aqebilvun2ycc7':'caster_power','qosdkbl0x44scigj':'caster_power','0oz1hx0q08v2i8j5':'caster_power','k6s01bg1bgnsqrew':'caster_haste','gs6jmtskmyhpbcnu':'caster_critical','8qdaale4k86t2ntn':'draw_speed','02did3uvilfob8c2':'draw_speed','ui0gusk2hniqwghe':'projectile_both','vxmb1u84wzfwpuq6':'projectile_both','k5g9ddvqzlzkxv9f':'ranged_damage+3%'}.items():node(id,key)
define('magistrate','Magistrate',[*[attr('spell_power:'+x,.12) for x in schools],attr('spell_power:haste',.05)],'minecraft:ender_eye','+12% Arcane/Fire/Frost Power\n+10% Spell Critical Chance\n+5% Spell Haste')
node('zdfpatqypd5nnqx8','magistrate')
for key,remove in [('martial_prowess','puffish_attributes:resistance'),('hunter','puffish_attributes:ranged_resistance')]:
 v=d[key];v['rewards']=[r for r in v['rewards'] if r['data'].get('attribute')!=remove]
 for r in v['rewards']:
  a=r['data'].get('attribute','')
  if a in ['puffish_attributes:melee_damage','puffish_attributes:ranged_damage']:r['data']['value']=.05
  if a=='attack_speed':r['data']['value']=.05
  if a=='puffish_attributes:armor_shred':r['data']['value']=.1
  if a=='luck':r['data']['value']=1
  if a in ['movement_speed','puffish_attributes:jump']:r['data']['value']=.05
 v['description']={'text':'+5% Melee Damage\n+10% Armor Penetration\n+5% Attack Speed' if key=='martial_prowess' else '+5% Ranged Damage\n+1 Luck\n+5% Movement Speed\n+5% Jump Strength'}
# Martial ordinary route: damage / speed / damage / speed / damage.
node('f8rbbpxi0myhi3cg','attack_speed+5%')
define('ranged_penetration','+5% Armor Penetration',[attr('puffish_attributes:armor_shred',.05)],'minecraft:flint')
node('vxmb1u84wzfwpuq6','ranged_penetration')
d['hunter']['rewards'] += [attr('puffish_attributes:armor_shred',.1),attr('puffish_attributes:bow_projectile_speed',.25),attr('puffish_attributes:crossbow_projectile_speed',.25)]
d['hunter']['description']['text'] += '\n+10% Armor Penetration\n+25% Bow and Crossbow Projectile Speed'
mid=['zhraasg53ibsn3dk','6i5us24w9a9ar7v1','rvzvos2233a9pi4g','rr6e15vhxj8hdyt4','owk0eiqwgomn6jwb']
for id,key in zip(mid,['health+2','healing+3%','health+2','healing+3%','health+2']):node(id,key)
for id,host,key in [('new_colossus_health',mid[-1],'health+2'),('new_colossus_healing',mid[-2],'healing+3%')]:
 s[id]={'definition':key,'x':s[host]['x'],'y':-50,'root':False};c['normal']['bidirectional'].append([host,id])
define('colossus','Colossus',[attr('minecraft:max_health',12,'addition'),attr('puffish_attributes:healing',.06)],'minecraft:totem_of_undying','+6 Hearts\n+6% Healing')
top=['xzau8wf7cmkahump','arcimlg0c3igwe0e','-7_-4','d67mbc3a2pg8781z','4zj68e0t9ythniot','2wtk419203if3s6n','bxm9laus4f3lfqjk']
for id,key in zip(top,['magic_resistance+2%','ranged_resistance+2%']*3+['magic_resistance+2%']):node(id,key)
define('aegis','Aegis',[attr('puffish_attributes:magic_resistance',.15),attr('puffish_attributes:ranged_resistance',.1),attr('minecraft:movement_speed',.06)],'minecraft:golden_apple','+15% Magic Resistance\n+10% Ranged Resistance\n+6% Movement Speed');node('6tinz5atn533lzo9','aegis')
bottom=['5h6pmokx7ro45qtw','sgg10fjuiiqlos8w','-7_4','fxxc8k4yljxkbepz','00zagtopdfilmaw7','8meyqbog5d7qmyzg','bvasjpryldz0yhpx','v2pojgmi36k5fuxe','vgrpua0glqz5sem5']
for id,key in zip(bottom,['resistance+1%','armor+1','resistance+1%','ranged_resistance+2%','armor+1','resistance+1%','ranged_resistance+2%','armor+1','ranged_resistance+2%']):node(id,key)
define('bulwark','Bulwark',[attr('puffish_attributes:resistance',.02),attr('puffish_attributes:ranged_resistance',.04),attr('minecraft:armor',2,'addition'),attr('minecraft:knockback_resistance',.2,'addition')],'minecraft:netherite_chestplate','+2% Resistance\n+4% Ranged Resistance\n+2 Armor\n+20% Knockback Resistance')
for id,key in {'2pj0fbogctx589t3':'orbs_experience+25%','yts2dfnkpz3xqziu':'movement_speed+3%','gk57oho18h66ghxe':'anvil_repair_cost-5%','vaios6drja2eapyg':'fortune+0.1'}.items():node(id,key)
removed=['i0j6pbbluuqne53e','s02hkhd2ha87ji1n','qxcrnvfslhwdg5ip','a036s916b2cp7hb2','i2oo5vl75gyj6b3d','usqd6s5h8ypx5gud','ijsno4eog2w0unyr','bysc5yiqm9ni1mkf']
for id in removed:del s[id]
for kind in c.values():
 for direction,edges in kind.items():kind[direction]=[[a,b] for a,b in edges if a in s and b in s]
c['exclusive']={'bidirectional':[]}
for group in [['3gpel3ejgsuvozax','ei4d3ri7bngns8lj',next(k for k,v in s.items() if v['x']==136 and v['y']==-68)],['zhraasg53ibsn3dk','xzau8wf7cmkahump','5h6pmokx7ro45qtw'],['qp2x45h0nbr233e1','828iwet0erdpwynl']]:
 c['exclusive']['bidirectional'].extend(map(list,itertools.combinations(group,2)))
d['root_versatile']['title']='Companion';d['root_versatile']['description']={'text':'Strengthen your pets to fight harder and survive longer.'};d['root_versatile']['icon']={'type':'item','data':{'item':'minecraft:bone'}}
pet={}
pet_specs={'damage':('Pet Damage +1',{'damage':1},'minecraft:bone'),'speed':('+3% Pet Speed',{'speed':.03},'minecraft:feather'),'knock':('+0.1 Pet Attack Knockback',{'knock':.1},'minecraft:piston'),'res':('+1 Pet Resistance',{'res':1},'minecraft:shield'),'kr':('+10% Pet Knockback Resistance',{'kr':.1},'minecraft:iron_boots'),'hp':('+2 Pet Hearts',{'hp':4},'minecraft:golden_apple')}
for key,(title,effect,icon) in pet_specs.items():define('pet_'+key,title,[],icon)
def pets(ids,types):
 for id,key in zip(ids,types):node(id,'pet_'+key);pet[id]=pet_specs[key][1]
pets(['15trz7ezr8z7kcp8'],['damage'])
pets(['qp2x45h0nbr233e1','e7fyzt06vz8mu5iv','5grntvcsxgm61gbq','vyt2xj8si9zbat4x','z6awauxhc53s0bhl','5frbav4zdc1hwquo','xz6a8myofnhkv0e0','o3fd9bzo883nqd57','629rr01c70gc90kj','kbheklivvbq2aaj8','87cn8ldpzmjljp0q'],['damage','speed','knock','damage','speed','res','knock','speed','damage','kr','hp'])
pets(['828iwet0erdpwynl','00ojkscfhac0ifpm','ha05s7wy4h135376','iswxmklicwgn4xmp','q0ul30ow7ifhtq6j','mk08ei1wfvcssxs6','mxm9kmy816lzgkbt','rbfta22ujrmwzugp','fjtdo0ic4650e43d','6vlgfj2olene3csb','fr0yn9kxau3qimvv'],['hp','res','hp','kr','res','hp','res','hp','kr','damage','speed'])
define('packleader','Packleader',[],'minecraft:bone','+20% Pet Damage\n+15% Pet Speed\n+10% Pet Resistance');node('iggdp0a33bljw4kl','packleader');pet['iggdp0a33bljw4kl']={'damage_pct':.2,'speed':.15,'res_pct':.1}
define('guardian_stead','Guardian Stead',[],'minecraft:diamond_horse_armor','+10 Pet Hearts\n+25% Pet Resistance\n+30% Pet Knockback Resistance');node('8wjzxjnmc1gr7dvq','guardian_stead');pet['8wjzxjnmc1gr7dvq']={'hp':20,'res_pct':.25,'kr':.3}
for key,title in {'martial_prowess':'Martial Prowess','hunter':'Hunter','magistrate':'Magistrate','aegis':'Aegis','colossus':'Colossus','bulwark':'Bulwark','packleader':'Packleader','guardian_stead':'Guardian Stead','root_versatile':'Companion'}.items():
 d[key]['title']={'text':title,'color':'blue'}
# Only emitted definitions can award anything; discard deleted and superseded definitions.
d={k:v for k,v in d.items() if k in {v['definition'] for v in s.values()}}
for n,obj in [('definitions',d),('skills',s),('connections',c)]:put(f,b+n+'.json',obj)
xp(f,b)
for mod,version in [('minecraft','[26.2,26.3)'),('spell_power','[1.6.1,)'),('ranged_weapon_api','[4.0.0,)'),('orb_of_oblivion_puffish_compat','[1.0.2,)')]:
 f['META-INF/neoforge.mods.toml']+=f'\n[[dependencies.adventurer_skills]]\nmodId="{mod}"\ntype="required"\nversionRange="{version}"\nordering="NONE"\nside="BOTH"\n'.encode()
write(ADV,f)
(WORK/'pet-bonuses.json').write_text(json.dumps(pet,indent=2)+'\n')
f=read(RPG)
for cat in ['class_skills','weapon_skills']:
 b=f'data/skill_tree_rpgs/puffish_skills/categories/{cat}/';original=json.loads(f[b+'definitions.json']);d=copy.deepcopy(original)
 for k,v in d.items():
  if k.startswith('paladin'):continue
  for r in v.get('rewards',[]):
   a=r.get('data',{}).get('attribute')
   if a in ['spell_power:arcane','spell_power:fire','spell_power:frost','spell_power:healing'] or (k.startswith('rogue') and a=='minecraft:attack_speed'):r['data']['value']=.04 if a in ['spell_power:arcane','spell_power:fire','spell_power:frost'] else .03
   if a=='minecraft:attack_damage':r['data']['value']=.02
   if a=='ranged_weapon:damage':r['data']['value']=.03
  if any(r.get('data',{}).get('attribute') in ['spell_power:arcane','spell_power:fire','spell_power:frost','spell_power:healing'] for r in v.get('rewards',[])) or k in ['rogue_root','rogue_boost']:
   v['description']={'text':('+4% ' if k.split('_')[0] in schools else '+3% ')+('Attack Speed' if k.startswith('rogue') else 'Healing Power' if k.startswith('priest') else k.split('_')[0].title()+' Spell Power')}
 assert all(v==d[k] for k,v in original.items() if k.startswith('paladin'))
 put(f,b+'definitions.json',d);xp(f,b)
write(RPG,f)
print('Rebuilt Adventurer and RPG skill data; pet mapping:',len(pet),'nodes')

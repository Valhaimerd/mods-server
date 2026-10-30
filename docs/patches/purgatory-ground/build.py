from compile import *
from zipfile import ZipFile
import hashlib,copy
source=next((W/'before').glob('spell_engine-*.jar'));out=W/'built';out.mkdir(exist_ok=True)
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
f=dict(old);key='net/spell_engine/internals/PaladinVisuals.class';f[key]=(C/key).read_bytes()
with ZipFile(next((R/'mods').glob('skill_tree-*.jar'))) as z:node=json.loads(z.read('data/skill_tree_rpgs/spell/fire_tier_3_spell_1_modifier_2.json'))
cloud=copy.deepcopy(node['deliver']['clouds'][0]);cloud['volume']={'radius':6,'area':{'vertical_range_multiplier':.3}};cloud['time_to_live_seconds']=.5;cloud['impact_tick_interval']=2147483647
cloud['spawn']={};cloud['despawn']={};cloud['spawn_ticks']=0;cloud['despawn_ticks']=0
spell={'school':'spell_power:healing','range':6,'tier':0,'type':'ACTIVE','target':{'type':'NONE'},'deliver':{'type':'CLOUD','clouds':[cloud]},'impacts':[],'cost':{'cooldown':{'duration':0}}}
f['data/paladins/spell/purgatory_ground.json']=(json.dumps(spell,indent=2)+'\n').encode()
with ZipFile(out/source.name,'w') as z:
 for info in infos:z.writestr(info,f[info.filename])
 for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
record={'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((out/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if f[n]!=old[n]],'added':sorted(f.keys()-old.keys())}
(H/'artifacts.json').write_text(json.dumps([record],indent=2)+'\n',encoding='utf-8');print('Built pulse-ground visual candidate')

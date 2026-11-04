from pathlib import Path
from zipfile import ZipFile
import json,sys
from data import DEFENSE,RESTORE
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
folder=R/'tmp/class-balance-phases-1-3'/('before' if '--baseline' in sys.argv else 'built')
checks=[];fails=[]
def check(ok,label):(checks if ok else fails).append(label)
def data(pattern,path):
 with ZipFile(next(folder.glob(pattern))) as z:return json.loads(z.read(path))
check('#spell_power:all' not in data('spell_power*','data/minecraft/tags/damage_type/bypasses_armor.json')['values'],'ordinary magic uses armor')
for id,c in RESTORE.items():
 d=data('wizards*',f'data/wizards/spell/{id}.json');actual=next(i['action']['damage']['spell_power_coefficient'] for i in d['impacts'] if i['action']['type']=='DAMAGE');check(actual==c,'restored coefficient '+id)
base='data/adventurer_skills/puffish_skills/categories/adventurer_skills/'
nodes=data('adventurer*',base+'skills.json');defs=data('adventurer*',base+'definitions.json');edges=data('adventurer*',base+'connections.json')['normal']['bidirectional']
for path in DEFENSE:
 d=defs[nodes[path[-1]]['definition']];check(d.get('required_skills')==2,'two-branch defense mastery '+path[-1]);check(all([x,path[-1]] in edges or [path[-1],x] in edges for x in path[5:7]),'both defense penultimates connected '+path[-1])
for id,v in [('throw',1.8),('throw_net',1.4)]:check(data('rogues*',f'data/rogues/spell/{id}.json')['deliver']['projectile']['launch_properties']['velocity']==v,id+' speed')
for id in ['last_stand','mortal_strike']:check(data('rogues*',f'data/rogues/spell/{id}.json')['active']['cast']['duration']==4,id+' four-second channel')
check(data('rogues*','data/rogues/spell/shock_powder.json')['cost']['cooldown']['duration']==12,'Shock Powder cooldown')
check('fractured_nullstone' in data('Nameless*','nameless-pack-balance.json')['removed'],'Nullstone retired')
out={'passed':checks,'failed':fails};label='baseline' if '--baseline' in sys.argv else 'candidate';(H/(label+'-resources.json')).write_text(json.dumps(out,indent=2)+'\n')
print(f'{label}: {len(checks)} passed, {len(fails)} failed');print('\n'.join(fails[:8]));sys.exit(0 if (bool(fails) if label=='baseline' else not fails) else 1)

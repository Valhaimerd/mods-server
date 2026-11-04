from pathlib import Path
from zipfile import ZipFile
import json,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;baseline='--baseline' in sys.argv;B=R/'tmp/class-balance-phases-4-6'/('before' if baseline else 'built');checks=[];failed=[]
def get(pat,id):
 ns='wizards' if pat=='wizards' else 'paladins' if pat=='paladins' else 'skill_tree_rpgs'
 with ZipFile(next(B.glob(pat+'-*'))) as z:return json.loads(z.read(f'data/{ns}/spell/{id}.json'))
def check(ok,label):(checks if ok else failed).append(label)
check(get('wizards','arcane_evocation')['active']['cast']['duration']==3,'Evocation three seconds')
check(get('wizards','arcane_barrage_bolt')['deliver']['projectile'].get('launch_properties',{}).get('velocity',1)==.8,'Emitter bolts 0.8x')
check(get('wizards','frost_spikes')['deliver']['type']=='CUSTOM','Targeted spike replaces old rows')
check(get('wizards','frost_lance')['active']['cast']['duration']==.75,'Lance charge .75s')
for id in ['arcane_bolt','arcane_blast']:check(get('wizards',id)['range']==24,id+' range24')
for id in ['heal','holy_shock']:check(get('paladins',id)['range']==24,id+' range24')
for id,speed in [('fireball',1.3),('fire_blast',1.625),('frostbolt',1.56),('frost_shard',1.56)]:check(abs(get('wizards',id)['deliver']['projectile'].get('launch_properties',{}).get('velocity',1)-speed)<.00001,id+' speed')
check(get('skill_tree','arcane_tier_1_passive_1')['cost']['cooldown']['duration']==6,'Fissile cooldown6')
check(get('skill_tree','arcane_tier_3_passive_1')['cost']['cooldown']['duration']==15,'Presence cooldown15')
check(get('skill_tree','frost_tier_1_passive_2')['impacts'][0]['action']['status_effect']['duration']==5,'Near Zero five seconds')
check(not get('skill_tree','frost_tier_3_spell_2_modifier_1')['modifiers'][0].get('power_modifier'),'Colossal crit removed')
label='baseline' if baseline else 'candidate';(H/(label+'-resources.json')).write_text(json.dumps({'passed':checks,'failed':failed},indent=2)+'\n');print(label,len(checks),'passed',len(failed),'failed');sys.exit(0 if (bool(failed) if baseline else not failed) else 1)

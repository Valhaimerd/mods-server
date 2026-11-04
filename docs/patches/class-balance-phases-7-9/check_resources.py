from pathlib import Path
from zipfile import ZipFile
import json,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;baseline='--baseline' in sys.argv;W=R/'tmp/class-balance-phases-7-9'/('before' if baseline else 'built');passed=[];failed=[]
def get(pat,id):
 ns='skill_tree_rpgs' if pat=='skill_tree' else pat
 with ZipFile(next(W.glob(pat+'-*'))) as z:return json.loads(z.read(f'data/{ns}/spell/{id}.json'))
def check(ok,label):(passed if ok else failed).append(label)
for id,value in [('lightwell',60),('barrier',40),('divine_protection',20),('judgement',18),('immolation',16)]:check(get('paladins',id)['cost']['cooldown']['duration']==value,id+' cooldown')
check(get('paladins','circle_of_healing')['impacts'][0]['action']['heal']['spell_power_coefficient']==.6,'Circle60%')
check(not get('paladins','levitate')['active']['cast']['haste_affected'],'Ascension fixed timing')
check(all(i['action']['status_effect']['duration']==2 for i in get('skill_tree','priest_tier_2_passive_1')['impacts']),'Fade2s')
check(get('skill_tree','paladin_tier_1_passive_2')['impacts'][0]['action']['status_effect']['amplifier_cap']==4,'Guardian five stacks')
for id,value in [('archer_tier_1_passive_1',4),('archer_tier_3_passive_2',30),('archer_tier_2_passive_2',8)]:check(get('skill_tree',id)['cost']['cooldown']['duration']==value,id+' cooldown')
check(get('archers','entangling_roots')['cost']['cooldown']['duration']==8,'Roots8s')
check(get('archers','entangling_roots')['deliver']['clouds'][0]['time_to_live_seconds']==4.5,'Roots4.5s')
check(any(i['action'].get('status_effect',{}).get('effect_id')=='skill_tree_rpgs:challenged' for i in get('archers','entangling_roots')['impacts']),'Roots uses existing roll-lock effect')
check(get('archers','magic_arrow')['deliver']['projectile']['launch_properties']['velocity']==1.95,'Magic Arrow1.95')
check(get('archers','barrage')['arrow_perks']['damage_multiplier']==.7,'Barrage70% normal weapon shot')
label='baseline' if baseline else 'candidate';(H/(label+'-resources.json')).write_text(json.dumps({'passed':passed,'failed':failed},indent=2)+'\n');print(label,len(passed),'passed',len(failed),'failed');sys.exit(0 if (bool(failed) if baseline else not failed) else 1)

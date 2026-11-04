from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/priest-ward-tuning'
baseline='--baseline' in sys.argv;folder='before' if baseline else 'built'
def data(pattern,path):
 with ZipFile(next((W/folder).glob(pattern))) as z:return json.loads(z.read(path))
checks=[]
def check(ok,label):
 checks.append({'pass':bool(ok),'check':label})
holy=data('paladins*','data/paladins/spell/holy_beam.json');penance=data('paladins*','data/paladins/spell/penance.json');circle=data('paladins*','data/paladins/spell/circle_of_healing.json');orb=data('skill_tree*','data/skill_tree_rpgs/spell/priest_tier_3_passive_2.json')
check(holy['cost']['cooldown']['duration']==12 and not holy['cost']['cooldown']['proportional'] and holy['cost']['cooldown']['haste_affected'],'Holy Light flat12s haste-aware metadata')
check(penance['impacts'][0]['action']['damage']['spell_power_coefficient']==.4,'Penance40% base')
check(circle['impacts'][0]['action']['heal']['spell_power_coefficient']==.65,'Circle65% base')
check(orb['impacts'][0]['action']['damage']=={'spell_power_coefficient':.4,'knockback':1.0},'Orb40% and strong knockback')
if not baseline:
 sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
 for row in json.loads((H/'artifacts.json').read_text()):
  check(sha(W/'built'/row['jar'])==row['after'],'candidate hash '+row['jar'])
  with ZipFile(W/'before'/row['jar']) as old,ZipFile(W/'built'/row['jar']) as new:
   check(new.testzip() is None and len(new.namelist())==len(set(new.namelist())),'archive integrity '+row['jar'])
   check(set(old.namelist()).issubset(new.namelist()),'no removed IDs '+row['jar'])
   check({n for n in old.namelist() if old.read(n)!=new.read(n)}==set(row['changed']),'exact changed members '+row['jar'])
   check(set(new.namelist())-set(old.namelist())==set(row['added']),'exact additions '+row['jar'])
   check(old.read('META-INF/neoforge.mods.toml')==new.read('META-INF/neoforge.mods.toml'),'unchanged version/dependencies '+row['jar'])
   if row['jar'].startswith('paladins-'):check(old.read('data/paladins/spell/heal.json')==new.read('data/paladins/spell/heal.json'),'Holy Wand dual-purpose fix preserved')
   if row['jar'].startswith('spell_engine-'):
    for cls in ['FinalPurelight','FinalPriest','FinalDome','FinalDefenses','SummonRules','MageSummons']:
     key='net/spell_engine/internals/'+cls+'.class';check(old.read(key)==new.read(key),'preserved '+cls)
  if '--installed' in sys.argv:
   tested=json.loads((H/'client-tested.json').read_text());check(tested[row['jar']]==row['after'],'exact runtime-tested bytes')
   for folder in ['mods','mod fixing/1 fixed packs']:check(sha(R/folder/row['jar'])==row['after'],'installed '+folder+'/'+row['jar'])
label='baseline' if baseline else 'verification'
(H/(label+'.json')).write_text(json.dumps(checks,indent=2)+'\n')
failed=[c['check'] for c in checks if not c['pass']];print(len(checks)-len(failed),'passed;',len(failed),'failed');print('\n'.join(failed));sys.exit(1 if failed else 0)

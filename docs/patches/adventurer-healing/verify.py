from pathlib import Path
from zipfile import ZipFile
import copy,hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/adventurer-healing'
r=json.loads((HERE/'artifacts.json').read_text());installed='--installed' in sys.argv
source=WORK/'before'/r['jar'];target=(ROOT/'mods' if installed else WORK/'built')/r['jar'];checks=0
assert hashlib.sha256(source.read_bytes()).hexdigest()==r['before_sha256']
assert hashlib.sha256(target.read_bytes()).hexdigest()==r['sha256']
if installed:assert target.read_bytes()==(ROOT/'mod fixing/1 fixed packs'/r['jar']).read_bytes()
with ZipFile(source) as a,ZipFile(target) as b:
 assert a.namelist()==b.namelist() and b.testzip() is None
 assert [n for n in a.namelist() if a.read(n)!=b.read(n)]==r['changed']
 entry=r['changed'][0];old=json.loads(a.read(entry));new=json.loads(b.read(entry))
 assert old.keys()==new.keys()
 for n in a.namelist():
  if n!=entry:assert a.read(n)==b.read(n);checks+=1
 for key in old:
  if key not in r['nodes']:assert old[key]==new[key];checks+=1
 for key,value in [('healing+3%',.03),('colossus',.06),('caster_power',.03),('magistrate',.1)]:
  original=copy.deepcopy(old[key]);actual=copy.deepcopy(new[key]);added=actual['rewards'].pop()
  assert added=={'type':'puffish_skills:attribute','data':{'attribute':'spell_power:healing','value':value,'operation':'multiply_total'}}
  if key=='caster_power':assert actual['title']=='+3% Magic Damage';actual['title']=original['title']
  if key=='magistrate':assert actual['description']['text']=='+10% Magic Damage\n+5% Spell Haste\n+5% Magic Resistance';actual['description']=original['description']
  assert actual==original;checks+=1
 assert new['healing+3%']['title']=='+3% Healing'
 assert new['colossus']['description']['text']=='+6 Hearts\n+6% Healing'
 assert new['caster_power']['rewards'][0]['data']['attribute']=='spell_power:arcane'
print(f'PASS {checks} archive/node preservation checks; four Healing Power rewards and concise labels verified'+('; active/fixed copies match' if installed else ''))

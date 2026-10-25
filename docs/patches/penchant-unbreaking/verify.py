from pathlib import Path
from zipfile import ZipFile
import hashlib,json,sys
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/penchant-unbreaking';a=json.loads((HERE/'artifacts.json').read_text(encoding='utf-8'));name=a['jar'];sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest();jar=ROOT/'mods'/name if '--installed' in sys.argv else WORK/'built'/name
assert sha(jar)==a['sha256']
with ZipFile(WORK/('before-'+name)) as old,ZipFile(jar) as new:
 changed=[n for n in old.namelist() if old.read(n)!=new.read(n)];added=sorted(set(new.namelist())-set(old.namelist()));assert changed==a['changed'] and added==a['added'];assert set(old.namelist())<=set(new.namelist())
 assert all(old.read(n)==new.read(n) for n in old.namelist() if n.endswith('.class')),'No Java changes allowed'
 for n in ['data/minecraft/enchantment/unbreaking.json','resourcepacks/durability_rework/data/minecraft/enchantment/unbreaking.json']:
  data=json.loads(new.read(n));assert data['max_level']==10 and 'penchant:unbreakable' not in data['effects']
 guide=json.loads(new.read('assets/penchant/patchouli_books/tome_of_penchant/en_us/entries/modules/durability_rework.json'));assert all(page['type']=='patchouli:text' for page in guide['pages'])
for label in ['candidate','module-off']:
 assert (HERE/(label+'-results.txt')).read_text(encoding='utf-8').startswith('PASS 18')
 assert json.loads((HERE/(label+'-tested-mods.json')).read_text(encoding='utf-8'))[name]==sha(jar)
assert (HERE/'baseline-results.txt').read_text(encoding='utf-8').startswith('FAIL')
if '--installed' in sys.argv:assert sha(ROOT/'mod fixing/1 fixed packs'/name)==sha(jar)
print('PASS exact tested bytes, unchanged Java/classes and prior policies, level X in both module states, corrected guide, 36 native checks')

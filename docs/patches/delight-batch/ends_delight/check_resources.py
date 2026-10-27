from pathlib import Path
import json,zipfile,subprocess,re
ROOT=Path(__file__).resolve().parents[4]
p=ROOT/'mod fixing/source/delight-batch-20261027/upstream/ends_delight'
r=p/'src/main/resources'
z=zipfile.ZipFile(ROOT/'mod fixing/delight extras (optional)/ends_delight-2.6.1+neoforge.1.21.1.jar')
def modern(v):
 if isinstance(v,list):return [modern(x) for x in v]
 if not isinstance(v,dict):return v
 if v.get('type')=='neoforge:compound':return {'neoforge:ingredient_type':'neoforge:compound','children':modern(v['children'])}
 if set(v)=={'item'} and isinstance(v['item'],str):return v['item']
 if set(v)=={'tag'}:return '#'+v['tag']
 return {k:modern(x) for k,x in v.items()}
recipes=[n for n in z.namelist() if n.startswith('data/ends_delight/recipe/') and n.endswith('.json')]
assert len(recipes)==75
for n in recipes:
 assert (r/n).is_file(),n
 before=json.loads(z.read(n));after=json.loads((r/n).read_text())
 assert before['type']==after['type'],n
 for k in ['result','cookingtime','experience','pattern','recipe_book_tab']:
  assert before.get(k)==after.get(k),(n,k)
 for k in ['ingredients','ingredient','tool','key','template','base','addition']:
  assert modern(before.get(k))==after.get(k),(n,k)
for n in z.namelist():
 if n.endswith('.png') and n.startswith('assets/'):
  assert z.read(n)==(r/n).read_bytes(),n
original=subprocess.check_output(['git','-C',str(p),'show','HEAD:src/main/java/cn/foggyhillside/ends_delight/FoodList.java'],text=True)
food=(p/'src/main/java/cn/foggyhillside/ends_delight/FoodList.java').read_text()
for name,expression in re.findall(r'FoodProperties (\w+) = (.*?);',original):
 expected=re.sub(r'\.effect\(\(\) -> new MobEffectInstance\([^)]*\),\s*[\d.]+F?\)','',expression).replace('.fast()','')
 actual=re.search(r'FoodProperties '+name+r' = (.*?);',food)[1]
 assert expected==actual,(name,'nutrition/saturation/always-edible')
 oldeffects=re.findall(r'new MobEffectInstance\([^)]*\)',expression)
 effectline=re.search(r'Consumable '+name+r'_CONSUMABLE = (.*?);',food)[1]
 neweffects=re.findall(r'new MobEffectInstance\([^)]*\)',effectline)
 oldeffects=[e.replace('DAMAGE_BOOST','STRENGTH').replace('DAMAGE_RESISTANCE','RESISTANCE').replace('MOVEMENT_SPEED','SPEED') for e in oldeffects]
 assert oldeffects==neweffects,(name,'effect IDs/duration/amplifier')
assert original.count('.effect(')==food.count('new ApplyStatusEffectsConsumeEffect(')==63
assert original.count('.fast()')==food.count('consumeSeconds(0.8F)')==7
models=list((r/'assets/ends_delight/models/item').glob('*.json'))
assert len(models)==64
registered=set(re.findall(r'ITEMS.register\("([^"]+)"',(p/'src/main/java/cn/foggyhillside/ends_delight/registry/ModItems.java').read_text()))
assert len(registered)==63
assert {f.stem for f in (r/'assets/ends_delight/items').glob('*.json')}==registered
for model in models:
 if model.stem in registered: assert (r/'assets/ends_delight/items'/model.name).is_file()
assert not (r/'data/neoforge/loot_modifiers/global_loot_modifiers.json').exists()
assert len(list((r/'data/ends_delight/loot_modifiers').glob('*.json')))==9
print('PASS 75 recipe IDs/types/results/times/XP/patterns; original PNG bytes; 63 food effects; 7 fast foods; 63 native item definitions; original unused model retained; 9 modifier files')

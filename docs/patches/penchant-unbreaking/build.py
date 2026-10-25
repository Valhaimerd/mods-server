from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/penchant-unbreaking';WORK.mkdir(exist_ok=True)
name='penchant-neoforge-26.2.0-1.6-2.5x-progress.jar';before=WORK/('before-'+name)
if not before.exists():shutil.copy2(ROOT/'mods'/name,before)
with ZipFile(ROOT/'mod fixing/source/nameless-trinkets/neoforge-curios/build/moddev/artifacts/minecraft-patched-26.2.0.88.jar') as z:vanilla=json.loads(z.read('data/minecraft/enchantment/unbreaking.json'))
vanilla['max_level']=10
with ZipFile(before) as z:
 infos=z.infolist();files={i.filename:z.read(i) for i in infos}
old=dict(files);module='resourcepacks/durability_rework/data/minecraft/enchantment/unbreaking.json';rework=json.loads(files[module]);rework['max_level']=10;rework['effects']=vanilla['effects']
files[module]=(json.dumps(rework,indent=2)+'\n').encode();files['data/minecraft/enchantment/unbreaking.json']=(json.dumps(vanilla,indent=2)+'\n').encode()
book='assets/penchant/patchouli_books/tome_of_penchant/en_us/entries/modules/durability_rework.json';guide=json.loads(files[book]);guide['pages']=[{'type':'patchouli:text','text':'$(bold)Mending is removed by this module.$() $(#7A2FBF)Unbreaking$() can now grow to $(bold)level X$(). Higher levels reduce durability loss, but even Unbreaking X gear can wear out and break.'},{'type':'patchouli:text','text':'$(bold)Durable, not unbreakable.$()$(br2)At Unbreaking X, tools and weapons lose durability about once in every 11 uses. Armor receives a smaller durability benefit. These are chances, so individual results vary.$(br2)Items that already have Mending keep it, but it no longer functions while this module is enabled.'}];files[book]=(json.dumps(guide,indent=2)+'\n').encode()
out=WORK/'built'/name;out.parent.mkdir(exist_ok=True)
with ZipFile(out,'w') as z:
 for i in infos:z.writestr(i,files[i.filename])
 for n in sorted(set(files)-set(old)):z.writestr(n,files[n])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(HERE/'artifacts.json').write_text(json.dumps({'jar':name,'before_sha256':sha(before),'sha256':sha(out),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old))},indent=2)+'\n',encoding='utf-8');print('Built data-only Unbreaking X without permanent unbreakability')

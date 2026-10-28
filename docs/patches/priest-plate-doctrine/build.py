"""Extend the installed doctrine and armor helpers without replaying older patches."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-plate-doctrine'
OUT=WORK/'built';OUT.mkdir(exist_ok=True);classes=WORK/'classes';classes.mkdir(exist_ok=True)
java=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
lines=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines()
cp=';'.join(str(p) for p in (ROOT/'mods').glob('*.jar'))+';'+lines[lines.index('"-cp"')+1].strip('"')
sources=[HERE/'PaladinDoctrine.java']
for package in ['net.paladins.item.armor','net.armory_rpgs.item']:
    p=WORK/'src'/package.replace('.','/')/'HealingArmorBalance.java';p.parent.mkdir(parents=True,exist_ok=True)
    p.write_text((HERE/'HealingArmorBalance.java').read_text().replace('package MOD_PACKAGE;',f'package {package};'));sources.append(p)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*map(str,sources)]
argfile=WORK/'compile.args';argfile.write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args))
subprocess.run([str(java/'javac.exe'),'@'+str(argfile)],check=True)
records=[]
for pattern,helper in [('skill_tree-*','net/skill_tree_rpgs/compat/PaladinDoctrine.class'),('paladins-*','net/paladins/item/armor/HealingArmorBalance.class'),('armory-*','net/armory_rpgs/item/HealingArmorBalance.class')]:
    source=next((WORK/'before').glob(pattern+'.jar'))
    with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
    files=dict(old);files[helper]=(classes/helper).read_bytes()
    if pattern=='skill_tree-*':
        for category in ['class_skills','weapon_skills']:
            name=f'data/skill_tree_rpgs/puffish_skills/categories/{category}/definitions.json';data=json.loads(files[name])
            assert data['priest_root']['description']['text']=='+3% Healing Power'
            data['priest_root']['description']['text']+='\nPaladin plate armor is forbidden.'
            files[name]=(json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode()
        name='assets/skill_tree_rpgs/lang/en_us.json';data=json.loads(files[name])
        data['skill_tree_rpgs.priest_oath']='Your Priest doctrine forbids Paladin plate armor.'
        data['skill_tree_rpgs.priest_oath.full']='Your Priest doctrine forbids Paladin plate armor. Free an inventory slot to recover your armor.'
        files[name]=(json.dumps(data,ensure_ascii=False,indent=2)+'\n').encode()
        values=[{'id':f'{set}_{piece}','required':False} for set in ['paladins:paladin_armor','paladins:crusader_armor','paladins:netherite_crusader_armor','armory_rpgs:justicar_armor','armory_rpgs:lightbringer_armor'] for piece in ['head','chest','legs','feet']]
        files['data/skill_tree_rpgs/tags/item/paladin_plate.json']=(json.dumps({'replace':False,'values':values},indent=2)+'\n').encode()
    with ZipFile(OUT/source.name,'w') as z:
        for info in infos:z.writestr(info,files[info.filename])
        for name in sorted(files.keys()-old.keys()):z.writestr(name,files[name])
    records.append({'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if files[n]!=old[n]],'added':sorted(files.keys()-old.keys())})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print(json.dumps(records,indent=2))

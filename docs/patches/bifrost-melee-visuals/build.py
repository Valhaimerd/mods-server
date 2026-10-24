"""Focused Bifrost model/icon and Punchy resource overlay; preserve repaired inputs."""
from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil

HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/bifrost-melee-visuals'
OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
records=[]
for folder,pattern in [('mods','bifrost-*.jar'),('mod store','modded-swords-x-punchy-*.jar')]:
    source=next((ROOT/folder).glob(pattern));before=WORK/('before-'+source.name)
    if not before.exists():shutil.copy2(source,before)
    with ZipFile(before) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
    files=dict(old)
    if folder=='mods':
        model_path='assets/bifrost/models/item/stormbreaker.json';model=json.loads(files[model_path])
        for hand in ['firstperson_righthand','firstperson_lefthand','thirdperson_righthand','thirdperson_lefthand']:
            model['display'][hand]['scale']=[round(v*1.5,5) for v in model['display'][hand]['scale']]
            model['display'][hand]['translation']=[-1,-4,-1]
        model['display']['gui']={'rotation':[25,215,-30],'translation':[-1,-2,0],'scale':[0.46,0.46,0.46]}
        files[model_path]=(json.dumps(model,indent=2)+'\n').encode()
        files['assets/bifrost/textures/spell/travel.png']=(HERE/'resources/travel.png').read_bytes()
    else:
        path='assets/minecraft/punchy/compat/zz_rpg_26_2.json';o=json.loads(files[path]);m=o['itemSpecific']
        aliases={'bifrost:stormbreaker':'paladins:netherite_great_hammer','bifrost:thors_hammer':'paladins:netherite_mace','bifrost:heimdalls_sword':'paladins:diamond_claymore'}
        for item,reference in aliases.items():
            assert item not in m,item
            m[item]=json.loads(json.dumps(m[reference]))
        files[path]=(json.dumps(o,indent=2)+'\n').encode()
    with ZipFile(OUT/source.name,'w') as z:
        for entry in infos:z.writestr(entry,files[entry.filename])
        for name in sorted(set(files)-set(old)):z.writestr(name,files[name])
    records.append({'folder':folder,'jar':source.name,'before_sha256':sha(before),'sha256':sha(OUT/source.name),'changed':[n for n in old if old[n]!=files[n]]})
(HERE/'artifacts.json').write_text(json.dumps(records,indent=2)+'\n')
print('Built Bifrost visual and exact reference Punchy mapping candidates.')

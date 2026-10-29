"""Read-only installed-asset audit. Writes a report; never edits pack archives."""
from pathlib import Path
from zipfile import ZipFile
import json,hashlib,io
from PIL import Image
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
baseline=json.loads((H/'registry-baseline.json').read_text());effects=json.loads((H/'registered-effects.json').read_text());assets={};languages={};atlases=[]
for name,digest in baseline['sha256'].items():
 assert hashlib.sha256((R/'mods'/name).read_bytes()).hexdigest()==digest, 'Registry baseline stale: '+name
for folder in ['mods','mod store']:
 for jar in (R/folder).glob('*.jar'):
  with ZipFile(jar) as z:
   for n in z.namelist():
    if n.startswith('assets/') and '/textures/mob_effect/' in n and n.endswith('.png'):
     raw=z.read(n);im=Image.open(io.BytesIO(raw)).convert('RGBA');assets.setdefault(n,[]).append({'jar':jar.name,'size':list(im.size),'nonempty':im.getbbox() is not None,'sha256':hashlib.sha256(raw).hexdigest()})
    if n.startswith('assets/') and n.endswith('/lang/en_us.json'):
     try:languages.update(json.loads(z.read(n)))
     except (ValueError,UnicodeDecodeError):pass
    if n.startswith('assets/') and '/atlases/mob_effects.json' in n:atlases.append({'jar':jar.name,'path':n,'data':json.loads(z.read(n))})
rows=[]
for effect in effects:
 ns,name=effect['id'].split(':');path=f'assets/{ns}/textures/mob_effect/{name}.png';matches=assets.get(path,[]);rows.append({'id':effect['id'],'category':effect['category'],'sprite':path,'providers':matches,'missing_file':not matches,'blank_file':bool(matches) and not matches[-1]['nonempty'],'has_name':('effect.'+ns+'.'+name) in languages,'native_attribute_rows':len(effect['attributes'])})
report={'date':baseline['date'],'scope':'Registered RPG class/control effects, including Paladin cross-class regression baseline; conventional sprite resolution and active archive assets. No client rendering claim.','registered_effects':len(rows),'missing_count':sum(r['missing_file'] for r in rows),'blank_count':sum(r['blank_file'] for r in rows),'missing_names':[r['id'] for r in rows if not r['has_name']],'atlas_overrides':atlases,'effects':rows}
(H/'icon-audit.json').write_text(json.dumps(report,indent=2)+'\n');print(f"{report['registered_effects']} effects; {report['missing_count']} missing icon files; {report['blank_count']} blank icons; {len(report['missing_names'])} missing names")
for row in rows:
 if row['missing_file']:print(row['id'],row['sprite'])

from pathlib import Path
from zipfile import ZipFile
import json,sys,io
from PIL import Image
H=Path(__file__).resolve().parent;R=H.parents[2];root=Path(sys.argv[1]);rows=json.loads((R/'docs/patches/class-visual-audit/registered-effects.json').read_text());assets={};fail=[]
for jar in [*root.glob('*.jar'),*(R/'mod store').glob('*.jar')]:
 with ZipFile(jar) as z:
  for n in z.namelist():
   if '/textures/mob_effect/' in n and n.endswith('.png'):assets[n]=z.read(n)
for row in rows:
 ns,name=row['id'].split(':');n=f'assets/{ns}/textures/mob_effect/{name}.png'
 if n not in assets:fail.append(n);continue
 im=Image.open(io.BytesIO(assets[n])).convert('RGBA')
 if im.getbbox() is None:fail.append(n+' EMPTY')
print(f'{len(rows)} registered effects; {len(fail)} unresolved or empty sprites')
print('\n'.join(fail));assert not fail

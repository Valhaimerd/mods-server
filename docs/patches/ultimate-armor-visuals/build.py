from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess,io
from PIL import Image
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/ultimate-armor-visuals';BEFORE=WORK/'before';OUT=WORK/'built'
BEFORE.mkdir(parents=True,exist_ok=True);OUT.mkdir(exist_ok=True)
active=next((ROOT/'mods').glob('spell_engine-*.jar'));source=BEFORE/active.name
if not source.exists():shutil.copy2(active,source)
java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');base=(ROOT/'tmp/berserker-visuals/compile.args').read_text().splitlines();oldcp=base[base.index('"-cp"')+1].strip('"')
cp=str(source)+';'+str(next((ROOT/'mods').glob('rogues-*.jar')))+';'+oldcp
classes=WORK/'classes';classes.mkdir(exist_ok=True)
sources=[HERE/n for n in ['LastStandAppearance.java','LastStandGlint.java','UltimateRenderStateMixin.java','UltimateGlintMixin.java','PatchArmor.java']]
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*map(str,sources)]
(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
with ZipFile(source) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
files=dict(old);n='net/spell_engine/entity/BerserkerAppearance.class';(WORK/'in.class').write_bytes(files[n]);subprocess.run([str(java/'java.exe'),'-cp',';'.join([str(classes),*[v for v in oldcp.split(';') if '/org/ow2/asm/' in v.replace('\\','/')]]),'PatchArmor',str(WORK/'in.class'),str(WORK/'out.class')],check=True);files[n]=(WORK/'out.class').read_bytes()
for p in classes.rglob('*.class'):
 if p.name!='PatchArmor.class':files[p.relative_to(classes).as_posix()]=p.read_bytes()
n='spell_engine.mixins.json';o=json.loads(files[n]);o['client']+=['client.render.tint.UltimateRenderStateMixin','client.render.tint.UltimateGlintMixin'];files[n]=(json.dumps(o,indent=2)+'\n').encode()
# Preserve vanilla's seamless scrolling glint pattern, recolored to gold.
vanilla=None
for path in oldcp.split(';'):
 p=Path(path)
 if p.suffix!='.jar' or not p.exists():continue
 with ZipFile(p) as z:
  name='assets/minecraft/textures/misc/enchanted_glint_armor.png'
  if name in z.namelist():vanilla=z.read(name);break
assert vanilla
im=Image.open(io.BytesIO(vanilla)).convert('RGBA');pixels=[]
for r,g,b,a in im.get_flattened_data():
 strength=max(r,g,b);pixels.append((strength,round(strength*.82),round(strength*.12),a))
im.putdata(pixels);buffer=io.BytesIO();im.save(buffer,format='PNG');files['assets/spell_engine/textures/misc/last_stand_glint.png']=buffer.getvalue()
with ZipFile(OUT/source.name,'w') as z:
 for info in infos:z.writestr(info,files[info.filename])
 for n in sorted(files.keys()-old.keys()):z.writestr(n,files[n])
r={'jar':source.name,'before_sha256':hashlib.sha256(source.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/source.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(files.keys()-old.keys()),'vanilla_glint_sha256':hashlib.sha256(vanilla).hexdigest()}
(HERE/'artifacts.json').write_text(json.dumps(r,indent=2)+'\n');print('Built gold Last Stand tint/glint candidate; active pack unchanged')

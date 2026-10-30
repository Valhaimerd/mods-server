from pathlib import Path
from zipfile import ZipFile
import hashlib,json,shutil,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/fall-and-toughness';B=W/'before';O=W/'built';B.mkdir(parents=True,exist_ok=True);O.mkdir(exist_ok=True)
p=next((R/'mods').glob('penchant-*.jar'));before=B/p.name
if not before.exists():shutil.copy2(p,before)
removed=R/'mods/toughness-enchantment-v1.0.2.jar'
if removed.exists() and not (B/removed.name).exists():shutil.copy2(removed,B/removed.name)
lines=(R/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8').splitlines();cp=str(before)+';'+lines[lines.index('"-cp"')+1].strip('"');classes=W/'classes';classes.mkdir(exist_ok=True)
a=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(H/'RetiredEnchantmentsMixin.java'),str(H/'RetiredProgressMixin.java')];arg=W/'compile.args';arg.write_text('\n'.join(json.dumps(v.replace('\\','/')) for v in a),encoding='utf-8');java=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin');subprocess.run([str(java/'javac.exe'),'@'+str(arg)],check=True)
with ZipFile(before) as z:infos=z.infolist();old={i.filename:z.read(i) for i in infos}
f=dict(old)
with ZipFile(Path((R/'tmp/class-follow-up/native-jar.txt').read_text(encoding='utf-8'))) as z:d=json.loads(z.read('data/minecraft/enchantment/feather_falling.json'))
for effect in d['effects']['minecraft:damage_protection']:
 v=effect['effect']['value'];assert v['type']=='minecraft:linear';v['base']=1.5;v['per_level_above_first']=1.5
f['data/minecraft/enchantment/feather_falling.json']=(json.dumps(d,indent=2)+'\n').encode()
for pclass in classes.rglob('*.class'):f[pclass.relative_to(classes).as_posix()]=pclass.read_bytes()
with ZipFile(O/p.name,'w') as z:
 for i in infos:z.writestr(i,f[i.filename])
 for n in sorted(f.keys()-old.keys()):z.writestr(n,f[n])
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
(H/'artifacts.json').write_text(json.dumps({'jar':p.name,'before_sha256':sha(before),'sha256':sha(O/p.name),'changed':[n for n in old if old[n]!=f[n]],'added':sorted(f.keys()-old.keys()),'removed_jar':removed.name,'removed_sha256':sha(B/removed.name)},indent=2)+'\n',encoding='utf-8')
print('Built Feather Falling / retired Toughness candidate')

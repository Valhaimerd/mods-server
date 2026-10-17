"""Build three same-name replacement JARs, using cached Java/NeoForge libraries."""
from pathlib import Path
import os,subprocess,zipfile,hashlib
HERE=Path(__file__).resolve().parent;ROOT=HERE.parents[2];WORK=ROOT/'tmp/skill-overhaul'
JAVA=Path(os.environ.get('SKILL_PATCH_JAVA','C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin'))
subprocess.run(['python',str(HERE/'repack.py')],check=True)
cp=[next((ROOT/'tmp/nameless-balance/server/libraries/net/neoforged/minecraft-server-patched/26.2.0.90').glob('*.jar'))]
server=ROOT/'tmp/nameless-balance/server'
lines=(server/'libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt').read_text().splitlines()
cp+=[server/n for n in lines[lines.index('-classpath')+1].split(';')]
cp+=list((server/'libraries/net/neoforged/neoforge/26.2.0.90').glob('*.jar'))
cp+=list((ROOT/'mods').glob('*.jar'))
classpath=';'.join(p.resolve().as_posix() for p in cp)
args=WORK/'build.args';args.write_text('--release\n25\n-cp\n"'+classpath+'"\n-d\n"'+(WORK/'classes').as_posix()+'"\n'+''.join('"'+(HERE/n).as_posix()+'"\n' for n in ['OrbOblivionPuffishCompat.java','SkillOverhaul.java']),encoding='utf-8')
subprocess.run([str(JAVA/'javac.exe'),'@'+str(args)],check=True)
name='orb_of_oblivion_puffish_compat-1.0.2.jar';original=WORK/('original-'+name)
assert hashlib.sha256(original.read_bytes()).hexdigest()=='23dd5eaa1482afa5e95eed412ea915d71218b93eba58e717c6f7dfb424039558'
with zipfile.ZipFile(original) as z:files={n:z.read(n) for n in z.namelist()}
for p in (WORK/'classes').rglob('*.class'):files[p.relative_to(WORK/'classes').as_posix()]=p.read_bytes()
files['skill-overhaul-pets.json']=(WORK/'pet-bonuses.json').read_bytes()
files['META-INF/neoforge.mods.toml']=files['META-INF/neoforge.mods.toml'].replace(b"Adds Adventurer Skills to the RPG Series Orb of Oblivion reset categories.",b"Adds Adventurer Skills reset support, one-time progression migration and Companion pet bonuses.")
with zipfile.ZipFile(WORK/name,'w',zipfile.ZIP_DEFLATED) as z:
 for n,b in files.items():z.writestr(n,b)
subprocess.run(['python',str(HERE/'verify.py'),str(WORK)],check=True)
print('Built three same-name replacements under',WORK)

"""Repack Iourus from pinned installed bytes; preserve race IDs and other mods."""
from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import hashlib,json,subprocess,shutil
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/race-balance';OUT=WORK/'built';OUT.mkdir(parents=True,exist_ok=True)
active=next((ROOT/'mods').glob('iourusraces-*.jar'));before=WORK/('before-'+active.name)
if not before.exists():shutil.copy2(active,before)
src=WORK/'src';src.mkdir(exist_ok=True)
base=ROOT/'docs/patches/asgardian-race/src'
s=(base/'Race.java').read_text().replace('GOBLIN(0.6f','GOBLIN(0.75f').replace('ELF(1.1f, 18.0, 1.05)','ELF(1.1f, 18.0, 1.08)').replace('ASGARDIAN(1.05f','ASGARDIAN(1.1f');(src/'Race.java').write_text(s)
s=(base/'RaceAttributes.java').read_text().replace('AsgardianRules.applyMining(player, race);','RaceBalanceRules.apply(player, race);').replace('AsgardianRules.removeMining(player);','AsgardianRules.removeMining(player);\n        RaceBalanceRules.remove(player);');(src/'RaceAttributes.java').write_text(s)
s=(base/'AsgardianRules.java').read_text();start=s.index(' public static void applyMining(');end=s.index(' public static void removeMining',start);s=s[:start]+' public static void applyMining(Player player,Race race){removeMining(player);}\n'+s[end:];(src/'AsgardianRules.java').write_text(s)
# Curated replacement retains unrelated Melfork/Draconic behavior.
shutil.copy2(HERE/'RaceAbilityHandler.java',src/'RaceAbilityHandler.java')
shutil.copy2(HERE/'RaceBalanceRules.java',src/'RaceBalanceRules.java')
classes=WORK/'classes';classes.mkdir(exist_ok=True);argsbase=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text();cp=str(before)+';'+argsbase.split('"-cp"\n')[1].split('\n')[0].strip('"');java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in src.glob('*.java')]];(WORK/'compile.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args));subprocess.run([str(java/'javac.exe'),'@'+str(WORK/'compile.args')],check=True)
with ZipFile(before) as z:files={n:z.read(n) for n in z.namelist()}
old=dict(files)
for p in classes.rglob('*.class'):files[p.relative_to(classes).as_posix()]=p.read_bytes()
for language in ['en_us','es_es']:
 key=f'assets/iourusraces/lang/{language}.json';o=json.loads(files[key]);english={
 'race.iourusraces.elf.trait.1':'+8% movement speed','race.iourusraces.elf.trait.2':'+ Night vision and slow falling','race.iourusraces.elf.tooltip':'Agile forest dwellers with +8% movement speed, enhanced jumps, night vision and slow falling. No racial projectile damage bonus.',
 'race.iourusraces.goblin.trait.2':'+ Small body (75% scale)',
 'race.iourusraces.orc.trait.1':'+ Melee and ranged damage at low health','race.iourusraces.orc.trait.3':'-10% attack speed','race.iourusraces.orc.tooltip':'At 50/40/30/20% health, gain +2/3/4/5 melee damage and +1/1.5/2/2.5 ranged damage. Bonuses stop increasing below 20%. Attack speed is 10% slower.',
 'race.iourusraces.dwarf.trait.1':'+2 armor and extra health','race.iourusraces.dwarf.tooltip':'Sturdy miners with +2 armor, 22 HP and improved mining efficiency.',
 'race.iourusraces.asgardian.trait.1':'+2% damage',
 'race.iourusraces.asgardian.tooltip':'Asgardian: 22 HP, 1.10x scale, 0.95x movement speed. +2% melee, ranged and magic damage, once per hit. Immune to direct lightning strikes, but takes 10% more magic damage. Ragnarok shattered their ancestral wards.'}
 spanish={
 'race.iourusraces.elf.trait.1':'+8% velocidad de movimiento','race.iourusraces.elf.trait.2':'+ Vision nocturna y caida lenta','race.iourusraces.elf.tooltip':'Elfos agiles con +8% de velocidad, saltos mejorados, vision nocturna y caida lenta. Sin bonificacion de dano de proyectiles.',
 'race.iourusraces.goblin.trait.2':'+ Cuerpo pequeno (escala 75%)','race.iourusraces.orc.trait.1':'+ Dano cuerpo a cuerpo y a distancia con poca vida','race.iourusraces.orc.trait.3':'-10% velocidad de ataque','race.iourusraces.orc.tooltip':'Con 50/40/30/20% de vida: +2/3/4/5 de dano cuerpo a cuerpo y +1/1.5/2/2.5 a distancia. No aumenta por debajo de 20%. Ataques 10% mas lentos.',
 'race.iourusraces.dwarf.trait.1':'+2 armadura y mas vida','race.iourusraces.dwarf.tooltip':'Mineros resistentes con +2 armadura, 22 PV y mayor eficiencia minera.',
 'race.iourusraces.asgardian.trait.1':'+2% dano','race.iourusraces.asgardian.tooltip':'Asgardiano: 22 PV, escala 1.10x y velocidad 0.95x. +2% dano cuerpo a cuerpo, a distancia y magico, una vez por golpe. Inmune a rayos directos; recibe 10% mas dano magico. Ragnarok destruyo sus defensas ancestrales.'}
 o.update(english if language=='en_us' else spanish);files[key]=(json.dumps(o,indent=2,ensure_ascii=False)+'\n').encode()
with ZipFile(OUT/active.name,'w',ZIP_DEFLATED) as z:
 for n,data in files.items():z.writestr(n,data)
r={'jar':active.name,'before_sha256':hashlib.sha256(before.read_bytes()).hexdigest(),'sha256':hashlib.sha256((OUT/active.name).read_bytes()).hexdigest(),'changed':[n for n in old if old[n]!=files[n]],'added':sorted(set(files)-set(old)),'sync_fall':True}
(HERE/'artifacts.json').write_text(json.dumps([r],indent=2)+'\n');print('Built race balance candidate; active JAR unchanged')

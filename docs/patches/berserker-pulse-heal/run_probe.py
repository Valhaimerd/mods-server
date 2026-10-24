from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/berserker-pulse-heal';SERVER=WORK/'server';SERVER.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
if not (SERVER/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{ROOT/'tmp/magic-third-tier/server/libraries'}' | Out-Null"],check=True)
mods=SERVER/'mods';mods.mkdir(exist_ok=True)
for p in mods.glob('*.jar'):p.unlink()
for p in (ROOT/'tmp/magic-third-tier/server/mods').glob('*.jar'):
 if p.name not in ['overhaul-probe.jar','pvp_flagging-26.2-1.1.2.jar'] and (ROOT/'mods'/p.name).exists():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
import sys
if '--baseline' not in sys.argv:
 for p in (WORK/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
if not (SERVER/'server.properties').exists():
 text=(ROOT/'tmp/magic-third-tier/server/server.properties').read_text().replace('server-port=25594','server-port=0').replace('level-name=simple-descriptions-20261021','level-name=WarriorTierThree')
 (SERVER/'server.properties').write_text(text);(SERVER/'eula.txt').write_text('eula=true\n')
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in (WORK/'built').glob('*.jar'))+';'+';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
classes=WORK/'test-classes';classes.mkdir(exist_ok=True)
probe=(ROOT/'docs/patches/throw-weapon-hit/WarriorProbe.java').read_text().replace('h.value().cost.cooldown.duration,5);eq("Net full damage coefficient"','h.value().cost.cooldown.duration,6);eq("Net full damage coefficient"').replace('SpellParameters.getCooldownDuration(player,h),3);victim','SpellParameters.getCooldownDuration(player,h),4);victim').replace('haste();\n  RogueTierTwoProbe','haste();WarriorTierTwoProbe.run(world,player,results::add);\n  RogueTierTwoProbe')
probe=probe.replace('WarriorTierTwoProbe.run(world,player,results::add);','WarriorTierTwoProbe.run(world,player,results::add);WarriorUltimatesProbe.run(world,player,results::add);')
probe=probe.replace('throwsTest();','CrimsonProbe.run(world,player,results::add);throwsTest();')
probe=probe.replace('RogueTierTwoProbe.run(world,player,results::add);','') # Historical Rogue expectations predate the separate Rogue rebalance.
(WORK/'WarriorProbe.java').write_text(probe)
old=(ROOT/'docs/patches/warrior-tier-two/WarriorTierTwoProbe.java').read_text().replace('getDuration(),100);eq("Momentum waits','getDuration(),120);eq("Momentum waits').replace('speed*1.05','speed*1.02').replace('as*1.05','as*1.02').replace('dmg*1.02','dmg').replace('cost.cooldown.duration,10);enemy','cost.cooldown.duration,12);enemy')
(WORK/'WarriorTierTwoProbe.java').write_text(old)
ultimate=(ROOT/'docs/patches/throw-weapon-hit/WarriorUltimatesProbe.java').read_text().replace('eq("Last Stand immunity pulse ticks",((net.spell_engine.api.entity.LivingEntityImmunity.Owner)p).getImmunities().getLast().validUntil()-p.tickCount,12);','').replace('eq("Last Stand immunity blocks",net.spell_engine.api.entity.LivingEntityImmunity.isImmune(p,world.damageSources().magic())?1:0,1);','eq("Last Stand does not grant immunity",net.spell_engine.api.entity.LivingEntityImmunity.isImmune(p,world.damageSources().magic())?1:0,0);')
ultimate=ultimate.replace('for(int i=1;i<=5;i++){p.setHealth(5);pulse(h);','for(int i=1;i<=5;i++){p.setHealth(1);pulse(h);eq("Every Berserker pulse fully heals "+i,p.getHealth(),p.getMaxHealth());eq("Speed IV only at fifth pulse "+i,p.hasEffect(MobEffects.SPEED)?1:0,i==5?1:0);',1)
(WORK/'WarriorUltimatesProbe.java').write_text(ultimate)
sources=[ROOT/'docs/patches/crimson-laststand/CrimsonProbe.java',WORK/'WarriorProbe.java',WORK/'WarriorTierTwoProbe.java',WORK/'WarriorUltimatesProbe.java',ROOT/'docs/patches/rogue-tier-two/RogueTierTwoProbe.java',ROOT/'docs/patches/magic-third-tier/PartyProbe.java']
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in sources]];(WORK/'test.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'test.args')],check=True)
with ZipFile(mods/'warrior-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="warrior_probe"\nversion="1"\ndisplayName="Warrior probe"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
result=SERVER/'warrior-result.txt';result.unlink(missing_ok=True)
with (WORK/('baseline.log' if '--baseline' in sys.argv else 'probe.log')).open('w',encoding='utf-8') as log:r=subprocess.run([str(JAVA/'java.exe'),'-Xmx3G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT)
print(result.read_text() if result.exists() else 'NO RESULT - inspect probe.log')
if r.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)

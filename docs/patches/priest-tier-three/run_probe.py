from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/priest-tier-three';SERVER=WORK/'server';SERVER.mkdir(exist_ok=True)
JAVA=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
if not (SERVER/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{SERVER/'libraries'}' -Target '{ROOT/'tmp/magic-third-tier/server/libraries'}' | Out-Null"],check=True)
mods=SERVER/'mods';mods.mkdir(exist_ok=True)
for p in mods.glob('*.jar'):p.unlink()
for p in (ROOT/'tmp/magic-third-tier/server/mods').glob('*.jar'):
 if p.name not in ['overhaul-probe.jar','pvp_flagging-26.2-1.1.2.jar'] and (ROOT/'mods'/p.name).exists():shutil.copy2(ROOT/'mods'/p.name,mods/p.name)
for p in (ROOT/'mods').glob('arsenal-*.jar'):shutil.copy2(p,mods/p.name)
import sys
if '--baseline' not in sys.argv:
 for p in (WORK/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
if not (SERVER/'server.properties').exists():
 text=(ROOT/'tmp/magic-third-tier/server/server.properties').read_text().replace('server-port=25594','server-port=0').replace('level-name=simple-descriptions-20261021','level-name=WarriorTierThree')
 (SERVER/'server.properties').write_text(text);(SERVER/'eula.txt').write_text('eula=true\n')
base=(ROOT/'tmp/magic-third-tier/compile.args').read_text();cp=';'.join(p.as_posix() for p in (WORK/'built').glob('*.jar'))+';'+';'.join(p.as_posix() for p in mods.glob('*.jar'))+';'+base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
classes=WORK/'test-classes';classes.mkdir(exist_ok=True)
party=(ROOT/'docs/patches/magic-third-tier/PartyProbe.java').read_text()
needle='protectedTarget ? target.getHealth() > holyHp : target.getHealth() < holyHp);'
addition='\n                    target.removeEffect(BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("skill_tree_rpgs:priest_penance_shield")).orElseThrow());target.setAbsorptionAmount(0);target.setHealth(target.getMaxHealth()/2);target.invulnerableTime=0;\n                    float penanceHp=target.getHealth();var penance=SpellRegistry.from(world).get(Identifier.parse("paladins:penance")).orElseThrow();\n                    net.spell_engine.internals.PriestRules.penanceTarget(caster,target,caster,penance,new SpellExecution.ImpactContext(1,1,target.position(),new SpellPower.Result(penance.value().school,40,0,1.5),focus,0));\n                    check("Penance mixed impact party/pet safety "+label,protectedTarget?target.getHealth()==penanceHp&&target.getAbsorptionAmount()==1:target.getHealth()<penanceHp&&target.getAbsorptionAmount()==0);\n'
assert needle in party
party=party.replace(needle,needle+addition)
(WORK/'PartyProbe.java').write_text(party)
probe=(ROOT/'docs/patches/priest-rework/PriestProbe.java').read_text().replace('timing();holy();','UltimatesProbe.run(this);PassiveProbe.run(this);timing();holy();')
(WORK/'PriestProbe.java').write_text(probe)
sources=[WORK/'PriestProbe.java',ROOT/'docs/patches/priest-passives/PassiveProbe.java',HERE/'UltimatesProbe.java',WORK/'PartyProbe.java']
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),*[str(p) for p in sources]];(WORK/'test.args').write_text('\n'.join('"'+a.replace('\\','/')+'"' for a in args))
subprocess.run([str(JAVA/'javac.exe'),'@'+str(WORK/'test.args')],check=True)
with ZipFile(mods/'priest-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="priest_probe"\nversion="1"\ndisplayName="Priest probe"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
result=SERVER/'priest-result.txt';result.unlink(missing_ok=True)
with (WORK/('baseline.log' if '--baseline' in sys.argv else 'probe.log')).open('w',encoding='utf-8') as log:r=subprocess.run([str(JAVA/'java.exe'),'-Xmx3G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT)
print(result.read_text() if result.exists() else 'NO RESULT - inspect probe.log')
if r.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)

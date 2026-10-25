from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,json,time,re,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/pvp-combat-limits';SERVER=ROOT/'tmp/pvp-26.2/server';mods=SERVER/'mods';java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
for old in mods.glob('*.jar'):
 if (ROOT/'mods'/old.name).exists():shutil.copy2(ROOT/'mods'/old.name,old)
for p in (WORK/'built').glob('*.jar'):shutil.copy2(p,mods/p.name)
source=(ROOT/'docs/patches/pvp-flagging-26.2/tests/PvpProbe.java').read_text(encoding='utf-8');source=source.replace('public PvpProbe(){','public PvpProbe(net.neoforged.bus.api.IEventBus bus){pack.pvp_probe.CombatProbe.register(bus);');source=source.replace('Files.writeString(Path.of("result.txt"),"PASS', 'pack.pvp_probe.CombatProbe.run(server,a,b);\n  Files.writeString(Path.of("result.txt"),"PASS');(WORK/'PvpProbe.java').write_text(source,encoding='utf-8')
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=';'.join(p.as_posix() for p in mods.glob('*.jar') if p.name!='pvp-probe.jar')+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'probe-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(WORK/'PvpProbe.java'),str(HERE/'tests/CombatProbe.java')];a=WORK/'server-probe.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'pvp-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="pvp_probe"\nversion="1"\ndisplayName="Combat limits regression"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
p=SERVER/'server.properties';s=re.sub(r'^level-name=.*\n?','',p.read_text(encoding='utf-8'),flags=re.M);p.write_text(s+'\nlevel-name=combat-limits-'+str(time.time_ns())+'\n',encoding='utf-8')
result=SERVER/'result.txt';result.unlink(missing_ok=True);(SERVER/'combat-checks.txt').unlink(missing_ok=True)
with (WORK/'server.log').open('w',encoding='utf-8') as log:r=subprocess.run([str(java/'java.exe'),'-Xmx3G','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT,timeout=240)
print(result.read_text(encoding='utf-8') if result.exists() else 'NO RESULT - see tmp/pvp-combat-limits/server.log')
if result.exists():shutil.copy2(result,HERE/'damage-results.txt')
if (SERVER/'combat-checks.txt').exists():shutil.copy2(SERVER/'combat-checks.txt',HERE/'server-checks.txt')
if r.returncode or not result.exists() or not result.read_text().startswith('PASS'):raise SystemExit(1)
print((HERE/'server-checks.txt').read_text(encoding='utf-8'))

(HERE/"server-runtime-mods.json").write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob("*.jar") if "probe" not in p.name},indent=2)+"\n",encoding="utf-8")

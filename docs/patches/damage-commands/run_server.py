from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import shutil,subprocess,json,time,re,hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent;WORK=ROOT/'tmp/damage-commands';SERVER=ROOT/'tmp/pvp-26.2/server';mods=SERVER/'mods';java=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
for old in mods.glob('*.jar'):
 if (ROOT/'mods'/old.name).exists():shutil.copy2(ROOT/'mods'/old.name,old)
name='pvp_flagging-26.2-1.1.2.jar';shutil.copy2(WORK/'built'/name,mods/name)
base=(ROOT/'tmp/rogue-tier-two/client/compile.args').read_text(encoding='utf-8');cp=';'.join(p.as_posix() for p in mods.glob('*.jar') if p.name!='pvp-probe.jar')+';'+base.split('"-cp"\n')[1].split('\n')[0].strip('"');classes=WORK/'probe-classes';classes.mkdir(exist_ok=True)
args=['--release','25','-proc:none','-cp',cp,'-d',str(classes),str(HERE/'tests/DamageCommandsProbe.java')];a=WORK/'probe.args';a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf-8');subprocess.run([str(java/'javac.exe'),'@'+str(a)],check=True)
with ZipFile(mods/'pvp-probe.jar','w',ZIP_DEFLATED) as z:
 z.writestr('META-INF/neoforge.mods.toml','modLoader="javafml"\nloaderVersion="[1,)"\nlicense="MIT"\n[[mods]]\nmodId="damage_commands_probe"\nversion="1"\ndisplayName="Damage commands regression"\n')
 for p in classes.rglob('*.class'):z.write(p,p.relative_to(classes).as_posix())
world='damage-commands-'+str(time.time_ns());p=SERVER/'server.properties';s=re.sub(r'^level-name=.*\n?','',p.read_text(encoding='utf-8'),flags=re.M);p.write_text(s+'\nlevel-name='+world+'\n',encoding='utf-8')
marker=SERVER/'verify-restart';marker.unlink(missing_ok=True)
for phase in ['initial','restart']:
 result=SERVER/'damage-command-result.txt';result.unlink(missing_ok=True)
 if phase=='restart':marker.write_text('same world restart',encoding='utf-8')
 with (WORK/(phase+'.log')).open('w',encoding='utf-8') as log:r=subprocess.run([str(java/'java.exe'),'-Xmx3G','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=SERVER,stdout=log,stderr=subprocess.STDOUT,timeout=240)
 text=result.read_text(encoding='utf-8') if result.exists() else 'NO RESULT';print(phase,text,flush=True)
 assert r.returncode==0 and text.startswith('PASS'),text
 shutil.copy2(result,HERE/(phase+'-results.txt'));shutil.copy2(SERVER/'damage-command-checks.txt',HERE/(phase+'-checks.txt'))
marker.unlink(missing_ok=True)
data=list((SERVER/world).rglob('*damage_percentages*'));assert data,'Missing saved damage settings'
(HERE/'runtime-tested-mods.json').write_text(json.dumps({p.name:hashlib.sha256(p.read_bytes()).hexdigest() for p in mods.glob('*.jar') if 'probe' not in p.name},indent=2)+'\n',encoding='utf-8')
print('PASS world data persisted and reloaded on actual server restart')

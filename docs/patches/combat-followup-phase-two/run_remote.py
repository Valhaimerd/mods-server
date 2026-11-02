from pathlib import Path
from zipfile import ZipFile,ZIP_DEFLATED
import sys,shutil,subprocess,json,uuid,time
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-followup-phase-two';baseline='--baseline' in sys.argv;label='baseline' if baseline else 'candidate';T=W/label;S=T/'server';I=T/'client';X=T/'exchange';J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');X.mkdir(parents=True,exist_ok=True)
for f in X.glob('*'):f.unlink()
for dest in [S/'mods',I/'mods']:
 dest.mkdir(parents=True,exist_ok=True)
 for p in dest.glob('*.jar'):p.unlink()
 for p in (R/'tmp/combat-followup-phase-one/server-candidate/mods').glob('*.jar'):
  if 'probe' not in p.name and (R/'mods'/p.name).exists():shutil.copy2(R/'mods'/p.name,dest/p.name)
 for pattern in ['Nameless Trinkets-*','arsenal-*']:
  for p in (R/'mods').glob(pattern):shutil.copy2(p,dest/p.name)
 for p in (W/('before' if baseline else 'built')).glob('*.jar'):shutil.copy2(p,dest/p.name)
cp=';'.join(str(p) for p in (S/'mods').glob('*.jar'))+';'+(R/'tmp/combat-followup-phase-one/build-classpath.txt').read_text();(W/'classpath.txt').write_text(cp)
def compile(src,dest):
 source=H/(src+'.java')
 if src=='RemoteServerProbe':
  with ZipFile(next((R/'mods').glob('skill_tree-*'))) as z:skills=json.loads(z.read('data/skill_tree_rpgs/puffish_skills/categories/class_skills/skills.json'))
  node=next(k for k,v in skills.items() if v.get('definition')=='warrior_tier_3_passive_1');source=W/(src+'.java');source.write_text((H/(src+'.java')).read_text().replace('ENRAGE_SKILL_ID',node))
 c=W/(src+'-classes');c.mkdir(exist_ok=True);a=W/(src+'.args');a.write_text('\n'.join('"'+x.replace('\\','/')+'"' for x in ['--release','25','-proc:none','-encoding','UTF-8','-cp',cp,'-d',str(c),str(source)]));subprocess.run([str(J/'javac.exe'),'@'+str(a)],check=True)
 with ZipFile(dest,'w',ZIP_DEFLATED) as z:
  id='remote_spatial_probe' if src=='RemoteServerProbe' else 'remote_spatial_client';z.writestr('META-INF/neoforge.mods.toml',f'modLoader="javafml"\nloaderVersion="[4,)"\nlicense="MIT"\n[[mods]]\nmodId="{id}"\nversion="1"\ndisplayName="Isolated remote spatial verification"\n')
  for p in c.rglob('*.class'):z.write(p,p.relative_to(c).as_posix())
compile('RemoteServerProbe',S/'mods/remote-server-probe.jar');compile('RemoteClientProbe',I/'mods/remote-client-probe.jar')
if not (S/'libraries').exists():subprocess.run(['powershell','-NoProfile','-Command',f"New-Item -ItemType Junction -Path '{S/'libraries'}' -Target '{R/'tmp/better-revive-regression/server/libraries'}' | Out-Null"],check=True)
(S/'eula.txt').write_text('eula=true\n');(S/'server.properties').write_text('level-name=isolated-'+uuid.uuid4().hex+'\nonline-mode=false\nlevel-type=minecraft:flat\nserver-port=25578\nview-distance=3\nsimulation-distance=2\nmax-tick-time=-1\nallow-flight=true\nspawn-protection=0\n')
(I/'options.txt').write_text('pauseOnLostFocus:false\nguiScale:3\nrenderDistance:3\nsimulationDistance:2\nmaxFps:30\n')
config=S/'config/penchant';config.mkdir(parents=True,exist_ok=True);(config/'server.toml').write_text('[moduleDefaults]\ndurabilityRework=true\n')
s=(R/'docs/patches/class-visual-retouch/client/launch-isolated-client.py').read_text().replace('Path(__file__).resolve().parents[4] / "tmp/class-visual-retouch/client"','Path(__file__).resolve().parent').replace('C:\\Users\\Valhaimerd\\.gradle\\jdks\\eclipse_adoptium-25-amd64-windows.2\\bin\\java.exe',str(J/'java.exe'));(I/'launch.py').write_text(s)
log=(T/'server-console.log').open('w',encoding='utf-8');server=subprocess.Popen([str(J/'java.exe'),'-Xmx2G','-Xverify:all','@libraries/net/neoforged/neoforge/26.2.0.90/win_args.txt','nogui'],cwd=S,stdout=log,stderr=subprocess.STDOUT)
(T/'server.pid').write_text(str(server.pid));print('server PID',server.pid,flush=True)
deadline=time.time()+110
while not (X/'ready').exists():
 if server.poll() is not None or time.time()>deadline:raise SystemExit('Server not ready; see '+str(T/'server-console.log'))
 time.sleep(1)
subprocess.run(['python',str(I/'launch.py'),'--join'],check=True);print('Remote fixture running; inspect '+str(I/'remote-result.txt'),flush=True)

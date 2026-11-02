from pathlib import Path
import subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/combat-followup-phase-two';C=W/'geometry-classes';C.mkdir(exist_ok=True);J=Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin');cp=';'.join(str(p) for p in (W/'built').glob('*.jar'))+';'+(W/'build-classpath.txt').read_text()
for tool,args,label in [('javac.exe',['--release','25','-proc:none','-cp',cp,'-d',str(C),str(H/'GeometryProbe.java')],'geometry-compile'),('java.exe',['-Xverify:all','-cp',str(C)+';'+cp,'GeometryProbe'],'geometry-run')]:
 a=W/(label+'.args');a.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args));q=subprocess.run([str(J/tool),'@'+str(a)],capture_output=True,text=True);print(q.stdout+q.stderr);assert q.returncode==0
(H/'geometry-result.txt').write_text(q.stdout)

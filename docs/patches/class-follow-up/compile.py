from pathlib import Path
import json,subprocess
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent;W=R/'tmp/class-follow-up';C=W/'classes';C.mkdir(exist_ok=True)
J=Path('C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
lines=(R/'tmp/rogue-tier-two/client/compile.args').read_text().splitlines()
asm=next((R/'tmp/nameless-balance/server/libraries/org/ow2/asm/asm').rglob('asm-*.jar'));tree=next((asm.parents[2]/'asm-tree').rglob('asm-tree-*.jar'))
cp=';'.join(map(str,[*(W/'before').glob('*.jar'),*(R/'mods').glob('*.jar'),*(R/'mod store').glob('*.jar'),asm,tree]))+';'+lines[lines.index('"-cp"')+1].strip('"')
args=['--release','25','-proc:none','-cp',cp,'-d',str(C),*[str(p) for p in H.glob('*.java') if not p.name.endswith('Probe.java')]]
(W/'compile.args').write_text('\n'.join(json.dumps(a.replace('\\','/')) for a in args))
subprocess.run([str(J/'javac.exe'),'@'+str(W/'compile.args')],check=True)

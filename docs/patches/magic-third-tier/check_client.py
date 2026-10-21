"""Link the patched client class and exercise its held-wall guard without launching Minecraft."""
from pathlib import Path
import subprocess
ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).resolve().parent
WORK = ROOT / 'tmp/magic-third-tier/client-check'
WORK.mkdir(parents=True, exist_ok=True)
base = (ROOT / 'tmp/magic-third-tier/compile.args').read_text()
cp = ';'.join(p.as_posix() for p in (ROOT / 'tmp/magic-third-tier/built').glob('*.jar'))
cp += ';' + base.split('-cp\n')[1].split('\n-d')[0].strip().strip('"')
java = Path('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin')
compile_args = '--release\n25\n-cp\n"' + cp + '"\n-d\n"' + WORK.as_posix() + '"\n"' + (HERE / 'ClientWallProbe.java').as_posix() + '"'
(WORK / 'compile.args').write_text(compile_args)
subprocess.run([str(java / 'javac.exe'), '@' + str(WORK / 'compile.args')], check=True)
(WORK / 'run.args').write_text('-Xverify:all\n-cp\n"' + WORK.as_posix() + ';' + cp + '"\nClientWallProbe')
subprocess.run([str(java / 'java.exe'), '@' + str(WORK / 'run.args')], check=True)

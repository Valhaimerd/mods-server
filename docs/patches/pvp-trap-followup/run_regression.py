from pathlib import Path
import shutil,hashlib,json
ROOT=Path(__file__).resolve().parents[3];work=ROOT/'tmp/pvp-trap-followup/regression';(work/'built').mkdir(parents=True,exist_ok=True)
for jar in (ROOT/'tmp/pvp-trap-followup/built').glob('*.jar'):shutil.copy2(jar,work/'built'/jar.name)
source=(ROOT/'docs/patches/tenacity-followup/run_probe.py').read_text().replace('tmp/tenacity-followup','tmp/pvp-trap-followup/regression')
exec(compile(source,str(__file__),'exec'))

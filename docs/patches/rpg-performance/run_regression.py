from pathlib import Path
import shutil,hashlib
ROOT=Path(__file__).resolve().parents[3]
work=ROOT/'tmp/rpg-performance/regression';(work/'built').mkdir(parents=True,exist_ok=True)
jar=next((ROOT/'tmp/rpg-performance/built').glob('spell_engine-*.jar'));shutil.copy2(jar,work/'built'/jar.name)
(work/'tested-sha256.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest(),encoding='utf-8')
source=(ROOT/'docs/patches/tenacity-followup/run_probe.py').read_text().replace('tmp/tenacity-followup','tmp/rpg-performance/regression')
exec(compile(source,str(__file__),'exec'))

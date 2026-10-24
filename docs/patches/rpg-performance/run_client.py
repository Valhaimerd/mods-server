from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[3]
jar=next((ROOT/'tmp/rpg-performance/built').glob('spell_engine-*.jar'))
(ROOT/'tmp/rpg-performance/client-tested-sha256.txt').write_text(hashlib.sha256(jar.read_bytes()).hexdigest(),encoding='utf-8')
source=(ROOT/'docs/patches/skill-tooltip-polish/run_client.py').read_text().replace('tmp/skill-tooltip-polish','tmp/rpg-performance').replace("str(HERE/'ClientTooltipProbe.java')","str(ROOT/'docs/patches/skill-tooltip-polish/ClientTooltipProbe.java')")
exec(compile(source,str(__file__),'exec'))

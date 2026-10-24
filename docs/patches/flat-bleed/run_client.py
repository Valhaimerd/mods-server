from pathlib import Path
import hashlib,json
ROOT=Path(__file__).resolve().parents[3]
records=json.loads((ROOT/'docs/patches/flat-bleed/artifacts.json').read_text());(ROOT/'tmp/flat-bleed/client-tested-hashes.json').write_text(json.dumps({r['jar']:r['sha256'] for r in records}))
source=(ROOT/'docs/patches/skill-tooltip-polish/run_client.py').read_text().replace('tmp/skill-tooltip-polish','tmp/flat-bleed').replace("str(HERE/'ClientTooltipProbe.java')","str(ROOT/'docs/patches/skill-tooltip-polish/ClientTooltipProbe.java')")
exec(compile(source,str(__file__),'exec'))

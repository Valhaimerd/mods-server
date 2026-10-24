"""Retain the Warrior, shield and party regression fixture with client-only tooltip code present."""
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
source=(ROOT/'docs/patches/tenacity-followup/run_probe.py').read_text().replace('tmp/tenacity-followup','tmp/skill-tooltip-polish')
exec(compile(source,str(__file__),'exec'))

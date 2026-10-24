"""Refresh the local owner reference from reviewed text and installed archive identity."""
from pathlib import Path
import hashlib
ROOT=Path(__file__).resolve().parents[3];HERE=Path(__file__).resolve().parent
jar=next((ROOT/'mods').glob('iourusraces-*.jar'));text=(HERE/'reference.md').read_text(encoding='utf-8')
text=text.replace('{{IOURUS_SHA256}}',hashlib.sha256(jar.read_bytes()).hexdigest())
for old,new in [('(../race-state/README.md)','(../docs/patches/race-state/README.md)'),('(README.md)','(../docs/patches/race-balance/README.md)'),('(../asgardian-expansion/README.md)','(../docs/patches/asgardian-expansion/README.md)'),('(../asgardian-race/README.md)','(../docs/patches/asgardian-race/README.md)'),('(../../../mod%20fixing/ASGARDIAN.md)','(ASGARDIAN.md)')]:text=text.replace(old,new)
out=ROOT/'mod fixing/Iourus Races - attributes and benefits.md';out.write_text(text,encoding='utf-8');print('Refreshed',out)

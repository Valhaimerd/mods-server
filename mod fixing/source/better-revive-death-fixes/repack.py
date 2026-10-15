import sys
import zipfile
from pathlib import Path
source, target, patch_dir = map(Path, sys.argv[1:])
base = 'de/mrstilu2k6/better_revive/'
replacements = {base + name + '.class': (patch_dir / 'patched' / base / (name + '.class')).read_bytes() for name in ('event/ModEvents', 'util/CompassHelper', 'util/DownedPlayerManager', 'util/GraveRegistry', 'block/GraveBlockEntity')}
replacements[base + 'util/DeathFix.class'] = (patch_dir / 'classes' / base / 'util/DeathFix.class').read_bytes()
with zipfile.ZipFile(source) as original, zipfile.ZipFile(target, 'w') as output:
    for info in original.infolist():
        if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF', '.RSA', '.DSA')):
            continue
        output.writestr(info, replacements.pop(info.filename, original.read(info.filename)))
    for name, data in replacements.items():
        output.writestr(name, data, compress_type=zipfile.ZIP_DEFLATED)

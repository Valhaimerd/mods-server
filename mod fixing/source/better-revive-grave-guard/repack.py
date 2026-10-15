import sys
import zipfile
from pathlib import Path

source, target, patch_dir = map(Path, sys.argv[1:])
replacements = {
    'de/mrstilu2k6/better_revive/block/GraveBlockEntity.class': (patch_dir / 'GraveBlockEntity.patched.class').read_bytes(),
    'de/mrstilu2k6/better_revive/util/GraveGuard.class': (patch_dir / 'classes/de/mrstilu2k6/better_revive/util/GraveGuard.class').read_bytes(),
}
with zipfile.ZipFile(source) as original, zipfile.ZipFile(target, 'w') as output:
    for info in original.infolist():
        if info.filename.upper().startswith('META-INF/') and info.filename.upper().endswith(('.SF', '.RSA', '.DSA')):
            continue
        output.writestr(info, replacements.pop(info.filename, original.read(info.filename)))
    for name, data in replacements.items():
        output.writestr(name, data, compress_type=zipfile.ZIP_DEFLATED)

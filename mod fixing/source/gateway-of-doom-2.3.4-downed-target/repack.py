"""Repack Gateway 2.3.4 with its original assets and a narrow target guard."""
import sys
import zipfile
from pathlib import Path

source, target, work = map(Path, sys.argv[1:])
package = "de/mrstilu2k6/gateway_of_doom/"
replacements = {
    package + str(path.relative_to(work / "patched")).replace("\\", "/"): path.read_bytes()
    for path in (work / "patched").rglob("*.class")
}
helper = package + "entity/DownedTargeting.class"
replacements[helper] = (work / "classes" / helper).read_bytes()
expected = set(replacements)
with zipfile.ZipFile(source) as original, zipfile.ZipFile(target, "w") as output:
    for info in original.infolist():
        if info.filename.upper().startswith("META-INF/") and info.filename.upper().endswith((".SF", ".RSA", ".DSA")):
            continue
        output.writestr(info, replacements.pop(info.filename, original.read(info.filename)))
    if set(replacements) != {helper}:
        raise RuntimeError(f"Unexpected new class entries: {sorted(replacements)}")
    output.writestr(helper, replacements[helper], compress_type=zipfile.ZIP_DEFLATED)
with zipfile.ZipFile(target) as output:
    if output.testzip() is not None or not expected.issubset(output.namelist()):
        raise RuntimeError("Invalid repack")
print(f"Repacked {target.name}: {len(expected) - 1} guarded classes and one helper")

"""Build the focused Naturally Trimmed 3.5.0 trade-range repair using cached ASM."""
from pathlib import Path
import hashlib
import os
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).resolve().parent
WORK = ROOT / "tmp/naturally-trimmed-trades"
JAVA = Path(os.environ.get("TRADE_PATCH_JAVA", "C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin"))
LIBS = Path(os.environ.get("APPDATA", "")) / ".minecraft/libraries/org/ow2/asm"
CP = os.pathsep.join(str(LIBS / name / "9.10.1" / f"{name}-9.10.1.jar") for name in ("asm", "asm-tree"))
ENTRY = "net/majo24/naturally_trimmed/mixin/VillagerTradesMixin.class"
upstream = WORK / "upstream.jar"
assert hashlib.sha256(upstream.read_bytes()).hexdigest() == "05497f47e977c6b10a2670a6b3484749ea66829680d56f15f95d8e2790deb63c"
with zipfile.ZipFile(upstream) as z:
    (WORK / "original.class").write_bytes(z.read(ENTRY))
subprocess.run([str(JAVA / "javac.exe"), "--release", "25", "-cp", CP, "-d", str(WORK), str(HERE / "PatchTrades.java")], check=True)
subprocess.run([str(JAVA / "java.exe"), "-cp", str(WORK) + os.pathsep + CP, "PatchTrades",
                str(WORK / "original.class"), str(WORK / "fixed.class")], check=True)
output = WORK / "naturally_trimmed-3.5.0+26.1-neoforge.jar"
with zipfile.ZipFile(upstream) as source, zipfile.ZipFile(output, "w") as target:
    for entry in source.infolist():
        target.writestr(entry, (WORK / "fixed.class").read_bytes() if entry.filename == ENTRY else source.read(entry))
with zipfile.ZipFile(upstream) as source, zipfile.ZipFile(output) as target:
    assert source.namelist() == target.namelist()
    changed = [n for n in source.namelist() if source.read(n) != target.read(n)]
    assert changed == [ENTRY], changed
    assert target.testzip() is None
print("Built", output.name, "with exactly one changed class")

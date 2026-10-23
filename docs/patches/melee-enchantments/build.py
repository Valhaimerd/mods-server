"""Overlay melee policy and exact Life Steal healing; never install automatically."""
from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED
import hashlib
import json
import subprocess

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / "tmp/melee-enchantments"
manifest = json.loads((HERE / "upstream.json").read_text())
classes = WORK / "heal-classes"
classes.mkdir(parents=True, exist_ok=True)
source = HERE / "src/local/lifesteal/LifeStealPack.java"
classpath = (ROOT / "tmp/bows-balance/classpath.txt").read_text().strip()
args = ["--release", "25", "-encoding", "UTF-8", "-cp", classpath, "-d", str(classes), str(source)]
argfile = WORK / "heal-compile.args"
argfile.write_text("\n".join('"' + a.replace("\\", "/") + '"' for a in args))
subprocess.run(["C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javac.exe", "@" + str(argfile)], check=True)
report = {}
for name, original in manifest.items():
    source = ROOT / original["input"]
    assert hashlib.sha256(source.read_bytes()).hexdigest() == original["sha256"]
    directory = HERE / "overrides" / name
    overrides = {p.relative_to(directory).as_posix(): p.read_bytes()
                 for p in directory.rglob("*") if p.is_file()}
    if name.startswith("life-steal"):
        path = "local/lifesteal/LifeStealPack.class"
        overrides[path] = (classes / path).read_bytes()
    output = WORK / name
    with ZipFile(source) as old, ZipFile(output, "w") as new:
        for entry in old.infolist():
            new.writestr(entry, overrides.get(entry.filename, old.read(entry)))
        for path, data in overrides.items():
            if path not in old.namelist():
                entry = ZipInfo(path, (1980, 1, 1, 0, 0, 0))
                entry.compress_type = ZIP_DEFLATED
                new.writestr(entry, data)
    with ZipFile(source) as old, ZipFile(output) as new:
        assert new.testzip() is None
        assert len(new.namelist()) == len(set(new.namelist()))
        assert all(new.read(n) == old.read(n) for n in old.namelist() if n not in overrides)
        assert all(new.read(n) == data for n, data in overrides.items())
    report[name] = {**original, "output_sha256": hashlib.sha256(output.read_bytes()).hexdigest(),
                    "changed_entries": sorted(overrides)}
(HERE / "artifacts.json").write_text(json.dumps(report, indent=2) + "\n")
print(json.dumps({n: {"resources": len(r["changed_entries"]), "sha256": r["output_sha256"]}
                  for n, r in report.items()}, indent=2))

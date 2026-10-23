"""Overlay ranged policy onto the hash-pinned current pack; do not install."""
from pathlib import Path
from zipfile import ZipFile, ZipInfo, ZIP_DEFLATED
import hashlib
import json
import subprocess

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
WORK = ROOT / "tmp/ranged-enchantments"
manifest = json.loads((HERE / "upstream.json").read_text())
classes = WORK / "critical-classes"
classes.mkdir(parents=True, exist_ok=True)
source = ROOT / "docs/patches/crossarms-firing/src/local/crossarms/mixin/ZeroDropCriticalMixin.java"
classpath = (ROOT / "tmp/bows-balance/classpath.txt").read_text().strip()
args = ["--release", "25", "-encoding", "UTF-8", "-cp", classpath, "-d", str(classes), str(source)]
argfile = WORK / "critical-compile.args"
argfile.write_text("\n".join('"' + a.replace("\\", "/") + '"' for a in args))
subprocess.run(["C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin/javac.exe", "@" + str(argfile)], check=True)
report = {}
for name, original in manifest.items():
    source = ROOT / original["input"]
    assert hashlib.sha256(source.read_bytes()).hexdigest() == original["sha256"]
    directory = HERE / "overrides" / name
    overrides = {p.relative_to(directory).as_posix(): p.read_bytes()
                 for p in directory.rglob("*") if p.is_file()}
    if name == "crossarms-1.4.jar":
        with ZipFile(source) as old:
            config = json.loads(old.read("crossarms-pack.mixins.json"))
        if "ZeroDropCriticalMixin" not in config["mixins"]:
            config["mixins"].append("ZeroDropCriticalMixin")
        overrides["crossarms-pack.mixins.json"] = (json.dumps(config, indent=2) + "\n").encode()
        path = "local/crossarms/mixin/ZeroDropCriticalMixin.class"
        overrides[path] = (classes / path).read_bytes()
    if not overrides:
        continue
    output = WORK / name
    output.parent.mkdir(parents=True, exist_ok=True)
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
print(json.dumps({name: {"resources": len(r["changed_entries"]), "sha256": r["output_sha256"]}
                  for name, r in report.items()}, indent=2))

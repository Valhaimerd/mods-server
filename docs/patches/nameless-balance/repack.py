"""Repack the preserved upstream JAR with compiled pack classes and filtered data.

Run from the repository root: python docs/patches/nameless-balance/repack.py
Compile src/ into tmp/nameless-balance/classes first (see README).
"""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json

ROOT = Path(__file__).resolve().parents[3]
HERE = Path(__file__).resolve().parent
WORK = ROOT / "tmp/nameless-balance"
spec = json.loads((HERE / "balance.json").read_text(encoding="utf-8"))
removed = {"nameless_trinkets:" + key for key in spec["removed"]}
upstream = WORK / "upstream.jar"
assert hashlib.sha256(upstream.read_bytes()).hexdigest() == "d9d7e30ce34ce5dc280130dab79ea8908dc58bbad52a32174c2c1ac08296f952"
output = WORK / "Nameless Trinkets-neoforge-26.2-1.2.4+26.2.1.jar"
changed, deleted = [], []
with ZipFile(upstream) as source, ZipFile(output, "w") as target:
    originals = set(source.namelist())
    for entry in source.infolist():
        name = entry.filename
        data = source.read(name)
        replacement = WORK / "classes" / name
        if name.endswith(".class") and replacement.is_file():
            data = replacement.read_bytes()
        if name.startswith("data/") and name.endswith(".json"):
            obj = json.loads(data)
            if "/recipe/" in name or "/advancement/" in name:
                if any('"' + key + '"' in data.decode("utf-8") for key in removed):
                    deleted.append(name)
                    continue
            if "/tags/item/" in name and "values" in obj:
                old = obj["values"]
                obj["values"] = [value for value in old
                    if (value if isinstance(value, str) else value.get("id")) not in removed]
                if old != obj["values"]:
                    data = (json.dumps(obj, indent=2) + "\n").encode("utf-8")
        if name == "assets/nameless_trinkets/lang/en_us.json":
            lang = json.loads(data)
            for key in spec["removed"]:
                lang["item.nameless_trinkets." + key] += " (Removed)"
            lang.update({
                "tooltip.nameless_trinkets.isDisabled": "Removed or disabled in this modpack; legacy item only.",
                "tooltip.nameless_trinkets.amphibious_hands_1": "Underwater mining speed: 1.25x.",
                "tooltip.nameless_trinkets.light_gloves_1": "Mining speed under cover: 1.5x.",
                "tooltip.nameless_trinkets.rage_mind_2": "Deal +25% damage to the marked creature type.",
                "tooltip.nameless_trinkets.scarab_amulet_1": "Movement speed on sand: 1.5x.",
                "tooltip.nameless_trinkets.shrinking_veil_2": "Movement speed: 1.25x.",
                "tooltip.nameless_trinkets.true_heart_of_the_sea_4": "Underwater mining speed: 1.25x.",
            })
            data = (json.dumps(lang, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
        if data != source.read(name):
            changed.append(name)
        target.writestr(entry, data)
    for path in (WORK / "classes").rglob("*.class"):
        name = path.relative_to(WORK / "classes").as_posix()
        if name not in originals:
            target.write(path, name)
            changed.append(name)
with ZipFile(output) as archive:
    assert archive.testzip() is None
report = {"upstream_sha256": hashlib.sha256(upstream.read_bytes()).hexdigest(),
          "changed_entries": changed, "deleted_entries": deleted,
          "sha256": hashlib.sha256(output.read_bytes()).hexdigest()}
(WORK / "repack.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
print(f"Repacked {len(changed)} changed/new entries; removed {len(deleted)} recipes/advancements")

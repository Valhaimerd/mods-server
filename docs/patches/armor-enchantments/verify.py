"""Check the installed armor-policy repack against its preserved input."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
report = json.loads((HERE / "artifacts.json").read_text())
original = ROOT / report["input"]
active = ROOT / "mods" / report["filename"]
fixed = ROOT / "mod fixing/1 fixed packs" / report["filename"]

assert hashlib.sha256(original.read_bytes()).hexdigest() == report["input_sha256"]
active_hash = hashlib.sha256(active.read_bytes()).hexdigest()
expected_hash = report["output_sha256"]
later_changed = set()
for folder in ("ranged-enchantments", "melee-enchantments"):
    path = HERE.parent / folder / "artifacts.json"
    later = json.loads(path.read_text()).get(report["filename"]) if path.exists() else None
    if later is not None:
        assert later["sha256"] == expected_hash, "Broken cumulative enchantment build chain"
        expected_hash = later["output_sha256"]
        later_changed.update(later["changed_entries"])
assert active_hash == expected_hash
assert fixed.read_bytes() == active.read_bytes()
with ZipFile(original) as old, ZipFile(active) as new:
    assert new.testzip() is None
    assert len(new.namelist()) == len(set(new.namelist()))
    armor_changed = set(report["changed_entries"])
    assert len(armor_changed) == 3
    changed = armor_changed | later_changed
    assert all(new.read(n) == old.read(n) for n in old.namelist() if n not in changed)
    for name in armor_changed:
        expected = json.loads((HERE / "overrides" / name).read_bytes())
        actual = json.loads(new.read(name))
        # Later passes may append disabled IDs without removing the armor policy.
        assert all(entry in actual["values"] for entry in expected["values"])
    assert not any(n.startswith("local/armor/") for n in new.namelist())
print("PASS: installed/fixed hashes, armor policy retained, preserved original entries, no probe classes")

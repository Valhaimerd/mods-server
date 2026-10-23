"""Verify the installed cumulative ranged policy and scoped critical repair."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
report = json.loads((HERE / "artifacts.json").read_text())
for name, artifact in report.items():
    original = ROOT / artifact["input"]
    active = ROOT / "mods" / name
    fixed = ROOT / "mod fixing/1 fixed packs" / name
    assert hashlib.sha256(original.read_bytes()).hexdigest() == artifact["sha256"]
    later_path = HERE.parent / "melee-enchantments/artifacts.json"
    later = json.loads(later_path.read_text()).get(name) if later_path.exists() else None
    expected = artifact["output_sha256"]
    if later is not None:
        assert later["sha256"] == expected, "Broken ranged-to-melee build chain"
        expected = later["output_sha256"]
    assert hashlib.sha256(active.read_bytes()).hexdigest() == expected
    assert active.read_bytes() == fixed.read_bytes()
    changed = set(artifact["changed_entries"]) | (set(later["changed_entries"]) if later else set())
    with ZipFile(original) as old, ZipFile(active) as new:
        assert new.testzip() is None
        assert len(new.namelist()) == len(set(new.namelist()))
        assert not set(old.namelist()) - set(new.namelist())
        assert set(new.namelist()) - set(old.namelist()) <= changed
        assert all(new.read(n) == old.read(n) for n in old.namelist() if n not in changed)
        for path in changed:
            source = HERE / "overrides" / name / path
            if source.exists() and not (later and path in later["changed_entries"]):
                assert new.read(path) == source.read_bytes()
        assert not any("RangedProbe" in n or "ArmorProbe" in n for n in new.namelist())
        if name.startswith("penchant"):
            hidden = json.loads(new.read("data/penchant/tags/enchantment/disabled.json"))["values"]
            expected_hidden = json.loads((HERE / "overrides" / name / "data/penchant/tags/enchantment/disabled.json").read_text())["values"]
            assert all(value in hidden for value in expected_hidden)
        else:
            assert "ZeroDropCriticalMixin" in json.loads(new.read("crossarms-pack.mixins.json"))["mixins"]
            assert b"CROSSARMS_NO_CRITICAL" in new.read("data/crossarms/function/enchantment/zero_drop.mcfunction")
            assert "local/crossarms/mixin/ZeroDropCriticalMixin.class" in new.namelist()
    print("PASS", name, "only", len(changed), "expected entries differ; active/fixed hashes match")

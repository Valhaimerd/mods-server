"""Check installed melee policy and Life Steal archives against pinned inputs."""
from pathlib import Path
from zipfile import ZipFile
import hashlib
import json

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
for name, artifact in json.loads((HERE / "artifacts.json").read_text()).items():
    original = ROOT / artifact["input"]
    active = ROOT / "mods" / name
    assert hashlib.sha256(original.read_bytes()).hexdigest() == artifact["sha256"]
    assert hashlib.sha256(active.read_bytes()).hexdigest() == artifact["output_sha256"]
    assert active.read_bytes() == (ROOT / "mod fixing/1 fixed packs" / name).read_bytes()
    changed = set(artifact["changed_entries"])
    with ZipFile(original) as old, ZipFile(active) as new:
        assert new.testzip() is None
        assert len(new.namelist()) == len(set(new.namelist()))
        assert not set(old.namelist()) - set(new.namelist())
        assert set(new.namelist()) - set(old.namelist()) <= changed
        assert all(new.read(n) == old.read(n) for n in old.namelist() if n not in changed)
        for path in changed:
            override = HERE / "overrides" / name / path
            if override.exists():
                assert new.read(path) == override.read_bytes()
        assert not any("MeleeProbe" in n for n in new.namelist())
        if name.startswith("penchant"):
            assert len(json.loads(new.read("data/penchant/tags/enchantment/disabled.json"))["values"]) == 15
        else:
            assert b"modpack_lifesteal_heal" in new.read("data/life_steal_enchantment/function/heal.mcfunction")
            assert "local/lifesteal/LifeStealPack.class" in new.namelist()
    print("PASS", name, "expected entries only; active/fixed hashes match")

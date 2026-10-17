"""Build a deterministic, resource-only NeoForge 26.2 translation JAR."""
import hashlib
import io
import json
from pathlib import Path
import re
import urllib.request
import zipfile

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]
URL = "https://cdn.modrinth.com/data/x0pG5ft0/versions/sFWFS6CK/SPEMOTES_TRANSLATIONS_v1.1.zip"
SHA256 = "6d174030ccef4b627a2b88151733f25f5f59969fb4a38d6db2abd2599e5b70b5"
data = urllib.request.urlopen(URL, timeout=60).read()
assert hashlib.sha256(data).hexdigest() == SHA256, "Upstream translation hash mismatch"
with zipfile.ZipFile(io.BytesIO(data)) as upstream:
    files = {n: upstream.read(n) for n in upstream.namelist() if not n.endswith("/")}
path = "assets/spemotes-emotes/lang/en_us.json"
english = json.loads(files[path])
english.update(json.loads((HERE / "en_us_additions.json").read_text(encoding="utf-8")))
files[path] = (json.dumps(english, ensure_ascii=False, indent=2) + "\n").encode()
files["pack.mcmeta"] = json.dumps({"pack": {
    "description": "SPEmotes translations with pack-local English additions for Minecraft 26.2",
    "min_format": 88, "max_format": 88
}}).encode()
files["META-INF/neoforge.mods.toml"] = b'''license="ARR"
showAsResourcePack=false
[[mods]]
modId="spemotes_translations"
version="1.1.1+26.2"
displayName="SPEmotes Translations (26.2)"
displayURL="https://modrinth.com/resourcepack/spemotes-translations"
authors="SPEmotes translation contributors; local packaging by Valhaimerd"
description="English SPEmotes labels, including local Dances 5 additions. Client resources only."
[[dependencies.spemotes_translations]]
modId="minecraft"
type="required"
versionRange="[26.2,26.3)"
side="CLIENT"
[[dependencies.spemotes_translations]]
modId="neoforge"
type="required"
versionRange="[26.2.0.90,)"
side="CLIENT"
[[dependencies.spemotes_translations]]
modId="emotecraft"
type="required"
versionRange="[3.4,)"
side="CLIENT"
'''
files["CREDITS.txt"] = ("SPEmotes Translations v1.1 by the original contributors.\n"
    "Source: https://modrinth.com/resourcepack/spemotes-translations\n"
    "Translations: MuSL_I / Orvyla / wykard / goobang / F0lia / MeDUlka traktor / qiqimora.\n"
    "Translation help: 3APA3EH. Upstream license: All Rights Reserved.\n"
    "Local changes: NeoForge 26.2 resource packaging and 82 English Dances 5 entries.\n"
    f"Upstream ZIP SHA256: {SHA256}\n").encode()

# Verify every translation component in the installed emote sets has an English label.
used = set()
for archive in (ROOT / "emotes").glob("*.zip"):
    with zipfile.ZipFile(archive) as emotes:
        for name in emotes.namelist():
            for match in re.findall(rb'\{"translate":"spemotes[^\x00]+?\}', emotes.read(name)):
                key = json.loads(match)["translate"]
                assert key in english, f"Missing English translation: {key}"
                assert not re.search(r"[\u0400-\u04ff]", english[key]), key
                used.add(key)

output = ROOT / "mod store" / "spemotes-translations-neoforge-26.2-1.1.1.jar"
with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as jar:
    for name, content in sorted(files.items()):
        info = zipfile.ZipInfo(name, (2026, 10, 17, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        jar.writestr(info, content)
print(f"Built {output.name}: {len(used)} used keys covered; {len(english)} English entries")

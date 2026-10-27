"""Launch NeoForge in this isolated game directory with an offline test identity."""
import json
import os
from pathlib import Path
import subprocess
import sys
import uuid

INSTANCE = Path(__file__).resolve().parents[4] / "tmp/paladin-doctrine/client"
MINECRAFT = Path(os.environ["APPDATA"]) / ".minecraft"
VERSION = "neoforge-26.2.0.90"
DATA = json.loads((MINECRAFT / "versions" / VERSION / f"{VERSION}.json").read_text(encoding="utf-8"))
JAVA = Path(r"C:\Users\Valhaimerd\.gradle\jdks\eclipse_adoptium-25-amd64-windows.2\bin\java.exe")


def enabled(entry):
    rules = entry.get("rules", [])
    if not rules:
        return True
    state = False
    for rule in rules:
        os_name = rule.get("os", {}).get("name")
        features = rule.get("features", {})
        if os_name not in (None, "windows") or any(features.values()):
            continue
        state = rule["action"] == "allow"
    return state


libraries = [MINECRAFT / "libraries" / lib["artifact"]["path"]
             for lib in DATA["libraries"] if enabled(lib) and "artifact" in lib]
assert all(path.is_file() for path in libraries)
classpath = os.pathsep.join(map(str, libraries + [MINECRAFT / "versions" / VERSION / f"{VERSION}.jar"]))
natives = MINECRAFT / "versions" / VERSION / "natives"
subdirs = {part: natives / part for part in ("java", "jna", "lwjgl", "netty")}
variables = {
    "${natives_directory}": str(natives),
    "${launcher_name}": "IsolatedTest",
    "${launcher_version}": "1",
    "${classpath}": classpath,
    "${library_directory}": str(MINECRAFT / "libraries"),
    "${auth_player_name}": "PaladinProbe",
    "${version_name}": VERSION,
    "${game_directory}": str(INSTANCE),
    "${assets_root}": str(MINECRAFT / "assets"),
    "${assets_index_name}": DATA["assetIndex"]["id"],
    "${auth_uuid}": uuid.uuid5(uuid.NAMESPACE_DNS, "OfflinePlayer:PaladinProbe").hex,
    "${auth_access_token}": "0",
    "${clientid}": "0",
    "${auth_xuid}": "0",
    "${version_type}": "isolated",
}


def expand(arg):
    for key, value in variables.items():
        arg = arg.replace(key, value)
    return arg


def values(entries):
    return [expand(value) for entry in entries if enabled(entry) for value in entry["values"]]


jvm = values(DATA["arguments"]["jvm"])
# The cached version's natives directory already contains launcher-extracted DLLs.
jvm = [arg.replace(str(subdirs[part]), str(natives)) for arg in jvm for part in ["java"]]
for part in ("jna", "lwjgl", "netty"):
    jvm = [arg.replace(str(subdirs[part]), str(natives)) for arg in jvm]
command = [str(JAVA), "-Xmx3G", *jvm, DATA["mainClass"], *values(DATA["arguments"]["game"])]
command.extend(["--width", "1280", "--height", "800"])
if "--join" in sys.argv:
    command.extend(["--quickPlayMultiplayer", "127.0.0.1:25578"])
if "--world" in sys.argv:
    command.extend(["--quickPlaySingleplayer", "Punchy Flat"])
log = (INSTANCE / "launcher-output.log").open("w", encoding="utf-8")
process = subprocess.Popen(command, cwd=INSTANCE, stdout=log, stderr=subprocess.STDOUT)
(INSTANCE / "client.pid").write_text(str(process.pid), encoding="ascii")
print(f"Isolated Minecraft launched: PID {process.pid}; gameDir {INSTANCE}")

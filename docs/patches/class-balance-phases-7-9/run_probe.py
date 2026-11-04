from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
source=(R/'docs/patches/ward-hit-retirement/run_probe.py').read_text()
source=source.replace("W=R/'tmp/ward-hit-retirement'","W=R/'tmp/class-balance-phases-7-9'").replace("str(H/'WardRetirementProbe.java')","str(H/'FinalProbe.java')")
source=source.replace('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin','C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
source=source.replace("for p in (W/'before').glob('*.jar'):shutil.copy2(p,M/p.name)","for pattern in ['PlayerRevive*','Better Revive - *','CreativeCore*','Better Revive X PlayerRevive*']:\n for dependency in (R/'mods').glob(pattern+'.jar'):shutil.copy2(dependency,M/dependency.name)\nfor p in (W/'before').glob('*.jar'):shutil.copy2(p,M/p.name)")
exec(compile(source,str(__file__),'exec'))

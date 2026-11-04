from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
# Reuse the proven isolated NeoForge launcher, changing only batch paths/probe.
source=(R/'docs/patches/ward-hit-retirement/run_probe.py').read_text()
source=source.replace("W=R/'tmp/ward-hit-retirement'","W=R/'tmp/class-balance-phases-1-3'").replace("str(H/'WardRetirementProbe.java')","str(H/'BatchProbe.java')")
source=source.replace('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin','C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
exec(compile(source,str(__file__),'exec'))

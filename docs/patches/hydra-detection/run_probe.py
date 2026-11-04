from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
source=(R/'docs/patches/ward-hit-retirement/run_probe.py').read_text()
source=source.replace("W=R/'tmp/ward-hit-retirement'","W=R/'tmp/hydra-detection'").replace("str(H/'WardRetirementProbe.java')","str(H/'HydraProbe.java')")
source=source.replace('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin','C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
exec(compile(source,str(__file__),'exec'))

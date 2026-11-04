from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
s=(R/'docs/patches/combat-phase-two/run_client.py').read_text().replace("W=R/'tmp/combat-phase-two'","W=R/'tmp/class-balance-phases-1-3'").replace("str(H/'SpatialClientProbe.java')","str(H/'BatchClientProbe.java')")
s=s.replace('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin','C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
exec(compile(s,str(__file__),'exec'))

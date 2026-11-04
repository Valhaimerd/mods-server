from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
source=(R/'docs/patches/ward-hit-retirement/run_probe.py').read_text()
source=source.replace("W=R/'tmp/ward-hit-retirement'","W=R/'tmp/class-balance-phases-7-9'")
source=source.replace("str(H/'WardRetirementProbe.java')","str(R/'docs/patches/hydra-detection/HydraProbe.java')")
source=source.replace('C:/Program Files/Eclipse Adoptium/jdk-26.0.2.101-hotspot/bin','C:/Users/Valhaimerd/.gradle/jdks/eclipse_adoptium-25-amd64-windows.2/bin')
source=source.replace("C=W/'spatial-classes';C.mkdir(exist_ok=True)","C=W/'regression-classes';shutil.rmtree(C,ignore_errors=True);C.mkdir(exist_ok=True)")
source=source.replace("S=W/('server-'+label)","S=W/('server-hydra-regression-'+label)")
source=source.replace("H/(label+","H/('hydra-regression-'+label+").replace("W/(label+'.log')","W/('hydra-regression-'+label+'.log')")
source=source.replace("for p in (W/'before').glob('*.jar'):shutil.copy2(p,M/p.name)","for p in (R/'tmp/class-balance-phases-1-3/before').glob('*.jar'):shutil.copy2(R/'mods'/p.name,M/p.name)\nfor p in (W/'before').glob('*.jar'):shutil.copy2(p,M/p.name)")
exec(compile(source,str(__file__),'exec'))

from pathlib import Path
R=Path(__file__).resolve().parents[3];H=Path(__file__).resolve().parent
s=(R/'docs/patches/ward-hit-retirement/run_probe.py').read_text().replace("W=R/'tmp/ward-hit-retirement'","W=R/'tmp/combat-followup-phase-one'").replace("str(H/'WardRetirementProbe.java')","str(H/'ClassProbe.java')")
s=s.replace("(W/'classpath.txt').read_text()","(W/'build-classpath.txt').read_text()")
exec(compile(s,str(__file__),'exec'))
